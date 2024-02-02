package GrooveClient.exceptions;

public class UnableToLoginException extends Exception {

    public UnableToLoginException(String id, String serverResponse) {
        super("Login failed for Username: " + id + " due to: " + serverResponse);
    }
    
}
