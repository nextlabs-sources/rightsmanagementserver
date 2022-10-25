package com.nextlabs.rms.repository;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.NightlyMaintenance;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.dto.repository.AuthorizedRepoUserDTO;
import com.nextlabs.rms.dto.repository.RepositoryDTO;
import com.nextlabs.rms.entity.repository.AuthorizedRepoUserDO;
import com.nextlabs.rms.entity.repository.RepositoryDO;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.entity.repository.CachedFileDO;
import com.nextlabs.rms.entity.repository.FavoriteFileDO;
import com.nextlabs.rms.entity.repository.OfflineFileDO;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.exception.RepositoryAuthorizationNotFound;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.persistence.EntityManagerHelper;
import com.nextlabs.rms.persistence.PersistenceManager;
import com.nextlabs.rms.persistence.RMCCachedFilePersistenceManager;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.pojo.SyncProfileDataContainer;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.servlets.OAuthHelper;

import java.util.HashSet;

import com.nextlabs.rms.util.UtilMethods;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RepositoryManager{

	private static RepositoryManager manager = new RepositoryManager();

	private static Logger logger = Logger.getLogger(RepositoryManager.class);

	public static final String REFRESH_TOKEN = "refresh_token";
	
	public static final String ACCESS_TOKEN = "access_token";
	
	public static final String TOKEN_STATE = "token_state";
	
	public static final String ACCESS_TOKEN_EXPIRY_TIME = "access_token_expiry_time";
	
	public static final String REPO_CACHE_ID_LIST = "repoCacheIds";
	
	public static long INACTIVE_CLEAR_THRESHOLD = NightlyMaintenance.INACTIVE_RECORDS_CLEAR_THRESHOLD_IN_DAYS * 24 * 60 * 60 * 1000;
	
	private RepositoryManager()	{
	}

	public static RepositoryManager getInstance() {
		return manager;
	}

	public List<IRepository> getRepositoryList(RMSUserPrincipal userPrincipal, boolean isPersonalOnly) throws RepositoryException {
		try {
			TenantUser tUser = new TenantUser(userPrincipal.getTenantId(), userPrincipal.getUid());
			List<AuthorizedRepoUserDO> list = PersistenceManager.getInstance().getUserAccessibleRepositories(tUser);
			List<IRepository> repoList = new ArrayList<IRepository>();
			HashSet<RepositoryDO> set = new HashSet<>();
			boolean isLDAPUser = RMSUserPrincipal.AUTH_LDAP.equals(userPrincipal.getAuthProvider());
			for (AuthorizedRepoUserDO auth : list) {
				RepositoryDO repo = auth.getRepository();
				if (repo.isShared() && isPersonalOnly || (!isLDAPUser && repo.getRepoType() == ServiceProviderType.SHAREPOINT_ONPREMISE)) {
					// If isPersonalOnly is true, then don't add shared repositories
				    // If user is not AD user, then don't add shared repositories
					continue;
				}
				set.add(repo);
				try {
					IRepository repository = RepositoryFactory.getInstance().getRepository(userPrincipal, convertToDTO(repo));
					
					repository.setUser(userPrincipal);
					if (auth.getSecToken() != null) {
						repository.getAttributes().put(RepositoryManager.REFRESH_TOKEN, auth.getSecToken());					
					}
					repository.setRepoName(repo.getRepoName());
					//TODO: What to do here
	//				String accountName = repoType.equals(RepositoryFactory.REPOTYPE_SHAREPOINT) ? auth.getAccountId() : auth.getAccountName();				
					repository.setAccountName(repo.getAccountId());
					repoList.add(repository);
				} catch (RepositoryException e) {
					logger.error(e.getMessage(), e);
					throw e;
				}
			}
			if(!isPersonalOnly){
			    List<RepositoryDO> sharedRepo = PersistenceManager.getInstance().getUserAccessibleSharedRepositories(tUser);
				for(RepositoryDO repo: sharedRepo){
					if(set.contains(repo) || (!isLDAPUser && repo.getRepoType() == ServiceProviderType.SHAREPOINT_ONPREMISE)){
						continue;
					}
					set.add(repo);
					try {
						IRepository repository = RepositoryFactory.getInstance().getRepository(userPrincipal, convertToDTO(repo));
						repository.setUser(userPrincipal);
						repository.setAccountName(repo.getAccountId());
						repository.setShared(repo.isShared());
						repository.setAccountId(repo.getAccountId());
						repository.setRepoName(repo.getRepoName());
						repository.setRepoId(repo.getId());
						repoList.add(repository);
					} catch (RepositoryException e) {
						logger.error(e.getMessage(), e);
						throw e;
					}
				}
			}
			return repoList;
		}
		finally {
			EntityManagerHelper.closeEntityManager();
		}
	}
	
    public RepositoryDTO addRepository(RepositoryDTO repoData) throws RepositoryAlreadyExists, DuplicateRepositoryNameException {
    	if (repoData == null) {
    		return repoData;
    	}
        try {
            if (checkIfRepositoryExists(repoData) ) {
            	throw new RepositoryAlreadyExists();
            }
        	
            if (fetchRepoWithSameName(repoData) != null) {
            	throw new DuplicateRepositoryNameException(repoData);
            }
            
            Date date = UtilMethods.getCurrentGMTDateTime();
            RepositoryDO repoDO = buildRepositoryDO(repoData);
            repoDO.setCreatedDate(date);
            repoDO.setUpdatedDate(date);
            
            EntityManagerHelper.beginTransaction();
            EntityManagerHelper.getEntityManager().persist(repoDO);
            AuthorizedRepoUserDTO repoUserDTO = repoData.getAuthorizedUser();
            if (repoUserDTO != null) {
                AuthorizedRepoUserDO repoUserDO = buildAuthorizedRepoUserDO(repoUserDTO);
                repoUserDO.setRepository(repoDO);
                repoUserDO.setUpdatedDate(date);
                EntityManagerHelper.getEntityManager().persist(repoUserDO);
            }
            EntityManagerHelper.commit();
            repoData.setId(repoDO.getId());
        } catch (Exception e) {
            if (EntityManagerHelper.getEntityManager().getTransaction().isActive()) {
                EntityManagerHelper.rollback();
            }
            logger.error("Error while Adding repository. userId: " + repoData.getCreatedByUserId(), e);
            throw e;
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
        return repoData;
    }
    
    private boolean applyChangesToRepositoryDO(RepositoryDTO dto, RepositoryDO repoDO) {
    	boolean changed = false;
    	if (!Objects.equals(repoDO.getAccountId(), dto.getRepoAccountId())) {
    		changed = true;
    		repoDO.setAccountId(dto.getRepoAccountId());
    	}
    	
    	if (!Objects.equals(repoDO.getRepoName(), dto.getRepoName())) {
    		changed = true;
            repoDO.setRepoName(dto.getRepoName());
    	}
    	
    	//ignore active, shared as these cannot be changed from update route        
        return changed;
    }
    
    
    public boolean updateRepositoryName(RepositoryDTO repoData) throws RepositoryNotFoundException, UnauthorizedOperationException, 
    DuplicateRepositoryNameException {
        boolean status = false;
        try {
        	EntityManagerHelper.beginTransaction();
            Date date = UtilMethods.getCurrentGMTDateTime();
            RepositoryDO repoDO = PersistenceManager.getInstance().lookupRepoById(repoData.getId());
            
            if (repoDO == null) {
            	throw new RepositoryNotFoundException(repoData.getId());
            }
            /*
             * isShared is not sent from client. we don't update isShared property.
             * Read it from DO and set it in DTO
             */
            repoData.setShared(repoDO.isShared());
            if (!canRename(repoData, repoDO)) {
            	throw new DuplicateRepositoryNameException(repoData);
            }
            repoDO.setRepoName(repoData.getRepoName());
            repoDO.setUpdatedDate(date);
            EntityManagerHelper.commit();
            deleteRepositoryFromCache(repoDO);
            status = true;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            logger.error("Error while updating repository. userId: " + repoData.getCreatedByUserId(), e);
            throw e;
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
        return status;	
    }
    
    private boolean canRename(RepositoryDTO repoData, RepositoryDO repoDO) throws UnauthorizedOperationException {
    	
    	TenantUser user = repoDO.getCreatedBy();
    	
    	if (!repoData.getCurrentUser().equals(user.getUserId()) || !repoData.getTenantId().equals(user.getTenantId())) {
    		throw new UnauthorizedOperationException("user: " + repoData.getCurrentUser() + " trying to rename repository: " + repoDO.getId());
    	}
    	
    	RepositoryDO sameNameRepoDO = fetchRepoWithSameName(repoData);
    	
    	if (sameNameRepoDO != null && sameNameRepoDO.getId() != repoDO.getId()) {
    		return false;
    	}
    	return true;
    }
    
    private RepositoryDO fetchRepoWithSameName(RepositoryDTO dto) {
    	if (dto.isShared()) {
    		return PersistenceManager.getInstance().getSharedRepositoryByTenantIdAndRepoName(dto.getTenantId(), dto.getRepoName());
    	}
    	
    	return PersistenceManager.getInstance().getPersonalRepoByTenantIdUserIdAndRepoName(dto.getTenantId(), dto.getCreatedByUserId(), dto.getRepoName());
    }
    
    public boolean updateRepository(RepositoryDTO repoData) throws RepositoryNotFoundException, UnauthorizedOperationException, DuplicateRepositoryNameException {
        boolean status = false;
        try {
        	EntityManagerHelper.beginTransaction();
            Date date = UtilMethods.getCurrentGMTDateTime();
            RepositoryDO repoDO = PersistenceManager.getInstance().lookupRepoById(repoData.getId());
            
            if (repoDO == null) {
            	throw new RepositoryNotFoundException(repoData.getId());
            }
            
            if (!canRename(repoData, repoDO)) {
            	throw new DuplicateRepositoryNameException(repoData);
            }
            
            boolean changed = applyChangesToRepositoryDO(repoData, repoDO);
            
            if (changed) {
            	repoDO.setUpdatedDate(date);
            }
            AuthorizedRepoUserDTO repoUserDTO = repoData.getAuthorizedUser();
            boolean delete = false;
            
            if (repoUserDTO == null) {
            	delete = true;	
            }
            TenantUser tUser = new TenantUser(repoData.getTenantId(), repoData.getCreatedByUserId());
            AuthorizedRepoUserDO authRepoUser = PersistenceManager.getInstance().getCurrentAuthorizedUser(repoDO, tUser);
            
            if (authRepoUser == null) {
            	if (!delete) {
            		//we need to insert new record
                    AuthorizedRepoUserDO repoUserDO = buildAuthorizedRepoUserDO(repoUserDTO);
                    repoUserDO.setRepository(repoDO);
                    repoUserDO.setUpdatedDate(date);
                    EntityManagerHelper.getEntityManager().persist(repoUserDO);
            	}
            } else {
            	if (delete) {
            		//mark the existing record as inactive
            		authRepoUser.setActive(false);
            		authRepoUser.setUpdatedDate(date);
            	} else {
            		//update the existing record
                    changed = applyChangesToAuthRepoUserDO(repoUserDTO, authRepoUser);
                    if (changed) {
                    	authRepoUser.setUpdatedDate(date);
                    }
            	}
            }
            EntityManagerHelper.commit();
            deleteRepositoryFromCache(repoDO);
            status = true;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            logger.error("Error while Adding repository. userId: " + repoData.getCreatedByUserId(), e);
            throw e;
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
        return status;
    }
    
    private boolean applyChangesToAuthRepoUserDO(AuthorizedRepoUserDTO repoUserDTO,
			AuthorizedRepoUserDO authRepoUser) {
    	boolean changed = false;
    	
    	//TODO when we have an interface for user to reauthorize, throw exception when accountId or accountName changes
    	
    	if (!Objects.equals(repoUserDTO.getRefreshToken(), authRepoUser.getSecToken())) {
    		changed = true;
    		authRepoUser.setSecToken(repoUserDTO.getRefreshToken());
    	}
    	if (!Objects.equals(repoUserDTO.getAccountId(), authRepoUser.getAccountId())) {
    		changed = true;
    		authRepoUser.setAccountId(repoUserDTO.getAccountId());
    	}
    	if (!Objects.equals(repoUserDTO.getAccountName(), authRepoUser.getAccountName())) {
    		changed = true;
    		authRepoUser.setAccountName(repoUserDTO.getAccountName());
    	}
    	return changed;
	}

	private RepositoryDO buildRepositoryDO(RepositoryDTO repoData) {
        RepositoryDO repoDO = new RepositoryDO();
        repoDO.setAccountId(repoData.getRepoAccountId());
        repoDO.setId(repoData.getId());
        repoDO.setRepoName(repoData.getRepoName());
        repoDO.setRepoType(ServiceProviderType.valueOf(repoData.getRepoType()));
        repoDO.setActive(true);
        repoDO.setShared(repoData.isShared());
        repoDO.setCreatedBy(new TenantUser(repoData.getTenantId(), repoData.getCreatedByUserId()));        
        return repoDO;
    }
    
    private AuthorizedRepoUserDO buildAuthorizedRepoUserDO(AuthorizedRepoUserDTO authRepoUser) {
        AuthorizedRepoUserDO authUserDO = new AuthorizedRepoUserDO();
        
        authUserDO.setAccountId(authRepoUser.getAccountId());
        authUserDO.setSecToken(authRepoUser.getRefreshToken());
        authUserDO.setUser(new TenantUser(authRepoUser.getTenantId(), authRepoUser.getUserId()));
        authUserDO.setAccountName(authRepoUser.getAccountName());
        authUserDO.setActive(true);
        return authUserDO;
    }

	private RepositoryDTO convertToDTO(RepositoryDO repoDO) {
		RepositoryDTO dto = new RepositoryDTO();
		dto.setCreatedByUserId(repoDO.getCreatedBy().getUserId());
		dto.setId(repoDO.getId());
		dto.setRepoAccountId(repoDO.getAccountId());
		dto.setTenantId(repoDO.getCreatedBy().getTenantId());
		dto.setRepoType(repoDO.getRepoType().name());
		dto.setShared(repoDO.isShared());
		dto.setRepoName(repoDO.getRepoName());
		return dto;
	}
	
	private RepositoryDTO convertToDTO(AuthorizedRepoUserDO repoUserDO) {
		RepositoryDTO dto = convertToDTO(repoUserDO.getRepository());
		AuthorizedRepoUserDTO repoUserDTO = new AuthorizedRepoUserDTO();
		repoUserDTO.setAccountId(repoUserDO.getAccountId());
		repoUserDTO.setAccountName(repoUserDO.getAccountName());
		repoUserDTO.setRefreshToken(repoUserDO.getSecToken());
		repoUserDTO.setUserId(repoUserDO.getUser().getUserId());
		repoUserDTO.setTenantId(repoUserDO.getUser().getTenantId());
		return dto;
	}
	
	private boolean checkIfRepositoryExists(RepositoryDTO repoDTO) {
		RepositoryDO repoDO = null;
		
		if (repoDTO == null) {
			return false;
		}
		if (repoDTO.isShared()){
			repoDO = getSharedRepositoryByTenantIdAccountIdAndRepoType(repoDTO.getTenantId(), repoDTO.getRepoAccountId(), repoDTO.getRepoType());
		} else{
			repoDO = getAuthorizedRepositoryUserByUserAndRepository(repoDTO);
		}
		return repoDO != null;
	}

	private RepositoryDO getAuthorizedRepositoryUserByUserAndRepository(RepositoryDTO repoDTO) {
		RepositoryDO repoDOfromDB = null;
		EntityManager em = EntityManagerHelper.getEntityManager();
		TypedQuery<AuthorizedRepoUserDO> q = em.createNamedQuery(
				"AuthorizedRepoUserDO.getExistingIdenticalPersonalRepository", AuthorizedRepoUserDO.class);
		q.setParameter("tenantId", repoDTO.getTenantId());
		q.setParameter("userAccountId", repoDTO.getAuthorizedUser().getAccountId());
		q.setParameter("repoType", ServiceProviderType.valueOf(repoDTO.getRepoType()));
		q.setParameter("accountId", repoDTO.getRepoAccountId());
		q.setParameter("user", new TenantUser(repoDTO.getTenantId(), repoDTO.getAuthorizedUser().getUserId()));
		List<AuthorizedRepoUserDO> userList = q.getResultList();

		for (AuthorizedRepoUserDO user : userList) {
			if (!user.getRepository().isShared()) {
				repoDOfromDB = user.getRepository();
				break;
			}
		}
		return repoDOfromDB;
	}

	private RepositoryDO getSharedRepositoryByTenantIdAccountIdAndRepoType(String tenantId, String accountId,
			String repoType) {
		RepositoryDO repoDO = null;
		// For a given tenant, there can't be 2 repository with same type and
		// accountId
		TypedQuery<RepositoryDO> q = EntityManagerHelper.getEntityManager()
				.createNamedQuery("RepositoryDO.findSharedRepoByTenantIdAccountIdRepoType", RepositoryDO.class);
		q.setParameter("repoType", ServiceProviderType.valueOf(repoType));
		q.setParameter("accountId", accountId);
		q.setParameter("tenantId", tenantId);
		List<RepositoryDO> repoList = q.getResultList();

		if (repoList.size() != 0) {
			repoDO = repoList.get(0);
		}
		return repoDO;
	}
	
	public RepositoryDTO fetchRepositoryById(long repoId) {
		RepositoryDTO repoDTO = null;
		try {
			RepositoryDO repo = PersistenceManager.getInstance().lookupRepoById(repoId);
			repoDTO = convertToDTO(repo);
		} catch (Exception e) {
			logger.error("Error while fetching repository by Id: " + repoId, e);
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
		return repoDTO;
	}

	public boolean deleteRepository(boolean isAdmin, TenantUser tUser, long repoId) throws RepositoryNotFoundException, UnauthorizedOperationException{
		boolean status = false;
		try {
			RepositoryDO repo = PersistenceManager.getInstance().lookupRepoById(repoId);
			
			if ( repo == null) {
				throw new RepositoryNotFoundException();
			}
			
			//TODO Add checks to see if user has permission to delete the repository
			//migrated sharepoint repos do not have createdBy set properly. Relying on isAdmin for such cases
			
			if ((!repo.isShared() && !repo.getCreatedBy().equals(tUser)) || 
					(repo.isShared() && (!repo.getCreatedBy().equals(tUser) && !isAdmin))) {
				logger.error("Unauthorized user trying to delete repository. repoId: " + repoId + " user: " + tUser);
				throw new UnauthorizedOperationException();
			}
			
			EntityManagerHelper.beginTransaction();
			PersistenceManager.getInstance().deleteRepository(repoId);
			EntityManagerHelper.commit();
			status = true;
			deleteRepositoryFromCache(repo);
		} catch (Exception e) {
			EntityManagerHelper.rollback();
			logger.error("Error while deleting repository repoId: " + repoId, e);
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
		return status;
	}

    public boolean removeInactiveRecords() {
        boolean status = false;
        try {
            EntityManagerHelper.beginTransaction();
            PersistenceManager.getInstance().removeInactiveRecords();
            EntityManagerHelper.commit();
            status = true;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            logger.error("Error while removing all inactive repositories: " + e);
            throw e;
        } finally {
            EntityManagerHelper.closeEntityManager();
        }

        return status;
    }

	public static List<IRepository> removePersonalRepo(List<IRepository> repoList) {
		List<IRepository> newRepoList = new ArrayList<IRepository>();
		for (IRepository repo : repoList) {
			if (repo.isShared()) {
				newRepoList.add(repo);
			}
		}
		return newRepoList;
	}

	public boolean isRepoValid(RMSUserPrincipal user, long repoId) {
		if (user == null)
			return false;
		RepositoryDTO repoDTO = fetchRepositoryById(repoId);
		if (repoDTO == null)
			return false;

		if (repoDTO.isShared() || (repoDTO.getCreatedByUserId().equals(user.getUid())
				&& repoDTO.getTenantId().equals(user.getTenantId()))) {
			return true;
		}
		return false;
	}
	
	public SyncProfileDataContainer getSyncDataUpdatedOnOrAfter(TenantUser user, Date gmtDate){
		SyncProfileDataContainer container = new SyncProfileDataContainer();
		try{
			EntityManager em = EntityManagerHelper.getEntityManager();
			PersistenceManager pm = PersistenceManager.getInstance();
			RMCCachedFilePersistenceManager rcpm = RMCCachedFilePersistenceManager.getInstance();
			
			boolean isDeltaUpdate = true;

			/*
			 * If the timestamp passed is earlier than (currentDate - INACTIVE_CLEAR_THRESHOLD) then send the full copy.
			 */
			
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			
			currentGMTDateTime.setTime(currentGMTDateTime.getTime() - INACTIVE_CLEAR_THRESHOLD);
			
			if (currentGMTDateTime.after(gmtDate)){
				isDeltaUpdate = false;
				gmtDate.setTime(0);
			}
			
			List<RepositoryDO> repoList = pm.getBoundRepositoriesUpdatedOnOrAfter(em, user, gmtDate, isDeltaUpdate);
			List<FavoriteFileDO> favList = rcpm.getFavoriteFilesUpdatedOnOrAfter(em, user, gmtDate, isDeltaUpdate);
			List<OfflineFileDO> offList = rcpm.getOfflineFilesUpdatedOnOrAfter(em, user, gmtDate, isDeltaUpdate);
			
			container.setFullCopy(!isDeltaUpdate);
			
			if (isDeltaUpdate){
				List<RepositoryDO> activeRepoList = new ArrayList<>();
				List<Long> deletedRepoList = new ArrayList<>();
				
				for (RepositoryDO repo: repoList){
					if (repo.isActive()){
						activeRepoList.add(repo);
					}
					else{
						deletedRepoList.add(repo.getId());
					}
				}
				
				List<AuthorizedRepoUserDO> repoUserAuthorizations = pm.getAuthorizationsUpdatedOnOrAfter(em,
						activeRepoList, user, gmtDate, isDeltaUpdate);
				
				container.setRepositoryList(getUniqueDTOList(activeRepoList, repoUserAuthorizations));
				container.setDeletedRepositoryList(deletedRepoList);

				List<FavoriteFileDO> activeFavList = new ArrayList<>();
				List<Long> deletedfavList = new ArrayList<>();
				
				segregateActiveAndInactive(favList, activeFavList, deletedfavList);
				
				container.setFavoriteFileList(activeFavList);
				container.setDeletedFavoriteFileList(deletedfavList);
				
				List<OfflineFileDO> activeOffList = new ArrayList<>();
				List<Long> deletedOffList = new ArrayList<>();
				
				segregateActiveAndInactive(offList, activeOffList, deletedOffList);
				
				container.setOfflineFileList(activeOffList);
				container.setDeletedOfflineFileList(deletedOffList);
				
			}
			else{
				//container.setRepositoryList(repoList);
				
				List<AuthorizedRepoUserDO> repoUserAuthorizations = pm.getAuthorizationsUpdatedOnOrAfter(em,
						repoList, user, gmtDate, isDeltaUpdate);
				
				
				container.setRepositoryList(getUniqueDTOList(repoList, repoUserAuthorizations));
				container.setFavoriteFileList(favList);
				container.setOfflineFileList(offList);				
			}
		}
		finally{
			EntityManagerHelper.closeEntityManager();
		}
		return container;
	}
	
	
	private List<RepositoryDTO> getUniqueDTOList(List<RepositoryDO> repoList, List<AuthorizedRepoUserDO> repoAuthUserList) {
		List<RepositoryDTO> dtoList = toDTOListFromAuthorizedRepoUserDOList(repoAuthUserList);
		
		Set<Long> processedRepos = new HashSet<Long>();
		
		for (RepositoryDTO dto : dtoList) {
			processedRepos.add(dto.getId());
		}
		
		for (RepositoryDO repo : repoList) {
			if (processedRepos.contains(repo.getId())) {
				continue;
			}
			processedRepos.add(repo.getId());
			dtoList.add(convertToDTO(repo));
		}
		return dtoList;
	}

	private List<RepositoryDTO> toDTOListFromAuthorizedRepoUserDOList(List<AuthorizedRepoUserDO> repoUserList) {
		List<RepositoryDTO> result = new ArrayList<>();
		for (AuthorizedRepoUserDO obj : repoUserList) {
			result.add(convertToDTO(obj));
		}
		return result;
	}
	
	private <T> void segregateActiveAndInactive(List<? extends CachedFileDO> inputList, List<T> activeList, List<Long> deletedList){	
		for (CachedFileDO item: inputList){
			if (item.isActive()){
				T obj = (T) item;
				activeList.add(obj);
			}
			else{
				deletedList.add(item.getId());
			}
		}
	}

	public IRepository getRepositoryFromCache(long repoId, String userId){
		String repoCacheId = getRepoCacheId(repoId, userId);
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_REPOSITORY);
		Element element = cache.get(repoCacheId);
		return element !=null ? (IRepository) element.getObjectValue(): null;
	}
	
	public void putRepositoryInCache(long repoId, String userId, IRepository repository){
		String repoCacheId = getRepoCacheId(repoId, userId);
		logger.debug("Adding repository to cache:"+repoCacheId);
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_REPOSITORY);
		cache.put(new Element(repoCacheId, repository));
	}
	
	private void deleteSharedRepositoriesFromcache(long repoId){
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_REPOSITORY);
		for (Object key: cache.getKeys()) {			
			if (!(key instanceof String)){
				continue;
			}
			String keyStr = (String) key;
			String cacheIdPrefix = repoId + "_";
			if (keyStr.startsWith(cacheIdPrefix)){
				logger.debug("Deleting Shared repository from cache:" + keyStr);
				cache.remove(keyStr);
			}
	  }
	}
	
	private void deleteRepositoryFromCache(RepositoryDO repository){
		long repoId = repository.getId();
		
		if (repository.isShared()){
			deleteSharedRepositoriesFromcache(repoId);
			return;
		}
		String userId = repository.getCreatedBy().getUserId();
		String repoCacheId = getRepoCacheId(repoId, userId)  ;
		logger.debug("Deleting personal repository from cache:"+ repoCacheId);
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_REPOSITORY);
		cache.remove(repoCacheId);
	}
	
	public boolean deleteRepositoryListFromCache(List<String> repoCacheIds){
		
		try{
			Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_REPOSITORY);
			if(repoCacheIds!=null){
				for(String repoCacheId:repoCacheIds){
					cache.remove(repoCacheId);
				}
			}
		}catch(Exception e){
			logger.error("Error occured while deleting repo from cache", e);
			return false;
		}
		logger.debug("Deleted user repositories from cache ");
		return true;
	}
	
	public static String getRepoCacheId(long repoId, String userId){
		return repoId + "_" + userId;
		
	}

	/**
	 * Called when user explicitly does the authorization
	 * @param authUser
	 * @return
	 */
	public boolean addAuthRepoUserToDB(AuthorizedRepoUserDTO authUser){
		
		if (authUser == null) {
			return false;
		}
		
		boolean added = false;
		AuthorizedRepoUserDO currentAuthorizedUser = null;
		try {
			EntityManager em = EntityManagerHelper.getEntityManager();
			EntityManagerHelper.beginTransaction();
			// Get the repository from DB
			RepositoryDO repoDO = PersistenceManager.getInstance().lookupRepoById(authUser.getRepoId());
			// Use the repository to find if user exists
			currentAuthorizedUser = PersistenceManager.getInstance().getCurrentAuthorizedUser(repoDO, 
					new TenantUser(authUser.getTenantId(), authUser.getUserId()));
			
			// If user doesn't exist then add user
			if (currentAuthorizedUser == null) {
				
				currentAuthorizedUser = buildAuthorizedRepoUserDO(authUser);
				
				currentAuthorizedUser.setRepository(repoDO);
				currentAuthorizedUser.setUpdatedDate(UtilMethods.getCurrentGMTDateTime());
				
				em.persist(currentAuthorizedUser);
			}
			// If user exists then update user and check if the accountId for
			// the user is the same.
			else {
				if (Objects.equals(currentAuthorizedUser.getAccountId(), authUser.getAccountId())) {
					currentAuthorizedUser.setSecToken(authUser.getRefreshToken());
					currentAuthorizedUser.setAccountName(authUser.getAccountName());
					currentAuthorizedUser.setUpdatedDate(UtilMethods.getCurrentGMTDateTime());
				} else {
					//TODO throw exception that this update is not valid
				}
			}
			EntityManagerHelper.commit();
			added = true;
		} catch (Exception e) {
			EntityManagerHelper.rollback();
			logger.error("Error while persisting repository authorization repoId: " + authUser.getRepoId() + " userId: " + authUser.getUserId(), e);
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
		return added;
	}

	public String addRepoToDBAndFetchRedirectURL(RepositoryDTO repoDTO) {
		String redirectURI;
		try {
			addRepository(repoDTO);
			String msg = RMSMessageHandler.getClientString("repoAddedSuccessfully", ServiceProviderSetting.getProviderTypeDisplayName(repoDTO.getRepoType()));
			redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?success=" + msg;
			return redirectURI;
		} catch (RepositoryAlreadyExists rae) {
			logger.error("Repository already exists", rae);
			String msg = RMSMessageHandler.getClientString("repoAlreadyExists", ServiceProviderSetting.getProviderTypeDisplayName(repoDTO.getRepoType()));
			redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error=" + msg;
			return redirectURI;
		} catch (DuplicateRepositoryNameException e) {
			logger.error("Duplicate Repository name:" + repoDTO.getRepoName(), e);
			String key = repoDTO.isShared() ? "string_shared" : "string_personal";
			String msg = RMSMessageHandler.getClientString("error_duplicate_repo_name", RMSMessageHandler.getClientString(key), repoDTO.getRepoName());
			redirectURI = GlobalConfigManager.RMS_CONTEXT_NAME + OAuthHelper.REDIRECT_URL_MANAGE_REPOSITORIES+"?error=" + msg;
			return redirectURI;
		}
	}

	/**
	 * Called from locations where we auto-update refresh tokens
	 * @param repoId
	 * @param userId
	 * @param refreshToken
	 */
	public void updateRefreshToken(long repoId, TenantUser tUser, String refreshToken) throws RepositoryAuthorizationNotFound{
		AuthorizedRepoUserDO currentAuthorizedUser = null;
		try {
			EntityManagerHelper.beginTransaction();
			// Get the repository from DB
			RepositoryDO repoDO = PersistenceManager.getInstance().lookupRepoById(repoId);
			currentAuthorizedUser = PersistenceManager.getInstance().getCurrentAuthorizedUser(repoDO, tUser);
			
			// If user doesn't exist then add user
			if (currentAuthorizedUser == null) {
				throw new RepositoryAuthorizationNotFound();
			}
			// If user exists then update user and check if the accountId for
			// the user is the same.
			currentAuthorizedUser.setSecToken(refreshToken);
			EntityManagerHelper.commit();
		} catch (Exception e) {
			EntityManagerHelper.rollback();
			logger.error("Error while persisting repository authorization repoId: " + repoId + " userId: " + tUser.getUserId(), e);
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
	}
}
