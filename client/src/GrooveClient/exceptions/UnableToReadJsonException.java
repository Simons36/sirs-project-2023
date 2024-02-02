package GrooveClient.exceptions;

public class UnableToReadJsonException extends Exception {

    public UnableToReadJsonException(String jsonFilePath) {
        super("Error reading the following json document: " + jsonFilePath);
    }
    
}
