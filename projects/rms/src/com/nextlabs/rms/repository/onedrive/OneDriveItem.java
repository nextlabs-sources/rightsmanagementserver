package com.nextlabs.rms.repository.onedrive;

import com.google.gson.annotations.SerializedName;

public class OneDriveItem {
	@SerializedName("name")
	private String name;
	@SerializedName("folder")
	private OneDriveFolder folder;
	@SerializedName("file")
	private OneDriveFile file;
	@SerializedName("parentReference")
	private OneDriveParentReference parentRef;
	@SerializedName("@content.downloadUrl")
	private String downloadUrl;
	@SerializedName("size")
	private long size;
	@SerializedName("lastModifiedDateTime")
	private java.util.Date mTime;

	public OneDriveFile getFile() {
		return file;
	}

	public OneDriveFolder getFolder() {
		return folder;
	}

	public String getName() {
		return name;
	}
	
	public OneDriveParentReference getParentRef() {
		return parentRef;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}
	
	public long getSize() {
		return size;
	}
	
	public java.util.Date getMTime() {
		return mTime;
	}
	
	public void setFile(OneDriveFile file) {
		this.file = file;
	}

	public void setFolder(OneDriveFolder folder) {
		this.folder = folder;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setParentRef(OneDriveParentReference parentRef) {
		this.parentRef = parentRef;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setMTime(java.util.Date mTime) {
		this.mTime = mTime;
	}
	
}
