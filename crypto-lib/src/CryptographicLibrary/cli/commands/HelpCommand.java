package CryptographicLibrary.cli.commands;

public class HelpCommand {
    
    public static void main(){
        System.out.println("Usage: crypto-lib <command> [<args>]");
        System.out.println("Commands:");
        System.out.println("    protect <path-to-json> <path-to-secret-key> <path-to-private-key> <path-to-write-results> <filename>");
        System.out.println("    check <path-to-encrypted-json> <path-to-iv> <path-to-encrypted-temp-key> <path-to-digital-signature> <path-to-public-key>");
        System.out.println("    crypto-lib unprotect <path-to-encrypted-json> <path-to-iv> <path-to-permanent-secret-key> <path-to-temporary-secret-key> <seconds-to-expire> <path-to-write-results> <filename>");
        System.out.println("    generate-key <path-to-store-key> <name-of-the-user>");
        System.out.println();
        System.out.println("Explanation:");
        System.out.println("    protect: Protects a json file, using the given secret key to encrypt with AES and CBC, and the private key to");
        System.out.println("             sign the encrypted and create a digital signature. The results are written to the given path.");
        System.out.println();
        System.out.println("    check:   Checks if the given encrypted json file is valid, using the given iv and digital signature, and the");
        System.out.println("             given public key to verify the digital signature. Returns true if the file is valid, false otherwise.");
        System.out.println();
        System.out.println("    unprotect: Unprotects the given encrypted json file, using the given secret key to decrypt with AES and CBC.");
        System.out.println("               Also checks for freshness and replay attacks (latter not available using cli). The decrypted file is");
        System.out.println("               written to the given path.");
        System.out.println();
        System.out.println("    generate-key: Generates a new temporary secret key, and writes it to the given path.");

    }

}
