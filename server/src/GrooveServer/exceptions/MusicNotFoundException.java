package GrooveServer.exceptions;

public class MusicNotFoundException extends RuntimeException{
    
    private String _musicTitle;
    private String _musicArtist;

    public MusicNotFoundException(String musicTitle, String musicArtist) {
        super("Music '" + musicTitle + "' by '" + musicArtist + "' not found.");
        
        _musicTitle = musicTitle;
        _musicArtist = musicArtist;
    }
}
