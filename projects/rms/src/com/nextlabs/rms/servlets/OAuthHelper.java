package com.nextlabs.rms.servlets;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.dto.repository.AuthorizedRepoUserDTO;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepositoryManager;

public class OAuthHelper {
	
	private static final Logger logger = Logger.getLogger(OAuthHelper.class);
	
	public static final String REDIRECT_URL_MANAGE_REPOSITORIES="/index.jsp#/home/manageRepositories";
	
	public static final String REDIRECT_URL_REPOSITORIES="/index.jsp#/home/repositories/";
	
	public static final int NO_REDIRECTION = 0;

	public static String addRecordToDB(String repoName, String tenantId, String creatorId, String repoAccountId,
			String repoType, boolean isShared, String userAccessToken, String authUserId, String userAccountName, String userAccountId, int redirectCode,
			long repoId) {
		String redirectUri;
		AuthorizedRepoUserDTO authUser = new AuthorizedRepoUserDTO();
        authUser.setAccountName(userAccountName);
        authUser.setRefreshToken(userAccessToken);
        authUser.setUserId(authUserId);
        authUser.setTenantId(tenantId);
        authUser.setAccountId(userAccountId);
        
        RepositoryManager repoMgr= RepositoryManager.getInstance();
        RepositoryDTO repo=new RepositoryDTO();
        if(!isShared){
	        if(redirectCode == NO_REDIRECTION){
		        createRepositoryDTO(repoName, tenantId, creatorId, repoAccountId, repoType, isShared, authUser, repo);
		        try{
		        	repoMgr.addRepository(repo);
		        	String msg = RMSMessageHandler.getClientString("repoAddedSuccessfully",ServiceProviderSetting.getProviderTypeDisplayName(repoType));
		        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + REDIRECT_URL_MANAGE_REPOSITORIES+ "?success="+ msg;
	//	        	GetFilesWithPathCommand.clearSessionRedirectParameters(request);
		        	return redirectUri;
		        }
		        catch (RepositoryAlreadyExists rae){
		        	logger.error("Repository already exists", rae);
		        	String msg = RMSMessageHandler.getClientString("repoAlreadyExists",ServiceProviderSetting.getProviderTypeDisplayName(repoType));
		        	redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + REDIRECT_URL_MANAGE_REPOSITORIES + "?error=" + msg;
		        	return redirectUri;
		        }
		        catch (DuplicateRepositoryNameException e) {
					logger.error("Duplicate Repository name:" + repo.getRepoName(), e);
					String key = repo.isShared() ? "string_shared" : "string_personal";
					String msg = RMSMessageHandler.getClientString("error_duplicate_repo_name", RMSMessageHandler.getClientString(key), repo.getRepoName());
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error=" + msg;
					return redirectUri;
				}
	        }else{
				//Handle personal repository reauthentication here
				authUser.setRepoId(repoId);
				boolean added = RepositoryManager.getInstance().addAuthRepoUserToDB(authUser);
				if(added){
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME+REDIRECT_URL_REPOSITORIES+repoId;
				}else{
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME+ REDIRECT_URL_MANAGE_REPOSITORIES +"?error="+RMSMessageHandler.getClientString("inaccessibleRepository");
				}
			}
        }else{
        	if(redirectCode == NO_REDIRECTION){
				createRepositoryDTO(repoName, tenantId, creatorId, repoAccountId, repoType, isShared, authUser, repo);
				redirectUri = RepositoryManager.getInstance().addRepoToDBAndFetchRedirectURL(repo);
//					String msg = RMSMessageHandler.getClientString("repoAddedSuccessfully", RepositoryFactory.getRepoTypeDisplayName(RepositoryFactory.REPOTYPE_SPONLINE));
//					redirectURI = ConfigManager.RMS_CONTEXT_NAME + "/index.jsp#/repoAdmin?success=" + msg;
			}else{
	        	authUser.setRepoId(repoId);
	        	//Handle personal repository reauthentication here
				boolean added = RepositoryManager.getInstance().addAuthRepoUserToDB(authUser);
				if(added){
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME+REDIRECT_URL_REPOSITORIES+repoId;
				}else{
					redirectUri = GlobalConfigManager.RMS_CONTEXT_NAME+ REDIRECT_URL_MANAGE_REPOSITORIES +"?error="+RMSMessageHandler.getClientString("inaccessibleRepository");
				}
			}
//			GetFilesWithPathCommand.clearSessionRedirectParameters(request);
        }
        return redirectUri;
	}

	public static void createRepositoryDTO(String repoName, String tenantId, String creatorId, String repoAccountId,
			String repoType, boolean isShared, AuthorizedRepoUserDTO authUser, RepositoryDTO repo) {
		repo.setRepoType(repoType);
		repo.setRepoName(repoName);
		repo.setShared(isShared);
		repo.setTenantId(tenantId);
		repo.setCreatedByUserId(creatorId);

		repo.setRepoAccountId(repoAccountId);
		repo.setAuthorizedUser(authUser);
	}
}
