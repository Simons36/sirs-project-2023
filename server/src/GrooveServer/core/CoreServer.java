package GrooveServer.core;

import java.util.ArrayList;
import java.util.List;

import CryptographicLibrary.structs.ProtectReturnStruct;
import GrooveServer.core.components.AccountManagement;
import GrooveServer.core.components.KeyManagement;
import GrooveServer.core.components.SubscriptionTimer;

import GrooveServer.database.DatabaseOperations;
import GrooveServer.service.exceptions.DatabaseExecStatementException;

import GrooveServer.exceptions.AccountNotFoundException;
import GrooveServer.exceptions.FamilyDoesNotExistException;
import GrooveServer.exceptions.FamilyNameAlreadyExistsException;
import GrooveServer.exceptions.MusicFormatNotFoundException;
import GrooveServer.exceptions.MusicNotFoundException;
import GrooveServer.exceptions.OtherErrorException;
import GrooveServer.exceptions.SecretKeyForUserNotFoundException;
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


public class CoreServer {

    private static CoreServer instance;

    private AccountManagement _accountManagement;

    private DatabaseOperations _databaseOperations;
    
    private CoreServer(int secondsToResetKeys, DatabaseOperations databaseOperations, String keystorePath, String privateKeyPath){
        _accountManagement = new AccountManagement(databaseOperations, new KeyManagement(keystorePath, databaseOperations), privateKeyPath);
        
        SubscriptionTimer.StartTimer(secondsToResetKeys, this);

        _databaseOperations = databaseOperations;
    }

    public static CoreServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CoreServer has not been initialized yet.");
        }
        return instance;
    }

    public static void init(int secondsToResetKeys, DatabaseOperations databaseOperations, String keystorePath, String privateKeyForDsPath) {
        if (instance != null) {
            throw new IllegalStateException("CoreServer has already been initialized.");
        }

        instance = new CoreServer(secondsToResetKeys, databaseOperations, keystorePath, privateKeyForDsPath);
    }

    /**
     * This function will reset all the temporary keys for all the users.
     */
    public void ResetAllTemporaryKeys(){
        _accountManagement.ResetAllTemporaryKeys();
    }


    /**
     * Creates new user (throws exception if user already exists)
     * @param username
     * @param password
     * @throws UsernameAlreadyExistsException
     * @throws OtherErrorException
     */
    public void Register(String username, String password) throws UsernameAlreadyExistsException, OtherErrorException{

        try {
            System.out.println("Received request to register new user with username: " + username);

            _accountManagement.CreateAccount(username, password);

            password = null; //clear the password from memory

            System.out.println("User " + username + " registered successfully.");
    
        } catch (UsernameAlreadyExistsException e){
            System.out.println("Register unsuccessful: " + e.getMessage());
            throw e;
        } catch (SecretKeyForUserNotFoundException e){
            System.out.println("Register unsuccessful: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Register unsuccessful: " + e.getMessage());
            throw new OtherErrorException(e.getMessage());
        }

    }


    /**
     * This function will login the user and return the encrypted temporary key
     * @param username
     * @param password
     * @return
     * @throws AccountNotFoundException
     * @throws WrongPasswordException
     * @throws UserAlreadyLoggedInException
     * @throws OtherErrorException
     */
    public void Login(String username, String password) throws AccountNotFoundException, WrongPasswordException, UserAlreadyLoggedInException, OtherErrorException{
        System.out.println("User " + username + " requested to login.");

        try {
            _accountManagement.Login(username, password);
            System.out.println("User " + username + " logged in successfully.");
            System.out.println();
            
        } catch (AccountNotFoundException e) {
            System.out.println("Login unsuccessful: " + e.getMessage());
            throw e;
        } catch (WrongPasswordException e) {
            System.out.println("Login unsuccessful: " + e.getMessage());
            throw e;
        } 
        catch (UserAlreadyLoggedInException e) {
            System.out.println("Login unsuccessful: " + e.getMessage());
            //throw e;
        } 
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Login unsuccessful: " + e.getMessage());
            throw new OtherErrorException(e.getMessage());
        }

    }


    /**
     * This function will add to the user's owned musics a music; it does not return the music
     * @param username
     * @param songTitle
     * @param artist
     * @throws UserNotLoggedInException
     * @throws MusicNotFoundException
     * @throws UserAlreadyOwnsMusicException
     * @throws OtherErrorException
     */
    public void PurchaseSong(String username, String songTitle, String artist) throws UserNotLoggedInException, 
                                                                                      MusicNotFoundException, 
                                                                                      UserAlreadyOwnsMusicException, 
                                                                                      OtherErrorException{
        try {
            System.out.println("User " + username + " requested to purchase song '" + songTitle + "' by '" + artist + "'.");

            _accountManagement.PurchaseSong(username, songTitle, artist);

            System.out.println("User " + username + " purchased song '" + songTitle + "' by '" + artist + "' successfully.");
            System.out.println();
            
        } catch (UserNotLoggedInException e) {
            System.out.println("Purchase unsuccessful: " + e.getMessage());
            throw e;
        } catch (MusicNotFoundException e) {
            System.out.println("Purchase unsuccessful: " + e.getMessage());
            throw e;
        } catch (UserAlreadyOwnsMusicException e) {
            System.out.println("Purchase unsuccessful: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Purchase unsuccessful: " + e.getMessage());
            throw new OtherErrorException(e.getMessage());
        }
    }


    /**
     * This function will return a list of all the musics that belong to the genre searched (for each music it will return all the available formats)
     * @param genre
     * @return
     * @throws DatabaseExecStatementException
     * @throws GenreDoesNotExistsException
     * @throws OtherErrorException
     */
    public List<Media> SearchByGenre(String genre) throws DatabaseExecStatementException, OtherErrorException{

        try {
            List<Integer> songsIds = _databaseOperations.GetSongsIdByGenre(genre);

            List<Media> allSongsMetadata = new ArrayList<Media>();

            for (Integer songId : songsIds) {

                String songTitle = _databaseOperations.GetSongTitleById(songId);

                String artist = _databaseOperations.GetSongArtistById(songId);

                List<String> genres = _databaseOperations.GetSongGenresById(songId);

                List<String> formats = _databaseOperations.GetSongFormatsById(songId);

                for(String format : formats){
                    Media media = new Media(format, songTitle, artist, genres.toArray(new String[0]));
                    allSongsMetadata.add(media);
                }
                
            }

            return allSongsMetadata;
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        }catch (OtherErrorException e) {
            throw e;
        }catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }

    }


    /**
     * This function will return a list of all the musics that belong to the artist searched (for each music it will return all the available formats)
     * @param artist
     * @return
     * @throws DatabaseExecStatementException
     * @throws MusicNotFoundException
     * @throws OtherErrorException
     */
    public List<Media> SearchByArtist(String artist) throws DatabaseExecStatementException, OtherErrorException{

        try {
            List<Integer> songsIds = _databaseOperations.GetSongsIdByArtist(artist);

            List<Media> allSongsMetadata = new ArrayList<Media>();

            for (Integer songId : songsIds) {

                String songTitle = _databaseOperations.GetSongTitleById(songId);

                String artistOfSong = _databaseOperations.GetSongArtistById(songId);

                List<String> genres = _databaseOperations.GetSongGenresById(songId);

                List<String> formats = _databaseOperations.GetSongFormatsById(songId);

                for(String format : formats){
                    Media media = new Media(format, songTitle, artistOfSong, genres.toArray(new String[0]));
                    allSongsMetadata.add(media);
                }
                
            }

            return allSongsMetadata;
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (OtherErrorException e) {
            throw e;
        }catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }

    /**
     * This function will return a list of all the musics that belong to the title searched (for each music it will return all the available formats)
     * @param songTitle
     * @return
     * @throws DatabaseExecStatementException
     * @throws OtherErrorException
     */
    public List<Media> SearchSongByTitle(String songTitle) throws DatabaseExecStatementException, OtherErrorException{

        try {
            List<Integer> songsIds = _databaseOperations.GetSongsIdByTitle(songTitle);

            List<Media> allSongsMetadata = new ArrayList<Media>();

            for (Integer songId : songsIds) {

                String songTitleOfSong = _databaseOperations.GetSongTitleById(songId);

                String artist = _databaseOperations.GetSongArtistById(songId);

                List<String> genres = _databaseOperations.GetSongGenresById(songId);

                List<String> formats = _databaseOperations.GetSongFormatsById(songId);

                for(String format : formats){
                    Media media = new Media(format, songTitleOfSong, artist, genres.toArray(new String[0]));
                    allSongsMetadata.add(media);
                }
                
            }

            return allSongsMetadata;
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (OtherErrorException e) {
            throw e;
        }catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }


    /**
     * This function returns all of the musics that the user owns
     * @param username
     * @return
     * @throws UserNotLoggedInException
     * @throws DatabaseExecStatementException
     * @throws OtherErrorException
     */
    public List<Media> GetUserPurchases(String username) throws UserNotLoggedInException, DatabaseExecStatementException, OtherErrorException{
        
        System.out.println("User " + username + " requested to get his purchases.");

        try {
            return _accountManagement.GetUserPurchases(username);
            
        } catch (UserNotLoggedInException e) {
            throw e;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (OtherErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }

    }

    /**
     * This function will add a music to the user's preferences
     * @param username
     * @param songTitle
     * @param artist
     * @throws UserNotLoggedInException
     * @throws DatabaseExecStatementException
     * @throws MusicNotFoundException
     * @throws UserDoesntOwnMusicException
     * @throws OtherErrorException
     */
    public void AddMusicToUserPreferences(String username, String songTitle, String artist) throws UserNotLoggedInException, 
                                                                                                   DatabaseExecStatementException, 
                                                                                                   MusicNotFoundException, 
                                                                                                   UserDoesntOwnMusicException, 
                                                                                                   OtherErrorException{

        try {
            _accountManagement.AddSongToUserPreferences(username, songTitle, artist);
        } catch (UserNotLoggedInException e) {
            throw e;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (MusicNotFoundException e) {
            throw e;
        } catch (UserDoesntOwnMusicException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }

    }


    /**
     * This function returns all of the musics that the user has in his preferences
     * @param username
     * @return
     * @throws UserNotLoggedInException
     * @throws DatabaseExecStatementException
     * @throws OtherErrorException
     */
    public List<Media> GetUserPreferences(String username) throws UserNotLoggedInException, DatabaseExecStatementException, OtherErrorException{

        try {

            return _accountManagement.GetUserPreferences(username);

        } catch (UserNotLoggedInException e) {
            throw e;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (OtherErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }

    }

    /**
     * This will fetch a song for a user (if he owns it) and return it encrypted (along with the used iv and encrypted temporary key, and also the digital signature)
     * @param username
     * @param songTitle
     * @param artist
     * @param format
     * @return
     * @throws UserNotLoggedInException
     * @throws DatabaseExecStatementException
     * @throws MusicNotFoundException
     * @throws UserDoesntOwnMusicException
     * @throws TemporaryKeyChangedException
     * @throws OtherErrorException
     */
    public ProtectReturnStruct GetSongForUser(String username, String songTitle, String artist, String format) throws UserNotLoggedInException, 
                                                                                                                      DatabaseExecStatementException, 
                                                                                                                      MusicNotFoundException, 
                                                                                                                      UserDoesntOwnMusicException,
                                                                                                                      OtherErrorException,
                                                                                                                      MusicFormatNotFoundException{

        try {
            return _accountManagement.GetSongForUser(username, songTitle, artist, format, false);
        } catch (UserNotLoggedInException e) {
            throw e;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (MusicNotFoundException e) {
            throw e;
        } catch (UserDoesntOwnMusicException e) {
            throw e;
        }catch (MusicFormatNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }


    /**
     * This is the same as GetSongForUser, but it will return a preview of the song (one quarter of the song). The user doesn't need to own the song
     * @param username
     * @param songTitle
     * @param artist
     * @param format
     * @return
     */
    public ProtectReturnStruct GetPreviewOfSong(String username, String songTitle, String artist, String format) throws UserNotLoggedInException, 
                                                                                                                        DatabaseExecStatementException, 
                                                                                                                        MusicNotFoundException,
                                                                                                                        OtherErrorException,
                                                                                                                        MusicFormatNotFoundException{
        try {
            return _accountManagement.GetSongForUser(username, songTitle, artist, format, true);
        } catch (UserNotLoggedInException e) {
            throw e;
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (MusicNotFoundException e) {
            throw e;
        } catch (MusicFormatNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }


    /**
     * This function will create a family with the provided name, add the user that called it to it and return a auth code (4 digit int) that
     * the user can give to other users to join the family
     * @param username
     * @param familyName
     * @return
     * @throws DatabaseExecStatementException
     * @throws AccountNotFoundException
     * @throws FamilyNameAlreadyExistsException
     * @throws UserAlreadyBelongsToAFamilyException
     * @throws OtherErrorException
     */
    public String CreateNewFamily(String username, String familyName) throws DatabaseExecStatementException,
                                                                          AccountNotFoundException,
                                                                          FamilyNameAlreadyExistsException,
                                                                          UserAlreadyBelongsToAFamilyException,
                                                                          OtherErrorException{
    
        try{
            return _accountManagement.CreateNewFamily(username, familyName);
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (FamilyNameAlreadyExistsException e) {
            throw e;
        } catch (UserAlreadyBelongsToAFamilyException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
        
    }

    /**
     * This function will add a user to a family (if the family exists and the password is correct)
     * @param username
     * @param familyName
     * @param familyPassword
     * @throws DatabaseExecStatementException
     * @throws AccountNotFoundException
     * @throws FamilyDoesNotExistException
     * @throws UserAlreadyBelongsToAFamilyException
     * @throws WrongFamilyPasswordException
     * @throws OtherErrorException
     */
    public void UserJoinsFamily(String username, String familyName, String familyPassword) throws DatabaseExecStatementException,
                                                                                                 AccountNotFoundException,
                                                                                                 FamilyDoesNotExistException,
                                                                                                 UserAlreadyBelongsToAFamilyException,
                                                                                                 WrongFamilyPasswordException,
                                                                                                 OtherErrorException{
        try {
            _accountManagement.UserJoinsFamily(username, familyName, familyPassword);
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (FamilyDoesNotExistException e) {
            throw e;
        } catch (UserAlreadyBelongsToAFamilyException e) {
            throw e;
        } catch (WrongFamilyPasswordException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }

    public void UserLeavesCurrentFamily(String username) throws DatabaseExecStatementException, 
                                                                UserNotLoggedInException, 
                                                                UserDoesNotBelongToAFamilyException, 
                                                                OtherErrorException{
        try {
            _accountManagement.UserLeavesCurrentFamily(username);
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (UserNotLoggedInException e) {
            throw e;
        } catch (UserDoesNotBelongToAFamilyException e) {
            throw e;
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }
    }
    
}
