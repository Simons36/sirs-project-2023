package CryptographicLibrary.exceptions;

public class OperationNotSupportedException extends RuntimeException{
    
    //super exception
    public OperationNotSupportedException(String message) {
        super(message);
    }
    
}
