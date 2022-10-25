package com.nextlabs.rms.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.FileListResult;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.SearchResult;

public class GetSearchResultsCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(GetSearchResultsCommand.class);
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String searchString = request.getParameter("searchString");
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request
				.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		List<IRepository> repoList = RepositoryManager.getInstance()
				.getRepositoryList(userPrincipal, false);
		if (repoList == null || repoList.size() == 0) {
			JsonUtil.writeJsonToResponse("", response);
			return;
		}
		FileListResult list = new FileListResult();
		List<String> errorList = new ArrayList<String>();
		String error = "";
		boolean result = true;
		List<SearchResult> searchRes = new ArrayList<SearchResult>();
		int errorCount = 0;
		for (IRepository repo : repoList) {
			long startTime = System.nanoTime();
			logger.debug("Search started in "+repo.getAccountName()+" ("+repo.getRepoType()+" repository)");
			try {
				List<SearchResult> res = repo.search(searchString);
				if (res != null && res.size() > 0) {
					searchRes.addAll(res);
				}
			} catch (Exception e) {
				error+=(errorCount == 0)?repo.getRepoName():", "+repo.getRepoName();
				errorCount++;
				logger.error("Error occurred when searching file in repository: " + e.getMessage(), e);
			}
			long endTime = System.nanoTime();
			logger.debug("Search completed in "+repo.getAccountName()+" ("+repo.getRepoType()+" repository) in "+(endTime - startTime)/Math.pow(10, 6) + " ms");	
		}		
		if (error.length() > 0) {
			if(errorCount>1){
				error = RMSMessageHandler.getClientString("repo_not_reachable_error_in_plural", error);
			} else{
				error = RMSMessageHandler.getClientString("repo_not_reachable_error_in_singular", error);
			}					
			result = false;
			errorList.add(error);
		}
		if (searchRes.isEmpty()) {
			result = false;
			errorList.add(RMSMessageHandler.getClientString("error_no_search_results"));			
		}
		if (!errorList.isEmpty()) {
			list.setMessages(errorList);
		}
		list.setContent(searchRes);		
		list.setResult(result);
		JsonUtil.writeJsonToResponse(list, response);
	}

}
