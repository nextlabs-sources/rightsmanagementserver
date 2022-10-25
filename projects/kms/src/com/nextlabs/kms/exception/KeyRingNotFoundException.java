package com.nextlabs.kms.exception;

public class KeyRingNotFoundException extends KeyManagementException {
	private static final long serialVersionUID = -1356984373448287822L;

	private String keyRingName;

	protected KeyRingNotFoundException(String message, String keyRingName) {
		super(message);
		this.keyRingName = keyRingName;
	}

	public KeyRingNotFoundException(String keyRingName) {
		this("Key ring is not found :" + keyRingName, keyRingName);
	}

	public String getKeyRingName() {
		return keyRingName;
	}
}
