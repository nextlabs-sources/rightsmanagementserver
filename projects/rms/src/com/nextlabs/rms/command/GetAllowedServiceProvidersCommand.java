package com.nextlabs.rms.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.util.StringUtils;

public class GetAllowedServiceProvidersCommand extends AbstractCommand{
	
	private static Logger logger = Logger.getLogger(GetAllowedServiceProvidersCommand.class);
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.debug("GetAllowedServiceProvidersCommand started");
		Map<String,ServiceProviderSetting> allowedRepos=new HashMap<String, ServiceProviderSetting>();
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		boolean isLDAPUser = RMSUserPrincipal.AUTH_LDAP.equals(userPrincipal.getAuthProvider());
		Map<String, ServiceProviderSetting> availableServiceProviders = ConfigManager.getInstance(userPrincipal.getTenantId()).getServiceProviderMap();
		for (Iterator<Map.Entry<String, ServiceProviderSetting>> it = availableServiceProviders.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, ServiceProviderSetting> entry = it.next();
			String type = entry.getKey();
			ServiceProviderSetting setting = entry.getValue();
			String allowPersonalRepo = setting.getAttributes().get(ServiceProviderSetting.ALLOW_PERSONAL_REPO);
			if(type.equals(ServiceProviderType.SHAREPOINT_CROSSLAUNCH.name())||type.equals(ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.name())){
				continue;
			} else if (!isLDAPUser && type.equals(ServiceProviderType.SHAREPOINT_ONPREMISE.name())){
			    continue;
			}
			if (userPrincipal.getRole().equalsIgnoreCase(RMSUserPrincipal.ADMIN_USER) || ( StringUtils.hasText(allowPersonalRepo) && allowPersonalRepo.trim().equals("true") ) ) {
				allowedRepos.put(type, setting);
			}
		}
		JsonUtil.writeJsonToResponse(allowedRepos, response);
	}
}
