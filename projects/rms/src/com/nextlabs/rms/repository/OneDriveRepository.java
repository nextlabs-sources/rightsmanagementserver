package com.nextlabs.rms.repository;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.exception.RepositoryAuthorizationNotFound;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.onedrive.OneDriveFile;
import com.nextlabs.rms.repository.onedrive.OneDriveFolder;
import com.nextlabs.rms.repository.onedrive.OneDriveItem;
import com.nextlabs.rms.repository.onedrive.OneDriveItems;
import com.nextlabs.rms.repository.onedrive.OneDriveOAuthHandler;
import com.nextlabs.rms.repository.onedrive.OneDrivePersonalClient;
import com.nextlabs.rms.util.StringUtils;

public class OneDriveRepository implements IRepository, Observer {
	private static final Logger logger = Logger.getLogger(OneDriveRepository.class);

	private RMSUserPrincipal userPrincipal;

	private long repoId;

	private ServiceProviderType repoType = ServiceProviderType.ONE_DRIVE;

	private String repoName;

	private String accountName;

	private String clientId;

	private String clientSecret;

	private String redirectUrl;
	
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

	private OneDrivePersonalClient client;
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	public OneDriveRepository(RMSUserPrincipal user, long repoId) {
		this.repoId = repoId;
		this.userPrincipal = user;
		client = new OneDrivePersonalClient(this);
		client.addObserver(this);
		String tenantId = userPrincipal.getTenantId();
		try {
			ServiceProviderSetting setting = ConfigManager.getInstance(tenantId).getServiceProviderMap().get(ServiceProviderType.ONE_DRIVE.name());
			if (setting == null) {
				throw new Exception("Service Provider Settings not specified for OneDrive integration.");
			}
			clientId = setting.getAttributes().get(ServiceProviderSetting.APP_ID);
			clientSecret = setting.getAttributes().get(ServiceProviderSetting.APP_SECRET);
			redirectUrl = setting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL);
			if (clientId == null || clientId.length() == 0 || clientSecret == null || clientSecret.length() == 0
					|| redirectUrl == null || redirectUrl.length() == 0) {
				throw new Exception("App Key and App Secret not specified for OneDrive integration.");
			}
			redirectUrl = redirectUrl + (StringUtils.endsWith(redirectUrl, "/") ? "" : "/")
					+ RepoConstants.ONE_DRIVE_AUTH_FINISH_URL;
		} catch (Exception e) {
			logger.error("Error loading App Info for OneDrive: " + e.getMessage(), e);
			return;
		}
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
	return clientSecret;
	}
	
	public String getRedirectUrl() {
		return redirectUrl;
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
	public long getRepoId() {
		return repoId;
	}

	@Override
	public void setRepoId(long repoId) {
		this.repoId = repoId;
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
	public ServiceProviderType getRepoType() {
		return repoType;
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
	public List<RepositoryContent> getFileList(String path) throws RMSException, RepositoryException {
		final List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
		List<OneDriveItems> itemList = null;
		try {
			itemList = client.listItems(path);
			for (OneDriveItems items: itemList) {
				if (items != null) {
					List<OneDriveItem> list = items.getValue();
					if (list != null && !list.isEmpty()) {
						for (OneDriveItem item : list) {
							OneDriveFile file = item.getFile();
							OneDriveFolder folder = item.getFolder();
							String name = item.getName();
							if (file != null || folder != null) {
								String pathName = path + (StringUtils.endsWith(path, "/") ? "" : "/") + name;
								RepositoryContent content = new RepositoryContent();
								if (file != null) {
									content.setFileSize(Long.valueOf(item.getSize()));
									content.setFileType(EvaluationHandler.getOriginalFileExtension(name));
									content.setProtectedFile(name.toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
								}
								content.setLastModifiedTime(item.getMTime() != null ? item.getMTime().getTime() : null);
								content.setFolder(folder != null);
								content.setName(name);
								content.setPath(pathName);
								content.setPathId(pathName);
								content.setRepoId(getRepoId());
								content.setRepoName(getRepoName());
								content.setRepoType(ServiceProviderType.ONE_DRIVE.name());
								contentList.add(content);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			OneDriveOAuthHandler.handleException(e);
		}
		return contentList;
	}

	@Override
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException {
		try {
			OneDriveItem item = client.getItem(fileId);
			OneDriveFolder folder = item.getFolder();
			if (folder != null) {
				throw new FileNotFoundException("Requested item is a folder: " + fileId);
			}
			String downloadUrl = item.getDownloadUrl();
			if (!StringUtils.hasText(downloadUrl)) {
				throw new FileNotFoundException("Download URL is not provided for requested item: " + fileId);
			}
			String fileName = item.getName();
			File file = new File(outputPath, fileName);
			FileUtils.copyURLToFile(new URL(downloadUrl), file);
			if (file == null || file.length() == 0) {
				FileUtils.forceDelete(file);
				throw new FileNotFoundException(fileId + " does not exist in the repository.");
			}
			return file;
		} catch (Exception e) {
			OneDriveOAuthHandler.handleException(e);
		}
		return null;
	}

	@Override
	public void refreshFileList(HttpServletRequest request, HttpServletResponse response, String path)
			throws RepositoryException, RMSException {
		try {
			getFileList(path);
		} catch (Exception e) {
			OneDriveOAuthHandler.handleException(e);
		}
	}

	@Override
	public List<SearchResult> search(String searchString) throws RepositoryException, RMSException {
		final List<SearchResult> searchResults = new ArrayList<SearchResult>();
		List<OneDriveItems> itemList = null;
		try {
			itemList = client.searchItems("/", searchString);
			for (OneDriveItems items: itemList) {
				if (items != null) {
					List<OneDriveItem> list = items.getValue();
					if (list != null && !list.isEmpty()) {
						for (OneDriveItem item : list) {
							OneDriveFile file = item.getFile();
							OneDriveFolder folder = item.getFolder();
							String name = item.getName();
	
							if (file != null || folder != null) {
								String parentPath = item.getParentRef().getPath();
								String pathName = parentPath.replaceFirst("/drive/root:", "") + "/" + name;
								SearchResult result = new SearchResult();
								result.setFolder(folder != null);
								result.setName(name);
								result.setPath(pathName);
								result.setPathId(pathName);
								result.setRepoId(repoId);
								result.setRepoType(repoType.name());
								result.setRepoName(repoName);
								if (file != null) {
									result.setFileType(EvaluationHandler.getOriginalFileExtension(name));
								}
								searchResults.add(result);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			OneDriveOAuthHandler.handleException(e);
		}
		return searchResults;
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
			OneDriveOAuthHandler.handleException(e);
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

	@Override
	public void update(Observable o, Object arg) {
		if (logger.isDebugEnabled()) {
			logger.debug("Received notification to update repository (Repo ID: " + getRepoId() + ")");
		}
		
		String refreshToken = (String) getAttributes().get(RepositoryManager.REFRESH_TOKEN);
		try {
			RepositoryManager.getInstance().updateRefreshToken(getRepoId(), 
					new TenantUser(getUser().getTenantId(), getUser().getUid()), refreshToken);
		} catch (RepositoryAuthorizationNotFound rae) {
			logger.error("Unable to update repository authorization record (Repo ID: " + repoId + " userId: " + 
					getUser().getUid() + "). It might be deleted", rae);
		}
		logger.info("Update new refresh token in repository successfully (Repo ID: " + getRepoId() + ")");
	}
}
