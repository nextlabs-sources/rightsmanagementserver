package com.nextlabs.kms.exception;

public class AccessDeniedException extends KeyManagementException {
	private static final long serialVersionUID = -2591323992953990392L;

	public AccessDeniedException(String message) {
		super(message);
	}
}
