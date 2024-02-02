package GrooveClient.exceptions;

public class UnableToCreateSocketException extends Exception {

    public UnableToCreateSocketException(String ip, int port, String trans_protocol) {
        super("Unable to create " + trans_protocol + " socket for ip: " + ip + " and port: " + port);
    }
    
}
