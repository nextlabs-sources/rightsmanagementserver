package com.nextlabs.rms.command;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.Repository;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryManager;

public class GetRepositoriesCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(GetRepositoriesCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		List<IRepository> repoList = RepositoryManager.getInstance().getRepositoryList(userPrincipal, false);
		Repository[] repoArr = getRepoArr(repoList);
		if (request.getSession().getAttribute(RepositoryManager.REPO_CACHE_ID_LIST) == null){
			List<String> repoCacheIds =new ArrayList<>();
			repoCacheIds=getRepoCacheIds(repoArr,userPrincipal);
			request.getSession().setAttribute(RepositoryManager.REPO_CACHE_ID_LIST,repoCacheIds);
		}
		logger.debug("Number of repositories for user '"+userPrincipal.getUserName()+"':"+repoArr.length);
		JsonUtil.writeJsonToResponse(repoArr, response);
	}
	
	private List<String> getRepoCacheIds(Repository[] repoArr,RMSUserPrincipal user){
		List<String> repoCacheIds =new ArrayList<>();
		for(Repository repo : repoArr){
			repoCacheIds.add(RepositoryManager.getRepoCacheId(repo.getRepoId(), user.getUid()));
		}
		return repoCacheIds;
	}

	public static Repository[] getRepoArr(List<IRepository> repoList) {
		Repository[] repoArr = new Repository[repoList.size()];
		int i=0;
		for (IRepository repo : repoList) {
			repoArr[i] = new Repository();
			repoArr[i].setRepoId(repo.getRepoId());
			repoArr[i].setRepoName(repo.getRepoName());
			repoArr[i].setRepoType(repo.getRepoType().name());
			repoArr[i].setRepoTypeDisplayName(ServiceProviderSetting.getProviderTypeDisplayName(repo.getRepoType().name()));
			repoArr[i].setSid(repo.getUser().getUid());
			repoArr[i].setAccountName(repo.getAccountName());
			repoArr[i].setShared(repo.isShared());
			i++;
		}
		return repoArr;
	}
	
}
