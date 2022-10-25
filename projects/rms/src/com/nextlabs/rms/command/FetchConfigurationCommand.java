package com.nextlabs.rms.command;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.eval.RMSPermissionMgr;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.sharedutil.OperationResult;

public class FetchConfigurationCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(FetchConfigurationCommand.class);
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.debug("FetchConfigurationCommand Started");
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		boolean isPermitted=RMSPermissionMgr.isActionAllowed(RMSPermissionMgr.ACTION_SAVE_SETTINGS, userPrincipal);
		if(!isPermitted){
			response.sendError(403, RMSMessageHandler.getClientString("userNotAdmin"));
			JsonUtil.writeJsonToResponse(null, response);
			logger.warn("User (username: " + userPrincipal.getUserName() + ", tenant: " + userPrincipal.getTenantId()
					+ ") doesn't have permission to access configuration fetching module.");
			return;
		}
		try {
			Map<String, String> configMap = SettingManager.getSettingValues(userPrincipal.getTenantId());
			JsonUtil.writeJsonToResponse(configMap, response);	
		} catch (Exception e) {
			logger.error("Error occurred when fetching configuration: " + e.getMessage(), e);
			OperationResult result = new OperationResult();
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_while_fetching_config"));
			JsonUtil.writeJsonToResponse(result, response);
		}
	}
}
