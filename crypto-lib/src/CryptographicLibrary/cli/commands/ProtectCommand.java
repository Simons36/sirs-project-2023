package CryptographicLibrary.cli.commands;

import java.security.Key;
import java.security.PrivateKey;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import CryptographicLibrary.api.CryptographicLibraryServerAPI;
import CryptographicLibrary.structs.ProtectReturnStruct;
import CryptographicLibrary.util.CryptoIO;
import CryptographicLibrary.util.JsonOperations;

public class ProtectCommand {
    //main
    public static void main(String[] args) throws Exception{

        if(args.length < 5){
            System.out.println("Usage : crypto-lib protect <path-to-json> <path-to-secret-key> <path-to-private-key> <path-to-write-results> <filename>");
            return;
        }

        final String jsonPath = args[0];
        final String secretKeyPath = args[1];
        final String privateKeyPath = args[2];
        final String pathToWriteResults = args[3];
        final String filename = args[4];

        //read json
        try{
            System.out.println("Starting protection of file " + jsonPath );
            System.out.println();

            JsonObject rootJson = JsonOperations.getJsonFromBytes(CryptoIO.readFile(jsonPath));

            //create api for server
            CryptographicLibraryServerAPI api = new CryptographicLibraryServerAPI(secretKeyPath, privateKeyPath);

            api.SetNewTemporaryKey();
            //protect
            ProtectReturnStruct protectReturnStruct = api.Protect(rootJson);


            //write results
            CryptoIO.writeToFile(pathToWriteResults + filename + ".enc", protectReturnStruct.getEncryptedContent());
            CryptoIO.writeToFile(pathToWriteResults + filename + ".iv", protectReturnStruct.getIv());
            CryptoIO.writeToFile(pathToWriteResults + filename + ".ds", protectReturnStruct.getDigitalSignature());
            CryptoIO.writeToFile(pathToWriteResults + filename + ".key", protectReturnStruct.getTempKeyEncrypted());

            System.out.println();
            System.out.println("Finished protection of file " + jsonPath  + "!");

        }catch(Exception e){
            System.out.println(e.getMessage());
            return;
        }
    }
}
