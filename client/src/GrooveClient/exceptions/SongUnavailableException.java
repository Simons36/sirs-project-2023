package GrooveClient.exceptions;

public class SongUnavailableException extends Exception {
    
    public SongUnavailableException(String songName, String fileFormat) {
        super("The song " + songName + " is not available in the format " + fileFormat + ".");
    }

    public SongUnavailableException(String songName) {
        super("The song " + songName + " does not exist in GrooveGalaxy.");
    }

}
