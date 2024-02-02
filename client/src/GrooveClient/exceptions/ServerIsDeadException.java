package GrooveClient.exceptions;

public class ServerIsDeadException extends Exception {

    public ServerIsDeadException() {
        super("The server is dead! Exitting...");
    }
    
}
