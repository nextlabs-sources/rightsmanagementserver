package com.nextlabs.rms.command;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.FileListResult;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.Repository;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.FileListRetriever;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryContent;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryAccessException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;
import com.nextlabs.rms.servlets.RMSContextListener;
import com.nextlabs.rms.util.StringUtils;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GetAllFilesCommand  extends AbstractCommand {
	
	private static Logger logger = Logger.getLogger(GetAllFilesCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		boolean isOnlyPersonalRepo = false;
		boolean result = true;
		List<String> tokenRepoNames = new ArrayList<>();
		List<String> authRepoNames = new ArrayList<>();
		List<String> accessRepoNames = new ArrayList<>();
		List<IRepository> repoList = RepositoryManager.getInstance().getRepositoryList(userPrincipal, isOnlyPersonalRepo);
		Repository[] repoArr = GetRepositoriesCommand.getRepoArr(repoList);
		List<String> errorMessages = new ArrayList<>();
		List<RepositoryContent> fileList=new ArrayList<RepositoryContent>();
		List<Callable<List<RepositoryContent>>> retrievers = new ArrayList<Callable<List<RepositoryContent>>>(repoArr.length);
		for (Repository repo : repoArr) {
			retrievers.add(new FileListRetriever(userPrincipal, repo.getRepoId()));
		}
		List<Future<List<RepositoryContent>>> fileListResults = null;
		fileListResults = RMSContextListener.getExecutorSvc().invokeAll(retrievers);
		for (Future<List<RepositoryContent>> fileListResult : fileListResults) {
			try {
				fileList.addAll(fileListResult.get());
			} catch(ExecutionException e) {
				result = false;
				if( e.getCause() instanceof InvalidTokenException){
					String repoName = ((InvalidTokenException)e.getCause()).getRepoName();
					tokenRepoNames.add(repoName);
	                logger.error("Cannot connect to repository due to invalid token: " + repoName);
				}else if(e.getCause() instanceof UnauthorizedRepositoryException){
					String repoName = ((UnauthorizedRepositoryException)e.getCause()).getRepoName();
					authRepoNames.add(repoName);
	                logger.error("Cannot connect to repository due to Authorization error: "+repoName);
				}else if(e.getCause() instanceof RepositoryAccessException){
					String repoName = ((RepositoryAccessException)e.getCause()).getRepoName();
					accessRepoNames.add(repoName);
					logger.error("Cannot connect to repository due to general error: " + repoName);
				}
			}
		}
		if (tokenRepoNames.size() > 0) {
			String tokenMessage = StringUtils.getListAsCSV(tokenRepoNames);
			if (tokenRepoNames.size() == 1) {
				errorMessages.add(RMSMessageHandler.getClientString("invalidRepositoryTokenOne", tokenMessage));
			}
			else {
				errorMessages.add(RMSMessageHandler.getClientString("invalidRepositoryTokenMany", tokenMessage));
			}
		}
		if (authRepoNames.size() > 0) {
			String authMessage = StringUtils.getListAsCSV(authRepoNames);
			if (authRepoNames.size() == 1) {
				errorMessages.add(RMSMessageHandler.getClientString("unauthorizedRepositoryAccessOne", authMessage));
			}
			else {
				errorMessages.add(RMSMessageHandler.getClientString("unauthorizedRepositoryAccessMany", authMessage));
			}
		}
		if (accessRepoNames.size() > 0) {
			String accessMessage = StringUtils.getListAsCSV(accessRepoNames);
			if (accessRepoNames.size() == 1) {
				errorMessages.add(RMSMessageHandler.getClientString("inaccessibleRepositoryOne", accessMessage));
			}
			else {
				errorMessages.add(RMSMessageHandler.getClientString("inaccessibleRepositoryMany", accessMessage));
			}
		}
		if (repoArr.length == 0) {
			errorMessages.add(RMSMessageHandler.getClientString("repoNonePresent"));
		}
		else if (fileList.size() == 0) {
			errorMessages.add(RMSMessageHandler.getClientString("repoNoFiles"));
		}
		FileListResult out = new FileListResult();
		out.setMessages(errorMessages);
		out.setResult(result);
		out.setContent(fileList);
		JsonUtil.writeJsonToResponse(out, response);
	}
	
}