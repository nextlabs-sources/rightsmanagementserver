/**
 * 
 */
package com.nextlabs.rms.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryFactory;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.RepositoryException;

/**
 * All util methods related to Repository files should go in this class
 * @author nnallagatla
 * 
 */
public class RepositoryFileUtil {

	private static Logger logger = Logger.getLogger(RepositoryFileUtil.class);
	
	/**
	 * This class represents the parameters needed for fetching file from a repository.
	 * Helper class to return all parameters as an object
	 * @author nnallagatla
	 *
	 */
	public static class RepositoryFileParams {
		private long repoId;
		private String filePath;
		private long lastModifiedDate;
		private String displayPath;
		
		public RepositoryFileParams(long aRepoId, String aFilePath, long alastModifiedDate, String aDisplayPath){
			repoId = aRepoId;
			filePath = aFilePath;
			lastModifiedDate = alastModifiedDate;
			displayPath = aDisplayPath;
			
		}

		public long getRepoId() {
			return repoId;
		}

		public String getFilePath() {
			return filePath;
		}
		
		public String getDisplayPath() {
			return displayPath;
		}
		
		public long getLastModifiedDate(){
			return lastModifiedDate;
		}
	}

	/**
	 * Returns the parameters from queryString that are needed to fetch file from the repository
	 * @param request
	 * @return
	 */
	public static RepositoryFileParams getRepositoryFileQueryStringParams(HttpServletRequest request) {
		List<NameValuePair> paramList = URLEncodedUtils.parse(request.getQueryString(), Charset.forName("UTF-8"));
		String repoId = UtilMethods.getParamValue("repoId", paramList);
		String filePath = UtilMethods.getParamValue("filePath", paramList);
		String lastModifiedDate = UtilMethods.getParamValue("lastModifiedDate", paramList);
		String displayPath = UtilMethods.getParamValue("displayPath", paramList);
		if (repoId == null && filePath == null && lastModifiedDate == null && displayPath == null ) {
			repoId = request.getParameter("repoId");
			filePath = request.getParameter("filePath");
			lastModifiedDate = request.getParameter("lastModifiedDate");
			displayPath = request.getParameter("displayPath");
		}
		if(repoId == null) {
			repoId = String.valueOf(IRepository.INVALID_REPOSITORY_ID);
		}
		if(lastModifiedDate == null) {
			lastModifiedDate = "0";
		}
		return new RepositoryFileParams(Long.parseLong(repoId), filePath, Long.parseLong(lastModifiedDate), displayPath);
	}
	
	/**
	 * This method downloads the repo file to outputPath. If outputPath is not valid, an attempt is made to create it
	 * @param userPrincipal
	 * @param repoType
	 * @param repoId
	 * @param filePath
	 * @param outputTempDir
	 * @return
	 * @throws RMSException 
	 * @throws RepositoryException 
	 */
	public static File downloadFileFromRepo(RMSUserPrincipal userPrincipal, RepositoryFileParams repoFileParams, String outputPath) throws RepositoryException, RMSException {
		long repoId = repoFileParams.getRepoId();
		String filePath = repoFileParams.getFilePath();
		IRepository repo = RepositoryFactory.getInstance().getRepository(userPrincipal, repoId);
		logger.debug("Request to download file:" + filePath + " from repoName:" + repo.getRepoName() + " repoId:" + repoId);
		File outputFolder = new File(outputPath);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		return repo.getFile(filePath, outputPath);
	}

	/**
	 * This method returns the folder into which repo file can be downloaded 
	 * @param userTempDir
	 * @return
	 */
	public static String getTempOutputFolder(String userTempDir) {
		Random random = new Random();
		File outputFolder = null;
		do {
			String threadDirId = Long.toString(random.nextLong());
			String outputTempDir = userTempDir + File.separator + threadDirId;
			outputFolder = new File(outputTempDir);
		} while (outputFolder.exists());
		outputFolder.mkdirs();

		String outputPath = outputFolder.getAbsolutePath();
		return outputPath;
	}

	public static File getFileFromRepo(HttpServletRequest request,
			RMSUserPrincipal userPrincipal) throws RepositoryException, RMSException {
			
		RepositoryFileParams params = getRepositoryFileQueryStringParams(request);
		long repoId = params.getRepoId();
		String filePath = params.getFilePath();
		if(!RepositoryManager.getInstance().isRepoValid(userPrincipal, repoId)){
			logger.error("Invalid Repository " + repoId + " for user " + userPrincipal.getPrincipalName() );
			throw new RMSException("Invalid Repository");
		}
		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		String outputPath = getTempOutputFolder(userTempDir);		
		File fileFromRepo = downloadFileFromRepo(userPrincipal, params, outputPath);
		if (!fileFromRepo.exists()) {
			logger.error("File " + filePath + " could not be obtained from the repository.");
			throw new RMSException("File " + filePath + " could not be obtained from the repository.");
		}
		return fileFromRepo;
		}

}
