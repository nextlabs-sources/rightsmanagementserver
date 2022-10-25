package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.RMSPermissionMgr;
import com.nextlabs.rms.exception.BadRequestException;
import com.nextlabs.rms.exception.ServiceProviderAlreadyExists;
import com.nextlabs.rms.exception.ServiceProviderDuplicateAppNameException;
import com.nextlabs.rms.exception.ServiceProviderNotFoundException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.util.StringUtils;

public class SaveServiceProviderSettingCommand extends AbstractCommand {
	
	private static Logger logger = Logger.getLogger(SaveServiceProviderSettingCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.debug("SaveServiceProviderSettingCommand Started");
		OperationResult result = new OperationResult();
		ServiceProviderSetting setting = new ServiceProviderSetting();
		try {
			RMSUserPrincipal user=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			boolean isPermitted=RMSPermissionMgr.isActionAllowed(RMSPermissionMgr.ACTION_SAVE_SETTINGS, user);
			if(!isPermitted){
				response.sendError(403, RMSMessageHandler.getClientString("userNotAdmin"));
				JsonUtil.writeJsonToResponse(null, response);
				logger.warn("User (username: " + user.getUserName() + ", tenant: " + user.getTenantId()
				+ ") doesn't have permission to save Service Provider settings");
				return;
			}
			
			String serviceProviderType = request.getParameter("providerType");
			setting.setProviderType(serviceProviderType);
			setting.setTenantId(user.getTenantId());
			
			String id = request.getParameter("id");
			if (StringUtils.hasText(id)) {
				try {
					long providerId = Long.parseLong(id);
					setting.setId(providerId);
				} catch (NumberFormatException nfe) {
					throw new BadRequestException(nfe);
				}
			}
			
			if (ServiceProviderType.DROPBOX.toString().equals(serviceProviderType) || ServiceProviderType.BOX.toString().equals(serviceProviderType) || ServiceProviderType.GOOGLE_DRIVE.toString().equals(serviceProviderType) ||
					ServiceProviderType.ONE_DRIVE.toString().equals(serviceProviderType) || ServiceProviderType.SHAREPOINT_ONLINE.toString().equals(serviceProviderType)) {
				populateOAuthSettings(setting, request); 
				populatePersonalRepoEnabledSetting(setting, request);
			} else if (ServiceProviderType.SHAREPOINT_ONPREMISE.toString().equals(serviceProviderType)) {
				populatePersonalRepoEnabledSetting(setting, request);
			} else if (ServiceProviderType.SHAREPOINT_CROSSLAUNCH.toString().equals(serviceProviderType)) {
				populateSPOnPremiseCrossLaunchAppSettings(setting, request);
			} else if (ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.toString().equals(serviceProviderType)) {
				populateCrossLaunchAppSettings(setting, request);
			} else {
				throw new BadRequestException("Invalid serviceProviderType: " + serviceProviderType == null? "" : serviceProviderType);
			}
			
			SettingManager.saveServiceProviderSetting(setting);
			ConfigManager.getInstance(user.getTenantId()).loadServiceProviderSettingsFromDB();
			result.setResult(true);
			result.setMessage(RMSMessageHandler.getClientString("success_save_service_provider_setting"));
		} catch (BadRequestException bre) {
			logger.error(bre.getMessage(), bre);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_bad_request"));
		} catch (ServiceProviderDuplicateAppNameException e) {
			logger.error(e.getMessage(), e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_service_provider_duplicate_app_name", 
					ServiceProviderSetting.getProviderTypeDisplayName(setting.getProviderType())));
		} catch (ServiceProviderAlreadyExists e) {
			logger.error(e.getMessage(), e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_service_provider_already_exists"));
		} catch (ServiceProviderNotFoundException e) {
			logger.error(e.getMessage(), e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_service_provider_not_found"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_save_service_provider_setting"));			
		}
		JsonUtil.writeJsonToResponse(result, response);
	}
	
	private void populatePersonalRepoEnabledSetting(ServiceProviderSetting setting, HttpServletRequest request) {
		String allowPR = request.getParameter(ServiceProviderSetting.ALLOW_PERSONAL_REPO);
		if (!StringUtils.hasText(allowPR) || !"true".equals(allowPR)) {
			setting.getAttributes().put(ServiceProviderSetting.ALLOW_PERSONAL_REPO, "false");
			return;
		}
		setting.getAttributes().put(ServiceProviderSetting.ALLOW_PERSONAL_REPO, "true");
	}
	
	private void populateOAuthSettings(ServiceProviderSetting setting, HttpServletRequest request) throws BadRequestException {
		
		String appId = request.getParameter(ServiceProviderSetting.APP_ID);
		String appSecret = request.getParameter(ServiceProviderSetting.APP_SECRET);
		String redirectUrl = request.getParameter(ServiceProviderSetting.REDIRECT_URL);
		if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret) || !StringUtils.hasText(redirectUrl)) {
			throw new BadRequestException("Mandatory parameters are missing");
		}
		
		setting.getAttributes().put(ServiceProviderSetting.APP_ID, appId);
		setting.getAttributes().put(ServiceProviderSetting.APP_SECRET, appSecret);
		setting.getAttributes().put(ServiceProviderSetting.REDIRECT_URL, redirectUrl);
	}
	
	private void populateCrossLaunchAppSettings(ServiceProviderSetting setting, HttpServletRequest request) throws BadRequestException {
		
		populateOAuthSettings(setting, request);
		
		String appName = request.getParameter(ServiceProviderSetting.APP_NAME);
		String appDisplayString = request.getParameter(ServiceProviderSetting.APP_DISPLAY_MENU);
		if (!StringUtils.hasText(appName)) {
			throw new BadRequestException("Mandatory parameters are missing");
		}
		setting.getAttributes().put(ServiceProviderSetting.APP_NAME, appName);
		setting.getAttributes().put(ServiceProviderSetting.APP_DISPLAY_MENU, appDisplayString);
	}
	
	private void populateSPOnPremiseCrossLaunchAppSettings(ServiceProviderSetting setting, HttpServletRequest request) throws BadRequestException {
		populateOAuthSettings(setting, request);
		populateCrossLaunchAppSettings(setting, request);
		
		String remoteURL = request.getParameter(ServiceProviderSetting.REMOTE_WEB_URL);
		if (!StringUtils.hasText(remoteURL)) {
			throw new BadRequestException("Mandatory parameters are missing");
		}
		setting.getAttributes().put(ServiceProviderSetting.REMOTE_WEB_URL, remoteURL);
	}
}
