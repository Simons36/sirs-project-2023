package CryptographicLibrary.test;

import com.google.gson.JsonObject;

import CryptographicLibrary.api.*;
import CryptographicLibrary.structs.ProtectReturnStruct;
import CryptographicLibrary.util.CryptoIO;
import CryptographicLibrary.util.JsonOperations;


public class TestCryptoLib {
    //main
    public static void main(String[] args) throws Exception{
        //read json
        try{
            final String filename = "document.json";
            final String pathToWriteResults = "../local-test/";
            final String jsonPath = pathToWriteResults + filename;
            final String secretKeyPath = "../server/keys/alice_secret.key";
            final String publicKeyPath = "../server/keys/server.pubkey";
            final String privateKeyPath = "../server/keys/server.privkey";

            System.out.println("Starting protection of file " + jsonPath );
            System.out.println();

            JsonObject rootJson = JsonOperations.getJsonFromBytes(CryptoIO.readFile(jsonPath));

            //create api for server
            CryptographicLibraryServerAPI apiServer = new CryptographicLibraryServerAPI(secretKeyPath, privateKeyPath);

            apiServer.SetNewTemporaryKey();

            CryptographicLibraryClientAPI apiClient = new CryptographicLibraryClientAPI(secretKeyPath, publicKeyPath, 1);

            //protect
            ProtectReturnStruct protectReturnStruct = apiServer.Protect(rootJson);
            System.out.println();
            System.out.println("Finished protection of file " + jsonPath  + "!");

            //add temporary key
            apiClient.AddEncryptedTemporaryKey(protectReturnStruct.getTempKeyEncrypted());

            //check
            System.out.println();
            apiClient.Check(protectReturnStruct.getEncryptedContent(), protectReturnStruct.getIv(), protectReturnStruct.getDigitalSignature());

            //unprotect
            JsonObject jsonObject = apiClient.Unprotect(protectReturnStruct.getEncryptedContent(), protectReturnStruct.getIv());

            System.out.println(jsonObject);

            //print json
            CryptoIO.writeJsonToFile(jsonObject, "../local-test/unprotected.json");


            apiClient.Unprotect(protectReturnStruct.getEncryptedContent(), protectReturnStruct.getIv());
            
            //print json
            System.out.println();
            System.out.println("Printing unprotected json:");
            System.out.println(jsonObject.toString());

            // ---------------------------- TEST --------------------------------

            // System.out.println();

            // byte[] secretKeyTestInBytes = CryptoIO.readSecretKey("../server/keys/secret.key").getEncoded();
            // System.out.println("Key not encrypted: " + Base64.getEncoder().encodeToString(secretKeyTestInBytes));
            // System.out.println("Key not encrypted length: " + secretKeyTestInBytes.length);

            // byte[] encryptedKeyInBytes = KeyOperations.EncryptKeyWithPassword(secretKeyTestInBytes, "passergword");
            // String encryptedKeyInBase64 = Base64.getEncoder().encodeToString(encryptedKeyInBytes);

            // System.out.println("Key encrypted: " + Base64.getEncoder().encodeToString(encryptedKeyInBytes));
            // System.out.println("Key encrypted length: " + encryptedKeyInBytes.length);
            // System.out.println("Key encrypted in base64: " + encryptedKeyInBase64.length());

            // byte[] decryptedKeyInBytes = KeyOperations.DecryptKeyWithPassword(encryptedKeyInBytes, "passergword");

            // System.out.println("Key decrypted: " + Base64.getEncoder().encodeToString(decryptedKeyInBytes));
            // System.out.println("Key decrypted length: " + decryptedKeyInBytes.length);

        }catch(Exception e){
            System.out.println(e.getMessage());
            return;
        }
    }
}
