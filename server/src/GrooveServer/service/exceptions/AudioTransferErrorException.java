package GrooveServer.service.exceptions;

public class AudioTransferErrorException extends RuntimeException{

    private static String _musicThatFailedPath; 

    public AudioTransferErrorException(String message , String musicThatFailedPath){
        super(message);
        _musicThatFailedPath = musicThatFailedPath;
    }
    
    @Override
    public String toString(){
        return "Error transfering music located at " + _musicThatFailedPath + ": " + getMessage();
    }
}
