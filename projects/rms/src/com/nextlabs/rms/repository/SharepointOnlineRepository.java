package com.nextlabs.rms.repository;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.sharepointrest.SPRestServiceException;
import com.nextlabs.rms.repository.sharepointrest.SharePointOnlineRepoAuthHelper;
import com.nextlabs.rms.repository.sharepointrest.SharepointRestClient;

public class SharepointOnlineRepository implements IRepository{

	private static final Logger logger = Logger.getLogger(SharepointOnlineRepository.class);
	
	private String accountName, repoName;
	
	private ServiceProviderType repoType;
	
	private long repoId;
	
	private RMSUserPrincipal user;
	
	private final Map<String,Object> attributes;
	
	private SharepointRestClient spClient = null;
	
	private boolean isShared = false;
	
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

	public SharepointOnlineRepository(RMSUserPrincipal userPrincipal, long repoId) {
		this.user = userPrincipal;		
		this.repoId = repoId;
		this.repoType = ServiceProviderType.SHAREPOINT_ONLINE;
		attributes = new HashMap<>();
		initialize();
	}
	
	private void initialize(){
		spClient = new SharepointRestClient();
	}
	
	@Override
	public RMSUserPrincipal getUser() {
		return user;
	}

	@Override
	public void setUser(RMSUserPrincipal userPrincipal) {
		this.user = userPrincipal;
	}

	@Override
	public String getAccountName() {
		return accountName;
	}

	@Override
	public void setAccountName(String accountName) {
		this.accountName = accountName;
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

	@Override
	public String getRepoName() {
		return repoName;
	}

	@Override
	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	@Override
	public List<RepositoryContent> getFileList(String path) throws RepositoryException, RMSException {
		try {
			String accessToken = getAccessToken();
			if(accessToken==null){
				SPRestServiceException ex = new SPRestServiceException(401, "Access Token is null", null);
				throw ex;
			}
			String spServer = URLDecoder.decode(getAccountName(),"UTF-8");
			return spClient.getFileList(path, accessToken, spServer, repoId, repoName);
		} catch(Exception e){
			SharePointOnlineRepoAuthHelper.handleException(e);
		}
		return null;		
	}

	@Override
	public void refreshFileList(HttpServletRequest request, HttpServletResponse response, String path)
		throws RepositoryException, RMSException {
		try{
			getFileList(path);
		} catch (Exception e){
			SharePointOnlineRepoAuthHelper.handleException(e);
		}
	}

	@Override
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException {
		if (fileId == null || fileId.length() == 0) {
			throw new IllegalArgumentException("Invalid file path");
		}
		try{
			String accessToken = getAccessToken();
			String spServer = URLDecoder.decode(getAccountName(),"UTF-8");
			byte[] content = spClient.downloadFile(fileId, accessToken, spServer);
			String fileName = fileId.substring(fileId.lastIndexOf("/")+1,fileId.length());
			File file = new File(outputPath + File.separator + fileName);
			FileUtils.writeByteArrayToFile(file, content);
			return file;
		} catch(Exception e){
			logger.error("Error occured while getting the contents of a file from SP online: ",e);
			SharePointOnlineRepoAuthHelper.handleException(e);	
		}
		return null;	
	}

	@Override
	public List<SearchResult> search(String searchString) throws RepositoryException, RMSException {
		int searchlimit=GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.SHAREPOINTONLINE_SEARCHLIMITCOUNT);
		if(searchlimit<=0){
			searchlimit = 5000;
		}
		try{
			String spServer = URLDecoder.decode(getAccountName(),"UTF-8");
			String accessToken = getAccessToken();
			return spClient.searchsp(searchString, accessToken, searchlimit, repoName, repoId, repoType.name(), spServer);
		} catch(Exception e){
			SharePointOnlineRepoAuthHelper.handleException(e);	
		}
		return Collections.emptyList();
	}
	
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public String getAccessToken() throws UnsupportedEncodingException, ClientProtocolException, IOException{
		//Get Sharepoint details
		String oAuthToken = (String) attributes.get(RepositoryManager.ACCESS_TOKEN);
		long expiryTime = 0L;
		if(oAuthToken != null && attributes.get(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME) != null){
			expiryTime = Long.parseLong((String) attributes.get(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME));
			if((System.currentTimeMillis()/1000)>=expiryTime){
				//Get new access token
				oAuthToken = getSPOnlineOAuthToken();
			}
		}else{
			//Get new access token from refresh token
			oAuthToken = getSPOnlineOAuthToken();
		}
		
		return oAuthToken;
	}

	private String getSPOnlineOAuthToken() throws UnsupportedEncodingException, ClientProtocolException, IOException {
		String secret = ConfigManager.getInstance(user.getTenantId()).getServiceProviderMap()
				.get(ServiceProviderType.SHAREPOINT_ONLINE.name()).getAttributes().get(ServiceProviderSetting.APP_SECRET);
		String contextId = ConfigManager.getInstance(user.getTenantId()).getStringProperty(ConfigManager.SP_ONLINE_APP_CONTEXT_ID);
		Map resultMap = SharePointOnlineRepoAuthHelper.getAccessTokenFromRefreshToken(secret, contextId, (String)getAttributes().get(RepositoryManager.REFRESH_TOKEN), accountName);
		String accessToken = (String) resultMap.get(RepositoryManager.ACCESS_TOKEN);
		String expiryTime = (String) resultMap.get("expires_on");
		attributes.put(RepositoryManager.ACCESS_TOKEN, accessToken);
		attributes.put(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME, expiryTime);
		return accessToken;
	}

	@Override
	public List<File> downloadFiles(String parentPathId, String[] filenames, String outputPath)
			throws RepositoryException, RMSException, MissingDependenciesException {
		List<File> downloadedFiles = new ArrayList<>();
		List<String> missingFiles = new ArrayList<>();
		try {
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
			SharePointOnlineRepoAuthHelper.handleException(e);
		}
		if (!missingFiles.isEmpty()) {
			throw new MissingDependenciesException(missingFiles);
		}
		return downloadedFiles;
	}
}
