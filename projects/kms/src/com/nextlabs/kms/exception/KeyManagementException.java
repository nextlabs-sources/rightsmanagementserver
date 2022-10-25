package com.nextlabs.kms.exception;

public class KeyManagementException extends Exception {
	private static final long serialVersionUID = 1739978943491427366L;

	public KeyManagementException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeyManagementException(String message) {
		super(message);
	}

	public KeyManagementException(Throwable cause) {
		super(cause);
	}
}