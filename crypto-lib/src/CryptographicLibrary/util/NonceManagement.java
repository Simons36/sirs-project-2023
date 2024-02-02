package CryptographicLibrary.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;

import CryptographicLibrary.exceptions.NonceAlreadyReceivedException;
import CryptographicLibrary.exceptions.NonceExpiredException;

public class NonceManagement {

    //messages received with timestamp older than this will be discarded
    private int _msToExpire;

    private List<String> receivedRandomBytesInBase64 = new ArrayList<>();

    private List<String> noncesUsedForCTREncryptionInBase64 = new ArrayList<>();

    public NonceManagement(int secondsToExpire){
        _msToExpire = secondsToExpire * 1000;
    }

    public NonceManagement(){
    }

    public JsonObject addNonce(JsonObject jsonObject) throws Exception{

        System.out.println("Adding nonce to json...");

        jsonObject.addProperty("nonce", GenerateNonce());
        return jsonObject;
    }
    
    
    public void verifyNonce(JsonObject rootJson) throws Exception{

        //nonce -> large number, encoded in base 64, composed of two parts: timestamp and random bytes

        //timestamp -> first part of nonce, number that marks timestamp of when the nonce was generated, serves to check FRESHNESS 
        //             (after a certain time of being sent, this message is no longer valid)

        //random bytes -> second part of nonce, random bytes, serves to check for REPLAY ATTACKS 
        //                (new messages cant have this part equal the random bytes of a previous message)

        
        //get the nonce in base64
        String nonceInBase64 = rootJson.get("nonce").getAsString();

        //decode it
        byte[] nonceInBytes = Base64.getDecoder().decode(nonceInBase64);
        
        long timestampNow = new Date().getTime();

        //divide it into two parts:


        //timestamp
        //create a byte array with the same size as the timestamp
        byte[] timestampInBytes = new byte[Long.toString(timestampNow).getBytes().length];

        //copy the timestamp to the byte array
        System.arraycopy(nonceInBytes, 0, timestampInBytes, 0, timestampInBytes.length);

        //get the timestamp
        Long timestamp = Long.parseLong(new String(timestampInBytes));

        System.out.println("Nonce's timestamp: " + timestamp);


        //random bytes
        //create a byte array with the same size as the random bytes
        byte[] randomBytes = new byte[nonceInBytes.length - timestampInBytes.length];

        //copy the random bytes to the byte array
        System.arraycopy(nonceInBytes, timestampInBytes.length, randomBytes, 0, randomBytes.length);

        String randomBytesInBase64 = Base64.getEncoder().encodeToString(randomBytes);

        System.out.println("Nonce's random bytes (base64): " + randomBytesInBase64);


        //---------------- REPEATED NONCE VERIFICATION ----------------
        
        if(receivedRandomBytesInBase64.contains(randomBytesInBase64))
            throw new NonceAlreadyReceivedException(randomBytesInBase64);

        receivedRandomBytesInBase64.add(randomBytesInBase64);

        //---------------- TIMESTAMP VERIFICATION ----------------

        long timeElapsed = timestampNow - timestamp;
        
        if(timeElapsed > _msToExpire){
            throw new NonceExpiredException(timeElapsed - _msToExpire);
        }
        
    }
    
    private static String GenerateNonce(){

        Long generatedTimestamp = new Date().getTime();

        String timestampString = Long.toString(generatedTimestamp);
        
        byte[] timestampByte = timestampString.getBytes();

        byte[] nonceByte = new byte[64];

        //nonce is 64 bytes long; first bytes are timestamp, rest are filled with random bytes
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonceByte);

        //array copy
        System.arraycopy(timestampByte, 0, nonceByte, 0, timestampByte.length);
        
        
        System.out.println("Nonce generated for timestamp: " + timestampString);

        return Base64.getEncoder().encodeToString(nonceByte);
    }

    public byte[] GenerateNonceAndCounterForCTREncryption(int numBitsTotal, int numBitsNonce){

        byte[] nonceByte = new byte[numBitsTotal / 8];

        //nonce is 64 bytes long; first bytes are timestamp, rest are filled with random bytes
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonceByte);

        if(noncesUsedForCTREncryptionInBase64.contains(Base64.getEncoder().encodeToString(nonceByte)))
            return GenerateNonceAndCounterForCTREncryption(numBitsTotal, numBitsNonce);

        noncesUsedForCTREncryptionInBase64.add(Base64.getEncoder().encodeToString(nonceByte));

        byte[] nonceAndCounter = new byte[(numBitsTotal + numBitsNonce) / 8];

        System.arraycopy(nonceByte, 0, nonceAndCounter, 0, nonceByte.length);

        return nonceAndCounter;

    }

}
