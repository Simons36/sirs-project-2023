package CryptographicLibrary.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.Date;

import com.google.gson.*;

public class JsonOperations {
    
    public static byte[] getBytesFromJson(JsonObject jsonObject) throws Exception {
        return jsonObject.toString().getBytes("UTF-8");
    }

    public static JsonObject getJsonFromBytes(byte[] bytes) throws Exception {
        return new Gson().fromJson(new String(bytes), JsonObject.class);
    }
    public static String generateNonce() throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException{
        String dateTimeString = Long.toString(new Date().getTime());
		System.out.println("Nonce generated for timestamp: " + dateTimeString);
        byte[] nonceByte = dateTimeString.getBytes();
        return Base64.getEncoder().encodeToString(nonceByte);
    }
}
