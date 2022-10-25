package com.nextlabs.rms.repository;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.exception.RepositoryException;

public interface IRepository {

	public final static long INVALID_REPOSITORY_ID = -1;
	
	public RMSUserPrincipal getUser();

	public void setUser(RMSUserPrincipal userPrincipal);

	public String getAccountName();
	
	public void setAccountName(String accountName);
	
	public String getAccountId();
	
	public void setAccountId(String accountId);
	
	public long getRepoId();

	public void setRepoId(long repoId);
	
	public ServiceProviderType getRepoType();
	
	public boolean isShared();
	
	public void setShared(boolean shared);
	
	public String getRepoName();
	
	public void setRepoName(String repoName);

	public Map<String,Object> getAttributes();
	
	public List<RepositoryContent> getFileList(String path) throws RepositoryException, RMSException;

	public void refreshFileList(HttpServletRequest request, HttpServletResponse response, String path)
			throws RepositoryException, RMSException;
	
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException;
	
	public List<SearchResult> search(String searchString) throws RepositoryException, RMSException;
	
	public List<File> downloadFiles(String parentPathId, String[] filenames, String outputPath)
			throws RepositoryException, RMSException, MissingDependenciesException;
}
