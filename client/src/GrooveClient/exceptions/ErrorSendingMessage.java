package GrooveClient.exceptions;

public class ErrorSendingMessage extends Exception {

    public ErrorSendingMessage(String ip, int port, String trans_protocol) {
        super("Error transmitting message using " + trans_protocol + " to ip: " + ip + " and port: " + port);
    }
    
}
