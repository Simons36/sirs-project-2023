package GrooveClient.exceptions;

public class ErrorReceivingMessage extends Exception {

    public ErrorReceivingMessage(String ip, int port, String trans_protocol) {
        super("Error receivong message using " + trans_protocol + " from ip: " + ip + " and port: " + port);
    }
    
}
