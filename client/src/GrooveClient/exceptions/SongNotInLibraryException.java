package GrooveClient.exceptions;

public class SongNotInLibraryException extends Exception {

    public SongNotInLibraryException(String songName) {
        super("The song " + songName+ " is not in your library");
    }
    
}
