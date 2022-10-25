package com.nextlabs.rms.json;

public class UploadFileResponse {
	private String name;
	private String error;
	private String viewerUrl;
	public UploadFileResponse(){
		name="";
		error="";
		viewerUrl="";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getViewerUrl() {
		return viewerUrl;
	}
	public void setViewerUrl(String viewerUrl) {
		this.viewerUrl = viewerUrl;
	}
}
