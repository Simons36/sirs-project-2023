package GrooveServer.service.exceptions;

public class AudioTransferConnectionException extends RuntimeException{

    public AudioTransferConnectionException(String message, Throwable cause){
        super(message, cause);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[ERROR]\n")
          .append("Error setting up audio transfer connection:\n")
          .append("Message: ").append(getMessage()).append("\n")
          .append("Cause: ").append(getCause()).append("\n")
          .append("[ERROR]\n");

        return sb.toString();
    }
    
}
