package CryptographicLibrary.api;

import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.google.gson.JsonObject;

import CryptographicLibrary.util.CryptoIO;
import CryptographicLibrary.util.CryptoOperations;
import CryptographicLibrary.util.DigestOperations;
import CryptographicLibrary.util.JsonOperations;
import CryptographicLibrary.util.NonceManagement;

public class CryptographicLibraryClientAPI{

    private SecretKey _permanentKey;
    
    private SecretKey _currentTemporaryKey;

    private byte[] _temporaryKeyEncrypted = null;
    
    private PublicKey _publicKey;

    private NonceManagement _nonceManagement;

    public CryptographicLibraryClientAPI(SecretKey permanentKey, PublicKey  publicKey, int secondsToExpire){
        _permanentKey = permanentKey;
        _currentTemporaryKey = null;
        _publicKey = publicKey;
        _nonceManagement = new NonceManagement(secondsToExpire);
        
    }

    public CryptographicLibraryClientAPI(String permanentKeyPath, PublicKey publicKey, int secondsToExpire) throws Exception{
        _permanentKey = (SecretKey) CryptoIO.readSecretKey(permanentKeyPath);
        _currentTemporaryKey = null;
        _publicKey = publicKey;
        _nonceManagement = new NonceManagement(secondsToExpire);
    }

    public CryptographicLibraryClientAPI(SecretKey permanentKey, String publicKeyPath, int secondsToExpire) throws Exception{
        _permanentKey = permanentKey;
        _currentTemporaryKey = null;      
        _publicKey = CryptoIO.readPublicKey(publicKeyPath);
        _nonceManagement = new NonceManagement(secondsToExpire);

    }
    
    public CryptographicLibraryClientAPI(String permanentKeyPath, String publicKeyPath, int secondsToExpire) throws Exception{
        _permanentKey = (SecretKey) CryptoIO.readSecretKey(permanentKeyPath);
        _currentTemporaryKey = null;
        _publicKey = CryptoIO.readPublicKey(publicKeyPath);
        _nonceManagement = new NonceManagement(secondsToExpire);
    }

    public CryptographicLibraryClientAPI(SecretKey permanentKey, int secondsToExpire){
        _permanentKey = permanentKey;
        _currentTemporaryKey = null;
        _publicKey = null;
        _nonceManagement = new NonceManagement(secondsToExpire);
    }

    public CryptographicLibraryClientAPI(String permanentKeyPath, int secondsToExpire) throws Exception{
        _permanentKey = (SecretKey) CryptoIO.readSecretKey(permanentKeyPath);
        _currentTemporaryKey = null;
        _publicKey = null;
        _nonceManagement = new NonceManagement(secondsToExpire);
    }

    public CryptographicLibraryClientAPI(PublicKey publicKey){
        _permanentKey = null;
        _currentTemporaryKey = null;
        _publicKey = publicKey;
        _nonceManagement = new NonceManagement();
    }

    public CryptographicLibraryClientAPI(String publicKeyPath) throws Exception{
        _permanentKey = null;
        _currentTemporaryKey = null;
        _publicKey = CryptoIO.readPublicKey(publicKeyPath);
        _nonceManagement = new NonceManagement();
    }

    public CryptographicLibraryClientAPI(String permanentKeyPath, String publicKeyPath) throws Exception{
        _permanentKey = (SecretKey) CryptoIO.readSecretKey(permanentKeyPath);
        _currentTemporaryKey = null;
        _publicKey = CryptoIO.readPublicKey(publicKeyPath);
        _nonceManagement = new NonceManagement();
    }

    /**
     * This will take the encrypted session key and decrypt it using the permanent key
     * @param encryptedSessionKey
     * @throws Exception
     */
    public void AddEncryptedTemporaryKey(byte[] encryptedSessionKey) throws Exception{
        _temporaryKeyEncrypted = encryptedSessionKey;
        _currentTemporaryKey = CryptoOperations.DecryptSecretKeyWithOtherSecretKey(encryptedSessionKey, _permanentKey);
    }


    public void AddEncryptedTemporaryKey(String encryptedTemporaryKeyPath) throws Exception{
        AddEncryptedTemporaryKey(CryptoIO.readFile(encryptedTemporaryKeyPath));
    }

    public SecretKey getCurrentTemporaryKey() {
        return _currentTemporaryKey;
    }


    /**
     * This will erase the current session key
     */
    public void Logout(){
        _currentTemporaryKey = null;
    }


    /**
     * This takes encrypted content, iv and digital signature and returns true if the digital signature is valid
     * @param encryptedContent
     * @param iv
     * @param digitalSignature
     * @return
     * @throws Exception
     */
    public boolean Check(byte[] encryptedContent, byte[] iv, byte[] digitalSignature) throws Exception{

        if(_temporaryKeyEncrypted == null)
            throw new Exception("Session key not provided! Use AddEncryptedSessionKey() method to add it.");

        if(_publicKey == null)
            throw new Exception("Public key not provided in constructor!");

        try {

            byte[] contentPlusIv = DigestOperations.ConcatenateByteArrays(encryptedContent, iv);
            byte[] contentPlusIvPlusEncryptedTempKey = DigestOperations.ConcatenateByteArrays(contentPlusIv, _temporaryKeyEncrypted);

            return DigestOperations.verifyDigitalSignature(contentPlusIvPlusEncryptedTempKey, digitalSignature, _publicKey);

        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * This takes encrypted content and iv, and using the session key returns the decrypted json object
     * @param encryptedContent
     * @param iv
     * @return
     * @throws Exception
     */
    public JsonObject Unprotect(byte[] jsonInBytes, byte[] iv) throws Exception{

        if(_currentTemporaryKey == null)
            throw new Exception("Session key not provided! Use AddEncryptedSessionKey() method to add it.");

        if(_permanentKey == null)
            throw new Exception("Permanent key not provided in constructor!");

        try {

            //first we create the json
            JsonObject rootJson = JsonOperations.getJsonFromBytes(jsonInBytes);


            //now we extract the encrypted audio in base 64
            String encryptedAudioInBase64 = rootJson.get("media").getAsJsonObject()
                                                    .get("mediaContent").getAsJsonObject()
                                                    .get("audioBase64").getAsString();


            //now we decode the encrypted audio from base 64 and decrypt ir
            byte[] decryptedAudio = CryptoOperations.SymmetricDecrypt(Base64.getDecoder().decode(encryptedAudioInBase64), _currentTemporaryKey, iv);

            //now we add back to the json the decrypted audio
            rootJson.get("media").getAsJsonObject()
                    .get("mediaContent").getAsJsonObject()
                    .addProperty("audioBase64", new String(decryptedAudio));

            //verify nonce
            _nonceManagement.verifyNonce(rootJson);

            return rootJson;
        } catch (Exception e) {
            throw e;
        }

    }   
    
}
