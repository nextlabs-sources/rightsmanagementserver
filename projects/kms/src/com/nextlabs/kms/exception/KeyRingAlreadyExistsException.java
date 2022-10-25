package com.nextlabs.kms.exception;

public class KeyRingAlreadyExistsException extends KeyManagementException {
	private static final long serialVersionUID = 2537331744748020922L;
	private String keyRingName;

	protected KeyRingAlreadyExistsException(String message, String keyRingName) {
		super(message);
		this.keyRingName = keyRingName;
	}

	public KeyRingAlreadyExistsException(String keyRingName) {
		this("Key ring already exists: " + keyRingName, keyRingName);
	}

	public String getKeyRingName() {
		return keyRingName;
	}
}
