package GrooveServer.service.exceptions;

public class DatabaseExecStatementException extends RuntimeException{

    private int _errorCode;

    private String _sqlState;

    public DatabaseExecStatementException(String message, Throwable cause, int errorCode, String sqlState){
        super(message, cause);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[ERROR]\n")
          .append("Error executing SQL statement:\n")
          .append("Message: ").append(getMessage()).append("\n")
          .append("Cause: ").append(getCause()).append("\n")
          .append("Error code: ").append(_errorCode).append("\n")
          .append("SQL state: ").append(_sqlState).append("\n")
          .append("[ERROR]\n");

        return sb.toString();
    }

}
