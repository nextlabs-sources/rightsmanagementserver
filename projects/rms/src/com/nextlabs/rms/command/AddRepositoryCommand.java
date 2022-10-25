package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.dto.repository.AuthorizedRepoUserDTO;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.sharedutil.OperationResult;

public class AddRepositoryCommand extends AbstractCommand {
	
	private static Logger logger = Logger.getLogger(AddRepositoryCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String repoType = request.getParameter("repoType");
		RepositoryDTO repo = new RepositoryDTO();
		String repoName = request.getParameter("repoName");
		String baseURL = request.getParameter("repoId");
		String allowAll = request.getParameter("showAll");
		repo.setRepoAccountId(baseURL);
		String msg = RMSMessageHandler.getClientString("repoAddedSuccessfully", ServiceProviderSetting.getProviderTypeDisplayName(repoType));
		
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);

		repo.setRepoName(repoName);
		repo.setRepoType(repoType);
		repo.setShared("true".equals(allowAll));
		repo.setCreatedByUserId(userPrincipal.getUid());
		repo.setTenantId(userPrincipal.getTenantId());
		
		/*
		 * create dummy auth user for sharepoint onpremise personal repo.
		 * If we do this, existing code will handle validation and fetching it, 
		 * else we will have to handle it separately
		 */
		if (!repo.isShared()) {
			AuthorizedRepoUserDTO authDTO = new AuthorizedRepoUserDTO();
			authDTO.setAccountId(repo.getRepoAccountId());
			authDTO.setUserId(repo.getCreatedByUserId());
			authDTO.setTenantId(repo.getTenantId());
			authDTO.setAccountName(repo.getRepoAccountId());
			authDTO.setRefreshToken(null);
			repo.setAuthorizedUser(authDTO);
		}
		
		OperationResult result = new OperationResult();
		String repoTypeDisplayName = ServiceProviderSetting.getProviderTypeDisplayName(repoType);
		
		try{
			RepositoryManager.getInstance().addRepository(repo);
			result.setResult(true);
			result.setMessage(msg);
		}
		catch (RepositoryAlreadyExists rae){
			logger.error("Repository already exists", rae);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("repoAlreadyExists", repoTypeDisplayName));
		}
		catch (DuplicateRepositoryNameException e) {
			logger.error("Duplicate Repository name:" + repo.getRepoName(), e);
			String key = repo.isShared() ? "string_shared" : "string_personal";
			msg = RMSMessageHandler.getClientString("error_duplicate_repo_name", RMSMessageHandler.getClientString(key), repo.getRepoName());
			result.setResult(false);
			result.setMessage(msg);
		}
		catch (Exception e) {
			logger.error("Add Repository Failed", e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("errAddRepo",repoTypeDisplayName));
		}
		JsonUtil.writeJsonToResponse(result, response);
	}


}
