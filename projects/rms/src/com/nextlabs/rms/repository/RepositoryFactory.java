package com.nextlabs.rms.repository;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.repository.exception.RepositoryException;


public class RepositoryFactory {
	
	public static final String[] allowedRepositories = {
			ServiceProviderType.DROPBOX.name(),
			ServiceProviderType.BOX.name(),
			ServiceProviderType.GOOGLE_DRIVE.name(), 
			ServiceProviderType.ONE_DRIVE.name(), 
			ServiceProviderType.SHAREPOINT_ONPREMISE.name(), 
			ServiceProviderType.SHAREPOINT_ONLINE.name()};

	public static final String[] crossLaunchApps = {
			ServiceProviderType.SHAREPOINT_CROSSLAUNCH.name(),
			ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.name()};
	
			
	private static RepositoryFactory repoFactory = new RepositoryFactory(); 
	
	private Logger logger = Logger.getLogger(RepositoryFactory.class);
	
	private RepositoryFactory() {
	}
	
	public static RepositoryFactory getInstance(){
		return repoFactory;
	}
	
	public IRepository getRepository(RMSUserPrincipal userPrincipal, long repoId) throws RepositoryException{
		String userId = userPrincipal.getUid();
		IRepository repo = RepositoryManager.getInstance().getRepositoryFromCache(repoId, userId);
		if (repo != null){
			return repo;
		}
		RepositoryDTO repoDO = RepositoryManager.getInstance().fetchRepositoryById(repoId);
		repo = createRepository(userPrincipal, repoDO);
		RepositoryManager.getInstance().putRepositoryInCache(repoId, userId, repo);		
		return repo;
	}

	public IRepository getRepository(RMSUserPrincipal userPrincipal, RepositoryDTO repoDO) throws RepositoryException{
		long repoId = repoDO.getId();
		String userId = userPrincipal.getUid();
		IRepository repo = RepositoryManager.getInstance().getRepositoryFromCache(repoId, userId);
		if (repo != null){
			return repo;
		}
		repo = createRepository(userPrincipal, repoDO);
		RepositoryManager.getInstance().putRepositoryInCache(repoId, userId, repo);		
		return repo;
	}
	
	private IRepository createRepository(RMSUserPrincipal userPrincipal, RepositoryDTO repoDTO) throws RepositoryException {
		IRepository repo = null;
		String repoType = repoDTO.getRepoType();
		long repoId = repoDTO.getId();
		
		if(repoType.equals(ServiceProviderType.DROPBOX.name())){
			repo = new DropboxRepository(userPrincipal, repoId);
		}else if(repoType.equals(ServiceProviderType.SHAREPOINT_ONPREMISE.name())){
			String repoName=repoDTO.getRepoName();
			String baseURL = repoDTO.getRepoAccountId();
			repo = new SharePointRepository(userPrincipal, repoId, baseURL,repoName);
		}else if(repoType.equals(ServiceProviderType.GOOGLE_DRIVE.name())){
			repo = new GoogleDriveRepository(userPrincipal, repoId);
		}else if(repoType.equals(ServiceProviderType.ONE_DRIVE.name())){
			repo = new OneDriveRepository(userPrincipal, repoId);
		}else if(repoType.equals(ServiceProviderType.SHAREPOINT_ONLINE.name())){
			repo = new SharepointOnlineRepository(userPrincipal, repoId);
		}else if(repoType.equals(ServiceProviderType.BOX.name())){
			repo = new BoxRepository(userPrincipal, repoId);
		}
		if (repo == null) {
			logger.error("Unsupported repository type: " + repoType);
			throw new RepositoryException("Unsupported repository type: " + repoType);
		}
		
		if (repoDTO.getAuthorizedUser() != null) {
			repo.setAccountId(repoDTO.getAuthorizedUser().getAccountId());
			repo.setAccountName(repoDTO.getAuthorizedUser().getAccountName());
		}
		repo.setShared(repoDTO.isShared());
		return repo;
	}

}
