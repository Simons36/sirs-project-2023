package GrooveServer.exceptions;

public class WrongPasswordException extends RuntimeException{
    
    private final String _username;

    public WrongPasswordException(String username){
        super("Wrong password for account with username '" + username + "'.");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }
    
}
