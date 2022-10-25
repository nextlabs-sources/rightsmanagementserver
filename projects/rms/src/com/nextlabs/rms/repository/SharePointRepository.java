package com.nextlabs.rms.repository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;

public class SharePointRepository implements IRepository {
	
	protected ServiceProviderType repoType = ServiceProviderType.SHAREPOINT_ONPREMISE;
	
	private SharePointClient spClient = null;
	
	private RMSUserPrincipal user = null;
	
	private String repoName;
	
	private Logger logger = Logger.getLogger(SharePointRepository.class);
	
	private long repoId;
	
	private String accountName;
	
	private boolean isShared;
	
	private String accountId;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public boolean isShared() {
		return isShared;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

	private final Map<String,Object> attributes;
	
	public SharePointRepository(RMSUserPrincipal userPrincipal, long repoId, String baseURL,String repoName){
		this.user = userPrincipal;
		this.repoId = repoId;
		this.repoName=repoName;
		
		attributes = new HashMap<>();
		//Account name is same as repoId for sharepoint repository
		this.setAccountName(baseURL);
	}

	private void initialize() {
		spClient = new SharePointClient(user.getUserName(), user.getPassword(), user.getDomain(), repoId, accountName,repoName);
	}

	@Override
	public RMSUserPrincipal getUser() {
		return user;
	}

	@Override
	public void setUser(RMSUserPrincipal user) {
		this.user = user;
	}

	@Override
	public long getRepoId() {
		return repoId;
	}

	@Override
	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

	@Override
	public ServiceProviderType getRepoType() {
		return repoType;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	private void handleException(Exception e) throws RepositoryException, RMSException {
		if (e instanceof AxisFault) {
			AxisFault ex = (AxisFault) e;
			if (ex.getMessage().contains("401 Error: Unauthorized")) {
				throw new UnauthorizedRepositoryException(ex.getMessage(), ex);
			}
			throw new RepositoryException(e.getMessage(), e);
		} else if (e instanceof java.io.FileNotFoundException) {
			throw new FileNotFoundException(e.getMessage(), e);
		} else {
			if (e instanceof RMSException) {
				throw (RMSException) e;
			} else {
				throw new RMSException(e.getMessage(), e);
			}
		}
	}

	@Override
	public List<RepositoryContent> getFileList(String path) throws RMSException, RepositoryException {
		try {
			if(spClient==null){
				initialize();
			}
			return spClient.getFileList(path, repoId, repoName);
		} catch (Exception e) {
			handleException(e);
		}
		return null;
	}

	@Override
	public void refreshFileList(HttpServletRequest request,
			HttpServletResponse response, String path) throws RepositoryException, RMSException {
		try {
			if(spClient==null){
				initialize();
			}
			spClient.getFileList(path, repoId, repoName);
		} catch (Exception e) {
			logger.error("Error occurred while refreshing list of files from Sharepoint for the path:"+path,e);
			handleException(e);
		}
	}

	@Override
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException {
        BufferedOutputStream bos;
        File fileFromRepo = null;
		try {
			if(spClient==null){
				initialize();
			}
			if (fileId == null || fileId.length() == 0) {
				throw new IllegalArgumentException("Invalid file path");
			}
			String[] arr = fileId.split("/");
			String fileName = arr[arr.length-1];
			fileFromRepo = new File(outputPath, fileName);
			bos = new BufferedOutputStream(new FileOutputStream(fileFromRepo));
			spClient.downloadFile(fileId,bos);
		} catch (Exception e) {
			handleException(e);
		} 
		return fileFromRepo;
	}

	@Override
	public String getRepoName() {
		return repoName;
	}

	@Override
	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	@Override
	public List<SearchResult> search(String searchString) throws RepositoryException, RMSException {
		try {
			if(spClient==null){
				initialize();
			}
			return spClient.searchsp(searchString, repoName);
		} catch (Exception e) {
			handleException(e);
		}
		return null;
	}

	@Override
	public List<File> downloadFiles(String parentPathId, String[] filenames, String outputPath)
			throws RepositoryException, RMSException, MissingDependenciesException {
		List<File> downloadedFiles = new ArrayList<>();
		List<String> missingFiles = new ArrayList<>();
		try {
			if(spClient==null){
				initialize();
			}
			parentPathId = parentPathId.endsWith("/") ? parentPathId.substring(0, parentPathId.length() - 1) : parentPathId;
			for (String filename : filenames) {
				try {
					File file = getFile(parentPathId + "/" + filename, outputPath);
					if (file == null) {
						throw new FileNotFoundException("Unable to download file '" + filename + "'");
					}
					downloadedFiles.add(file);
				} catch (FileNotFoundException e) {
					missingFiles.add(filename);
				}
			}
		} catch (Exception e) {
			handleException(e);
		}
		if (!missingFiles.isEmpty()) {
			throw new MissingDependenciesException(missingFiles);
		}
		return downloadedFiles;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
