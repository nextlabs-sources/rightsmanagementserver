/**
 * 
 */
package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.eval.RMSPermissionMgr;
import com.nextlabs.rms.exception.ServiceProviderNotFoundException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.sharedutil.OperationResult;

/**
 * @author nnallagatla
 *
 */
public class DeleteServiceProviderSettingCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(DeleteServiceProviderSettingCommand.class);
	public DeleteServiceProviderSettingCommand() {
	}

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.debug("DeleteServiceProviderSettingCommand Started");
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		boolean isPermitted = RMSPermissionMgr.isActionAllowed(RMSPermissionMgr.ACTION_SAVE_SETTINGS, userPrincipal);
		if (!isPermitted) {
			response.sendError(403, RMSMessageHandler.getClientString("userNotAdmin"));
			logger.warn("User (username: " + userPrincipal.getUserName() + ", tenant: " + userPrincipal.getTenantId()
					+ ") doesn't have permission to delete service provider.");
			JsonUtil.writeJsonToResponse(null, response);
			return;
		}
		OperationResult result = new OperationResult();
		try {
			long id = Long.parseLong(request.getParameter("serviceProviderId"));
			SettingManager.deleteServiceProvider(userPrincipal.getTenantId(), id);
			ConfigManager.getInstance(userPrincipal.getTenantId()).loadServiceProviderSettingsFromDB();
			result.setResult(true);
			result.setMessage(RMSMessageHandler.getClientString("success_delete_service_provider_setting"));
		}catch (ServiceProviderNotFoundException e) {		
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_service_provider_not_found"));
		}catch (Exception e) {
			logger.error("Error occurred when deleting service provider: " + e.getMessage(), e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_while_deleting_service_providers_setting"));
		}
		JsonUtil.writeJsonToResponse(result, response);
	}

}
