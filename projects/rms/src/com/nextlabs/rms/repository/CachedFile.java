package com.nextlabs.rms.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.nextlabs.rms.conversion.WaterMark;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;

public class CachedFile implements Serializable{
	
	private static final long serialVersionUID = -6960898640377591668L;

	private byte[] fileContent;
	
	private String fileName;
	
	private boolean isPrintAllowed;
	
	private WaterMark waterMark;
	
	private boolean isPMIAllowed;
	
	private long repoId;
	
	private String repoType;
	
	private String repoName;
	
	private String filePath;
	
	private String displayPath;
	
	private long lastModifiedDate;
	
	private long fileSize;
	
	private ContentType type;
	
	private Map<String, List<String>> tagMap;
	
	public enum ContentType {
		_2D, _3D
	}
	
	public boolean isPMIAllowed() {
		return isPMIAllowed;
	}

	public void setPMIAllowed(boolean isPMIAllowed) {
		this.isPMIAllowed = isPMIAllowed;
	}
	
	public void setContentType(ContentType type){
		this.type = type;
	}

	private String errorMsg;
	

	public CachedFile(String fileName, byte[] fileContent,boolean printPermission, WaterMark waterMarkObj,String errorMsg,boolean isPMIAllowed){
		this.fileName = fileName;
		this.fileContent = fileContent;
		this.isPrintAllowed=printPermission;
		this.waterMark=waterMarkObj;
		this.errorMsg=errorMsg;
		this.isPMIAllowed=isPMIAllowed;
	}
	
	public CachedFile(String fileName, byte[] fileContent,boolean printPermission, WaterMark waterMarkObj,
			String errorMsg,boolean isPMIAllowed, RepositoryFileParams params, IRepository repo, long fileSize, Map<String, List<String>> tagMap){
			this(fileName, fileContent, printPermission, waterMarkObj, 
					errorMsg, isPMIAllowed);
			this.repoId = params.getRepoId();
			this.filePath = params.getFilePath();
			this.displayPath = params.getDisplayPath();
			this.lastModifiedDate = params.getLastModifiedDate();
			this.fileSize = fileSize;
			if(repo!=null) {
				this.repoName = repo.getRepoName();
				this.repoType = repo.getRepoType().name();
			}
			this.tagMap=tagMap;
	}
	

	public byte[] getFileContent() {
		return fileContent;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isPrintAllowed() {
		return isPrintAllowed;
	}

	public void setPrintAllowed(boolean printPermission) {
		this.isPrintAllowed = printPermission;
	}

	public WaterMark getWaterMark() {
		return waterMark;
	}

	public void setWaterMark(WaterMark waterMark) {
		this.waterMark = waterMark;
	}
	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public long getRepoId() {
		return repoId;
	}

	public String getRepoType() {
		return repoType;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public long getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	public long getFileSize(){
		return fileSize;
	}
	
	public ContentType getContentType() {
		return type;
	}

	public Map<String, List<String>> getTagMap() {
		return tagMap;
	}

	public void setTagMap(Map<String, List<String>> tagMap) {
		this.tagMap = tagMap;
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
	
}
