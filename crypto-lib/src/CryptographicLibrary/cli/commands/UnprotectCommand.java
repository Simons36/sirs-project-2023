package CryptographicLibrary.cli.commands;

import com.google.gson.JsonObject;

import CryptographicLibrary.api.CryptographicLibraryClientAPI;
import CryptographicLibrary.util.CryptoIO;

public class UnprotectCommand {
    
    public static void main(String[] args) {
        
        if(args.length < 7){
            System.out.println("Usage : crypto-lib unprotect <path-to-encrypted-json> <path-to-iv> " + 
            "<path-to-permanent-secret-key> <path-to-temporary-secret-key> <seconds-to-expire> <path-to-write-results> <filename>");
            return;
        }

        final String encryptedJsonPath = args[0];
        final String ivPath = args[1];
        final String secretKeyPath = args[2];
        final String tempSecretKeyPath = args[3];
        final int secondsToExpire = Integer.parseInt(args[4]);
        final String pathToWriteResults = args[5];
        final String filename = args[6];

        try {

            System.out.println("Starting unprotection of file " + encryptedJsonPath);
            System.out.println();

            //read encrypted json
            byte[] encryptedJson = CryptoIO.readFile(encryptedJsonPath);

            //read iv
            byte[] iv = CryptoIO.readFile(ivPath);

            //create api for server
            CryptographicLibraryClientAPI api = new CryptographicLibraryClientAPI(secretKeyPath, secondsToExpire);

            api.AddEncryptedTemporaryKey(tempSecretKeyPath);

            //unprotect
            JsonObject jsonObject = api.Unprotect(encryptedJson, iv);

            //write results
            CryptoIO.writeJsonToFile(jsonObject, pathToWriteResults + filename);

            System.out.println();
            System.out.println("Finished unprotection of file " + encryptedJsonPath + "!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

}
