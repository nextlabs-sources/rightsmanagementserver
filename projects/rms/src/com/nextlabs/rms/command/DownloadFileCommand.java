package com.nextlabs.rms.command;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.util.HTTPUtil;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;

public class DownloadFileCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(DownloadFileCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);
		long repoId = params.getRepoId();
		String filePath = params.getFilePath();
		
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		String sessionId=request.getSession().getId();		
		if(!RepositoryManager.getInstance().isRepoValid(userPrincipal, repoId)){
			logger.error("Invalid Repository " + repoId + " for user " + userPrincipal.getPrincipalName() );
			redirectToErrorPage(response, userPrincipal, filePath, sessionId,RMSMessageHandler.getClientString("repoDeleted"));
			return;
		}
		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);

		String outputPath = RepositoryFileUtil.getTempOutputFolder(userTempDir);

		File fileFromRepo = null;
		try {
			fileFromRepo = RepositoryFileUtil.downloadFileFromRepo(userPrincipal, params, outputPath);
		}
		catch(RepositoryException re){
			logger.error("Error downloading File " + filePath + " from the repository.", re);
			String error = null;
			if (re instanceof FileNotFoundException){
				error = RMSMessageHandler.getClientString("fileDeletedFromRepo");
			} else{
				error = RMSMessageHandler.getClientString("fileDownloadErr");
			}
			redirectToErrorPage(response, userPrincipal, filePath, sessionId,error);
			return;
		}
		
		if (!fileFromRepo.exists()) {
			logger.error("File " + filePath + " could not be obtained from the repository.");
			redirectToErrorPage(response, userPrincipal, filePath, sessionId,RMSMessageHandler.getClientString("fileNotFound"));
			return;
		}
		
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", HTTPUtil.getContentDisposition(fileFromRepo.getName()));
    FileUtils.copyFile(fileFromRepo, response.getOutputStream());
    response.flushBuffer();
	}
	
	/*
	 * TODO move this method to a util class and use it from all places where we are redirecting to error page
	 */
	private void redirectToErrorPage(HttpServletResponse response, RMSUserPrincipal userPrincipal, String filePath,
			String sessionId, String errMsg) throws IOException {
		String cacheId = EvaluationHandler.setError(userPrincipal, filePath, sessionId, errMsg);
		String redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
		response.sendRedirect(redirectURL);
	}
	
}
