package com.nextlabs.rms.repository.sharepointrest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.dto.repository.AuthorizedRepoUserDTO;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepoConstants;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.servlets.OAuthHelper;

public class SharePointOnlineOAuthHandler {

	private static Logger logger = Logger.getLogger(SharePointOnlineOAuthHandler.class);
	
	public static String startAuthRequest(HttpServletRequest request,HttpServletResponse response) throws IOException {
		
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		String name = request.getParameter("name");
		String siteName = request.getParameter("siteName");
		String isShared = request.getParameter("isShared");
		String repoId = request.getParameter("repoId");
		String redirectCode = request.getParameter("redirectCode");
		if (!siteName.endsWith("/")) {
			siteName = siteName + "/";
		}
		String appId = ConfigManager.getInstance(userPrincipal.getTenantId()).getServiceProviderMap()
				.get(ServiceProviderType.SHAREPOINT_ONLINE.name()).getAttributes().get(ServiceProviderSetting.APP_ID);
		String tenantId = userPrincipal.getTenantId();
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting sharepointSetting = latestRepoSettingMap.get(ServiceProviderType.SHAREPOINT_ONLINE.name());
		String redirectUri = "";
		if(sharepointSetting != null){
			redirectUri = sharepointSetting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL)+GlobalConfigManager.RMS_CONTEXT_NAME+"/"+RepoConstants.SHAREPOINT_ONLINE_AUTH_FINISH_URL;		
		}
		String serverName = siteName.toLowerCase();
		if(serverName.startsWith("https://")){
			serverName = serverName.substring(8);
		}
		String encodedRedirectUrl = redirectUri+"?clientID="+appId+"&siteName="+serverName+"&name="+name+"&repoType="+ServiceProviderType.SHAREPOINT_ONLINE.name()+"&isShared="+isShared;
		if(redirectCode!=null){
			encodedRedirectUrl+="&redirectCode="+redirectCode;
		}
		if(repoId!=null){
			encodedRedirectUrl+="&repoId="+repoId;
		}
		String redirectURL = siteName+"_layouts/15/appredirect.aspx?client_id="+appId+"&redirect_uri="+URLEncoder.encode(encodedRedirectUrl,"UTF-8");
		return redirectURL;
	}	
	
	public static String finishAuthRequest(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException{
		String refresh_token;
		Map<String,String> tokens = null;
		String redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="+RMSMessageHandler.getClientString("appNotFound");
		String username = "";
		boolean isShared = (request.getParameter("isShared")!=null)?((request.getParameter("isShared").equalsIgnoreCase("true")?true:false)):false;
		int redirectCode = (request.getParameter("redirectCode")!=null)?Integer.parseInt(request.getParameter("redirectCode").trim()):0;
		long repoId = (request.getParameter("repoId")!=null)?Long.parseLong(request.getParameter("repoId").trim()):-1;
		String spServer=URLDecoder.decode(request.getParameter("siteName"),"UTF-8");
		
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		try{
			ServiceProviderSetting setting = ConfigManager.getInstance(userPrincipal.getTenantId()).getServiceProviderMap().get(ServiceProviderType.SHAREPOINT_ONLINE.name());
			tokens = SharePointOnlineRepoAuthHelper.getOAuthToken(request, setting.getAttributes().get(ServiceProviderSetting.APP_SECRET));
			refresh_token = tokens.get(RepositoryManager.REFRESH_TOKEN);
		}catch(RMSException e){
			redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME+ OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="+RMSMessageHandler.getClientString("appNotFound");
			return redirectURI;
		}
		try {
			Map<String, List<String>> userProperties = SharePointOnlineRepoAuthHelper.getUserProperties(tokens.get(RepositoryManager.ACCESS_TOKEN), spServer);
			username = userProperties.get("UserName").get(0);
		} catch (ClientProtocolException e) {
			logger.debug(e.getMessage(),e);
		} catch (IOException e) {
			logger.debug(e.getMessage(),e);
		}
		if (!spServer.endsWith("/")) {
			spServer = spServer + "/";
		}
	
		String accName=spServer;
		if(!accName.startsWith("https://")){
			accName="https://"+accName;
		}			
		String repoName = request.getParameter("name");
		AuthorizedRepoUserDTO authUser = new AuthorizedRepoUserDTO();
		authUser.setAccountName(accName);
		authUser.setRefreshToken(refresh_token);
		authUser.setUserId(userPrincipal.getUid());
		authUser.setTenantId(userPrincipal.getTenantId());
		authUser.setAccountId(username);
		//Handle shared with reauthentication
		
		
		//Handle personal with first time authentication
		if(!isShared){
			if(redirectCode == OAuthHelper.NO_REDIRECTION){
				RepositoryDTO spOnlineRepo = new RepositoryDTO();
				spOnlineRepo.setRepoName(repoName);
				spOnlineRepo.setRepoType(ServiceProviderType.SHAREPOINT_ONLINE.name());
				spOnlineRepo.setCreatedByUserId(userPrincipal.getUid());
				spOnlineRepo.setCurrentUser(userPrincipal.getUid());
				spOnlineRepo.setShared(isShared);
				spOnlineRepo.setTenantId(userPrincipal.getTenantId());
				spOnlineRepo.setRepoAccountId(accName);
				spOnlineRepo.setAuthorizedUser(authUser);
				return RepositoryManager.getInstance().addRepoToDBAndFetchRedirectURL(spOnlineRepo);
			}else{
				//Handle personal repository reauthentication here
				authUser.setRepoId(repoId);
				boolean added = RepositoryManager.getInstance().addAuthRepoUserToDB(authUser);
				if(added){
					redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME+ OAuthHelper.REDIRECT_URL_REPOSITORIES + repoId;
				}else{
					//TODO Pranava to decide what to do in case of failure
				}
			}
		}else{
			if(redirectCode == OAuthHelper.NO_REDIRECTION){
				RepositoryDTO spOnlineRepo = new RepositoryDTO();
				spOnlineRepo.setRepoName(repoName);
				spOnlineRepo.setRepoType(ServiceProviderType.SHAREPOINT_ONLINE.name());
				spOnlineRepo.setCreatedByUserId(userPrincipal.getUid());
				spOnlineRepo.setCurrentUser(userPrincipal.getUid());
				spOnlineRepo.setShared(isShared);
				spOnlineRepo.setTenantId(userPrincipal.getTenantId());
				spOnlineRepo.setRepoAccountId(accName);
				spOnlineRepo.setAuthorizedUser(authUser);
				redirectURI = RepositoryManager.getInstance().addRepoToDBAndFetchRedirectURL(spOnlineRepo);
//					String msg = RMSMessageHandler.getClientString("repoAddedSuccessfully", RepositoryFactory.getRepoTypeDisplayName(RepositoryFactory.REPOTYPE_SPONLINE));
//					redirectURI = ConfigManager.RMS_CONTEXT_NAME + "/index.jsp#/repoAdmin?success=" + msg;
			}else{
				authUser.setRepoId(repoId);
				boolean added = RepositoryManager.getInstance().addAuthRepoUserToDB(authUser);
				if(added){
					redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME+OAuthHelper.REDIRECT_URL_REPOSITORIES+repoId;
				}else{
					redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME+ OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="+RMSMessageHandler.getClientString("inaccessibleRepository");
				}
			}
		}
		return redirectURI;
		
	}
}
