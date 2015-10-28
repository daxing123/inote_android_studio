package org.dayup.decription;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.dayup.common.Communication;

import android.util.Log;

public class Decrypt {
	private final static byte[] iv = new byte[]{
        (byte)0x8E, 0x12, 0x39, (byte)0x9C,
        0x08, 0x72, 0x6F, 0x5A
    };
	
    private static AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
    
	private static final String key = "14evrx9me7cj1kbop5aj6s4j93kpemph07mv54xeiarxd6w2cx6fs7oh4p6poefhd56klbdg7vz6v8abiamadr0p098a6ioxknd00icuukvt35f2nan00kknk8rlg293ke1z2k2p1ag2gj6m9i5he0pkex171twzbzbsf2yz0aw6kgqghv2aec3y9i7dxe6fnbgyvzd,1ekh";
	private static final PublicKey pubKey = readKeyFromFile();
	
	public static String getConent(String filename) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		try {
			StringBuffer buf = new StringBuffer();
			int ch;
			while ((ch = reader.read()) != -1) {
				buf.append((char) ch);
			}
			return buf.toString();
		} finally {
			reader.close();
		}
	}
	
	public static String decrypt(String input) {
		if (pubKey == null) {
			return null;
		}
		
		try {
    		String[] inputs = input.split(" ");
    		
    		Cipher cipher = Cipher.getInstance("RSA");
    		cipher.init(Cipher.DECRYPT_MODE, pubKey);
    		byte[] desKeyInBytes = cipher.doFinal(decodeBASE64(inputs[0].trim()));
    		//Log.d(Communication.TAG, "length: " + decodeBASE64(inputs[0].trim()).length);
    		//Log.d(Communication.TAG, "desKeyInBytes's length: " + desKeyInBytes.length);
    		SecretKey desKey = new SecretKeySpec(desKeyInBytes, "DES");
    		cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    		cipher.init(Cipher.DECRYPT_MODE, desKey, paramSpec);
    		return new String(cipher.doFinal(decodeBASE64(inputs[1])), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Error occurred when decrypting.", e);
		}
	}
	
	
	private static PublicKey readKeyFromFile() {
		try {
    		BigInteger m = new BigInteger(key.substring(0, key.indexOf(',')).trim(), 36);
    		BigInteger e = new BigInteger(key.substring(key.indexOf(',') + 1).trim(), 36);
    		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
    		KeyFactory fact = KeyFactory.getInstance("RSA");
    
    		PublicKey pubKey = fact.generatePublic(keySpec);
		return pubKey;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Decode BASE64 encoded string to bytes array
	 * 
	 * @param text
	 *            The string
	 * @return Bytes array
	 * @throws IOException
	 */
	private static byte[] decodeBASE64(String text) throws IOException {
		return Base64Coder.decodeLines(text);
	}
	
}
