package com.nextlabs.rms.command;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.Repository;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryManager;

public class GetManagedRepositoriesCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(GetRepositoriesCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		boolean isOnlyPersonalRepo = true;
		if(userPrincipal.getRole().equalsIgnoreCase(RMSUserPrincipal.ADMIN_USER)){
			isOnlyPersonalRepo = false;
		}		
		List<IRepository> repoList = RepositoryManager.getInstance().getRepositoryList(userPrincipal, isOnlyPersonalRepo);
		Repository[] repoArr = GetRepositoriesCommand.getRepoArr(repoList);
		logger.debug("Number of repositories for user '"+userPrincipal.getUserName()+"':"+repoArr.length);
		JsonUtil.writeJsonToResponse(repoArr, response);
	}

}