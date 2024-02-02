package GrooveServer.exceptions;

public class UserAlreadyLoggedInException extends RuntimeException{
    
    private final String _username;

    public UserAlreadyLoggedInException(String username){
        super("User with username '" + username + "' is already logged in.");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }

}
