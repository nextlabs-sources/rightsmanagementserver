package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.sharedutil.OperationResult;

public class UpdateRepositoryCommand extends AbstractCommand {
	
	private static Logger logger = Logger.getLogger(UpdateRepositoryCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String repoId = request.getParameter("repoId");
		
		OperationResult result = new OperationResult();
		RepositoryDTO repo = new RepositoryDTO();
		try{
			String repoName = request.getParameter("repoName");
			
			RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			
			repo.setRepoName(repoName);
			repo.setId(Long.parseLong(repoId));
			repo.setCreatedByUserId(userPrincipal.getUid());
			repo.setCurrentUser(userPrincipal.getUid());
			repo.setTenantId(userPrincipal.getTenantId());
			boolean flag = RepositoryManager.getInstance().updateRepositoryName(repo);
			result.setResult(flag);
			if (flag)
				result.setMessage(RMSMessageHandler.getClientString("status_success_update_repository"));
			else 
				result.setMessage(RMSMessageHandler.getClientString("status_failure_update_repository"));
		}
		catch (RepositoryNotFoundException rae){
			logger.error("Repository not found", rae);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("status_failure_repository_misisng"));
		}
		catch (UnauthorizedOperationException rae){
			logger.error("Unauthorized operation on repo", rae);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("status_failure_unathorized_operation"));
		}
		catch (DuplicateRepositoryNameException e) {
			logger.error("Duplicate Repository name:" + repo.getRepoName(), e);
			String key = repo.isShared() ? "string_shared" : "string_personal";
			String msg = RMSMessageHandler.getClientString("error_duplicate_repo_name", RMSMessageHandler.getClientString(key), repo.getRepoName());
			result.setResult(false);
			result.setMessage(msg);
		}
		catch (Exception e) {
			logger.error("Update Repository Failed", e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("status_failure_update_repository"));
		}
		JsonUtil.writeJsonToResponse(result, response);
	}


}
