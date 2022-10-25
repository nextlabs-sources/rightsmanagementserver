package com.nextlabs.rms.repository.sharepointrest;

import com.google.gson.annotations.SerializedName;

public class SPRestError {
	@SerializedName("message")
	private String message;

	@SerializedName("code")
	private String code;	

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
