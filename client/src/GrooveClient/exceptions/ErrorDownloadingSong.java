package GrooveClient.exceptions;

public class ErrorDownloadingSong extends Exception {
    
    public ErrorDownloadingSong() {
        super("Error while decrypting and checking the received song secure document.");
    }

}
