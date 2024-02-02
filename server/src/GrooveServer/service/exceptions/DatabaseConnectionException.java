package GrooveServer.service.exceptions;

public class DatabaseConnectionException extends RuntimeException{

    private int _errorCode;

    private String _sqlState;

    public DatabaseConnectionException(String message, Throwable cause, int errorCode, String sqlState){
        super(message, cause);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[ERROR]\n")
          .append("Error connecting to the database:\n")
          .append("Message: ").append(getMessage()).append("\n")
          .append("Cause: ").append(getCause()).append("\n")
          .append("Error code: ").append(_errorCode).append("\n")
          .append("SQL state: ").append(_sqlState).append("\n")
          .append("[ERROR]\n");

        return sb.toString();
    }
    
}
