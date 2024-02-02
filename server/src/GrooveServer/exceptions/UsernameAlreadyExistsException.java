package GrooveServer.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException{

    private final String _username;

    public UsernameAlreadyExistsException(String username){
        super("Account with username '" + username + "' already exists.");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }
    
}
