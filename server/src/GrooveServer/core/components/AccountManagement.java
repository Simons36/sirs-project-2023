package GrooveServer.core.components;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import CryptographicLibrary.api.CryptographicLibraryServerAPI;
import CryptographicLibrary.structs.ProtectReturnStruct;
import CryptographicLibrary.util.CryptoIO;
import GrooveServer.database.DatabaseOperations;
import GrooveServer.exceptions.AccountNotFoundException;
import GrooveServer.exceptions.FamilyDoesNotExistException;
import GrooveServer.exceptions.FamilyNameAlreadyExistsException;
import GrooveServer.exceptions.MusicFormatNotFoundException;
import GrooveServer.exceptions.MusicNotFoundException;
import GrooveServer.exceptions.OtherErrorException;
import GrooveServer.exceptions.UserAlreadyBelongsToAFamilyException;
import GrooveServer.exceptions.UserAlreadyLoggedInException;
import GrooveServer.exceptions.UserAlreadyOwnsMusicException;
import GrooveServer.exceptions.UserDoesNotBelongToAFamilyException;
import GrooveServer.exceptions.UserDoesntOwnMusicException;
import GrooveServer.exceptions.UserNotLoggedInException;
import GrooveServer.exceptions.UsernameAlreadyExistsException;
import GrooveServer.exceptions.WrongFamilyPasswordException;
import GrooveServer.exceptions.WrongPasswordException;
import GrooveServer.objects.Media;
import GrooveServer.service.exceptions.DatabaseExecStatementException;

public class AccountManagement {

    private HashMap<String, CryptographicLibraryServerAPI> _accountsLoggedIn = 
                        new HashMap<String, CryptographicLibraryServerAPI>(); // <username, cryptographicLibrary>

    
    private DatabaseOperations _databaseOperations;

    private KeyManagement _keyManagement;

    private String _privateKeyPath;
    
    public AccountManagement(DatabaseOperations databaseOperations, KeyManagement keyManagement, String privateKeyPath){
        _databaseOperations = databaseOperations;
        _keyManagement = keyManagement;
        _privateKeyPath = privateKeyPath;
    }

    public void ResetAllTemporaryKeys(){

        //remove members to be removed from families
        RemoveMembersToBeRemovedFromFamilies();
        
        List<String> usersInFamilies = ResetFamiliesTemporaryKeys();

        List<String> usernamesLoggedIn = new ArrayList<String>();

        for(String username : _accountsLoggedIn.keySet()){

            usernamesLoggedIn.add(username);
            String tempKeyInBase64 = new String();

            CryptographicLibraryServerAPI cryptographicLibrary = _accountsLoggedIn.get(username);
            synchronized(cryptographicLibrary){


                //if the user is in a family, then the temporary key will be the family's temporary key
                if(usersInFamilies.contains(username)){

                    //get the family name
                    String familyName = _databaseOperations.GetFamilyOfUser(username);
                    tempKeyInBase64 = _databaseOperations.GetFamilyTemporaryKey(familyName);
                    byte[] familyTemporaryKeyInBytes = Base64.getDecoder().decode(tempKeyInBase64);

                    cryptographicLibrary.SetNewTemporaryKey(familyTemporaryKeyInBytes);

                }else{
                    byte[] tempKeyInBytes = cryptographicLibrary.SetNewTemporaryKey().getEncoded();
                    tempKeyInBase64 = Base64.getEncoder().encodeToString(tempKeyInBytes);
                }

            }
            _databaseOperations.addTemporaryKeyToUser(username, tempKeyInBase64);
        }


        List<String> allUsernames = _databaseOperations.getAllUsernames();

        for(String username : allUsernames){
            
            if(!usernamesLoggedIn.contains(username)){

                String tempKeyInBase64 = new String();

                if(usersInFamilies.contains(username)){
                    String familyName = _databaseOperations.GetFamilyOfUser(username);
                    
                    tempKeyInBase64 = _databaseOperations.GetFamilyTemporaryKey(familyName);
                }else{
                    byte[] tempKeyInBytes = CryptographicLibraryServerAPI.GenerateNewTemporaryKey().getEncoded();
                    tempKeyInBase64 = Base64.getEncoder().encodeToString(tempKeyInBytes);
                }

                _databaseOperations.addTemporaryKeyToUser(username, tempKeyInBase64);
            }

        }
    }

    private List<String> ResetFamiliesTemporaryKeys(){

        List<String> usersInFamilies = _databaseOperations.getAllUsersInFamilies();

        List<String> familiesNames = _databaseOperations.getAllFamiliesNames();

        for (String familyName : familiesNames) {
            
            byte[] newTempKeyInBytes = CryptographicLibraryServerAPI.GenerateNewTemporaryKey().getEncoded();

            _databaseOperations.setFamilyTemporaryKey(familyName, Base64.getEncoder().encodeToString(newTempKeyInBytes));

        }

        return usersInFamilies;
    }

    private void RemoveMembersToBeRemovedFromFamilies(){

        _databaseOperations.removeToBeRemovedMembersFromFamilies();

    }

    private String CreateRandomAuthCodeForFamily(int length){
        
        StringBuilder randomString = new StringBuilder();

        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();

    }

    
    
    /**
     * This function will create a new user and call method to create keys (will throw exception if user already exists)
     * @param username
     * @param password
     * @throws UsernameAlreadyExistsException
     * @throws Exception
     */
    public void CreateAccount(String username, String password) throws UsernameAlreadyExistsException, Exception{
        try {

            String hashPassword = CryptographicLibraryServerAPI.HashPassword(password);
            
            System.out.println("hash password: " + hashPassword.length());
            
            _databaseOperations.addNewUser(username, hashPassword);
            
            _keyManagement.SelectPermanentKey(username);

        } catch (UsernameAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
    
    
    public void Login(String username, String password) throws AccountNotFoundException, WrongPasswordException, UserAlreadyLoggedInException, Exception{
        
        if(IsUserLoggedIn(username))
            throw new UserAlreadyLoggedInException(username);
        
        if(!CheckIfUsernameExists(username))
            throw new AccountNotFoundException(username);
        
        if(!CheckIfPasswordMatches(username, password))
            throw new WrongPasswordException(username);

        try {
            password = null; //clear the password from memory<

            SecretKey permanentKey = _keyManagement.GetPermanentKey(username);
            
            //get the encrypted temporary key (this will be returned to the client)
            byte[] temporaryKeyInBytes = _keyManagement.GetEncryptedTemporaryKey(username);
            
            _accountsLoggedIn.put(username, new CryptographicLibraryServerAPI(permanentKey,
                                                                              GetServerPrivateKey(),
                                                                              new SecretKeySpec(temporaryKeyInBytes, "AES")));

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    
    }
    
    
    private boolean IsUserLoggedIn(String username){
        return _accountsLoggedIn.containsKey(username);
    }

    private boolean CheckIfUsernameExists(String username) throws DatabaseExecStatementException{
        try {
            return _databaseOperations.checkIfUserAlreadyExists(username);
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }

    private boolean CheckIfPasswordMatches(String username, String password) throws DatabaseExecStatementException, Exception{
        try {

            String passwordHashInBase64FromDatabase = _databaseOperations.GetPasswordHash(username);

            return CryptographicLibraryServerAPI.ValidatePassword(password, passwordHashInBase64FromDatabase);

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
    

    private PrivateKey GetServerPrivateKey() throws Exception{
        return CryptoIO.readPrivateKey(_privateKeyPath);
    }

    
    public void PurchaseSong(String username, String songTitle, String artist) throws UserNotLoggedInException, 
                                                                                      MusicNotFoundException, 
                                                                                      UserAlreadyOwnsMusicException, 
                                                                                      Exception{

        if(!IsUserLoggedIn(username))
            throw new UserNotLoggedInException(username);
        

        try {
            _databaseOperations.executeUserPurchase(username, songTitle, artist);
            
        } catch (UserAlreadyOwnsMusicException e) {
            throw e;
        } catch (MusicNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Media> GetUserPurchases(String username) throws UserNotLoggedInException, DatabaseExecStatementException, OtherErrorException{

        if(!IsUserLoggedIn(username))
            throw new UserNotLoggedInException(username);
        
        try {
            List<Integer> songsIds = _databaseOperations.GetSongsIdsOfUserPurchasedSongs(username);

            List<Media> songs = new ArrayList<Media>();

            for (int id : songsIds) {
                String songTitle = _databaseOperations.GetSongTitleById(id);

                String artist = _databaseOperations.GetSongArtistById(id);

                List<String> genre = _databaseOperations.GetSongGenresById(id);

                for(String format : _databaseOperations.GetSongFormatsById(id)){
                    songs.add(new Media(format, artist, songTitle, genre.toArray(new String[0])));
                }

            }

            return songs;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }


    public void AddSongToUserPreferences(String username, String songTitle, String artist) throws UserNotLoggedInException, 
                                                                                                  DatabaseExecStatementException, 
                                                                                                  MusicNotFoundException, 
                                                                                                  UserDoesntOwnMusicException, 
                                                                                                  OtherErrorException{
        
        if(!IsUserLoggedIn(username))
            throw new UserNotLoggedInException(username);

        try {
            _databaseOperations.AddSongToUserPreferences(username, songTitle, artist);
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (MusicNotFoundException e){
            throw e;
        } catch (UserDoesntOwnMusicException e){
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }

    }


    public List<Media> GetUserPreferences(String username) throws UserNotLoggedInException, DatabaseExecStatementException, OtherErrorException{

        if(!IsUserLoggedIn(username))
            throw new UserNotLoggedInException(username);
        
        try {
            List<Integer> songsIds = _databaseOperations.GetSongsIdsOfUserPreferences(username);

            List<Media> songs = new ArrayList<Media>();

            for (int id : songsIds) {
                String songTitle = _databaseOperations.GetSongTitleById(id);

                String artist = _databaseOperations.GetSongArtistById(id);

                List<String> genre = _databaseOperations.GetSongGenresById(id);

                songs.add(new Media(null, artist, songTitle, genre.toArray(new String[0])));

            }

            return songs;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }


    /**
     * This function will get the song for the user and return it encrypted; if temporary key changes, then it will throw an exception
     * @param username
     * @param songTitle
     * @param artist
     * @param format
     * @return
     * @throws UserNotLoggedInException
     * @throws DatabaseExecStatementException
     * @throws MusicNotFoundException
     * @throws OtherErrorException@
     * @throws UserDoesntOwnMusicException
     * @throws TemporaryKeyChangedException
     */
    public ProtectReturnStruct GetSongForUser(String username, String songTitle, String artist, 
                                              String format, boolean isPreview)                 throws UserNotLoggedInException, 
                                                                                                       DatabaseExecStatementException, 
                                                                                                       MusicNotFoundException, 
                                                                                                       OtherErrorException,
                                                                                                       UserDoesntOwnMusicException,
                                                                                                       MusicFormatNotFoundException{

        if(!IsUserLoggedIn(username) && !isPreview)
            throw new UserNotLoggedInException(username);
        
        try {
            Media song = _databaseOperations.getSongForUser(songTitle, artist, format, username);

            //get the api for the user
            CryptographicLibraryServerAPI cryptographicLibrary = _accountsLoggedIn.get(username);

            synchronized(cryptographicLibrary){
                
                //cut by a quarter if preview
                if(isPreview){
                    song.CutAudioBySomeFraction(0.25);
                }
                
                //get the encrypted song
                return cryptographicLibrary.Protect(song.toJSON());
            }
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (MusicNotFoundException e){
            throw e;
        } catch (UserNotLoggedInException e){
            throw e;
        } catch (UserDoesntOwnMusicException e){
            throw e;
        } catch (MusicFormatNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
        
    }


    public String CreateNewFamily(String username, String familyName) throws DatabaseExecStatementException, 
                                                                          AccountNotFoundException, 
                                                                          FamilyNameAlreadyExistsException,
                                                                          UserAlreadyBelongsToAFamilyException,
                                                                          Exception{

        try {

            if(!IsUserLoggedIn(username))
                throw new AccountNotFoundException(username);
            
            _databaseOperations.CreateNewFamilyForUser(username, familyName);
            
            String familyPassword = CreateRandomAuthCodeForFamily(10);

            String familyPasswordHashInBase64 = CryptographicLibraryServerAPI.HashPassword(familyPassword);

            _databaseOperations.RegisterFamilyPassword(familyName, familyPasswordHashInBase64);

            //need to generate a new temporary key for the family
            CryptographicLibraryServerAPI cryptographicLibrary = _accountsLoggedIn.get(username);
            synchronized(cryptographicLibrary){

                byte[] newTempKey = cryptographicLibrary.SetNewTemporaryKey().getEncoded();

                String familyTemporaryKeyInBase64 = Base64.getEncoder().encodeToString(newTempKey);

                _databaseOperations.setFamilyTemporaryKey(familyName, familyTemporaryKeyInBase64);

                _databaseOperations.addTemporaryKeyToUser(username, familyTemporaryKeyInBase64);
            }

            return familyPassword;

        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (FamilyNameAlreadyExistsException e) {
            throw e;
        } catch (UserAlreadyBelongsToAFamilyException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }

    }


    public void UserJoinsFamily(String username, String familyName, String familyPassword) throws AccountNotFoundException,
                                                                                                  WrongFamilyPasswordException,
                                                                                                  UserAlreadyBelongsToAFamilyException,
                                                                                                  FamilyDoesNotExistException,
                                                                                                  Exception{

        try {

            if(!IsUserLoggedIn(username))
                throw new AccountNotFoundException(username);

            String familyPasswordHash = _databaseOperations.GetFamilyPassowrdHash(familyName);

            if(!CryptographicLibraryServerAPI.ValidatePassword(familyPassword, familyPasswordHash))
                throw new WrongFamilyPasswordException(familyName);

            _databaseOperations.AddUserToFamily(username, familyName);

            String familyTemporaryKeyInBase64 = _databaseOperations.GetFamilyTemporaryKey(familyName);

            //need to generate a new temporary key for the family
            CryptographicLibraryServerAPI cryptographicLibrary = _accountsLoggedIn.get(username);
            synchronized(cryptographicLibrary){
                cryptographicLibrary.SetNewTemporaryKey(Base64.getDecoder().decode(familyTemporaryKeyInBase64));
                
                _databaseOperations.addTemporaryKeyToUser(username, familyTemporaryKeyInBase64);
            }
            
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (WrongFamilyPasswordException e) {
            throw e;
        } catch (UserAlreadyBelongsToAFamilyException e) {
            throw e;
        } catch (FamilyDoesNotExistException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        

    }

    private boolean DoesUserBelongToAFamily(String username) throws DatabaseExecStatementException{
        try {
            return _databaseOperations.DoesUserBelongToAFamily(username);
        } catch (DatabaseExecStatementException e) {
            throw e;
        }
    }


    public void UserLeavesCurrentFamily(String username) throws UserNotLoggedInException, 
                                                                UserDoesNotBelongToAFamilyException, 
                                                                DatabaseExecStatementException,
                                                                Exception{
        try{
            if(!IsUserLoggedIn(username))
                throw new UserNotLoggedInException(username);

            if(!DoesUserBelongToAFamily(username)){
                throw new UserDoesNotBelongToAFamilyException(username);
            }

            _databaseOperations.SetUserToLeaveFamily(username);
        } catch (DatabaseExecStatementException e ){
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }



}
