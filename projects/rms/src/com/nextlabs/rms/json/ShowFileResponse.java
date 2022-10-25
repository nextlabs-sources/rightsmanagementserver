package com.nextlabs.rms.json;

public class ShowFileResponse {
	
	private String viewerUrl;
	
	public ShowFileResponse(){
		setViewerUrl("");
	}

	public String getViewerUrl() {
		return viewerUrl;
	}

	public void setViewerUrl(String viewerUrl) {
		this.viewerUrl = viewerUrl;
	}

}
