package CryptographicLibrary.cli.commands;

import javax.crypto.SecretKey;

import CryptographicLibrary.api.CryptographicLibraryServerAPI;
import CryptographicLibrary.util.CryptoIO;

public class GenerateKeyCommand {
    
    public static void main(String[] args)throws Exception {
        if(args.length < 2){
            System.out.println("Usage : crypto-lib generate-key <path-to-store-key> <name-of-the-user>");
            return;
        }

        final String keyPath = args[0];
        final String keyName = args[1];

        try {

            System.out.println("Starting key generation");
            System.out.println();

            //create api for client
            SecretKey newSecretKey = CryptographicLibraryServerAPI.GenerateNewTemporaryKey();

            String fullPath = keyPath;

            if(!fullPath.endsWith("/")){
                fullPath += "/";
            }

            fullPath += keyName +"_secret.key";

            //generate key
            CryptoIO.writeToFile(fullPath, newSecretKey.getEncoded());

            System.out.println();
            System.out.println("Key generation was successful! Create key for user " + keyName + " at " + keyPath);

        } catch (Exception e) {
            throw e;
        }
    }

}
