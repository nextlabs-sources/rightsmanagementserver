package com.nextlabs.rms.repository.onedrive;

import com.google.gson.annotations.SerializedName;

public class OneDriveErrorResponse {
	@SerializedName("error")
	private OneDriveError error;

	public OneDriveError getError() {
		return error;
	}

	public void setError(OneDriveError error) {
		this.error = error;
	}
}
