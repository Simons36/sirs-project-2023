package CryptographicLibrary.cli.commands;

import CryptographicLibrary.api.CryptographicLibraryClientAPI;
import CryptographicLibrary.util.CryptoIO;

public class CheckCommand {
    
    public static void main(String[] args)throws Exception {
        if(args.length < 6){
            System.out.println("Usage : crypto-lib check <path-to-encrypted-json> <path-to-iv> <path-to-permanent-secret-key> <path-to-encrypted-temp-key> <path-to-digital-signature> <path-to-public-key>");
            return;
        }

        final String encryptedJsonPath = args[0];
        final String ivPath = args[1];
        final String permanentKeyPath = args[2];
        final String encryptedTempKeyPath = args[3];
        final String digitalSignaturePath = args[4];
        final String publicKeyPath = args[5];


        try {

            System.out.println("Starting check of file " + encryptedJsonPath);
            System.out.println();

            //read encrypted json
            byte[] encryptedJson = CryptoIO.readFile(encryptedJsonPath);

            //read iv
            byte[] iv = CryptoIO.readFile(ivPath);

            //read digital signature
            byte[] digitalSignature = CryptoIO.readFile(digitalSignaturePath);

            //create api for client
            CryptographicLibraryClientAPI api = new CryptographicLibraryClientAPI(permanentKeyPath,publicKeyPath);
            api.AddEncryptedTemporaryKey(encryptedTempKeyPath);

            //check
            boolean wasSuccessful = api.Check(encryptedJson, iv, digitalSignature);

            System.out.println();

            if(wasSuccessful)
                System.out.println("Verification was successful!");
            else
                System.out.println("Verification was not successful!");

        } catch (Exception e) {
            throw e;
        }
    }

}
