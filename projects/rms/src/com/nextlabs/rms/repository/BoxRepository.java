package com.nextlabs.rms.repository;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIConnectionListener;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxItem.Info;
import com.box.sdk.BoxSearch;
import com.box.sdk.BoxSearchParameters;
import com.box.sdk.PartialCollection;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.exception.RepositoryAuthorizationNotFound;
import com.nextlabs.rms.pojo.ServiceProviderSetting;

import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryContent;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.box.BoxAuthHelper;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.NonUniqueFileException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.util.Nvl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;



public class BoxRepository implements IRepository {

	private long repoId;
	private RMSUserPrincipal userPrincipal;
	private final Map<String, Object> attributes;
	private String accountName;
	private String accountId;
	private String repoName;

	private static Logger logger = Logger.getLogger(BoxRepository.class);

	public BoxRepository(RMSUserPrincipal user, long repoId) {
		this.repoId = repoId;
		this.userPrincipal = user;
		setRepoId(repoId);
		attributes = new HashMap<>();
	}

	@Override
	public RMSUserPrincipal getUser() {
		return userPrincipal;
	}

	@Override
	public void setUser(RMSUserPrincipal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	@Override
	public String getAccountName() {
		return this.accountName;
	}

	@Override
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@Override
	public String getAccountId() {
		return this.accountId;
	}

	@Override
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	@Override
	public ServiceProviderType getRepoType() {
		return ServiceProviderType.BOX;
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
	public boolean isShared() {
		return false;
	}

	@Override
	public void setShared(boolean shared) {

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
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	private BoxAPIConnection getAPIConnection() throws InvalidTokenException {
		String refreshToken = (String)attributes.get(RepositoryManager.REFRESH_TOKEN);
		if (refreshToken == null) {
			throw new InvalidTokenException("Invalid token");
		}
		String clientID = getAppKey(userPrincipal.getTenantId()) ;
		String clientSecret = getAppSecret(userPrincipal.getTenantId());
		BoxAPIConnectionListener listener = new BoxAPIConnectionListener() {

			@Override
			public void onRefresh(BoxAPIConnection api) {
				
				String token = api.getRefreshToken();
				String state = api.save();
				String accessToken = api.getAccessToken();
				try {
					RepositoryManager.getInstance().updateRefreshToken(getRepoId(),new TenantUser(getUser().getTenantId(), getUser().getUid()), token);
				} catch (RepositoryAuthorizationNotFound rae) {
					logger.error("Unable to update repository authorization record (Repo ID: " + repoId + " userId: " + 
							getUser().getUid() + "). It might be deleted", rae);
				}		
				attributes.put(RepositoryManager.ACCESS_TOKEN, accessToken);
				attributes.put(RepositoryManager.REFRESH_TOKEN, token);
				attributes.put(RepositoryManager.TOKEN_STATE, state);
			}

			@Override
			public void onError(BoxAPIConnection arg0, BoxAPIException bae) {
				logger.error("Unable to update refresh token record (Repo ID: " + repoId + " userId: " + 
						getUser().getUid() + "). It might be deleted", bae);		
			}
		};
		String state = (String)attributes.get(RepositoryManager.TOKEN_STATE);
		BoxAPIConnection connection = null;
		if(state == null){
			connection = new BoxAPIConnection(clientID, clientSecret, null, refreshToken);
		} else{
			connection = BoxAPIConnection.restore(clientID, clientSecret, state);
		}		
		connection.addListener(listener);
		return connection;
	}

	private static String getAppKey(String tenantId) {
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting boxSetting = latestRepoSettingMap.get(ServiceProviderType.BOX.name());
		if(boxSetting != null){
			return boxSetting.getAttributes().get(ServiceProviderSetting.APP_ID);
		}
		return null;
	}

	private static String getAppSecret(String tenantId) {
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting boxSetting = latestRepoSettingMap.get(ServiceProviderType.BOX.name());
		if(boxSetting != null){
			return boxSetting.getAttributes().get(ServiceProviderSetting.APP_SECRET);
		}
		return null;
	}

	@Override
	public List<RepositoryContent> getFileList(String path) throws RepositoryException, RMSException {
		if (path == null) {
			throw new IllegalArgumentException("Invalid path");
		}
		List<RepositoryContent> result = new ArrayList<RepositoryContent>();
		BoxAPIConnection connection = getAPIConnection();
		BoxFolder api = null;	
		if ("/".equalsIgnoreCase(path)) {
			api = BoxFolder.getRootFolder(connection);
		} else {
			path = getBasePathId(path);
			api = new BoxFolder(connection, path);
		}
		try {
			Iterable<Info> it = api.getChildren(BoxFolder.ALL_FIELDS);
			for (BoxItem.Info itemInfo : it) {
				RepositoryContent content = new RepositoryContent();
				RepoPath repoPath = null;
				repoPath = retrieveParentFromId(itemInfo);
				String rootPath = repoPath.getName() != null ? "/" + repoPath.getName() : "";	
				String rootPathId = repoPath.getId() != null ? "/" + repoPath.getId() : "";
				if (itemInfo instanceof BoxFile.Info) {
					BoxFile.Info fileInfo = (BoxFile.Info)itemInfo;				
					content.setFileSize(fileInfo.getSize());
					content.setFileType(EvaluationHandler.getOriginalFileExtension((fileInfo.getName())));
					content.setProtectedFile(fileInfo.getName().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));				
					content.setFileSize(fileInfo.getSize());			
				}  
				content.setPath(rootPath  +"/"+ itemInfo.getName());					
				content.setPathId(rootPathId + "/" +itemInfo.getID());
				content.setLastModifiedTime(itemInfo.getContentModifiedAt() != null ? itemInfo.getContentModifiedAt().getTime() : null);
				content.setFolder(itemInfo instanceof BoxFolder.Info);
				content.setName(itemInfo.getName());	
				content.setRepoId(getRepoId());
				content.setRepoName(getRepoName());
				content.setRepoType(ServiceProviderType.BOX.name());
				result.add(content);
			}
		} catch (BoxAPIException e) {
			BoxAuthHelper.handleException(e);
		}
		return result;
	}


	private RepoPath retrieveParentFromId(BoxItem.Info parentInfo) {
		List<com.box.sdk.BoxFolder.Info> parents = parentInfo.getPathCollection();
		RepoPath repoPath = new RepoPath();
		java.util.Collections.reverse(parents);
		for(com.box.sdk.BoxFolder.Info parent: parents){
				String id = parent.getID();
				String title = parent.getName();
				// 0 is the id for the root folder
				if(id.equalsIgnoreCase("0") ){
					continue;
				} else{
					repoPath.setId(repoPath.getId() != null ? id + "/" + Nvl.nvl(repoPath.getId()) : id);
					repoPath.setName(repoPath.getName() != null ? title + "/" + Nvl.nvl(repoPath.getName()) : title);
				}			
		}
		return repoPath;
	}
	
	private String getBasePathId(String pathId) {
		if (pathId == null || pathId.length() == 0) {
			throw new IllegalArgumentException("Invalid file path");
		}
		int idx = pathId.lastIndexOf("/");
		pathId = idx >= 0 ? pathId.substring(idx + 1) : pathId;
		return pathId;
	}

	@Override
	public void refreshFileList(HttpServletRequest request, HttpServletResponse response, String path)
			throws RepositoryException, RMSException {
		try {
			getFileList(path);
		} catch (Exception e) {
			BoxAuthHelper.handleException(e);
		}

	}

	@Override
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException {
		BoxAPIConnection api = getAPIConnection();
		fileId = getBasePathId(fileId);
		BoxFile file = new BoxFile(api, fileId);	
		BoxFile.Info info;
		try {
		    info = file.getInfo();
		} catch(BoxAPIException e){
		    if(e.getResponseCode() == 404) {
		        throw new FileNotFoundException("Unable to retrieve file in " + fileId);
		    } else {
		        throw e;
		    }
		}
		
		File f = new File(outputPath, info.getName());
		try (FileOutputStream fos = new FileOutputStream(f)) {
			file.download(fos);
		} catch (Exception e) {
			BoxAuthHelper.handleException(e);
		} 
		return f;
	}

	@Override
	public List<File> downloadFiles(String parentPathId, String[] filenames, String outputPath)
			throws RepositoryException, RMSException, com.nextlabs.rms.eval.MissingDependenciesException {
		List<File> downloadedFiles = new ArrayList<>();
		List<String> missingFiles = new ArrayList<>();
		try {
			List<SearchResult> list = searchWithNoDuplicate(parentPathId, filenames);
			parentPathId = parentPathId.endsWith("/") ? parentPathId.substring(0, parentPathId.length() - 1) : parentPathId;
			if (filenames != null && filenames.length > 0) {
				for (SearchResult searchResult : list) {
					String filename = searchResult.getName();
					String pathId = searchResult.getPathId();
					try {
						File file = getFile(pathId, outputPath);
						if (file == null) {
							throw new FileNotFoundException("Unable to download file '" + filename + "'");
						}
						downloadedFiles.add(file);
					} catch (FileNotFoundException e) {
						missingFiles.add(filename);
					}
				}
			}
		} catch (MissingDependenciesException e) {
			throw e;
		} catch (Exception e) {
			BoxAuthHelper.handleException(e);
		}
		if (!missingFiles.isEmpty()) {
			throw new MissingDependenciesException(missingFiles);
		}
		return downloadedFiles;
	}
	
	private List<SearchResult> searchWithNoDuplicate(String parentPathId,
			String[] filenames) throws RepositoryException, RMSException, MissingDependenciesException {
		try {
			Set<String> filenameSet = new HashSet<>();
			List<SearchResult> search = search(parentPathId, filenames);
			if (search != null && !search.isEmpty()) {
				for (SearchResult result : search) {
					String name = result.getName();
					if (filenameSet.contains(name.toLowerCase())) {
						throw new NonUniqueFileException(name + " is duplicated in repository", name);
					}
					filenameSet.add(name.toLowerCase());
				}
			}
			List<String> missingFile = new ArrayList<>();
			for (String filename : filenames) {
				if (!filenameSet.contains(filename.toLowerCase())) {
					missingFile.add(filename);
				}
			}
			if (!missingFile.isEmpty()) {
				MissingDependenciesException ex = new MissingDependenciesException(missingFile);
				throw ex;
			}
			return search;
		} catch (RepositoryException e) {
			throw e;
		} catch (RMSException e) {
			throw e;
		}
	}
	
	private List<SearchResult> search(String parentPathId, String[] keywords) throws RepositoryException, RMSException {
		List<SearchResult> results = doSearch(parentPathId, keywords, true);
		return results;
	}

	private List<SearchResult> doSearch(String parentPathId, String[] keywords, boolean searchinSameFolder) throws RepositoryException, RMSException {
		List<SearchResult> contentList = new ArrayList<SearchResult>();
		BoxAPIConnection connection = getAPIConnection();
		BoxSearch bs = new BoxSearch(connection);
		for(String fileName :keywords){
			try {		
				BoxSearchParameters bsp = new BoxSearchParameters(fileName);
				bsp.setContentTypes(Arrays.asList("name"));
				if (parentPathId != null) {
					if(parentPathId.isEmpty()){
						parentPathId = "0";
					}
					parentPathId = getBasePathId(parentPathId);
					List<String> list = new ArrayList<String>();
					list.add(parentPathId);
					bsp.setAncestorFolderIds(list);
				}			
				PartialCollection<BoxItem.Info> results = bs.searchRange(0,1000, bsp);	
				RepoPath repoPath = null;
				for (BoxItem.Info itemInfo : results) {	
					boolean match = false;
					if(searchinSameFolder){
						match = itemInfo.getName().toLowerCase().equals(fileName.toLowerCase()) && itemInfo.getParent().getID().equals(parentPathId);
					}
					else{
						match = itemInfo.getName().toLowerCase().contains(fileName.toLowerCase());
					}
					if(match){			
						repoPath = retrieveParentFromId(itemInfo);
						String rootPath = repoPath.getName() != null ? "/" + repoPath.getName() : "";	
						String rootPathId = repoPath.getId() != null ? "/" + repoPath.getId() : "";
						SearchResult result = new SearchResult();	
						if (itemInfo instanceof BoxFile.Info) {					
							result.setFolder(false);
							result.setFileType(EvaluationHandler.getOriginalFileExtension((itemInfo.getName())));
							result.setProtectedFile(itemInfo.getName().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));		 
						}  else  {
							result.setFolder(true);
						} 
						result.setPathId(rootPathId + "/" +itemInfo.getID());
						result.setName(itemInfo.getName());
						result.setPath(rootPath  +"/"+ itemInfo.getName());
						result.setRepoId(repoId);
						result.setRepoType(ServiceProviderType.BOX.name());
						result.setRepoName(repoName);
						contentList.add(result);
					}
				}
			} catch (BoxAPIException e) {	
				BoxAuthHelper.handleException(e);			
			}			
		}	
		return contentList;
	}
    
	
	@Override
	public List<SearchResult> search(String searchString) throws RepositoryException, RMSException {
		
		List<SearchResult> result = doSearch(null, new String[] { searchString }, false);
		return result;

	}
}
