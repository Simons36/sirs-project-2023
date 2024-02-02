package GrooveClient.exceptions;

public class AuthenticityCheckFailed extends Exception {
    
    public AuthenticityCheckFailed() {
        super("The audio received did not pass the authentication tests.");
    }

}
