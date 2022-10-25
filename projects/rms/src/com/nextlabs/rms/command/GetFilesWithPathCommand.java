package com.nextlabs.rms.command;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.FileListResult;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.*;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;
import com.nextlabs.rms.repository.servlets.OAuthManagerServlet;
import com.nextlabs.rms.util.Nvl;
import com.nextlabs.rms.util.UtilMethods;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetFilesWithPathCommand extends AbstractCommand{

	private static Logger logger = Logger.getLogger(GetFilesWithPathCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String path = URLDecoder.decode(request.getParameter("path"), "UTF-8");
		logger.debug("Getting files for path:" + path);
		long repoId = UtilMethods.parseLong(request.getParameter("repoId"), -1);
		String repoType = request.getParameter("repoType");
		String returnDataPath = request.getParameter("returnDataPath");
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		FileListResult result = new FileListResult();
		if (!RepositoryManager.getInstance().isRepoValid(userPrincipal, repoId)) {
			result.setResult(false);
			result.setMessages(Collections.singletonList(RMSMessageHandler.getClientString("repoDeleted")));
			JsonUtil.writeJsonToResponse(result, response);
			return;
		}
		returnDataPath = Nvl.nvl(returnDataPath);
		IRepository repository = RepositoryFactory.getInstance().getRepository(userPrincipal, repoId);
		List<RepositoryContent> fileList = new ArrayList<>();
		try {
			fileList = repository.getFileList(path);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			String redirectUrl = null;
			String error = null;
			String webContext = UtilMethods.getServerUrl(request)+GlobalConfigManager.RMS_CONTEXT_NAME;
			if (e instanceof InvalidTokenException) {
				error = RMSMessageHandler.getClientString("invalidRepositoryToken");
				if (repoType != null && !repoType.equals("")) {
					redirectUrl = getRedirectUrl(webContext, repoId, repoType, repository);
					setSessionParameter(request, OAuthManagerServlet.INVALID_REPOSITORY_REDIRECTION , repoId);
				}
			} else if (e instanceof UnauthorizedRepositoryException) {
				error = RMSMessageHandler.getClientString("unauthorizedRepositoryAccess");
				if (repoType != null && !repoType.equals("")) {
					redirectUrl = getRedirectUrl(webContext, repoId, repoType, repository);
					setSessionParameter(request, OAuthManagerServlet.UNAUTHORIZED_REPOSITORY_REDIRECTION, repoId);
				}
			} else {
				error = RMSMessageHandler.getClientString("inaccessibleRepository");
			}			
			result.setResult(false);			
			result.setMessages(Collections.singletonList(error));
			result.setRedirectUrl(redirectUrl);
			JsonUtil.writeJsonToResponse(result, response);
			return;
		}
		if (fileList.size() == 0) {
			result.setResult(false);
			result.setMessages(Collections.singletonList(RMSMessageHandler.getClientString("empty_folder")));
			JsonUtil.writeJsonToResponse(result, response);
			return;
		}
		if (path.equals("/")) {
			addElementsToRoot(response, fileList);
		} else {
			result.setResult(true);
			result.setContent(fileList);
			JsonUtil.writeJsonToResponse(result, response);
		}
	}

	private String getRedirectUrl(String webContext, long repoId, String repoType, IRepository repository) {
		String redirectUri = null;
		if (repoType.equals(ServiceProviderType.DROPBOX.name())) {
			redirectUri = webContext + "/" + RepoConstants.DROPBOX_AUTH_START_URL;
		} else if (repoType.equals(ServiceProviderType.GOOGLE_DRIVE.name())) {
			redirectUri = webContext + "/" + RepoConstants.GOOGLE_DRIVE_AUTH_START_URL;
		} else if(repoType.equals(ServiceProviderType.ONE_DRIVE.name())) {
			redirectUri = webContext + "/" + RepoConstants.ONE_DRIVE_AUTH_START_URL;
		} else if (repoType.equals(ServiceProviderType.SHAREPOINT_ONLINE.name())) {
			redirectUri= webContext + "/" + RepoConstants.SHAREPOINT_ONLINE_AUTH_START_URL + "?name=" + repository.getRepoName() + "&isShared=" + repository.isShared() + "&siteName=" + repository.getAccountName()+"&repoType="+repository.getRepoType() + "&redirectCode=1&repoId=" + repoId;
		}

		return redirectUri;
	}

	private void addElementsToRoot(HttpServletResponse response, List<RepositoryContent> fileList) {
		RepositoryContent content = new RepositoryContent();
		content.setChildren(fileList);
		content.setName("Root");
		FileListResult result = new FileListResult();
		result.setResult(true);
		result.setContent(content);
		JsonUtil.writeJsonToResponse(result, response);
	}
	 private void setSessionParameter(HttpServletRequest request,int redirectCode, long repoId) {
		request.getSession().setAttribute("redirectCode", redirectCode);
		request.getSession().setAttribute("repoId", repoId);
	 }
	 public static void clearSessionRedirectParameters(HttpServletRequest request) {
		 request.getSession().removeAttribute("redirectCode");
		 request.getSession().removeAttribute("repoId");
	 }
}
