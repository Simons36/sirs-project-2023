package GrooveClient.exceptions;

public class UnableToConnectException extends Exception {

    public UnableToConnectException(String ip, int port, String trans_protocol) {
        super("Unable to create " + trans_protocol + " connection for ip: " + ip + " and port: " + port);
    }
    
}
