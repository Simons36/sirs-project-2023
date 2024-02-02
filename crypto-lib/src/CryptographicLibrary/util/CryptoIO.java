package CryptographicLibrary.util;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.spec.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


public class CryptoIO {

    public static byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static void writeToFile(String filepath, byte[] content) throws Exception {
        FileOutputStream fos = new FileOutputStream(filepath);
        fos.write(content);
        fos.close();
        System.out.println("Wrote " + content.length + " bytes to file " + filepath);
    }

    public static void writeJsonToFile(JsonObject jsonObject, String filepath) throws Exception {
        try (FileWriter fileWriter = new FileWriter(filepath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        }
    }

    public static Key readSecretKey(String secretKeyPath) throws FileNotFoundException, IOException{
        try {
            
            byte[] encoded = readFile(secretKeyPath);
            SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");
            return keySpec;
        } catch (Exception e) {
            throw e;
        }
    }

    public static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        System.out.println("Reading public key from file " + publicKeyPath + " ...");
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        PublicKey pub = keyFacPub.generatePublic(pubSpec);
        return pub;
    }

    public static PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);
        return priv;
    }

}
