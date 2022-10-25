package com.nextlabs.rms.repository.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepoConstants;
import com.nextlabs.rms.repository.box.BoxAuthHelper;
import com.nextlabs.rms.repository.dropbox.DropBoxAuthHelper;
import com.nextlabs.rms.repository.dropbox.RMSDropboxException;
import com.nextlabs.rms.repository.googledrive.GoogleDriveOAuthHandler;
import com.nextlabs.rms.repository.onedrive.OneDriveOAuthHandler;
import com.nextlabs.rms.repository.sharepointrest.SharePointOnlineOAuthHandler;
import com.nextlabs.rms.servlets.OAuthHelper;

public class OAuthManagerServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3092566310043076819L;
	private static final Logger logger = Logger.getLogger(OAuthManagerServlet.class);
	public static final int INVALID_REPOSITORY_REDIRECTION = 1;
	public static final int UNAUTHORIZED_REPOSITORY_REDIRECTION = 2;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		String uri = request.getRequestURI();
		try{
			String repoType = getRepoType(uri);
			if(repoType.equalsIgnoreCase(ServiceProviderType.SHAREPOINT_ONLINE.name())){	
				String redirectUri = handleSharePointOAuth(request, response, uri);
				response.sendRedirect(redirectUri);
				return;
			}else if(repoType.equalsIgnoreCase(ServiceProviderType.DROPBOX.name())){
				String redirectUri = handleDropboxOAuth(request, response, uri);
				response.sendRedirect(redirectUri);
			}else if(repoType.equalsIgnoreCase(ServiceProviderType.GOOGLE_DRIVE.name())){
				String redirectUri = handleGoogleDriveOAuth(request, response, uri);
				response.sendRedirect(redirectUri);
			}else if(repoType.equalsIgnoreCase(ServiceProviderType.ONE_DRIVE.name())){ 
				String redirectUri = handleOneDriveOAuth(request, response, uri);
				response.sendRedirect(redirectUri);
			}else if(repoType.equalsIgnoreCase(ServiceProviderType.BOX.name())){ 
				String redirectUri = handleBoxOAuth(request, response, uri);
				response.sendRedirect(redirectUri);
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
	}


	private String handleOneDriveOAuth(HttpServletRequest request, HttpServletResponse response, String uri) {
		String redirectUri = "";
		if (uri.endsWith(RepoConstants.ONE_DRIVE_AUTH_START_URL)) {
			try {
				redirectUri = OneDriveOAuthHandler.startAuth(request, response);
				return redirectUri;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME +OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="
							+ RMSMessageHandler.getClientString("repoRedirectURLErr", 
									ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.ONE_DRIVE.name()));
				return redirectUri;
			}
		} else if (uri.endsWith(RepoConstants.ONE_DRIVE_AUTH_FINISH_URL)) {
			try {
				redirectUri = OneDriveOAuthHandler.finishAuth(request, response);
				return redirectUri;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return redirectUri;
	}
	
	private String handleBoxOAuth(HttpServletRequest request, HttpServletResponse response, String uri) {
		String redirectUri = "";
		if (uri.endsWith(RepoConstants.BOX_AUTH_START_URL)) {
			try {
				redirectUri = BoxAuthHelper.startAuth(request, response);
				return redirectUri;
			} catch (Exception e) {
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME +OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +"?error="
							+ RMSMessageHandler.getClientString("repoRedirectURLErr", 
									ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.BOX.name()));
				return redirectUri;
			}
		} else if (uri.endsWith(RepoConstants.BOX_AUTH_FINSIH_URL)) {
			try {
				redirectUri = BoxAuthHelper.finishAuth(request, response);
				return redirectUri;
			} catch (Exception e) {
				logger.error("Error occurred while finishing Box Auth: " + e.getMessage(), e);
			}
		}
		return redirectUri;
	}


	private String handleGoogleDriveOAuth(HttpServletRequest request, HttpServletResponse response, String uri) {
		String redirectUri = "";
		if (uri.endsWith(RepoConstants.GOOGLE_DRIVE_AUTH_START_URL)) {
			try {
				redirectUri = GoogleDriveOAuthHandler.startGDAuth(request, response);
				return redirectUri;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error="
						+ RMSMessageHandler.getClientString("repoRedirectURLErr",
								ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.GOOGLE_DRIVE.name()));
				return redirectUri;
			}
		} else if (uri.endsWith(RepoConstants.GOOGLE_DRIVE_AUTH_FINISH_URL)) {
			try {
				redirectUri = GoogleDriveOAuthHandler.finishGDAuth(request, response);
				return redirectUri;
			} catch (Exception e) {
				logger.error("Error occurred while finishing Google Drive Auth: " + e.getMessage(), e);
			}
		}
		return redirectUri;
	}


	private String handleDropboxOAuth(HttpServletRequest request, HttpServletResponse response, String uri) {
		
		String redirectUrl = GlobalConfigManager.RMS_CONTEXT_NAME + 
				OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error="+RMSMessageHandler.getClientString("repoRedirectURLErr",
						ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
		if(uri.endsWith(RepoConstants.DROPBOX_AUTH_START_URL)){
			try {		
				redirectUrl = DropBoxAuthHelper.startDBAuth(request, response);
				return redirectUrl;
			}
			catch(RMSDropboxException e){
				 logger.error(e);
				 redirectUrl = GlobalConfigManager.RMS_CONTEXT_NAME + 
						 OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error="+e.getMessage();
			    return redirectUrl;				
			}catch (Exception e) {
				logger.error(e);
					redirectUrl = GlobalConfigManager.RMS_CONTEXT_NAME + 
							OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error="+RMSMessageHandler.getClientString("repoRedirectURLErr",
									ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
		        return redirectUrl;
			}
		}else if(uri.endsWith(RepoConstants.DROPBOX_AUTH_FINSIH_URL)){
			try {
				redirectUrl = DropBoxAuthHelper.finishDBAuth(request, response);
				return redirectUrl;
			} catch (Exception e) {
				logger.error("Error occurred while finishing DropBox Auth: "+ e.getMessage(), e);
				return redirectUrl;
			}
		}
		return redirectUrl;
	}


	private String handleSharePointOAuth(HttpServletRequest request, HttpServletResponse response, String uri)
			throws IOException, UnsupportedEncodingException {
		String redirectUri="";
		if(uri.endsWith(RepoConstants.SHAREPOINT_ONLINE_AUTH_START_URL)){
			redirectUri = SharePointOnlineOAuthHandler.startAuthRequest(request, response);
		}else if(uri.endsWith(RepoConstants.SHAREPOINT_ONLINE_AUTH_FINISH_URL)){
			redirectUri = SharePointOnlineOAuthHandler.finishAuthRequest(request, response);
		}
		return redirectUri;
	}


	private String getRepoType(String uri) {
		if(uri.endsWith(RepoConstants.DROPBOX_AUTH_START_URL) || uri.endsWith(RepoConstants.DROPBOX_AUTH_FINSIH_URL)){
			return ServiceProviderType.DROPBOX.name();
		}else if(uri.endsWith(RepoConstants.SHAREPOINT_ONLINE_AUTH_START_URL) || uri.endsWith(RepoConstants.SHAREPOINT_ONLINE_AUTH_FINISH_URL)){
			return  ServiceProviderType.SHAREPOINT_ONLINE.name();
		}else if(uri.endsWith(RepoConstants.GOOGLE_DRIVE_AUTH_START_URL) || uri.endsWith(RepoConstants.GOOGLE_DRIVE_AUTH_FINISH_URL)){
			return  ServiceProviderType.GOOGLE_DRIVE.name();
		}else if(uri.endsWith(RepoConstants.ONE_DRIVE_AUTH_START_URL) || uri.endsWith(RepoConstants.ONE_DRIVE_AUTH_FINISH_URL)){
			return ServiceProviderType.ONE_DRIVE.name();
		}else if(uri.endsWith(RepoConstants.BOX_AUTH_START_URL) || uri.endsWith(RepoConstants.BOX_AUTH_FINSIH_URL)){
			return ServiceProviderType.BOX.name();
		}
		return null;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response){
		doGet(request, response);
	}
}
