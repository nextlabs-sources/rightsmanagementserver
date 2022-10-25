package com.nextlabs.rms.repository.sharepointrest;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.RepositoryContent;
import com.nextlabs.rms.repository.SearchResult;
import com.nextlabs.rms.repository.SharepointOnlineRepository;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.sharedutil.RestletUtil;
import com.nextlabs.rms.sharedutil.RestletUtil.IHTTPResponseHandler;
import com.nextlabs.rms.repository.sharepointrest.SPRestSearchJsonHelper.Cells;
import com.nextlabs.rms.repository.sharepointrest.SPRestSearchJsonHelper.Results;

public class SharepointRestClient {
	
	private static Logger logger = Logger.getLogger(SharepointOnlineRepository.class);
	
	private static final int HTTP_SUCCESS_RESPONSE_CODE = 200;
	
//	private static final String AUTHORIZATION = "Authorization";
	private static final MediaType ACCEPTED_MEDIA_TYPE = new MediaType("application/json;odata=verbose"); 

	private List<Preference<MediaType>> getMediaTypes(MediaType ... mediaTypeList) {
		List<Preference<MediaType>> mediaTypes = new ArrayList<>(mediaTypeList != null ? mediaTypeList.length : 0);
		if(mediaTypeList != null && mediaTypeList.length > 0) {
			for(MediaType mediaType : mediaTypeList  ) {
				mediaTypes.add(new Preference<>(mediaType));
			}
		}
		return mediaTypes;
	}

	public List<RepositoryContent> getFileList(String path, String accessToken, String spServer, long repoId, String repoName) throws RepositoryException, RMSException{
		List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
		SPRestFilesJsonHelper helper = null;
		try{
			if("/".equalsIgnoreCase(path)){
				path = "";
				return getSiteContents(path, accessToken, spServer, repoName, repoId);
			}
			path = path.replaceFirst("/", "");
			path = path.startsWith("Documents") ? path.replaceFirst("Documents", "Shared Documents") : path;
			String uri = spServer+"_api/web/GetFolderByServerRelativeUrl('"+path+"')";
			Form form = new Form();			
			form.add("$expand","Folders,Files");
			String queryString = "?" + form.getQueryString();
			ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
			challengeResponse.setRawValue(accessToken);
			List<Preference<MediaType>> mediaTypes = getMediaTypes(ACCEPTED_MEDIA_TYPE);
			helper = RestletUtil.sendRequest(uri + queryString, Method.GET, form.getWebRepresentation(CharacterSet.UTF_8), null, challengeResponse, mediaTypes, new SharepointResponseHandler<>(SPRestFilesJsonHelper.class));				
			List<SPRestFilesJsonHelper.Results> fileResults = helper.getSpResponse().getFiles().results;
			List<SPRestFilesJsonHelper.Results> folderResults = helper.getSpResponse().getFolders().results;
			String relativePath = null;
			for (SPRestFilesJsonHelper.Results result: fileResults) {
				String fileName = result.getName();
				if (fileName.lastIndexOf(".") > -1) {
					RepositoryContent content = new RepositoryContent();
					content.setFolder(false);
					content.setName(fileName);
					content.setFileType(EvaluationHandler.getOriginalFileExtension((fileName)));
					content.setFileSize(Long.parseLong(result.getLength()));
					content.setProtectedFile(fileName.toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
					if(result.getTimeLastModified()!=null){
						Date date=getDateFromResult(result);
						if(date!=null){
							content.setLastModifiedTime(date.getTime());
						}else{
							content.setLastModifiedTime(null);
						}
					}
					relativePath = result.getServerRelativeUrl();
					if (relativePath.startsWith("Shared Documents")) {
						relativePath = relativePath.replaceFirst("Shared Documents", "Documents");
					}
					content.setPath(relativePath);
					content.setPathId(relativePath);
					content.setRepoId(repoId);
					content.setRepoName(repoName);
					content.setRepoType(ServiceProviderType.SHAREPOINT_ONLINE.name());
					contentList.add(content);
				}
			}
			for(SPRestFilesJsonHelper.Results result: folderResults){
				String folderName = result.getName();
				if(!folderName.equalsIgnoreCase("Forms")){									
					RepositoryContent content = new RepositoryContent();
					content.setFolder(true);
					content.setName(folderName);
					if(result.getTimeLastModified()!=null){
						Date date=getDateFromResult(result);
						if(date!=null){
							content.setLastModifiedTime(date.getTime());
						}else{
							content.setLastModifiedTime(null);
						}
					}
					relativePath = result.getServerRelativeUrl();
					if(relativePath.startsWith("Shared Documents")){
						relativePath = relativePath.replaceFirst("Shared Documents", "Documents");
					}
					content.setPath("/"+relativePath);
					content.setPathId("/"+relativePath);
					content.setRepoId(repoId);
					content.setRepoName(repoName);
					contentList.add(content);
				}
			}
	} catch (Exception e) {			
		SharePointOnlineRepoAuthHelper.handleException(e);		
	} 
	return contentList;		
	}
	
	private Date getDateFromResult(SPRestFilesJsonHelper.Results result){
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = sdf.parse(result.getTimeLastModified());
			return date;
		}catch(Exception ex){
			logger.error("Cannot parse the last modified time",ex);
			return null;
		}
		
	}
	
	private List<RepositoryContent> getSiteContents(String path, String accessToken, String spServer, String repoName, long repoId) {
		List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
		String uri = spServer+"_api/web/lists?$select=BaseTemplate,Title,LastItemModifiedDate&$filter=BaseTemplate eq 101";		
		ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
		challengeResponse.setRawValue(accessToken);
		List<Preference<MediaType>> mediaTypes = getMediaTypes(ACCEPTED_MEDIA_TYPE);
		SPRestList helper = RestletUtil.sendRequest(uri, Method.GET, null, null, challengeResponse, mediaTypes, new SharepointResponseHandler<>(SPRestList.class));
		List<SPRestList.Results> results = helper.getSpResponse().getResults();
		String title = null;
		for(SPRestList.Results eachResult : results){
			RepositoryContent content = new RepositoryContent();
			title = eachResult.getTitle();
			if(eachResult.getLastItemModifiedDate()!=null){
				try{
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
					Date date = sdf.parse(eachResult.getLastItemModifiedDate());
					content.setLastModifiedTime(date.getTime());
				}catch(Exception ex){
					logger.error("Cannot parse the last modified time");
					content.setLastModifiedTime(null);
				}
			}
			content.setName(title);
			content.setFolder(true);
			content.setPath("/"+title);
			content.setPathId("/"+title);
			content.setRepoId(repoId);
			content.setRepoName(repoName);
			contentList.add(content);
		}
		return contentList;
	}

	public byte[] downloadFile(String fileId, String accessToken, String spServer) throws RepositoryException, IOException{
		if(fileId.startsWith("/")){
			fileId = fileId.replaceFirst("/", "");
		}
		fileId = fileId.startsWith("Documents") ? fileId.replaceFirst("Documents", "Shared Documents") : fileId;
		String uri = spServer+"_api/web/GetFileByServerRelativeUrl('/"+fileId+"')/$value";
		ClientResource client = new ClientResource(uri);
//		headers.set(AUTHORIZATION,"Bearer " + accessToken);
		ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
		challengeResponse.setRawValue(accessToken);
		client.getRequest().setChallengeResponse(challengeResponse);
		byte[] content = IOUtils.toByteArray(client.get().getStream());
		return content;
	}
	
	public List<SearchResult> searchsp(String searchText, String accessToken, int searchlimit, String repoName, long repoId, String repoType, String spServer){
		List<SearchResult> contentList = new ArrayList<SearchResult>();
		SPRestSearchJsonHelper helper = null;
		String uri = spServer+"_api/search/query?querytext='"+searchText+"+site:\""+spServer+"\"'&trimduplicates=false&rowlimit="+searchlimit;
		ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
		challengeResponse.setRawValue(accessToken);
		List<Preference<MediaType>> mediaTypes = getMediaTypes(ACCEPTED_MEDIA_TYPE);
		helper = RestletUtil.sendRequest(uri, Method.GET, null, null, challengeResponse, mediaTypes, new SharepointResponseHandler<>(SPRestSearchJsonHelper.class));
		List<SPRestSearchJsonHelper.Results> rowResults = helper.getSpResponse().getQuery().getPrimaryQueryResult().getRelevantResults().getTable().getRows().getResults();			
		for(SPRestSearchJsonHelper.Results eachResult : rowResults){
			Cells cell = eachResult.getCells();
			List<Results> cellResultList = cell.getResults();
			SearchResult sr = new SearchResult();
			String title = null;
			String fullPath = null;				
			String fileExt = null;
			String parentLink = null;
			for(SPRestSearchJsonHelper.Results eachMap : cellResultList){
				if(("Title").equals(eachMap.getKey())){
					title = eachMap.getValue();
				} else if(("OriginalPath").equals(eachMap.getKey())){
					fullPath = eachMap.getValue();
				} else if(("SecondaryFileExtension").equals(eachMap.getKey())){
					fileExt = eachMap.getValue();
				} else if(("ParentLink").equals(eachMap.getKey())){
					parentLink = eachMap.getValue();
				} 
			}
			if(!spServer.equals(parentLink+"/") && parentLink!=null){
				boolean isFolder = fileExt == null ? true : false;
				if(isFolder){
					title = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.length());	
				} else {
					title += "."+fileExt;
				}
				String relativePath = null;
				String[] parentLinkArr = parentLink.split(spServer);
				String parentRelativePath = parentLinkArr[1];
				String[] parentFolderArr = parentRelativePath.split("/");					
				if(parentFolderArr.length > 1 && parentFolderArr[1].equals("Forms")){
					sr.setName(title);
					relativePath = parentFolderArr[0]+ "/" + title;
					if(relativePath.startsWith("Shared Documents")){
						relativePath = relativePath.replaceFirst("Shared Documents", "Documents");
					}
					sr.setPath("/"+relativePath);
					sr.setPathId("/"+relativePath);
				} else {
					sr.setName(title);
					parentRelativePath = parentRelativePath +"/"+ title;
					if(parentRelativePath.startsWith("Shared Documents")){
						parentRelativePath = parentRelativePath.replaceFirst("Shared Documents", "Documents");
					}
					sr.setPath("/"+parentRelativePath);
					sr.setPathId("/"+parentRelativePath);
				}				
				sr.setFolder(isFolder);	
				sr.setRepoId(repoId);
				sr.setRepoName(repoName);
				sr.setRepoType(repoType);
				sr.setFileType(EvaluationHandler.getOriginalFileExtension((title)));
				sr.setProtectedFile(title.toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
				contentList.add(sr);
			}
		}
		return contentList;
	}
	
	static class SharepointResponseHandler<T> implements IHTTPResponseHandler<T> {
		private Class<T> resultClass;

		public SharepointResponseHandler(Class<T> resultClass) {
			this.resultClass = resultClass;
		}

		@Override
		public T handle(Representation representation, Status status) {
			return handleResponse(representation, status, resultClass);
		}

		private <Result> Result handleResponse(Representation representation, Status status, Class<Result> resultClass) {
			int code = status.getCode();
			if (code == HTTP_SUCCESS_RESPONSE_CODE) {
				return handleJSONResponse(representation, resultClass);
			} else {
				handleErrorResponse(representation, status);
			}
			return null;
		}

		private <Result> Result handleJSONResponse(Representation representation, Class<Result> resultClass) {
			final Gson gson = new Gson();
			StringWriter writer = new StringWriter();
			Result result = null;
			try {
				representation.write(writer);
				result = gson.fromJson(writer.toString(), resultClass);
			} catch (final Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			return result;
		}

		private void handleErrorResponse(Representation representation, Status status) {
			final String errorMessage = status.getDescription();
			final int code = status.getCode();			
			final Gson gson = new Gson();
			StringWriter writer = new StringWriter();
			SPRestErrorResponse error;
			try {
				representation.write(writer);
				error = gson.fromJson(writer.toString(), SPRestErrorResponse.class);
			} catch (final Exception ex) {
				String msg = writer.toString();
				error = new SPRestErrorResponse();
				com.nextlabs.rms.repository.sharepointrest.SPRestError e = new SPRestError();
				e.setCode(String.valueOf(status.getCode()));
				e.setMessage("Raw error: " + (StringUtils.hasText(msg) ? msg : ex.getMessage()));
				error.setError(e);
			}
			throw new SPRestServiceException(code, errorMessage, error);
		}
	}
}
