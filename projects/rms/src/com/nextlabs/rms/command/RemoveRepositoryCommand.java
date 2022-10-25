package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.util.UtilMethods;

public class RemoveRepositoryCommand extends AbstractCommand {
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		OperationResult result = new OperationResult();
		try{
			long repoId = UtilMethods.parseLong(request.getParameter("repoId"), IRepository.INVALID_REPOSITORY_ID);
			RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			boolean isAdmin = userPrincipal.getRole().equalsIgnoreCase(RMSUserPrincipal.ADMIN_USER);
			TenantUser tUser = new TenantUser(userPrincipal.getTenantId(), userPrincipal.getUid());
			/*
			 * we should be passing RMSUserPrincipal instead of isAdmin and tUser 
			 * but we do not have RMSUserPrincipal when user sends request from RMC
			 */
			boolean res = RepositoryManager.getInstance().deleteRepository(isAdmin, tUser, repoId);
			result.setResult(res);
			result.setMessage(RMSMessageHandler.getClientString("success_delete_repo"));
			JsonUtil.writeJsonToResponse(result, response);
		} catch (RepositoryNotFoundException e) {
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("status_error_repo_not_found"));
			JsonUtil.writeJsonToResponse(result, response);
		} catch (UnauthorizedOperationException rae){
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("status_failure_unathorized_operation"));
			JsonUtil.writeJsonToResponse(result, response);
		}
		catch(Exception e){
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_delete_repo"));
			JsonUtil.writeJsonToResponse(result, response);
		}
	}
}