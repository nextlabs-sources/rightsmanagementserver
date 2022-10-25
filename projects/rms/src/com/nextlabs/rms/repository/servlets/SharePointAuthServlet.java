package com.nextlabs.rms.repository.servlets;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.sharepointrest.SharePointOnlineRepoAuthHelper;

public class SharePointAuthServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private  static final String RMS_SHAREPOINT = "RMS_SHAREPOINT";

	private static Logger logger = Logger.getLogger(SharePointAuthServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException{
		processReq(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException{
		processReq(request, response);
	}

	@SuppressWarnings("unchecked")
	private void processReq(HttpServletRequest request,HttpServletResponse response) throws ClientProtocolException, IOException {
		String uri = request.getRequestURI();
		String path="";
		String siteName="";
		String protocol="https";
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		if(userPrincipal!=null){
			if(userPrincipal.isRMSUser()){
				String redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("spDuplicateLogin");
				response.sendRedirect(redirectURL);
				return;
			}
		}
		try {
			path = request.getParameter("path");
			siteName=request.getParameter("siteName");
			if(siteName.startsWith("https://")){
				siteName=siteName.trim().substring(8);
			}
			else if(siteName.startsWith("http://")){
				protocol = "http";
				siteName=siteName.trim().substring(7);
			}

		} catch (Exception e1) {
			logger.error(e1);
		}
		String tenantId = request.getParameter("tenantID");
		if(tenantId == null || tenantId.length()==0){
			tenantId = GlobalConfigManager.DEFAULT_TENANT_ID;
		}
		logger.debug("Path of file is "+path);
		logger.debug("Site name is "+siteName);
		String url = "/RMSViewer/DisplaySPFile"+"?siteName="+URLEncoder.encode(siteName,"UTF-8")
		+"&path="+URLEncoder.encode(path,"UTF-8")
		+"&protocol="+URLEncoder.encode(protocol, "UTF-8");
		String appId=request.getParameter("clientID");
		if(uri.endsWith("/SharePointAuth/AuthStart")){

			if(isValidToken((String)request.getSession().getAttribute(ConfigManager.SP_OAUTH_TOKEN),(String)request.getSession().getAttribute(GlobalConfigManager.SP_OAUTH_TOKEN_EXPIRY_TIME),userPrincipal)){
				try {
					logger.debug("Using cached OAuth token.");
					getServletContext().getRequestDispatcher(url).forward(request, response);
					return;
				} catch (Exception e) {
					logger.error("Error occurred while redirecting user to Display Page",e);
				}
			}
			else{
				logger.debug("OAuth token not found in the cache");
				try {
					if(userPrincipal != null && userPrincipal.getTenantId()!=null){
						tenantId = userPrincipal.getTenantId();
					}
					ServiceProviderSetting sharepointSetting=SettingManager.fetchServiceProviderByTenantIdKeyAndValue(tenantId, 
							ServiceProviderSetting.APP_ID, request.getParameter("clientID"));
					if(sharepointSetting==null){
						throw new RMSException(RMSMessageHandler.getClientString("appNotFound"));
					}
					else{
						StringBuilder redirectURL = new StringBuilder();
						redirectURL.append(sharepointSetting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL));
						redirectURL.append(GlobalConfigManager.RMS_CONTEXT_NAME);
						redirectURL.append("/SharePointAuth/AuthFinish");
						redirectURL.append("?siteName=");
						redirectURL.append(siteName);
						redirectURL.append("&path=");
						redirectURL.append(path);
						redirectURL.append("&clientID=");
						redirectURL.append(appId);
						redirectURL.append("&tenantID=");
						redirectURL.append(tenantId);
						StringBuilder responseURL= new StringBuilder();
						responseURL.append("https://");
						responseURL.append(siteName);
						responseURL.append("/_layouts/15/appredirect.aspx?client_id=");
						responseURL.append(appId);
						responseURL.append("&redirect_uri=");
						responseURL.append(URLEncoder.encode(redirectURL.toString(),"UTF-8"));
						response.sendRedirect(responseURL.toString());
						return;
					}		
				} catch (IOException e) {
					logger.error("Error occurred while redirecting to SharePoint Page", e);
				} catch (RMSException e) {
					logger.error("App not found.", e);
					response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("appNotFound"));
					return;
				}
			}
		}
		else if(uri.endsWith("/SharePointAuth/AuthFinish")){
			String token=null;
			try{
				if(userPrincipal != null && userPrincipal.getTenantId()!=null){
					tenantId = userPrincipal.getTenantId();
				}
				ServiceProviderSetting app=SettingManager.fetchServiceProviderByTenantIdKeyAndValue(tenantId, 
						ServiceProviderSetting.APP_ID, request.getParameter("clientID"));
				if(app==null){
					throw new RMSException(RMSMessageHandler.getClientString("appNotFound"));
				}
				Map<String,String> tokens = SharePointOnlineRepoAuthHelper.getOAuthToken(request,app.getAttributes().get(ServiceProviderSetting.APP_SECRET));
				token = tokens.get(RepositoryManager.ACCESS_TOKEN);
				request.getSession().setAttribute(GlobalConfigManager.SP_OAUTH_TOKEN_EXPIRY_TIME, tokens.get(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME));
			}catch(RMSException e){
				response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("appNotFound"));
				return;
			}
			String spServer=URLDecoder.decode(request.getParameter("siteName"),"UTF-8");
			Map<String,List<String>> userAttributes=SharePointOnlineRepoAuthHelper.getUserProperties(token,spServer);
			if(userAttributes==null){
				String redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("sharepointUserPropErr");
				response.sendRedirect(redirectURL);
				return;
			}
			redirectToFileDisplayPage(request, response, url, token,
					userAttributes,tenantId);
			return;
		}else if (uri.endsWith("/SharePointAuth/OnPremiseAuth")){
			logger.debug("OnPremiseAuth flow..");
			Map<String, String[]> paramMap = request.getParameterMap();
			Map<String, List<String>> attribMap = new HashMap<String, List<String>>();
			Set<String> keys = paramMap.keySet();
			for (String key : keys) {
				//Each parameter will be passed only once.
				String val = paramMap.get(key)[0];
				attribMap.put(key, Arrays.asList(val));
				logger.debug("Key:"+key+ " value:"+val);
			}
			String token=attribMap.get("accessToken").get(0);
			url += "&popup=true";
			redirectToFileDisplayPage(request, response, url, token, attribMap,tenantId);
			return;
		}
	}

	public boolean isValidToken(String token, String expiryTime, RMSUserPrincipal userPrincipal) {
		try {
			if(token==null || expiryTime==null || userPrincipal==null){
				return false;
			}
			if((System.currentTimeMillis() / 1000)>Long.parseLong(expiryTime)){
				return false;
			}
		} catch (Exception e) {
			logger.error("Error occured while checking error expiry.",e);
			return false;
		}
		return true;
	}

	private void redirectToFileDisplayPage(HttpServletRequest request,
			HttpServletResponse response, String url, String token, Map<String, List<String>> userAttributes,String tenantId) {
		RMSUserPrincipal principal =new RMSUserPrincipal();
		String displayName=userAttributes.get("DisplayName").get(0);
		String sid=userAttributes.get("SID").get(0);
		if(displayName!=null){
			principal.setUserName(displayName);
		}
		if(sid!=null){
			principal.setUid(sid);
		}
		principal.setTenantId(tenantId);
		principal.setUserAttributes(userAttributes);
		principal.setLoginContext(RMS_SHAREPOINT);
		logger.debug("Created a new user principal \n UseName: "+principal.getUserName()+"/n SID: "+principal.getUid()+"/n LoginCOntext: "+principal.getLoginContext());
		AuthFilter.addSessionParameters(request.getSession(), principal);
		request.getSession().setAttribute(ConfigManager.SP_OAUTH_TOKEN, token);
		try {
			getServletContext().getRequestDispatcher(url).forward(request, response);
		} catch (Exception e) {
			logger.error("Error occurred while redirecting to Display File Page", e);
		}
	}
}
