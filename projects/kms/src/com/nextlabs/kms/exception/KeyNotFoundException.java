package com.nextlabs.kms.exception;

public class KeyNotFoundException extends KeyManagementException {
	private static final long serialVersionUID = -5445505753229597968L;
	private String keyId;

	protected KeyNotFoundException(String message, String keyId) {
		super(message);
		this.keyId = keyId;
	}

	public KeyNotFoundException(String keyId) {
		this("Key is not found :" + keyId, keyId);
	}

	public String getKeyId() {
		return keyId;
	}
}
