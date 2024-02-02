package CryptographicLibrary.structs;

import javax.crypto.SecretKey;

public class ProtectReturnStruct {

    public byte[] encryptedContent;
    public byte[] iv;
    public byte[] digitalSignature;
    public byte[] tempKeyEncrypted;
    
    public ProtectReturnStruct(byte[] encryptedContent, byte[] iv, byte[] digitalSignature, byte[] tempKeyEncrypted) {
        this.encryptedContent = encryptedContent;
        this.iv = iv;
        this.digitalSignature = digitalSignature;
        this.tempKeyEncrypted = tempKeyEncrypted;
    }
    
    //getters
    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public byte[] getTempKeyEncrypted() {
        return tempKeyEncrypted;
    }
    
}
