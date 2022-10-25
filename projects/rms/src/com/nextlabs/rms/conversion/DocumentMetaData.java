package com.nextlabs.rms.conversion;

import java.util.List;
import java.util.Map;

public class DocumentMetaData {

	private String displayName;

	private int numPages;

	private boolean isPrintAllowed;

	private String errMsg;

	private WaterMark watermark;

	private boolean isPMIAllowed;
	
	private long repoId;
	
	private String repoType;
	
	private String repoName;
	
	private String filePath;
	
	private String displayPath;
	
	private long lastModifiedDate;
	
	private long fileSize;
	
	private Map<String, String> rightsLocaleMap;
	
	private Map<String, List<String>> tagsMap;
	
	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getNumPages() {
		return numPages;
	}

	public void setNumPages(int numPages) {
		this.numPages = numPages;
	}

	public boolean isPrintAllowed() {
		return isPrintAllowed;
	}

	public void setPrintAllowed(boolean printPermission) {
		this.isPrintAllowed = printPermission;
	}

	public boolean isPMIAllowed() {
		return isPMIAllowed;
	}

	public void setPMIAllowed(boolean isPMIAllowed) {
		this.isPMIAllowed = isPMIAllowed;
	}

	public WaterMark getWatermark() {
		return watermark;
	}

	public void setWatermark(WaterMark watermark) {
		this.watermark = watermark;
	}

	public long getRepoId() {
		return repoId;
	}

	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

	public String getRepoType() {
		return repoType;
	}

	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Map<String, String> getRightsLocaleMap() {
		return rightsLocaleMap;
	}

	public void setRightsLocaleMap(Map<String, String> rightsLocaleMap) {
		this.rightsLocaleMap = rightsLocaleMap;
	}

	public Map<String, List<String>> getTagsMap() {
	return tagsMap;
	}

	public void setTagsMap(Map<String, List<String>> tagsMap) {
	this.tagsMap = tagsMap;
	}

	public long getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getDisplayPath() {
		return displayPath;
	}

	public void setDisplayPath(String displayPath) {
		this.displayPath = displayPath;
	}

}