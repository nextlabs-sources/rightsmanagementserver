package com.nextlabs.kms.entity.enums;

public enum ErrorCode {
	BAD_REQUEST(400, "error.badrequest"),
	ACCESS_DENIED(403, "error.access.denied"),
	UNKNOWN(500, "error.unspecified"),
	KEY_RING_NOT_FOUND(560, "error.keyring.notfound"),
	KEY_RING_ALREADY_EXISTS(561, "error.keyring.exists"),
	KEY_NOT_FOUND(562, "error.key.notfound"),
	TENANT_NOT_FOUND(563, "error.tenant.notfound"),
	TENANT_ALREADY_EXISTS(564, "error.tenant.exists"),
	KEY_ALGORITHM_NOT_SUPPORTED(565, "error.keyalgorithm.notsupported"),
	KEY_RING_DISABLED(566, "error.keyring.disabled"),
	KEY_ALREADY_EXISTS(568, "error.key.exists");
	
	private String code;
	private int statusCode;

	private ErrorCode(int statusCode, String code) {
		this.code = code;
		this.statusCode = statusCode;
	}

	public String getCode() {
		return code;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
