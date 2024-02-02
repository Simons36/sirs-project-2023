package GrooveClient.exceptions;

public class UnableToPlayAudioException extends Exception {

    public UnableToPlayAudioException() {
        super("There was a problem reproducing the audio.");
    }
    
}
