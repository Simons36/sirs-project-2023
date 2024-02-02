package GrooveServer.exceptions;

public class UserDoesNotBelongToAFamilyException extends RuntimeException {
    
    private final String _username;

    public UserDoesNotBelongToAFamilyException(String username){
        super("User " + username + " does not belong to a family");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }
}
