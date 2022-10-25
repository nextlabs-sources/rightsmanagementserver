package com.nextlabs.rms.repository.dropbox;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxRequestConfig.Builder;
import com.dropbox.core.util.IOUtil.ReadException;
import com.dropbox.core.util.IOUtil.WrappedException;
import com.dropbox.core.util.IOUtil.WriteException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.users.FullAccount;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.command.GetFilesWithPathCommand;
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
import com.nextlabs.rms.servlets.OAuthHelper;

public class DropBoxAuthHelper {
	
    private static Logger logger = Logger.getLogger(DropBoxAuthHelper.class); 
	
	private static DbxAppInfo initDbxAppInfo(String tenantId) {
		DbxAppInfo dbxAppInfo = null;
		try {
			Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId)
					.getServiceProviderMap();
			ServiceProviderSetting dropboxSetting = latestRepoSettingMap.get(ServiceProviderType.DROPBOX.name());
			String appKey = null;
			String appSecret = null;
			if (dropboxSetting != null) {
				appKey = dropboxSetting.getAttributes().get(ServiceProviderSetting.APP_ID);
				appSecret = dropboxSetting.getAttributes().get(ServiceProviderSetting.APP_SECRET);
			}
			if (appKey == null || appKey.length() == 0 || appSecret == null || appSecret.length() == 0) {
				throw new Exception("App Key or App Secret is not specified for DropBox integration");
			}
			StringBuilder sb = new StringBuilder();
			sb.append("{\"key\":\"");
			sb.append(appKey);
			sb.append("\",\"secret\":\"");
			sb.append(appSecret);
			sb.append("\"}");
			dbxAppInfo = DbxAppInfo.Reader.readFully(sb.toString());
		} catch (Exception e) {
			logger.error("Error loading App Info for DropBox: " + e.getMessage(), e);
		}
		return dbxAppInfo;
	}

	private static DbxWebAuth getWebAuth(final HttpServletRequest request) throws Exception
    {
        // After we redirect the user to the Dropbox website for authorization,
        // Dropbox will redirect them back here.
		
		RMSUserPrincipal user=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		String tenantId = user.getTenantId();
		DbxAppInfo dbxAppInfo = initDbxAppInfo(tenantId);
		
		Map<String, ServiceProviderSetting> latestRepoSettingMap = ConfigManager.getInstance(tenantId).getServiceProviderMap();
		ServiceProviderSetting dropboxSetting = latestRepoSettingMap.get(ServiceProviderType.DROPBOX.name());
		String redirectURL = null;
		if(dropboxSetting != null){
			redirectURL = dropboxSetting.getAttributes().get(ServiceProviderSetting.REDIRECT_URL);		
		}
		if(redirectURL==null||redirectURL.length()==0){
			throw new RMSDropboxException(RMSMessageHandler.getClientString("repoRedirectURLErr",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name())));
		}
		if(dbxAppInfo==null){
			throw new RMSDropboxException(RMSMessageHandler.getClientString("repoAppKeyErr",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name())));
		}
		if(redirectURL.endsWith("/")){
			redirectURL = redirectURL.substring(0, redirectURL.length()-1);
		}
		redirectURL = redirectURL + GlobalConfigManager.RMS_CONTEXT_NAME + "/" + RepoConstants.DROPBOX_AUTH_FINSIH_URL;
        // Select a spot in the session for DbxWebAuth to store the CSRF token.
        HttpSession session = request.getSession(true);
        String sessionKey = "dropbox-auth-csrf-token";
        DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, sessionKey);
        return new DbxWebAuth(getRequestConfig(request), dbxAppInfo, redirectURL, csrfTokenStore);
    }
	
    private static DbxRequestConfig getRequestConfig(HttpServletRequest request)
    {
    	 Builder builder = DbxRequestConfig.newBuilder(RepoConstants.RMS_CLIENT_IDENTIFIER);
         builder.withUserLocaleFrom(request.getLocale());
         return builder.build();
    }

	public static String startDBAuth(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String name=request.getParameter("name");
		// Start the authorization process with Dropbox.
        String authorizeUrl = getWebAuth(request).start("name="+name);
        return authorizeUrl;
//        response.sendRedirect(authorizeUrl);
	}

	@SuppressWarnings("unchecked")
	public static String finishDBAuth(HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		String repoName="";
		String redirectUri = "";
		if (!checkGet(request, response)) return "";
        DbxAuthFinish authFinish;
        try {
            authFinish = getWebAuth(request).finish(request.getParameterMap());
            repoName=authFinish.getUrlState();
            repoName=repoName.substring(repoName.lastIndexOf("=")+1);
            logger.debug(authFinish.getUrlState());
        }
        catch (DbxWebAuth.BadRequestException e) {
            logger.error("On /dropbox-auth-finish: Bad request: " + e.getMessage());
        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +
        			"?error="+RMSMessageHandler.getClientString("errAddRepo",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
        	return redirectUri;
        }
        catch (DbxWebAuth.BadStateException e) {
        	logger.error(e.getMessage(), e);
        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES + 
        			"?error="+RMSMessageHandler.getClientString("errAddRepo",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
        	return redirectUri;
        }
        catch (DbxWebAuth.CsrfException e) {
        	logger.error(e.getMessage(), e);
        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +
        			"?error="+RMSMessageHandler.getClientString("errAddRepo",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
        	return redirectUri;
        }
        catch (DbxWebAuth.NotApprovedException e) {
        	logger.error(e.getMessage(), e);
        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +
        			"?error="+RMSMessageHandler.getClientString("repoUnauthorizedAccess",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
        	return redirectUri;
        }
        catch (DbxWebAuth.ProviderException e) {
        	logger.error(e.getMessage(), e);
        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +
        			"?error="+RMSMessageHandler.getClientString("errAddRepo",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
            return redirectUri;
        }
        catch (DbxException e) {
        	logger.error(e.getMessage(), e);
        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES +
        			"?error="+RMSMessageHandler.getClientString("errAddRepo",ServiceProviderSetting.getProviderTypeDisplayName(ServiceProviderType.DROPBOX.name()));
        	return redirectUri;
        }
        // We have an Dropbox API access token now.  This is what will let us make Dropbox API
        // calls.  Save it in the database entry for the current user.
        String accessToken = authFinish.getAccessToken();
//        logger.debug("access token is "+accessToken);
        //String state = authFinish.urlState;
        logger.info("Access Token received from DropBox...");
        RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
        Builder builder = DbxRequestConfig.newBuilder(RepoConstants.RMS_CLIENT_IDENTIFIER);
        builder.withUserLocaleFrom(Locale.getDefault());
        DbxRequestConfig config = builder.build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);
        FullAccount account = client.users().getCurrentAccount();
         
        int redirectCode = (request.getSession().getAttribute("redirectCode")!=null)?(int) request.getSession().getAttribute("redirectCode"):0;
        long repoId = (request.getSession().getAttribute("repoId")!=null)?(long) request.getSession().getAttribute("repoId"):0;

        String accountName = account.getEmail();
        String accountId = ""+authFinish.getUserId();
        String tenantId = userPrincipal.getTenantId();
        String creatorId = userPrincipal.getUid();
        String repoAccountId =	account.getEmail();
        String repoType = ServiceProviderType.DROPBOX.name();

        redirectUri = OAuthHelper.addRecordToDB(repoName, tenantId, creatorId, repoAccountId, repoType, false, accessToken, creatorId, accountName, accountId, redirectCode, repoId);
    	GetFilesWithPathCommand.clearSessionRedirectParameters(request);
        return redirectUri;
      }
	
	private static boolean checkGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		if (!request.getMethod().equals("GET")) {
			response.sendError(405); // 405 - Incorrect method.
			return false;
		}
		return true;
	}

	public static void handleException(Exception e) throws RepositoryException, RMSException {
		if (e instanceof DbxException) {
			if (e instanceof InvalidAccessTokenException) {
				throw new InvalidTokenException(e.getMessage(), e);
			} else if (e instanceof NetworkIOException) {
				throw new com.nextlabs.rms.repository.exception.IOException(e.getMessage(), e);
			}
			throw new RepositoryException(e.getMessage(), e);
		} else if (e instanceof WrappedException) {
			if (e instanceof ReadException || e instanceof WriteException) {
				throw new com.nextlabs.rms.repository.exception.IOException(e.getMessage(), e);
			}
			throw new RepositoryException(e.getMessage(), e);
		} else if (e instanceof DownloadErrorException ) {
			throw new FileNotFoundException(e.getMessage(), e);
		} else if (e instanceof RepositoryException) {
			throw (RepositoryException) e;
		} else if (e instanceof RMSException) {
			throw (RMSException) e;
		} else {
			throw new RMSException(e.getMessage(), e);
		}
	}
}
