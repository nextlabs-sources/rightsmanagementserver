package com.nextlabs.kms.exception;

public class KeyRingDisabledException extends KeyManagementException {
	private static final long serialVersionUID = 2537331744748020922L;
	private String keyRingName;

	protected KeyRingDisabledException(String message, String keyRingName) {
		super(message);
		this.keyRingName = keyRingName;
	}

	public KeyRingDisabledException(String keyRingName) {
		this("Key ring is disabled: " + keyRingName, keyRingName);
	}

	public String getKeyRingName() {
		return keyRingName;
	}
}
