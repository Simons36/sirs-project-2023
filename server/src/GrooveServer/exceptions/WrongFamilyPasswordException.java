package GrooveServer.exceptions;

public class WrongFamilyPasswordException extends RuntimeException{
    
    private final String _familyName;

    public WrongFamilyPasswordException(String familyName){
        super("Wrong password for family with name '" + familyName + "'.");
        _familyName = familyName;
    }

    public String getFamilyName(){
        return _familyName;
    }

}
