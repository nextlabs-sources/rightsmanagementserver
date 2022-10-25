package com.nextlabs.rms.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.NonUniqueFileException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.googledrive.GoogleDriveOAuthHandler;
import com.nextlabs.rms.util.Nvl;
import com.nextlabs.rms.util.StringUtils;


public class GoogleDriveRepository implements IRepository{

	private RMSUserPrincipal userPrincipal;

	public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

	private static final int MAX_RESULTS = 250;
	private static final String FILENAME_ATTR = "title";

	private long repoId;

	private ServiceProviderType repoType = ServiceProviderType.GOOGLE_DRIVE;

	private String repoName;

	private String accountName;

	private final Map<String,Object> attributes;

	public boolean isShared = false;

	private String accountId;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	private static final Logger logger = Logger.getLogger(GoogleDriveRepository.class);

	public GoogleDriveRepository(RMSUserPrincipal user, long repoId) {
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

	@Override
	public List<RepositoryContent> getFileList(String path) throws RMSException, RepositoryException {
		List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
		try {
			if (path == null || path.length() == 0) {
				throw new IllegalArgumentException("Invalid path");
			}
			Drive service = getDrive();
			if ("/".equalsIgnoreCase(path)) {				
				About about = service.about().get().execute();
				path = about.getRootFolderId();
			}
			RepoPath repoPath = retrieveParentFromId(service, path);
			path = getBasePathId(path);
			List<com.google.api.services.drive.model.File> children = getChildren(service, path);
			for (com.google.api.services.drive.model.File file : children) {
				boolean isFolder = FOLDER_MIME_TYPE.equalsIgnoreCase(file.getMimeType()) && file.getFileExtension() == null
						? true : false;
				RepositoryContent content = new RepositoryContent();
				if (!isFolder){
						content.setFileSize(file.getFileSize());
						content.setFileType(EvaluationHandler.getOriginalFileExtension(file.getTitle()));
						content.setProtectedFile(file.getTitle().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
				}
				content.setLastModifiedTime(file.getModifiedDate().getValue());
				
	        	
				content.setFolder(isFolder);
				content.setName(file.getTitle());
				String rootPath = repoPath.getName() != null ? "/" + repoPath.getName() : "";
				String rootPathId = repoPath.getId() != null ? "/" + repoPath.getId() : "";
				content.setPath(rootPath + "/" + file.getTitle());
				content.setPathId(rootPathId + "/" + file.getId());
				content.setRepoId(getRepoId());
				content.setRepoName(getRepoName());
				content.setRepoType(ServiceProviderType.GOOGLE_DRIVE.name());
				contentList.add(content);
			}
		} catch (Exception e) {
			GoogleDriveOAuthHandler.handleException(e);
		}
		return contentList;
	}

	private Drive getDrive() {
		Credential credential = GoogleDriveOAuthHandler.buildCredential(userPrincipal.getTenantId());
		credential.setRefreshToken((String)attributes.get(RepositoryManager.REFRESH_TOKEN));
		Drive drive = GoogleDriveOAuthHandler.getDrive(credential);
		return drive;
	}

	private RepoPath retrieveParentFromId(Drive drive, String path) throws RMSException, RepositoryException {
		List<String> parentList = new ArrayList<>();
		if (path.contains("/")) {
			String[] parentFromPath = org.springframework.util.StringUtils.tokenizeToStringArray(path, "/", true, true);
			for (int i = 0; i < parentFromPath.length; i++) {
				parentList.add(parentFromPath[i]);
			}
		}
		java.util.Collections.reverse(parentList);
		RepoPath repoPath = new RepoPath();
		try {
			for(String parent: parentList){
				com.google.api.services.drive.model.File parentFolder = drive.files().get(parent).execute();
				String id = parentFolder.getId();
				String title = parentFolder.getTitle();
				repoPath.setId(repoPath.getId() != null ? id + "/" + Nvl.nvl(repoPath.getId()) : id);
				repoPath.setName(repoPath.getName() != null ? title + "/" + Nvl.nvl(repoPath.getName()) : title);
			}
		} catch (Exception e) {
			GoogleDriveOAuthHandler.handleException(e);
		}

		return repoPath;
	}	

	private List<com.google.api.services.drive.model.File> getChildren(Drive service, String folderId)
			throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("'").append(folderId).append("' in parents and trashed = false");
		com.google.api.services.drive.Drive.Files.List request = service.files().list().setQ(builder.toString());
		request.setMaxResults(MAX_RESULTS);
		List<com.google.api.services.drive.model.File> fileList = new ArrayList<com.google.api.services.drive.model.File>(); 
		listFilesByPaginating(request, fileList);		
		return fileList;
	}

	private void listFilesByPaginating(com.google.api.services.drive.Drive.Files.List request, List<com.google.api.services.drive.model.File> fileList) throws IOException{
		String npTok = null;
		do {
			FileList result = request.execute();

			if (result != null) {
				fileList.addAll(result.getItems());
				npTok = result.getNextPageToken(); // GET POINTER TO THE NEXT PAGE
				request.setPageToken(npTok);
			} else {
				break;
			}
		} while (npTok != null && npTok.length() > 0);
	}

	@Override
	public void refreshFileList(HttpServletRequest request, HttpServletResponse response,String path) throws RepositoryException, RMSException {
		try {
			getFileList(path);
		} catch (Exception e) {
			GoogleDriveOAuthHandler.handleException(e);
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
	public File getFile(String fileId, String outputPath) throws RepositoryException, RMSException {
		File file = null;
		try {
			fileId = getBasePathId(fileId);
			Drive service = getDrive();
			Get repoFile = service.files().get(fileId);			
			com.google.api.services.drive.model.File gFile = repoFile.execute();
			if (gFile == null || gFile.getExplicitlyTrashed()){
				throw new FileNotFoundException("Unable to retrieve file in " + fileId);
			}
			String fileName = gFile.getTitle();
			InputStream inputStream = null;
			Map<String, String> exportLinks = gFile.getExportLinks();
			if (exportLinks != null) {
				// export all Google docs to the viewer as PDF
				String downloadURL = exportLinks.get("application/pdf");
				inputStream = downloadFile(service, downloadURL);
			}
			else {
				inputStream = repoFile.executeMediaAsInputStream();
			}
			file = new File(outputPath + File.separator + fileName);
			writeInputeStreamToFile(file, inputStream);
			return file;
		} catch (Exception e) {
			GoogleDriveOAuthHandler.handleException(e);
		}
		return null;
	}

    private InputStream downloadFile(Drive service, String downloadURL) {
        if (downloadURL != null && downloadURL.length() > 0) {
            try {
                HttpResponse resp =
                        service.getRequestFactory().buildGetRequest(new GenericUrl(downloadURL))
                                .execute();
                return resp.getContent();
            } catch (IOException e) {
                logger.error("An error occurred while trying to download a converted Google document.", e);
                return null;
            }
        } else {
            return null;
        }
    }

	private void writeInputeStreamToFile(File file, InputStream inputStream) {
		OutputStream outputStream = null;

		try {
			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[4096];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			logger.error("Couldn't write NXL file to disk for decrypting: ",e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				logger.error("Couldn't close stream: ", e);
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				logger.error("Couldn't close stream: ", e);
			}
		}
	}

	public Map<String, List<String>> findPath(String currId, Map<String, com.google.api.services.drive.model.File> fileMap,
			Map<String, Map<String, List<String>>> map) {
		List<String> currPaths = map.get("paths").get(currId);
		List<String> currIds   = map.get("ids").get(currId);

		if (currPaths == null) {
			currPaths = new ArrayList<String>();
			currIds   = new ArrayList<String>();

			com.google.api.services.drive.model.File currFile = fileMap.get(currId);
			if (currFile == null) {
				return null;
			}

			String currPath = currFile.getTitle();

			List<ParentReference> parents = currFile.getParents();
			for (ParentReference parent : parents) {
				if (parent.getIsRoot()) {
					currPaths.add("root");
					currIds.add(parent.getId());
				}
				else {
					Map<String, List<String>> result = findPath(parent.getId(), fileMap, map);
					currPaths.addAll(result.get("paths"));
					currIds.addAll(result.get("ids"));
				}
			}

			for (int i=0; i < currPaths.size(); ++i) {
				String path = currPaths.get(i);
				String id   = currIds.get(i);

				if (path.equals("root")) {
					currPaths.set(i, "/" + currPath);
					currIds.set(i, "/" + currId);
				}
				else {
					currPaths.set(i, path + "/" + currPath);
					currIds.set(i, id + "/" + currId);
				}
			}
			map.get("paths").put(currId, currPaths);
			map.get("ids").put(currId, currIds);
		}

		Map<String, List<String>> result = new HashMap<String, List<String>>();
		result.put("paths", currPaths);
		result.put("ids", currIds);

		return result;
	}

	@Override
	public List<SearchResult> search(String searchString) throws RepositoryException, RMSException {
		List<SearchResult> result = doSearch(null, new String[] { searchString }, false);
		return result;
	}

	private String getBasePathId(String pathId) {
		if (pathId == null || pathId.length() == 0) {
			throw new IllegalArgumentException("Invalid file path");
		}
		int idx = pathId.lastIndexOf("/");
		pathId = idx >= 0 ? pathId.substring(idx + 1) : pathId;
		return pathId;
	}

	private void listItems(Drive service, String query, List<com.google.api.services.drive.model.File> files)
			throws IOException {
		Drive.Files.List list = service.files().list();
		if (StringUtils.hasText(query)) {
			list.setQ(query);
		}
		list.setMaxResults(MAX_RESULTS);
		listFilesByPaginating(list, files);
	}

	private List<SearchResult> doSearch(String parentPathId, String[] keywords, boolean exactMatch) throws RepositoryException, RMSException {
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		StringBuilder builder = new StringBuilder("(((");
		for (int i = 0; i < keywords.length; i++) {
			if (i > 0) {
				builder.append(" or ");
			}
			builder.append(FILENAME_ATTR).append(" contains '").append(keywords[i]).append("'");
		}
		builder.append(") ");
		if (StringUtils.hasText(parentPathId)) {
			String pathId = getBasePathId(parentPathId);
			builder.append("and '").append(pathId).append("' in parents");
		}
		builder.append(") or ").append("mimeType = '")
		.append(FOLDER_MIME_TYPE).append("') ").append("and trashed = false");

		List<com.google.api.services.drive.model.File> files = new ArrayList<com.google.api.services.drive.model.File>();
		try {
			Drive service = getDrive();
			listItems(service, builder.toString(), files);
		} catch (IOException e) {
			GoogleDriveOAuthHandler.handleException(e);
		}

		Map<String, com.google.api.services.drive.model.File> fileMap = new HashMap<String, com.google.api.services.drive.model.File>();
		for (com.google.api.services.drive.model.File file : files) {
			fileMap.put(file.getId(), file);
		}

		Map<String, Map<String, List<String>>> pathsMap = new HashMap<String, Map<String, List<String>>>();
		pathsMap.put("ids", new HashMap<String, List<String>>());
		pathsMap.put("paths", new HashMap<String, List<String>>());

		for (com.google.api.services.drive.model.File file : files) {
			boolean match = false;
			if (exactMatch) {
				match = StringUtils.containsElement(keywords, file.getTitle(), true);
			} else {
				for (String keyword : keywords) {
					if (file.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
						match = true;
						break;
					}
				}
			}
			if (match) {
				Map<String, List<String>> paths = findPath(file.getId(), fileMap, pathsMap);
				for (int i = 0; i < paths.get("paths").size(); ++i) {
					SearchResult sr = new SearchResult();
					sr.setPath(paths.get("paths").get(i));
					sr.setPathId(paths.get("ids").get(i));
					sr.setName(file.getTitle());
					sr.setRepoId(repoId);
					sr.setRepoType(repoType.name());
					sr.setRepoName(repoName);
					sr.setFolder(file.getMimeType().equals(FOLDER_MIME_TYPE));
					if (!sr.isFolder()) {
						sr.setFileType(EvaluationHandler.getOriginalFileExtension(file.getTitle()));
						sr.setProtectedFile(file.getTitle().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
					}
					searchResults.add(sr);
				}
			}
		}
		return searchResults;
	}

	private List<SearchResult> search(String parentPathId, String[] keywords) throws RepositoryException, RMSException {
		List<SearchResult> results = doSearch(parentPathId, keywords, true);
		return results;
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

	@Override
	public List<File> downloadFiles(String parentPathId, String[] filenames, String outputPath)
			throws RepositoryException, RMSException, MissingDependenciesException {
		List<File> downloadedFiles = new ArrayList<>();
		List<String> missingFiles = new ArrayList<>();
		try {
			List<SearchResult> list = searchWithNoDuplicate(parentPathId, filenames);
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
		} catch (MissingDependenciesException e) {
			throw e;
		} catch (Exception e) {
			GoogleDriveOAuthHandler.handleException(e);
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
	public void setShared(boolean shared) {
		this.isShared=shared;		
	}

	@Override
	public boolean isShared() {
		return isShared;
	}
}
