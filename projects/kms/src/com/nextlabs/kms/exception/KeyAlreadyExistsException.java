package com.nextlabs.kms.exception;

public class KeyAlreadyExistsException extends KeyManagementException {
	private static final long serialVersionUID = 2537331744748020922L;
	private String keyId;

	public KeyAlreadyExistsException(String message, String keyId) {
		super(message);
		this.keyId = keyId;
	}

	public KeyAlreadyExistsException(String keyId) {
		this("Key already exists :" + keyId, keyId);
	}

	public String getKeyId() {
		return keyId;
	}
}
