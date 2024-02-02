package GrooveClient.exceptions;

public class ErrorClosingConnection extends Exception {

    public ErrorClosingConnection(String ip, int port, String trans_protocol) {
        super("Error closing the " + trans_protocol + " socket connection to ip: " + ip + " and port: " + port);
    }
    
}
