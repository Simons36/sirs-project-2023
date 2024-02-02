package GrooveServer.exceptions;

public class MusicFormatNotFoundException extends RuntimeException{

    private String _musicTitle;
    private String _musicArtist;
    private String _musicFormat;

    public MusicFormatNotFoundException(String musicTitle, String musicArtist, String musicFormat) {
        super("Music " + musicTitle + " by " + musicArtist + " not available in format '" + musicFormat + "'.");
        
        _musicTitle = musicTitle;
        _musicArtist = musicArtist;
        _musicFormat = musicFormat;
    }

    //getters
    public String getMusicTitle(){
        return _musicTitle;
    }

    public String getMusicArtist(){
        return _musicArtist;
    }

    public String getMusicFormat(){
        return _musicFormat;
    }
    
}
