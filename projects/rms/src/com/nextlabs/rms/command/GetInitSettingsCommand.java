package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.InitSettings;
import com.nextlabs.rms.json.JsonUtil;

public class GetInitSettingsCommand extends AbstractCommand {

	private Logger logger = Logger.getLogger(GetInitSettingsCommand.class);

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.debug("GetInitSettings initiated");
		JsonUtil.writeJsonToResponse(getInitSettings(request), response);
	}

	private InitSettings getInitSettings(HttpServletRequest request) {
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		InitSettings settings = new InitSettings();
		if (userPrincipal != null) {
			settings.setUserName(userPrincipal.getUserName());
			settings.setAdmin(userPrincipal.getRole().equals(RMSUserPrincipal.ADMIN_USER));
			settings.setSid(userPrincipal.getUid());
			settings.setRmsVersion(GlobalConfigManager.getInstance().getRMSVersionNumber());
			settings.setCopyrightYear(GlobalConfigManager.getInstance().getCopyrightYear());
			boolean isPersonalRepoEnabled = ConfigManager.getInstance(userPrincipal.getTenantId())
					.getBooleanProperty(ConfigManager.ENABLE_PERSONAL_REPO);
			settings.setPersonalRepoEnabled(isPersonalRepoEnabled);
			String rmcZipPath = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RMC_ZIP_PATH);
			if (rmcZipPath != null && rmcZipPath.length() > 0) {
				settings.setRMCConfigured(true);
			} else {
				settings.setRMCConfigured(false);
			}
		}
		return settings;
	}
	
	public JsonObject getInitSettingsJSON(HttpServletRequest request) {
		InitSettings settings = getInitSettings(request);
		return JsonUtil.getJsonObject(settings);
	}
}
