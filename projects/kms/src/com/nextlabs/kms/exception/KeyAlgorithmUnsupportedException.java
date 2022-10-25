package com.nextlabs.kms.exception;

public class KeyAlgorithmUnsupportedException extends KeyManagementException {
	private static final long serialVersionUID = -5384856176151728026L;
	private String keyAlgorithm;
	private int keyLength;

	protected KeyAlgorithmUnsupportedException(String message, String keyAlgorithm, int keyLength) {
		super(message);
		this.keyAlgorithm = keyAlgorithm;
		this.keyLength = keyLength;
	}

	public KeyAlgorithmUnsupportedException(String keyAlgorithm, int keyLength) {
		this("Key algorithm not supported for " + keyAlgorithm + " " + keyLength , keyAlgorithm, keyLength);
	}
	
	public int getKeyLength() {
		return keyLength;
	}
	
	public String getKeyAlgorithm(){
		return keyAlgorithm;
	}
}
