package GrooveServer.exceptions;

public class UserNotLoggedInException extends RuntimeException{

    private final String _username;

    public UserNotLoggedInException(String username){
        super("User " + username + " is not logged in");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }
    
}
