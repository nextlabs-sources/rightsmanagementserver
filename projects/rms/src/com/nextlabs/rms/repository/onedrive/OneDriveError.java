package com.nextlabs.rms.repository.onedrive;

import com.google.gson.annotations.SerializedName;

public class OneDriveError {
	@SerializedName("message")
	private String message;

	@SerializedName("code")
	private String code;

	@SerializedName("innererror")
	private OneDriveInnerError innererror;

	public String getCode() {
		return code;
	}

	public OneDriveInnerError getInnererror() {
		return innererror;
	}

	public String getMessage() {
		return message;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setInnererror(OneDriveInnerError innererror) {
		this.innererror = innererror;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
