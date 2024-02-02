package GrooveServer.exceptions;

public class UserAlreadyBelongsToAFamilyException extends RuntimeException{

    private final String _username;

    private final String _familyName;

    public UserAlreadyBelongsToAFamilyException(String username, String familyName){
        super("User '" + username + "' already belongs to the family '" + familyName + "'.");
        _username = username;
        _familyName = familyName;
    }

    public String getUsername(){
        return _username;
    }

    public String getFamilyName(){
        return _familyName;
    }
    
}
