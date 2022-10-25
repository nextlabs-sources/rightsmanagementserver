package com.nextlabs.rms.repository.sharepointrest;

import com.google.gson.annotations.SerializedName;

public class SPRestErrorResponse {
	@SerializedName("error")
	private SPRestError error;

	public SPRestError getError() {
		return error;
	}

	public void setError(SPRestError error) {
		this.error = error;
	}
}
