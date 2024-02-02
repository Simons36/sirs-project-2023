package GrooveServer.exceptions;

public class SecretKeyForUserNotFoundException extends RuntimeException{
    
    private final String _username;

    public SecretKeyForUserNotFoundException(String username){
        super("Secret key for user '" + username + "' not found.");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }
}
