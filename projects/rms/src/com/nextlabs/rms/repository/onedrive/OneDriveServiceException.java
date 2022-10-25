package com.nextlabs.rms.repository.onedrive;

public class OneDriveServiceException extends RuntimeException {
	private static final long serialVersionUID = 1039996722239826337L;
	private final OneDriveErrorResponse error;
	private int statusCode;

	public OneDriveServiceException(int statusCode, String msg, OneDriveErrorResponse error) {
		super(msg);
		this.error = error;
		this.statusCode = statusCode;
	}

	public OneDriveErrorResponse getError() {
		return error;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int code) {
		this.statusCode = code;
	}
}
