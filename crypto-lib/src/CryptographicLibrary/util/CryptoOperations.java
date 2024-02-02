package CryptographicLibrary.util;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoOperations {

    final static String ALGO = "AES/CTR/NoPadding";
    
    public static byte[] SymmetricEncrypt(byte[] plainBytes, Key key, byte[] iv) throws Exception {
        // cipher data

        System.out.println();
        System.out.println("IV: " + Base64.getEncoder().encodeToString(iv));
        System.out.println("Key: " + Base64.getEncoder().encodeToString(key.getEncoded()));
        System.out.println();

        System.out.println("Ciphering with " + ALGO + "...");
        Cipher cipher = Cipher.getInstance(ALGO);

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherBytes = cipher.doFinal(plainBytes);
        System.out.println("Result: " + cipherBytes.length + " bytes");
        return cipherBytes;
    }

    public static byte[] SymmetricDecrypt(byte[] cipherBytes, Key key, byte[] iv) throws Exception {
        // cipher data

        System.out.println("Ciphering with " + ALGO + "...");
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        System.out.println("Result: " + plainBytes.length + " bytes");
        return plainBytes;
    }

    public static byte[] EncryptSecretKeyWithOtherSecretKey(SecretKey keyThatIsEncrypted, SecretKey keyThatWillEncrypt) throws Exception {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keyThatWillEncrypt);

        byte[] cipherBytes = cipher.doFinal(keyThatIsEncrypted.getEncoded());

        return cipherBytes;
    }

    public static SecretKey DecryptSecretKeyWithOtherSecretKey(byte[] keyBeingDecrypted, SecretKey keyThatWillDecrypt) throws Exception {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keyThatWillDecrypt);

        byte[] keyDecryptedBytes = cipher.doFinal(keyBeingDecrypted);

        return new SecretKeySpec(keyDecryptedBytes, "AES");
    }

    
}
