package com.nextlabs.rms.repository.googledrive;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.util.ObjectUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepoConstants;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;
import com.nextlabs.rms.servlets.OAuthHelper;
import com.nextlabs.rms.util.Nvl;

public class GoogleDriveOAuthHandler {

	private static final Logger logger = Logger.getLogger(GoogleDriveOAuthHandler.class);

	public static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE, "email", "profile");
	
	/**
	 * Build an authorization flow and store it as a static class attribute.
	 *
	 * @return GoogleAuthorizationCodeFlow instance.
	 * @throws IOException
	 *           Unable to load client_secret.json.
	 */
	private static GoogleAuthorizationCodeFlow getFlow(String tenantId) throws IOException {
		GoogleAuthorizationCodeFlow flow = null;
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, getAppKey(tenantId), getAppSecret(tenantId), SCOPES)
				.setAccessType("offline").setApprovalPrompt("auto").build();
		return flow;
	}

	/**
	 * Retrieve the authorization URL.
	 *
	 * @param emailAddress
	 *          User's e-mail address.
	 * @param state
	 *          State for the authorization URL.
	 * @return Authorization URL to redirect the user to.
	 * @throws IOException
	 *           Unable to load client_secret.json.
	 * @throws RMSException
	 */
	private static String getAuthorizationUrl(String tenantId, String state) throws IOException, RMSException {
		String redirectURL = getRedirectURL(tenantId);
		GoogleAuthorizationCodeRequestUrl urlBuilder = getFlow(tenantId).newAuthorizationUrl().setRedirectUri(redirectURL)
				.setApprovalPrompt("force").setState(state);
		return urlBuilder.build();
	}

	public static String startGDAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Start the authorization process with Google Drive.
		
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		String authorizeUrl = getAuthorizationUrl(userPrincipal.getTenantId(), generateStateToken(request));
		return authorizeUrl;
	}

	public static String finishGDAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String authCode = request.getParameter("code");
		String error = request.getParameter("error");
		String state = request.getParameter("state");
		String redirectUri = "";
		if (logger.isDebugEnabled()) {
			logger.debug("Auth code: " + authCode + ", state: " + state + ", error: " + Nvl.nvl(error));
		}
		String displayName = null;
		if (state != null) {
			int idx = state.lastIndexOf(";");
			if (idx >= 0) {
				displayName = state.substring(idx + 1);
			}
		}
		if (authCode == null || error != null) {
			logger.error("You are not authorized to access the Google Drive account: " + error);
			redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME +  OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="
					+ RMSMessageHandler.getClientString("repoUnauthorizedAccess", ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.GOOGLE_DRIVE.name()));
			return redirectUri;
		}		
		Credential credential = null;
		About about = null;
		
		RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		
		try {
			credential = getOAuthTokens(userPrincipal.getTenantId(), authCode);
			Drive service = getDrive(credential);
			about = service.about().get().execute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME +  OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error=";
			if (e instanceof UnauthorizedRepositoryException) {
				redirectUri += RMSMessageHandler.getClientString("repoUnauthorizedAccess",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.GOOGLE_DRIVE.name()));
			} else {
				redirectUri = redirectUri + RMSMessageHandler.getClientString("errAddRepo",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.GOOGLE_DRIVE.name()));
			}
			return redirectUri;
		}
		int redirectCode = (request.getSession().getAttribute("redirectCode")!=null)?(int) request.getSession().getAttribute("redirectCode"):0;
        long repoId = (request.getSession().getAttribute("repoId")!=null)?(long) request.getSession().getAttribute("repoId"):0;
        
        String creatorId = userPrincipal.getUid();
        String tenantId = userPrincipal.getTenantId();
        String repoName = displayName != null ? displayName : about.getName();String repoAccountId = about.getUser().getEmailAddress();
        String repoType = ServiceProviderType.GOOGLE_DRIVE.name();
        boolean isShared = false;
        String userAccessToken = credential.getRefreshToken();
        String userAccountName = about.getUser().getEmailAddress();
        
        redirectUri = OAuthHelper.addRecordToDB(repoName, tenantId, creatorId, repoAccountId, repoType, isShared, userAccessToken, creatorId, userAccountName, userAccountName, redirectCode, repoId);
        return redirectUri;
	}

	private static Credential getOAuthTokens(String tenantId, String authCode) throws RepositoryException, RMSException {
		GoogleTokenResponse response;
		Credential credential = null;
		try {
			String redirectURL = getRedirectURL(tenantId);
			response = getFlow(tenantId).newTokenRequest(authCode).setRedirectUri(redirectURL).execute();
			credential = buildCredential(tenantId).setFromTokenResponse(response);
		} catch (Exception e) {
			handleException(e);
		}
		return credential;
	}

	private static String getRedirectURL(String tenantId) throws RMSException {
		String url = null;
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting googleDriveSetting = latestRepoSettingMap.get(ServiceProviderType.GOOGLE_DRIVE.name());
		if(googleDriveSetting != null){
			url = googleDriveSetting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL);
		}
		
		if (url == null || url.trim().length() == 0) {
			throw new RMSException(RMSMessageHandler.getClientString("repoRedirectURLErr",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.GOOGLE_DRIVE.name())));
		}
		url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
		return url + GlobalConfigManager.RMS_CONTEXT_NAME + "/" + RepoConstants.GOOGLE_DRIVE_AUTH_FINISH_URL;
	}

	private static String getAppKey(String tenantId) {
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting googleDriveSetting = latestRepoSettingMap.get(ServiceProviderType.GOOGLE_DRIVE.name());
		if(googleDriveSetting != null){
			return googleDriveSetting.getAttributes().get(ServiceProviderSetting.APP_ID);
		}
		return null;
	}

	private static String getAppSecret(String tenantId) {
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting googleDriveSetting = latestRepoSettingMap.get(ServiceProviderType.GOOGLE_DRIVE.name());
		if(googleDriveSetting != null){
			return googleDriveSetting.getAttributes().get(ServiceProviderSetting.APP_SECRET);
		}
		return null;
	}
	
	public static Credential buildCredential(String tenantId) {
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY).setClientSecrets(getAppKey(tenantId), getAppSecret(tenantId)).build();
		return credential;
	}
	
	public static Drive getDrive(Credential credential) {
		Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(RepoConstants.RMS_CLIENT_IDENTIFIER).build();
		return drive;
	}

	public static void handleException(Exception e) throws RepositoryException, RMSException {
		if (e instanceof IOException) {
			if (e instanceof HttpResponseException) {
				HttpResponseException ex = (HttpResponseException) e;
				int statusCode = ex.getStatusCode();
				if (statusCode == 400) {
					if (e instanceof TokenResponseException) {
						TokenResponseException ex1 = (TokenResponseException) e;
						TokenErrorResponse details = ex1.getDetails();
						if (details != null) {
							String error = details.getError();
							String errorDescription = details.getErrorDescription();
							String[] errorCodes = new String[] { "invalid_grant", "unauthorized_client" };
							if (ObjectUtils.containsElement(errorCodes, error)) {
								throw new InvalidTokenException(errorDescription != null ? errorDescription : error, e);
							}
						}
					}
				} else if (statusCode == 401) {
					throw new UnauthorizedRepositoryException(e.getMessage(), ex);
				} else if (statusCode == 404) {
					String msg = e.getMessage();
					if (e instanceof GoogleJsonResponseException) {
						GoogleJsonResponseException ex1 = (GoogleJsonResponseException) e;
						GoogleJsonError details = ex1.getDetails();
						if (details != null) {
							msg = details.getMessage();
						}
					}
					throw new FileNotFoundException(msg, e);
				}
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

	/**
	 * Generates a secure state token
	 */
	private static String generateStateToken(HttpServletRequest request) {
		String displayName = request.getParameter("name");
		SecureRandom sr1 = new SecureRandom();
		String stateToken = "google;" + sr1.nextInt() + ";" + displayName;
		return stateToken;
	}
}
