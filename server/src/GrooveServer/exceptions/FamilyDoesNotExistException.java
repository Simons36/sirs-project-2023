package GrooveServer.exceptions;

public class FamilyDoesNotExistException extends RuntimeException{

    private final String _familyName;

    public FamilyDoesNotExistException(String familyName){
        super("Family with name '" + familyName + "' does not exist.");
        _familyName = familyName;
    }

    public String getFamilyName(){
        return _familyName;
    }
    
}
