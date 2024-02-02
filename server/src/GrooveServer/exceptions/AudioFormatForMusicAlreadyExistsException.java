package GrooveServer.exceptions;

public class AudioFormatForMusicAlreadyExistsException extends RuntimeException{
    
    private final String _musicTitle;
    private final String _musicArtist;
    private final String _audioFormat;

    public AudioFormatForMusicAlreadyExistsException(String musicTitle, String musicArtist, String audioFormat) {
        super("Audio format " + audioFormat + " for music " + musicTitle + " by " + musicArtist + " already exists");
        
        _musicTitle = musicTitle;
        _musicArtist = musicArtist;
        _audioFormat = audioFormat;
    }

    public String getMusicTitle(){
        return _musicTitle;
    }

    public String getMusicArtist(){
        return _musicArtist;
    }

    public String getAudioFormat(){
        return _audioFormat;
    }
}
