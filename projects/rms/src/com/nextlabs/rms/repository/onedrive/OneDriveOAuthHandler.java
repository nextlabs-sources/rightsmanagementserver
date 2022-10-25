package com.nextlabs.rms.repository.onedrive;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepoConstants;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;
import com.nextlabs.rms.repository.onedrive.OneDriveOwner.OneDriveUser;
import com.nextlabs.rms.repository.onedrive.OneDrivePersonalClient.OneDriveResponseHandler;
import com.nextlabs.rms.servlets.OAuthHelper;
import com.nextlabs.rms.sharedutil.RestletUtil;
import com.nextlabs.rms.util.StringUtils;

public class OneDriveOAuthHandler {

	private static final Logger logger = Logger.getLogger(OneDriveOAuthHandler.class);

	private static final String SCOPES = "wl.signin wl.offline_access onedrive.readwrite";

	private static final String ONE_DRIVE_CODE_URL = "https://login.live.com/oauth20_authorize.srf";
	private static final String ONE_DRIVE_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
	private static final String ONE_DRIVE_API_URL = "https://api.onedrive.com/v1.0";

	private static class OneDriveAppInfo {
		String clientId;
		String clientSecret;
		String redirectUrl;
		
		OneDriveAppInfo(String clientId, String clientSecret, String redirectUrl) {
			this.clientId = clientId;
			this.clientSecret = clientSecret;
			this.redirectUrl = redirectUrl;
		}
		
	}
	
	private static OneDriveAppInfo initOneDriveAppInfo(String tenantId) throws Exception {
		try {
			String clientId = null;
			String clientSecret = null;
			String redirectUrl = null;
			Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
			ServiceProviderSetting oneDriveSetting = latestRepoSettingMap.get(ServiceProviderType.ONE_DRIVE.name());
			if(oneDriveSetting != null){
				clientId = oneDriveSetting.getAttributes().get(ServiceProviderSetting.APP_ID);
				clientSecret = oneDriveSetting.getAttributes().get(ServiceProviderSetting.APP_SECRET);
				redirectUrl = oneDriveSetting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL);				
			}
			if (clientId == null || clientId.length() == 0 || clientSecret == null || clientSecret.length() == 0
					|| redirectUrl == null || redirectUrl.length() == 0) {
				throw new Exception("App Key or App Secret or Redirect URL is not specified for OneDrive integration.");
			}

			if (redirectUrl.endsWith("/")) {
				redirectUrl = redirectUrl.substring(0, redirectUrl.length() - 1);
			}
			redirectUrl = redirectUrl + GlobalConfigManager.RMS_CONTEXT_NAME + "/" + RepoConstants.ONE_DRIVE_AUTH_FINISH_URL;
			return new OneDriveAppInfo(clientId, clientSecret, redirectUrl);
		} catch (Exception e) {
			logger.error("Error loading App Info for OneDrive: " + e.getMessage(), e);
			throw e;
		}
	}

	public static String startAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		OneDriveAppInfo info = initOneDriveAppInfo(userPrincipal.getTenantId());
		String repoName = request.getParameter("name");
		String state = generateStateToken() + ";" + repoName;
		ClientResource cr = new ClientResource(ONE_DRIVE_CODE_URL);
		cr.addQueryParameter("client_id", info.clientId);
		cr.addQueryParameter("scope", SCOPES);
		cr.addQueryParameter("response_type", "code");
		cr.addQueryParameter("redirect_uri", info.redirectUrl);
		cr.addQueryParameter("state", state);
		String redirectUri = cr.getRequest().getResourceRef().toString();
		return redirectUri;
	}

	public static String finishAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		String error = request.getParameter("error");
		String redirectUri = "";
		if (!StringUtils.hasLength(code) || error != null) {
			logger.warn("You are not authorized to access the One Drive account: " + error);
			redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME +OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="
					+ RMSMessageHandler.getClientString("repoUnauthorizedAccess",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.ONE_DRIVE.name()));
			return redirectUri;
		}
		String repoName = null;
		if (state != null && state.length() > 0) {
			int idx = state.lastIndexOf(";");
			if (idx >= 0) {
				repoName = state.substring(idx + 1);
			}
		}
		
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		OneDriveTokenResponse tokenResponse = getTokens(userPrincipal.getTenantId(), code);
		if(tokenResponse == null) {
			throw new InvalidTokenException("Token Response is null.");
		}
		
		Map<String,Object> attrs = new HashMap<>();
		attrs.put(RepositoryManager.ACCESS_TOKEN, tokenResponse.getAccessToken());
		attrs.put(RepositoryManager.REFRESH_TOKEN, tokenResponse.getRefreshToken());
		long now = System.currentTimeMillis()/1000;
		attrs.put(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME, tokenResponse.getExpiresInSeconds() + now);
		
		String accessToken = tokenResponse.getAccessToken();
		logger.info("Access Token received from OneDrive ...");
		
		String refreshToken = tokenResponse.getRefreshToken();
		logger.info("Refresh Token received from OneDrive ...");
		
		OneDriveUser user = getUser(accessToken);
		if(user == null) {
			throw new InvalidTokenException("Failed to get user info with the provided access token.");
		}
		logger.info("User info received from OneDrive ...");
		
		int redirectCode = (request.getSession().getAttribute("redirectCode")!=null)?(int) request.getSession().getAttribute("redirectCode"):0;
        long repoId = (request.getSession().getAttribute("repoId")!=null)?(long) request.getSession().getAttribute("repoId"):0;
        String accName = user.getDisplayName();
        String authUserAccountId = user.getId();
        String repoDiplayName = user.getDisplayName(); 
        String tenantId = userPrincipal.getTenantId();
        String creatorId = userPrincipal.getUid();
        //repoAccountId is same as userAccountId
        redirectUri = OAuthHelper.addRecordToDB(repoName, tenantId, creatorId, repoDiplayName, ServiceProviderType.ONE_DRIVE.name(), 
        		false, refreshToken, creatorId, accName, authUserAccountId, redirectCode, repoId);
        return redirectUri;
	}

	private static OneDriveTokenResponse getTokens(String tenantId, String code) throws Exception{
		
		OneDriveAppInfo info = initOneDriveAppInfo(tenantId);
		
		Form form = new Form();
		form.add("client_id", info.clientId);
		form.add("client_secret", info.clientSecret);
		form.add("code", code);
		form.add("redirect_uri", info.redirectUrl);
		form.add("grant_type", "authorization_code");
		String queryString = "?" + form.getQueryString();
		OneDriveTokenResponse tokenResponse = null;
		try {
			tokenResponse = RestletUtil.sendRequest(ONE_DRIVE_TOKEN_URL + queryString, Method.POST, form.getWebRepresentation(CharacterSet.UTF_8), null,
					new OneDriveResponseHandler<>(OneDriveTokenResponse.class));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			handleException(e);
		}
		return tokenResponse;
	}
	
	private static OneDriveUser getUser(String accessToken) throws RepositoryException, RMSException{
		Form form = new Form();
		form.add("access_token", accessToken);
		String queryString = "?" + form.getQueryString();
		OneDriveUser user = null;
		try {
			OneDriveDriveResponse drive = RestletUtil.sendRequest(ONE_DRIVE_API_URL+"/drive"+queryString, Method.GET, form.getWebRepresentation(CharacterSet.UTF_8), null,
					new OneDriveResponseHandler<>(OneDriveDriveResponse.class));
			user = drive.getOwner().getUser();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			handleException(e);
		}
		return user;
	}
	
	private static String generateStateToken() {
		SecureRandom sr1 = new SecureRandom();
		String stateToken = "rms_onedrive;" + sr1.nextLong();
		return stateToken;
	}

	public static void handleException(Exception e) throws RepositoryException, RMSException {
		if (e instanceof OneDriveServiceException) {
			OneDriveServiceException ex = (OneDriveServiceException) e;
			String errorMsg = ex.getMessage();
			int statusCode = ex.getStatusCode();
			OneDriveErrorResponse error = ex.getError();
			if (error != null) {
				OneDriveError oneDriveError = error.getError();
				if (oneDriveError != null) {
					errorMsg = oneDriveError.getMessage();
				}
			}
			if (statusCode == 401) {
				throw new UnauthorizedRepositoryException(errorMsg);
			} else if (statusCode == 404) {
				throw new com.nextlabs.rms.repository.exception.FileNotFoundException(errorMsg);
			}
			throw new RepositoryException(e.getMessage(), e);
		} else if (e instanceof RepositoryException) {
			throw (RepositoryException) e;
		} else if (e instanceof RMSException) {
			throw (RMSException) e;
		} else {
			throw new RMSException(e.getMessage(), e);
		}
	}
}
