package com.nextlabs.rms.repository;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.DbxRequestConfig.Builder;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.dropbox.DropBoxAuthHelper;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DropboxRepository implements IRepository{

	private RMSUserPrincipal userPrincipal;

	private String accountId;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	private long repoId;
	
	private ServiceProviderType repoType = ServiceProviderType.DROPBOX;
	
	private String repoName;
	
	private String accountName;

	private boolean isShared = false;
	
	private final Map<String,Object> attributes;
	
	private static Logger logger = Logger.getLogger(DropboxRepository.class);
	
	public DropboxRepository(RMSUserPrincipal user, long repoId) {
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
	public String getAccountName() {
		return accountName;
	}

	@Override
	public void setAccountName(String accountName) {
		this.accountName=accountName;
	}

	private DbxRequestConfig buildRequestConfig(HttpRequestor httpRequestor) {
		Builder builder = DbxRequestConfig.newBuilder(RepoConstants.RMS_CLIENT_IDENTIFIER);
		builder.withUserLocaleFrom(Locale.getDefault());
		if (httpRequestor != null) {
			builder.withHttpRequestor(httpRequestor);
		}
		return builder.build();
	}

	private DbxClientV2 getClient(DbxRequestConfig config) throws InvalidTokenException {

		String refreshToken = (String)attributes.get(RepositoryManager.REFRESH_TOKEN);
		if (refreshToken == null ) {
			throw new InvalidTokenException("No access token");
		}
		return new DbxClientV2(config, refreshToken);
	}

	@Override
	public List<RepositoryContent> getFileList(String path) throws RMSException, RepositoryException {
		DbxClientV2 client = getClient(buildRequestConfig(null));	
		ListFolderResult result = null;
		List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
		try {
			logger.debug("Getting metadata for path: " + path);
			if ("/".equals(path)) {
				path = "";
			}
			result = client.files().listFolder(path);	
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					RepositoryContent content = new RepositoryContent();
					if (metadata instanceof FileMetadata){
						FileMetadata fileMetadata = (FileMetadata)metadata;
						content.setFileSize(fileMetadata.getSize());
						content.setLastModifiedTime(fileMetadata.getServerModified().getTime());
						content.setFileType(EvaluationHandler.getOriginalFileExtension((fileMetadata.getName())));
						content.setProtectedFile(fileMetadata.getName().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
						content.setFolder(false);
					} else if (metadata instanceof FolderMetadata) {
						content.setFolder(true);
						
					} else {
						continue;
					}
					content.setPathId(metadata.getPathDisplay());
					content.setName(metadata.getName());
					content.setPath(metadata.getPathDisplay());
					content.setRepoId(getRepoId());
					content.setRepoName(getRepoName());
					content.setRepoType(ServiceProviderType.DROPBOX.name());
					contentList.add(content);

				}
				if (!result.getHasMore()) {
					break;
				}
				result = client.files().listFolderContinue(result.getCursor());
			}
		} catch (DbxException e) {
			DropBoxAuthHelper.handleException(e);
		}
		return contentList;
	}

	private HttpRequestor getExtendedTimeoutHttpRequestor() {
		return new StandardHttpRequestor(StandardHttpRequestor.Config.builder().withConnectTimeout(RepoConstants.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).withReadTimeout(RepoConstants.READ_TIMEOUT, TimeUnit.MILLISECONDS).build());
	}

	@Override
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException {
		File file=null;
		BufferedOutputStream outStr = null;
		DbxDownloader<FileMetadata> dropboxFile = null;
		try{
			if (fileId == null || fileId.length() == 0) {
				throw new IllegalArgumentException("Invalid file path");
			}
			DbxClientV2 clientLocal = getClient(buildRequestConfig(getExtendedTimeoutHttpRequestor()));
			dropboxFile = clientLocal.files().download(fileId);
			if (dropboxFile == null) {
				throw new FileNotFoundException("Unable to retrieve file in " + fileId);
			}
			FileMetadata metadata = dropboxFile.getResult();
			file = new File(outputPath, metadata.getName());
			outStr = new BufferedOutputStream(new FileOutputStream(file));
			dropboxFile.download(outStr);
			return file;
		} catch (Exception e) {
			DropBoxAuthHelper.handleException(e);
		} finally {
			if (outStr != null) {
				try {
					outStr.close();
					outStr = null;
				} catch (java.io.IOException e) {
					logger.error("Error occurred when closing output stream");
				}
			}
			if (dropboxFile == null) {
				if (file != null && file.exists()) {
					try {
						FileUtils.forceDelete(file);
					} catch (java.io.IOException e) {
						logger.error("Unable to delete file (path: " + file.getAbsolutePath() + "): " + e.getMessage(), e);
					}
				}
			}
		}
		return null;
	}

	
	@Override
	public void refreshFileList(HttpServletRequest request, HttpServletResponse response,String path) throws RepositoryException, RMSException {
		try {
			getFileList(path);
		} catch (Exception e) {
			DropBoxAuthHelper.handleException(e);
		}
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
		List<SearchResult> contentList = new ArrayList<SearchResult>();
		try {
			DbxClientV2 clientLocal = getClient(buildRequestConfig(null));
			com.dropbox.core.v2.files.SearchResult  results = clientLocal.files().search("", searchString);
			for (SearchMatch entry : results.getMatches()) {
				SearchResult result = new SearchResult();
				Metadata mdata = entry.getMetadata();
				if (mdata instanceof FileMetadata) {
					FileMetadata fmData = (FileMetadata)mdata;
					result.setFolder(false);
					result.setFileType(EvaluationHandler.getOriginalFileExtension((fmData.getName())));
					result.setProtectedFile(fmData.getName().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));		 
				}  else if (mdata instanceof FolderMetadata) {
					result.setFolder(true);
				} else {
					continue;
				}
				result.setPathId(mdata.getPathLower());
				result.setName(mdata.getName());
				result.setPath(mdata.getPathLower());
				result.setRepoId(repoId);
				result.setRepoType(repoType.name());
				result.setRepoName(repoName);
				contentList.add(result);

			}
		} catch (Exception e) {
			DropBoxAuthHelper.handleException(e);
		}
		return contentList;
	}

	@Override
	public List<File> downloadFiles(String parentPathId, String[] filenames, String outputPath)
			throws RepositoryException, RMSException, MissingDependenciesException {
		List<File> downloadedFiles = new ArrayList<>();
		List<String> missingFiles = new ArrayList<>();
		try {
			parentPathId = parentPathId.endsWith("/") ? parentPathId.substring(0, parentPathId.length() - 1) : parentPathId;
			if (filenames != null && filenames.length > 0) {
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
			}
		} catch (Exception e) {
			DropBoxAuthHelper.handleException(e);
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

	public boolean isShared() {
		return isShared;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}
}
