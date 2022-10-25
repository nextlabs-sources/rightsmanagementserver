package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.RepoUrl;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepoConstants;
import com.nextlabs.rms.sharedutil.OperationResult;

public class GetRepoAuthLinkCommand extends AbstractCommand{

	private static Logger logger = Logger.getLogger(GetRepoAuthLinkCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String repoType = request.getParameter("repoType");		
		RepoUrl url = null;
		if(ServiceProviderType.DROPBOX.name().equalsIgnoreCase(repoType)){
			url =new RepoUrl(GlobalConfigManager.RMS_CONTEXT_NAME+"/"+RepoConstants.DROPBOX_AUTH_START_URL);
		} else if (ServiceProviderType.GOOGLE_DRIVE.name().equalsIgnoreCase(repoType)){
			url = new RepoUrl(GlobalConfigManager.RMS_CONTEXT_NAME+"/"+RepoConstants.GOOGLE_DRIVE_AUTH_START_URL);
		} else if (ServiceProviderType.ONE_DRIVE.name().equalsIgnoreCase(repoType)){
			url = new RepoUrl(GlobalConfigManager.RMS_CONTEXT_NAME+"/"+RepoConstants.ONE_DRIVE_AUTH_START_URL);
		} else if (ServiceProviderType.SHAREPOINT_ONLINE.name().equalsIgnoreCase(repoType)){
			url = new RepoUrl(GlobalConfigManager.RMS_CONTEXT_NAME+"/"+RepoConstants.SHAREPOINT_ONLINE_AUTH_START_URL);
		} else if(ServiceProviderType.BOX.name().equalsIgnoreCase(repoType)){
			url = new RepoUrl(GlobalConfigManager.RMS_CONTEXT_NAME+"/"+RepoConstants.BOX_AUTH_START_URL);
		}
		else {
			OperationResult result = new OperationResult();
			result.setResult(false);
			String repoTypeDisplayName = ServiceProviderSetting.getProviderTypeDisplayName(repoType);
			result.setMessage(RMSMessageHandler.getClientString("errAddRepo",repoTypeDisplayName));
			JsonUtil.writeJsonToResponse(result, response);
			logger.error("Unrecognized Repo: " + repoType);
			return;
		}
		JsonUtil.writeJsonToResponse(url, response);
	}
}
