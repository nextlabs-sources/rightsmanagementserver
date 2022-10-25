package com.nextlabs.rms.json;

public class Repository {
	
	private long repoId;
	
	private String repoName;
	
	private String sid;
	
	private String repoType;

	private String accountName;
	
	private String repoTypeDisplayName;
	
	private boolean isShared;
	
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public long getRepoId() {
		return repoId;
	}

	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getRepoType() {
		return repoType;
	}

	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}

	public String getRepoTypeDisplayName() {
		return repoTypeDisplayName;
	}

	public void setRepoTypeDisplayName(String repoTypeDisplayName) {
		this.repoTypeDisplayName = repoTypeDisplayName;
	}

	public boolean isShared() {
		return isShared;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

}
