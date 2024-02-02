package CryptographicLibrary.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class DigestOperations{

    final static String SIGNATURE_ALGO = "SHA256withRSA";

    public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {

		// get a signature object and sign the plain text with the private key
		Signature sig = Signature.getInstance(SIGNATURE_ALGO);
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] signature = sig.sign();

		return signature;
	}

	public static boolean verifyDigitalSignature(byte[] bytes, byte[] signature, PublicKey publicKey) throws Exception {

		try {
			// verify the signature with the public key
			System.out.println("Verifying signature...");

			Signature sig = Signature.getInstance(SIGNATURE_ALGO);
			sig.initVerify(publicKey);
			sig.update(bytes);
			boolean verifySuccessful = sig.verify(signature);

			if(verifySuccessful){
				System.out.println("The file has not been tampered with!");
			}else{
				System.out.println("The file has been tampered with!");
			}
	
			return verifySuccessful;

		} catch (Exception e) {
			throw e;
		}
	}

	public static byte[] ConcatenateByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length]; 
		System.arraycopy(a, 0, result, 0, a.length); 
		System.arraycopy(b, 0, result, a.length, b.length); 
		return result;
	}

	
}
