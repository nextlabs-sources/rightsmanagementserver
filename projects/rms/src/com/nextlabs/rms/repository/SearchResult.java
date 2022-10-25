package com.nextlabs.rms.repository;

public class SearchResult {
	
	private String path;
	
	private String pathId;
	
	private String name;

	private long repoId;
	
	private String repoType;
	
	private boolean isFolder;
	
	private String repoName;
	
	private String fileType;
	
	private boolean protectedFile;

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public String getRepoType() {
		return repoType;
	}

	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}

	public long getRepoId() {
		return repoId;
	}

	public String getFileType() {
		return fileType;
	}

	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPathId() {
		return pathId;
	}

	public void setPathId(String pathId) {
		this.pathId = pathId;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	
	public void setProtectedFile(boolean protectedFile) {
		this.protectedFile = protectedFile;
	}

}