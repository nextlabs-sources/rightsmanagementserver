package com.nextlabs.rms.repository.box;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxUser;
import com.box.sdk.BoxUser.Info;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;

import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepoConstants;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.servlets.OAuthHelper;


public class BoxAuthHelper {

	private static Logger logger = Logger.getLogger(BoxAuthHelper.class); 

	public static String startAuth(HttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, RMSException {
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(userPrincipal.getTenantId()).getServiceProviderMap();
		ServiceProviderSetting setting = latestRepoSettingMap.get(ServiceProviderType.BOX.name());
		String displayName = request.getParameter("name");
		return getAuthorizationUrl(setting, request, generateStateToken(displayName, userPrincipal.getPrincipalName()));
	}

	private static String getAuthorizationUrl(ServiceProviderSetting setting, HttpServletRequest request, String state
			) throws URISyntaxException, RMSException {
		String appKey = setting.getAttributes().get(ServiceProviderSetting.APP_ID);
		URL authorizationURL = BoxAPIConnection.getAuthorizationURL(appKey, getRedirectURL(setting, request), state, null);
		return authorizationURL.toString();
	}

	private static String generateStateToken(String displayName, String accountId) {
		JsonObject state = new JsonObject();
		SecureRandom sr1 = new SecureRandom();
		state.addProperty("hash", "box;" + sr1.nextInt());
		state.addProperty("displayName", displayName);
		state.addProperty("accountId", accountId);
		return state.toString();
	}
	private static URI getRedirectURL(ServiceProviderSetting setting, HttpServletRequest request) throws URISyntaxException, RMSException {
		String redirectURL = null;
		if (setting != null) {
			redirectURL = setting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL);
		}
		if (redirectURL == null || redirectURL.trim().length() == 0) {
			throw new RMSException(RMSMessageHandler.getClientString("repoRedirectURLErr",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.GOOGLE_DRIVE.name())));
		}
		if (redirectURL.endsWith("/")) {
			redirectURL = redirectURL.substring(0, redirectURL.length() - 1);
		}
		StringBuilder uriBuilder = new StringBuilder(redirectURL);
		uriBuilder.append(GlobalConfigManager.RMS_CONTEXT_NAME).append('/').append(RepoConstants.BOX_AUTH_FINSIH_URL);
		return new URI(uriBuilder.toString());
	}

	public static String finishAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authCode = request.getParameter("code");
		String error = request.getParameter("error");
		String state = request.getParameter("state");
		String redirectUri = "";
		String displayName = null;
		if (state != null) {
			Gson gson = new Gson();
			JsonElement element = gson.fromJson(state, JsonElement.class);
			JsonObject jsonState = element.getAsJsonObject();
			displayName = jsonState.get("displayName").getAsString();
			logger.debug("displayName" + displayName);
		}
		if (authCode == null || error != null) {
			logger.error("You are not authorized to access the Google Drive account: " + error);
			redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME +  OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="
					+ RMSMessageHandler.getClientString("repoUnauthorizedAccess", ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.BOX.name()));
			return redirectUri;
		}
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);

		String tenantId = userPrincipal.getTenantId();
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting setting = latestRepoSettingMap.get(ServiceProviderType.BOX.name());
		BoxAPIConnection connection = getOAuthTokens(setting, authCode);
		BoxUser userInfo = BoxUser.getCurrentUser(connection);
		Info info = userInfo.getInfo();
		String repoName = displayName != null ? displayName : info.getName();
		String accountId = userInfo.getID();
		String accountName = info.getLogin();
		String token = connection.getRefreshToken();
		boolean isShared = false;
		String creatorId = userPrincipal.getUid();
		int redirectCode = (request.getSession().getAttribute("redirectCode")!=null)?(int) request.getSession().getAttribute("redirectCode"):0;
		long repoId = (request.getSession().getAttribute("repoId")!=null)?(long) request.getSession().getAttribute("repoId"):0;
		String repoType = ServiceProviderType.BOX.name();
		String repoAccountId = info.getLogin();
		redirectUri = OAuthHelper.addRecordToDB(repoName, tenantId, creatorId, repoAccountId, repoType, isShared, token, creatorId, accountName, accountId, redirectCode, repoId);
		return redirectUri;
	}

	private static BoxAPIConnection getOAuthTokens(ServiceProviderSetting setting, String authCode)
			throws IOException {
		String appKey = setting.getAttributes().get(ServiceProviderSetting.APP_ID);
		String appSecret = setting.getAttributes().get(ServiceProviderSetting.APP_SECRET);
		return new BoxAPIConnection(appKey, appSecret, authCode);
	}

	public static void handleException(Exception e) throws RepositoryException, RMSException {
		if (e instanceof BoxAPIException) {
			BoxAPIException err = (BoxAPIException) e;
			int responseCode = err.getResponseCode();
			String error = err.getResponse();
			if (responseCode == 400) {
				if (error.contains("invalid_grant") || error.contains("unauthorized_client")) {
					throw new InvalidTokenException(error, e);
				}
			} else if (responseCode == 404) {
				throw new com.nextlabs.rms.repository.exception.FileNotFoundException(error, e);
			}
		} else if (e instanceof RepositoryException) {
			throw (RepositoryException) e;
		} else if (e instanceof RMSException) {
			throw (RMSException) e;
		} else {
			throw new RMSException(e.getMessage(), e);
		}

	}
}
