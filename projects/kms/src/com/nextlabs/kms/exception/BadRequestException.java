package com.nextlabs.kms.exception;

public class BadRequestException extends KeyManagementException {
	private static final long serialVersionUID = -2591323992953990392L;

	public BadRequestException(String message) {
		super(message);
	}
}
