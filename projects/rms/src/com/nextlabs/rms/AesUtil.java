package com.nextlabs.rms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

public class AesUtil {
	
	private static final String IV = "F27D5C9927726BCEFE7510B1BDD3D137";
    private static final String SALT = "3FF2EC019C627B945225DEBAD71A01B6985FE84C95A70EB132882F88C0A59A55";
    private static final int KEY_SIZE = 128;
    private static final int ITERATION_COUNT = 10000;
    private static Logger logger = Logger.getLogger(AesUtil.class);
    private final int keySize;
    private final int iterationCount;
    private final Cipher cipher;
    
    public AesUtil() {
        this.keySize = KEY_SIZE;
        this.iterationCount = ITERATION_COUNT;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e) {
        	logger.error(e);
            throw fail(e);
        }
    }

    public String getEncryptedText(String inputText, String passPhrase) {    
		try {			
			SecretKey key = generateKey(SALT, passPhrase);
	        byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, IV, inputText.getBytes("UTF-8"));
	        return base64(encrypted);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			throw fail(e);
		}
    }
    
    public String getDecryptedText(String inputText, String passPhrase) {       	
    	try {
            SecretKey key = generateKey(SALT, passPhrase);
            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, IV, base64(inputText));
            return new String(decrypted, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            logger.error(e);
        	throw fail(e);
        }
    }
    
    public void encryptFile(String passPhrase, File plainFile, File encFile){
    	SecretKey key = generateKey(SALT, passPhrase);
    	try {
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(hex(IV)));
			BufferedInputStream buffInStream = new BufferedInputStream(new FileInputStream(plainFile));
			CipherInputStream cipherIn = new CipherInputStream(buffInStream, cipher);
			BufferedOutputStream buffOutStream = new BufferedOutputStream(new FileOutputStream(encFile));
			int i;
			while((i=cipherIn.read())!=-1){
				buffOutStream.write(i);
			}
			buffOutStream.close();
			buffInStream.close();
			cipherIn.close();
		} catch (Exception e) {	
			logger.error(e);
			throw fail(e);
		} 
    }
    
    public void decryptFile(String passPhrase, File encFile, File plainFile){
    	SecretKey key = generateKey(SALT, passPhrase);
    	try {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(hex(IV)));
			BufferedInputStream buffInStream = new BufferedInputStream(new FileInputStream(encFile));
			CipherInputStream cipherIn = new CipherInputStream(buffInStream, cipher);
			BufferedOutputStream buffOutStream = new BufferedOutputStream(new FileOutputStream(plainFile));
			int i;
			while((i=cipherIn.read())!=-1){
				buffOutStream.write(i);
			}
			buffOutStream.close();
			buffInStream.close();
			cipherIn.close();
		} catch (Exception e) {	
			logger.error(e);
			throw fail(e);
		} 
    }
    
//    public byte[] encrypt(String passPhrase, byte[] inputByteArr){
//    	SecretKey key = generateKey(SALT, passPhrase);
//        byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, IV, inputByteArr);
//        return encrypted;
//    }
//    
//    public byte[] decrypt(String passPhrase, byte[] inputByteArr){
//    	SecretKey key = generateKey(SALT, passPhrase);
//        byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, IV, inputByteArr);
//        return decrypted;
//    }
    
    private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
        try {
            cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
            return cipher.doFinal(bytes);
        }
        catch (Exception e) {
        	logger.error(e);
            throw fail(e);
        }
    }
    
    private SecretKey generateKey(String salt, String passphrase) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch (Exception e) {
        	logger.error(e);
            throw fail(e);
        }
    }
    
    public static String random(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return hex(salt);
    }
    
    public static String base64(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }
    
    public static byte[] base64(String str) {
        return DatatypeConverter.parseBase64Binary(str);
    }
    
    public static String hex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
    
    public static byte[] hex(String str) {
        return DatatypeConverter.parseHexBinary(str);
    }
    
    private IllegalStateException fail(Exception e) {
        return new IllegalStateException(e);
    }
}