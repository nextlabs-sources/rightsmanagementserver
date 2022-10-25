/**
 * 
 */
package com.nextlabs.rms.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.eval.RMSPermissionMgr;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.ServiceProviderSettings;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepositoryFactory;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.util.UtilMethods;

/**
 * @author nnallagatla
 *
 */
public class FetchServiceProviderSettingsCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(FetchServiceProviderSettingsCommand.class);
	public FetchServiceProviderSettingsCommand() {
	}

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.debug("FetchServiceProviderSettingsCommand Started");
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		boolean isPermitted = RMSPermissionMgr.isActionAllowed(RMSPermissionMgr.ACTION_SAVE_SETTINGS, userPrincipal);
		if (!isPermitted) {
			response.sendError(403, RMSMessageHandler.getClientString("userNotAdmin"));
			logger.warn("User (username: " + userPrincipal.getUserName() + ", tenant: " + userPrincipal.getTenantId()
					+ ") doesn't have permission to fetch service provider settings.");
			JsonUtil.writeJsonToResponse(null, response);
			return;
		}

		try {
			String[] supportedProviders = RepositoryFactory.allowedRepositories;
			String redirectUrl=UtilMethods.getServerUrl(request);
			Map<String, String> supportedProvidersMap = new HashMap<String, String>();
			populateTypeDisplayNames(supportedProvidersMap, supportedProviders);
			String[] crossLaunchProviders = RepositoryFactory.crossLaunchApps;
			Map<String, String> supportedCrossLaunchMap = new HashMap<String, String>();
			populateTypeDisplayNames(supportedCrossLaunchMap, crossLaunchProviders);
			List<ServiceProviderSetting> serviceProviderSettings = SettingManager
					.getServiceProviderSettingsByTenant(userPrincipal.getTenantId());
			JsonUtil.writeJsonToResponse(new ServiceProviderSettings(serviceProviderSettings, supportedProvidersMap,
					supportedCrossLaunchMap,redirectUrl), response);
		} catch (Exception e) {
			logger.error("Error occurred when fetching service provider settings: " + e.getMessage(), e);
			OperationResult result = new OperationResult();
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_while_fetching_service_providers_settings"));
			JsonUtil.writeJsonToResponse(result, response);
		}
	}

	private void populateTypeDisplayNames(Map<String, String> typetoNameMap, String[] providers) {
		for (String type : providers) {
			typetoNameMap.put(type, RMSMessageHandler.getClientString(type + "_display_name"));
		}
	}
}
