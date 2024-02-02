package GrooveClient.keyManagement;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import CryptographicLibrary.api.CryptographicLibraryClientAPI;
import CryptographicLibrary.util.CryptoIO;
import GrooveClient.exceptions.AuthenticityCheckFailed;
import GrooveClient.exceptions.UnableToCreateKeyStore;
import GrooveClient.exceptions.UnableToRetriveSymmetricKey;

public class KeyStoreMan {

    public static final String SYMMETRIC_ALGO = "AES"; 
    
    public static final String KEYSTORE_INSTANCE = "pkcs12";

    public static final String PASSWORD = "changeme";
    
    public static final String KEY_FOLDER_PATH = "/home/josecruz/Documents/MEIC/SIRS/a42-miguel-jose-simao/client/keys/";
    public static final String KEYSTORE_PATH = "keys/keystore.p12";
    
    public static final String SECRET_KEY_PATH = "keys/secret.key";
    public static final String SECRET_KEY_ALIAS = "secretKey";
    
    public static final String SESSION_KEY_ALIAS = "sessionKey";

    public static final String SERVER_KEYSTORE_PATH = "keys/server.p12";
    public static final String SERVER_PUBKEY_PATH = "keys/server.pubkey";

    public KeyStore keystore;

    /**
     * Loads the user KeyStore and stores the initial secret there.
     * 
     * @throws UnableToCreateKeyStore
     */
    public KeyStoreMan() throws UnableToCreateKeyStore{
        try {
            keystore = KeyStore.getInstance(KEYSTORE_INSTANCE);
            keystore.load(new FileInputStream(KEYSTORE_PATH), PASSWORD.toCharArray());
            System.out.println("keystore loaded");
            if (!keystore.containsAlias(SECRET_KEY_ALIAS)) {
                storeSymmetricKeyByPath(SECRET_KEY_PATH, SECRET_KEY_ALIAS);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new UnableToCreateKeyStore();
        }
    }

    
    public SecretKey getSymmetricKey(String keyAlias) throws UnableToRetriveSymmetricKey {
        try {
            SecretKey symmetricKey = (SecretKey) keystore.getKey(keyAlias, PASSWORD.toCharArray());
            return symmetricKey;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new UnableToRetriveSymmetricKey(keyAlias);
        }
    }

    public void storeSymmetricKeyByPath(String keyPath, String keyAlias) throws Exception {
        try {
            java.security.Key symKey = CryptoIO.readSecretKey(keyPath);
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(PASSWORD.toCharArray());
            SecretKey newSessionKey = new SecretKeySpec(symKey.getEncoded(), SYMMETRIC_ALGO);;
            KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(newSessionKey);
            this.keystore.setEntry(keyAlias, skEntry, protParam);
            this.keystore.store(new java.io.FileOutputStream(KEYSTORE_PATH), PASSWORD.toCharArray());
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Stores a session key, this session key can either be individual or belong to a family.
     * 
     * @param encryptedKey
     * @param iv
     * @param digitalSignature
     * @param keyPath used to decrypt
     * @param cryptoAPI used to check authenticity
     * @throws AuthenticityCheckFailed
     */
    public void storeNewSessionKey(CryptographicLibraryClientAPI cryptoAPI, String keyId) throws AuthenticityCheckFailed {
        try {
            SecretKey newSessionKey = cryptoAPI.getCurrentTemporaryKey();

            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(PASSWORD.toCharArray());
            KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(newSessionKey);
            this.keystore.setEntry(SESSION_KEY_ALIAS+keyId, skEntry, protParam);
            this.keystore.store(new java.io.FileOutputStream(KEYSTORE_PATH), PASSWORD.toCharArray());
        } catch (Exception e) {
            throw new AuthenticityCheckFailed();
        }
    }
    
    
    public KeyManagerFactory getKeyManagerFactory(String keyAlias) throws Exception {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(this.keystore, PASSWORD.toCharArray());

            // Optionally, filter the KeyManagers based on the key alias
            KeyManager[] keyManagers = Arrays.stream(keyManagerFactory.getKeyManagers())
                    .filter(km -> km instanceof X509ExtendedKeyManager)
                    .map(km -> (X509ExtendedKeyManager) km)
                    .filter(km -> km.getClientAliases("RSA", null) != null)
                    .filter(km -> Arrays.asList(km.getClientAliases("RSA", null)).contains(keyAlias))
                    .toArray(KeyManager[]::new);

            // Set the filtered KeyManagers back to the KeyManagerFactory
            keyManagerFactory.init(this.keystore, PASSWORD.toCharArray());;

            return keyManagerFactory;
        } catch (Exception e) {
            throw e;
        }
    }
    
    public TrustManagerFactory getTrustManagerFactory() throws Exception {
        try {
            // Load the server KeyStore
            KeyStore trustStore = KeyStore.getInstance(KEYSTORE_INSTANCE);
            try (FileInputStream fis = new FileInputStream(SERVER_KEYSTORE_PATH)) {
                trustStore.load(fis, PASSWORD.toCharArray());
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        } catch (Exception e) {
            throw e;
        }
    }

}
