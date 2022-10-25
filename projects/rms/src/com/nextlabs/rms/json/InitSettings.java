package com.nextlabs.rms.json;

public class InitSettings {
	
	private String userName;
	
	private String sid;
	
	private boolean isAdmin;
	
	private boolean isPersonalRepoEnabled;
	
	private String rmsVersion;

	private boolean isRMCConfigured;
	
	private String copyrightYear;
	
	public String getRmsVersion() {
		return rmsVersion;
	}

	public void setRmsVersion(String rmsVersion) {
		this.rmsVersion = rmsVersion;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean isPersonalRepoEnabled() {
		return isPersonalRepoEnabled;
	}

	public void setPersonalRepoEnabled(boolean isPersonalRepoEnabled) {
		this.isPersonalRepoEnabled = isPersonalRepoEnabled;
	}

	public boolean isRMCConfigured() {
		return isRMCConfigured;
	}

	public void setRMCConfigured(boolean isRMCConfigured) {
		this.isRMCConfigured = isRMCConfigured;
	}

	public String getCopyrightYear() {
		return copyrightYear;
	}

	public void setCopyrightYear(String copyrightYear) {
		this.copyrightYear = copyrightYear;
	}

}
