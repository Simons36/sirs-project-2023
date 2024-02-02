package GrooveClient.exceptions;

public class UnableToRetriveSymmetricKey  extends Exception {

    public UnableToRetriveSymmetricKey(String keyAlias) {
        super("Unable to retrive the symmetric key with the following alias: " + keyAlias);
    }
    
}
