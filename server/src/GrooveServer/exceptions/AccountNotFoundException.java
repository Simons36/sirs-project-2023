package GrooveServer.exceptions;

public class AccountNotFoundException extends RuntimeException{

    private final String _username;

    public AccountNotFoundException(String username){
        super("Account with username '" + username + "' not found.");
        _username = username;
    }

    public String getUsername(){
        return _username;
    }
    
}
