package GrooveServer.exceptions;

public class UserAlreadyOwnsMusicException extends RuntimeException {

    private String _username;
    private String _songTitle;
    private String _artist;

    public UserAlreadyOwnsMusicException(String username, String songTitle, String artist) {
        super("User " + username + " already owns song '" + songTitle + "' by '" + artist + "'.");
        _username = username;
        _songTitle = songTitle;
        _artist = artist;
    }

    //getters
    public String getUsername() {
        return _username;
    }

    public String getSongTitle() {
        return _songTitle;
    }

    public String getArtist() {
        return _artist;
    }
}
