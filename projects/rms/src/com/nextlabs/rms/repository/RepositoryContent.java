package com.nextlabs.rms.repository;

import java.util.ArrayList;
import java.util.List;

public class RepositoryContent {
	
	private String path;
	
	private String name;
	
	private String pathId;
	
	private boolean isFolder;
	
	private boolean isRepo;
	
	private String owner;
	
	private Long lastModifiedTime;
	
	private Long fileSize;
	
	private long repoId;
	
	private String repoName;
	
	private String repoType;
	
	private String fileType;
	
	private boolean protectedFile;
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Long getLastModifiedTime() {
		return lastModifiedTime;
	}
	public void setLastModifiedTime(Long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	
	private List<RepositoryContent> children;

	public RepositoryContent(){
		isRepo=false;
		children=new ArrayList<RepositoryContent>();
	}
	public boolean isRepo() {
		return isRepo;
	}
	public void setRepo(boolean isRepo) {
		this.isRepo = isRepo;
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

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}
	
	public List<RepositoryContent> getChildren() {
		return children;
	}
	
	public void setChildren(List<RepositoryContent> children) {
		this.children = children;
	}

	public String getPathId() {
		return pathId;
	}

	public void setPathId(String pathId) {
		this.pathId = pathId;
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
	public String getRepoType() {
		return repoType;
	}
	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public boolean isProtectedFile() {
		return protectedFile;
	}
	public void setProtectedFile(boolean isProtectedFile) {
		this.protectedFile = isProtectedFile;
	}	
}
