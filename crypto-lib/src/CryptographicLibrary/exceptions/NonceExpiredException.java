package CryptographicLibrary.exceptions;

public class NonceExpiredException extends RuntimeException{
    
    public NonceExpiredException(long msAgo) {
        super("Nonce expired " + msAgo + " ms ago");
    }

}
