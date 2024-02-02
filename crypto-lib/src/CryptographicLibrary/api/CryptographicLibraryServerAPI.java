package CryptographicLibrary.api;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonObject;

import CryptographicLibrary.structs.ProtectReturnStruct;
import CryptographicLibrary.util.CryptoIO;
import CryptographicLibrary.util.CryptoOperations;
import CryptographicLibrary.util.DigestOperations;
import CryptographicLibrary.util.JsonOperations;
import CryptographicLibrary.util.KeyOperations;
import CryptographicLibrary.util.NonceManagement;
import CryptographicLibrary.util.PasswordHash;

public class CryptographicLibraryServerAPI{

    private PrivateKey  _privateKey;

    private SecretKey _permanentKey;

    private SecretKey _currentTempKey;

    private NonceManagement _nonceManagement;

    private static final int BITS_FOR_NONCE = 64;
    private static final int BITS_FOR_COUNTER = 64;
    
    public CryptographicLibraryServerAPI(SecretKey permanentKey, PrivateKey privateKey){
        _currentTempKey = null;
        _permanentKey = permanentKey;
        _privateKey = privateKey;
        _nonceManagement = new NonceManagement();
    }

    public CryptographicLibraryServerAPI(SecretKey permanentKey, PrivateKey privateKey, SecretKey currentTempKey){
        this(permanentKey, privateKey);
        _currentTempKey = currentTempKey;
        _nonceManagement = new NonceManagement();
    }

    public CryptographicLibraryServerAPI(SecretKey permanentKey, String privateKeyPath) throws Exception{
        _permanentKey = permanentKey;
        _currentTempKey = null;
        _privateKey = CryptoIO.readPrivateKey(privateKeyPath);
        _nonceManagement = new NonceManagement();
    }

    public CryptographicLibraryServerAPI(SecretKey permanentKey, String privateKeyPath, SecretKey currentTempKey) throws Exception{
        this(permanentKey, privateKeyPath);
        _currentTempKey = currentTempKey;
        _nonceManagement = new NonceManagement();
    }

    //constructor for string, string
    public CryptographicLibraryServerAPI(String permanentKeyPath, String privateKeyPath) throws Exception{
        _permanentKey = (SecretKey) CryptoIO.readSecretKey(permanentKeyPath);
        _currentTempKey = null;
        _privateKey = CryptoIO.readPrivateKey(privateKeyPath);
        _nonceManagement = new NonceManagement();
    }

    public CryptographicLibraryServerAPI(String permanentKeyPath, String privateKeyPath, SecretKey currentTempKey) throws Exception{
        this(permanentKeyPath, privateKeyPath);
        _currentTempKey = currentTempKey;
        _nonceManagement = new NonceManagement();
    }


    /**
     * This will take a json object, encrypt it, and return the encrypted content, the iv and the digital signature
     * @param encryptedContent
     * @param iv
     * @param digitalSignature
     * @return
     * @throws Exception
     */
    public ProtectReturnStruct Protect(JsonObject jsonObject) throws Exception{

        if(_currentTempKey == null){
            throw new Exception("No temporary key provided! To generate a temporary key, use AddNewTemporaryKey()");
        }

        //add nonce to json
        _nonceManagement.addNonce(jsonObject);

        try {
            //encrypt json
            byte[] iv = _nonceManagement.GenerateNonceAndCounterForCTREncryption(BITS_FOR_NONCE, BITS_FOR_COUNTER);

            //first we get the audio in base64
            String audioInBase64 = jsonObject.get("media").getAsJsonObject()
                                             .get("mediaContent").getAsJsonObject()
                                             .get("audioBase64").getAsString();

            //we encrypt the audio with the session key
            byte[] encryptedAudioBase64 = CryptoOperations.SymmetricEncrypt(audioInBase64.getBytes(), _currentTempKey, iv);


            //replace the audio with the encrypted version
            jsonObject.get("media").getAsJsonObject()
                      .get("mediaContent").getAsJsonObject()
                      .addProperty("audioBase64", Base64.getEncoder().encodeToString(encryptedAudioBase64));

            //convert json to byte[]
            byte[] jsonBytes = JsonOperations.getBytesFromJson(jsonObject);
            
            //concatenate encrypted content with iv (if iv is changed, the decryption will fail)
            byte[] contentPlusIV = DigestOperations.ConcatenateByteArrays(jsonBytes, iv);

            //also add temp key encrypted with permanent key
            byte[] tempKeyEncrypted = getSessionKeyEncryptedWithPermanentKey();
            byte[] contentPlusIVPlusTempKey = DigestOperations.ConcatenateByteArrays(contentPlusIV, tempKeyEncrypted);

            //make digital signature
            byte[] digitalSignature = DigestOperations.makeDigitalSignature(contentPlusIVPlusTempKey, _privateKey);
    
            return new ProtectReturnStruct(jsonBytes, iv, digitalSignature, tempKeyEncrypted);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * This method returns the session key encrypted with the permanent key, to be sent to the client
     * @return
     * @throws Exception
     */
    public byte[] getSessionKeyEncryptedWithPermanentKey() throws Exception{

        try {
            return CryptoOperations.EncryptSecretKeyWithOtherSecretKey(_currentTempKey, _permanentKey);
            
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * This method encrypts a given key with another key: to be used on account creation
     * @param temporaryKey
     * @param permanentKey
     * @return
     * @throws Exception
     */
    public static byte[] GetTemporaryKeyEncryptedWithPermanentKey(SecretKey temporaryKey, SecretKey permanentKey) throws Exception{
        return CryptoOperations.EncryptSecretKeyWithOtherSecretKey(temporaryKey, permanentKey);
    }

    public SecretKey SetNewTemporaryKey(){
        _currentTempKey = GenerateNewTemporaryKey();
        return _currentTempKey;
    }

    public void SetNewTemporaryKey(byte[] newTemporaryKey){
        _currentTempKey = new SecretKeySpec(newTemporaryKey, "AES");
    }


    public static String HashPassword(String password) throws Exception {
        return PasswordHash.createHash(password);
    }

    public static boolean ValidatePassword(String password, String correctHash) throws Exception {
        return PasswordHash.validatePassword(password, correctHash);
    }

    public static SecretKey ReadSecretKeyFromFile(String filepath) throws IOException{
        try {
            return (SecretKey) CryptoIO.readSecretKey(filepath);
        } catch (IOException e) {
            throw e;
        }
    }

    public static byte[] EncryptKeyWithPassword(byte[] keyInBytes, String password) throws Exception{
        return KeyOperations.EncryptKeyWithPassword(keyInBytes, password);
    }

    public static byte[] DecryptKeyWithPassword(byte[] keyInBytes, String password) throws Exception{
        return KeyOperations.DecryptKeyWithPassword(keyInBytes, password);
    }

    public static SecretKey GenerateNewTemporaryKey(){
        return KeyOperations.CreateTemporaryKey();
    }

    public static SecretKey DecryptTemporaryKeyWithPermanentKey(byte[] encryptedTemporaryKey, SecretKey permanentKey) throws Exception{
        return CryptoOperations.DecryptSecretKeyWithOtherSecretKey(encryptedTemporaryKey, permanentKey);
    }
    
}
