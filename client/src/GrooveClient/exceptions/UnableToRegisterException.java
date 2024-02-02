package GrooveClient.exceptions;

public class UnableToRegisterException extends Exception {

    public UnableToRegisterException(String id, String serverResponse) {
        super("Registration failed for Username: " + id + " due to: " + serverResponse);
    }
    
}
