package GrooveServer.exceptions;

public class UserDoesntOwnMusicException extends RuntimeException{
    
    private String _username;
    private String _musicTitle;
    private String _musicArtist;

    public UserDoesntOwnMusicException(String username, String musicTitle, String musicArtist){
        super("User '" + username + "' doesn't own music '" + musicTitle + "' by '" + musicArtist + "'.");
        _username = username;
        _musicTitle = musicTitle;
        _musicArtist = musicArtist;
    }

    //getters
    public String getUsername(){
        return _username;
    }

    public String getMusicTitle(){
        return _musicTitle;
    }

    public String getMusicArtist(){
        return _musicArtist;
    }
}
