package com.nextlabs.rms.repository.onedrive;

import com.google.gson.annotations.SerializedName;

public class OneDriveParentReference {
	@SerializedName("path")
	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
