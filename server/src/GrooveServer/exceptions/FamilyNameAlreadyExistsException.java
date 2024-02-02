package GrooveServer.exceptions;

public class FamilyNameAlreadyExistsException extends RuntimeException{

    private final String _familyName;

    public FamilyNameAlreadyExistsException(String familyName){
        super("Family with name '" + familyName + "' already exists.");
        _familyName = familyName;
    }

    public String getFamilyName(){
        return _familyName;
    }
    
}
