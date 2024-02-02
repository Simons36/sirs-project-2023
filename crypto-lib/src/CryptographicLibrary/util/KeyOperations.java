package CryptographicLibrary.util;

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class KeyOperations{

    private static final String ALGO = "AES";

    private static final int KEY_SIZE = 128;

    private static final String PBE_ALGO = "PBEWithSHA1AndDESede"; //PBE: Password Based Encryption
    
    public static SecretKey CreateTemporaryKey(){

        try {
            // create a key generator
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGO);
            keyGen.init(KEY_SIZE);
    
            // generate a key
            SecretKey key = keyGen.generateKey();
            return key;
            
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static byte[] EncryptKeyWithPassword(byte[] keyInBytes, String password) throws Exception{

        int count = 20;// hash iteration count
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        // Create PBE parameter set
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBE_ALGO);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(PBE_ALGO);

        // Initialize PBE Cipher with key and parameters
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

        // Encrypt the encoded Private Key with the PBE key
        byte[] ciphertext = pbeCipher.doFinal(keyInBytes);

        // Now construct  PKCS #8 EncryptedPrivateKeyInfo object
        AlgorithmParameters algparms = AlgorithmParameters.getInstance(PBE_ALGO);
        algparms.init(pbeParamSpec);
        EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);


        // encryptedPkcs8
        return encinfo.getEncoded();
    }

    public static byte[] DecryptKeyWithPassword(byte[] encryptedPkcs8, String password) throws Exception{

        EncryptedPrivateKeyInfo encryptPKInfo = new EncryptedPrivateKeyInfo(encryptedPkcs8);
        
        // Create PBE parameter set
        PBEParameterSpec pbeParamSpec = encryptPKInfo.getAlgParameters().getParameterSpec(PBEParameterSpec.class);

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBE_ALGO);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(PBE_ALGO);

        // Initialize PBE Cipher with key and parameters
        pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

        return pbeCipher.doFinal(encryptPKInfo.getEncryptedData());
    }

    

}
