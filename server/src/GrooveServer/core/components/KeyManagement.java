package GrooveServer.core.components;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.Data;

import CryptographicLibrary.api.CryptographicLibraryServerAPI;
import GrooveServer.database.DatabaseOperations;
import GrooveServer.exceptions.OtherErrorException;
import GrooveServer.exceptions.SecretKeyForUserNotFoundException;
import GrooveServer.service.exceptions.DatabaseExecStatementException;
import GrooveServer.util.UtilClasses;

public class KeyManagement {

    //IMPORTANT: keys are being stored using the following format: <username>_secret.key 
    //                                        and in the path: <_keystorePath> (Specified in the pom.xml)  
    private String _keystorePath;

    private DatabaseOperations _databaseOperations;

    public KeyManagement(String keystorePath, DatabaseOperations databaseOperations){
        _keystorePath = keystorePath;
        _databaseOperations = databaseOperations;
    }

    public void ResetAllTemporaryKeys(){

    }

    public void SelectPermanentKey(String username, String password) throws SecretKeyForUserNotFoundException, OtherErrorException{

        try {
            String userKeyPath = GetUserSecretKeyPath(username);

            //read the permanent key from the file
            SecretKey permanentKey = CryptographicLibraryServerAPI.ReadSecretKeyFromFile(userKeyPath);

            //encrypt this key with user password
            byte[] permanentKeyEncryptedInBytes = CryptographicLibraryServerAPI.EncryptKeyWithPassword(permanentKey.getEncoded(), password);

            password = null; //clear the password from memory

            //encode in base64
            String permanentKeyEncryptedInBase64 = Base64.getEncoder().encodeToString(permanentKeyEncryptedInBytes);

            SecretKey temporaryKey = CryptographicLibraryServerAPI.GenerateNewTemporaryKey();

            byte[] temporaryKeyEncryptedInBytes = CryptographicLibraryServerAPI.GetTemporaryKeyEncryptedWithPermanentKey(temporaryKey, permanentKey);
            String temporaryKeyEncryptedInBase64 = Base64.getEncoder().encodeToString(temporaryKeyEncryptedInBytes);

            _databaseOperations.addPermanentKeyToUser(username, permanentKeyEncryptedInBase64);
            _databaseOperations.addTemporaryKeyToUser(username, temporaryKeyEncryptedInBase64);

            //replace the permanent key with the encrypted one
            UtilClasses.WriteToFile(permanentKeyEncryptedInBase64.getBytes(), userKeyPath);

            
            //erase the old permanent key and store the encrypted version
            UtilClasses.RenameFile(userKeyPath, _keystorePath + username + "_secret_encrypted_base64.key");

        } catch (IOException e) {
            throw new SecretKeyForUserNotFoundException(username);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtherErrorException(e.getMessage());
        }

    }

    public void SelectPermanentKey(String username) throws SecretKeyForUserNotFoundException, OtherErrorException{
        try {
            String userKeyPath = GetUserSecretKeyPath(username);

            //read the permanent key from the file
            SecretKey permanentKey = CryptographicLibraryServerAPI.ReadSecretKeyFromFile(userKeyPath);

            //encode in base64
            String permanentKeyInBase64 = Base64.getEncoder().encodeToString(permanentKey.getEncoded());

            SecretKey temporaryKey = CryptographicLibraryServerAPI.GenerateNewTemporaryKey();

            String temporaryKeyInBase64 = Base64.getEncoder().encodeToString(temporaryKey.getEncoded());

            _databaseOperations.addPermanentKeyToUser(username, permanentKeyInBase64);
            _databaseOperations.addTemporaryKeyToUser(username, temporaryKeyInBase64);

        } catch (IOException e) {
            throw new SecretKeyForUserNotFoundException(username);
        } catch (Exception e) {
            throw new OtherErrorException(e.getMessage());
        }

    }


    private String GetUserSecretKeyPath(String username){

        if(_keystorePath.charAt(_keystorePath.length() - 1) != '/'){
            _keystorePath += "/";
        }


        return _keystorePath + username + "_secret.key";

    }

    public SecretKey GetEncryptedPermanentKeyAndDecryptIt(String username, String password) throws DatabaseExecStatementException, Exception{
        try {
            //get the encrypted permanent key
            String encryptedPermanentKeyInBase64 = _databaseOperations.GetEncryptedPermanentKeyInBase64(username);
            
            //decode from base64
            byte[] encryptedPermanentKeyInBytes = Base64.getDecoder().decode(encryptedPermanentKeyInBase64);
            
            //decrypt the permanent key using user password
            byte[] permanentKeyInBytes = CryptographicLibraryServerAPI.DecryptKeyWithPassword(encryptedPermanentKeyInBytes, password);
            
            //return the permanent key
            return (SecretKey) new SecretKeySpec(permanentKeyInBytes, 0, permanentKeyInBytes.length, "AES");
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    //The above function is to get PERMANENT KEY and decrypt using PASSWORD
    //The below function is to get TEMPORARY KEY and decrypt using PERMANENT KEY (unencrypted)

    public SecretKey GetEncryptedTemporaryKeyAndDecryptIt(String username, SecretKey permanentKey) throws DatabaseExecStatementException, Exception{
        try {
            //get the encrypted temporary key
            String encryptedTemporaryKeyInBase64 = _databaseOperations.GetEncryptedTemporaryKeyInBase64(username);
            
            //decode from base64
            byte[] encryptedTemporaryKeyInBytes = Base64.getDecoder().decode(encryptedTemporaryKeyInBase64);
            
            //decrypt the temporary key using permanent key and return it
            return CryptographicLibraryServerAPI.DecryptTemporaryKeyWithPermanentKey(encryptedTemporaryKeyInBytes, permanentKey);
            
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public byte[] GetEncryptedTemporaryKey(String username) throws DatabaseExecStatementException, Exception{
        try {
            //get the encrypted temporary key
            String encryptedTemporaryKeyInBase64 = _databaseOperations.GetEncryptedTemporaryKeyInBase64(username);
            
            //decode from base64
            byte[] encryptedTemporaryKeyInBytes = Base64.getDecoder().decode(encryptedTemporaryKeyInBase64);
            
            //return the encrypted temporary key
            return encryptedTemporaryKeyInBytes;
            
            
        } catch (DatabaseExecStatementException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public SecretKey GetPermanentKey(String username){
        String permanentKeyInBase64 = _databaseOperations.GetEncryptedPermanentKeyInBase64(username);

        byte[] permanentKeyInBytes = Base64.getDecoder().decode(permanentKeyInBase64);

        return (SecretKey) new SecretKeySpec(permanentKeyInBytes, 0, permanentKeyInBytes.length, "AES");
    } 

    public SecretKey DecryptTemporaryKeyWithPermanentKey(byte[] encryptedTemporaryKey, SecretKey permanentKey) throws Exception{
        return CryptographicLibraryServerAPI.DecryptTemporaryKeyWithPermanentKey(encryptedTemporaryKey, permanentKey);
    }
    
}
