package CryptographicLibrary.exceptions;

import java.util.Base64;

public class NonceAlreadyReceivedException extends RuntimeException{

    public NonceAlreadyReceivedException(String nonce) {
        super("Already received message with random bytes (in base 64): " + nonce);
    }
}
