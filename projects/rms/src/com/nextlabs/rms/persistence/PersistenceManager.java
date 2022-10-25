package com.nextlabs.rms.persistence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.DBConfigManager;
import com.nextlabs.rms.config.NightlyMaintenance;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.entity.repository.AuthorizedRepoUserDO;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.entity.repository.RepositoryDO;
import com.nextlabs.rms.entity.setting.ServiceProviderDO;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.entity.setting.SettingDO;
import com.nextlabs.rms.util.Nvl;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UtilMethods;

public class PersistenceManager {
	private static Logger logger = Logger.getLogger(PersistenceManager.class);
	private static final PersistenceManager persistenceMgr = new PersistenceManager();

	private PersistenceManager() {
		// TODO how to handle this in multi-tenant case
		// checkAndInsertNewSettings();
	}

	public void checkAndInsertNewSettings() {
		Map<String, String> settings = this.getSettings(ConfigManager.KMS_DEFAULT_TENANT_ID);
		List<String> allSettings = SettingManager.getPropertyNameList();
		List<String> missingProperties=new ArrayList<String>();
		for(String property: allSettings){
			if(!settings.containsKey(property)){
				missingProperties.add(property);
			}
		}
		insertPropertiesInSettingsTable(ConfigManager.KMS_DEFAULT_TENANT_ID, missingProperties);
		logger.info("Inserted new settings in database");
	}
	
	public synchronized static PersistenceManager getInstance() {
		return persistenceMgr;
	}

	private void insertPropertiesInSettingsTable(String tenantId, List<String> properties) {
		if (properties == null || properties.size() == 0) {
			return;
		}
		try {
			EntityManagerHelper.beginTransaction();
			int i = 0;
			for (String property : properties) {
				SettingDO setting = new SettingDO(property, null);
				setting.setTenantId(tenantId);
				EntityManagerHelper.getEntityManager().persist(setting);
				logger.debug("Added a new property: " + property + " to the settings table.");
				
				if ((++i) % DBConfigManager.DB_OPERATION_BATCH_SIZE == 0){
					EntityManagerHelper.getEntityManager().flush();
					EntityManagerHelper.getEntityManager().clear();
				}
			}
			EntityManagerHelper.commit();
		} catch (Exception e) {
			logger.error("An error occurred while inserting in settings table. " + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}
		finally{
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	public void deleteRepository(long repoId) {
		EntityManager em = EntityManagerHelper.getEntityManager();
		RepositoryDO repoDO = em.getReference(RepositoryDO.class, repoId);
		RMCCachedFilePersistenceManager.getInstance().deleteCachedFilesByRepoId(em, repoDO);
		RMCCachedFilePersistenceManager.getInstance().deleteOfflineFilesByRepoId(em, repoDO);
		updateInactiveAuthorizedRepoUsers(repoDO);
		repoDO.setActive(false);
		repoDO.setUpdatedDate(UtilMethods.getCurrentGMTDateTime());
	}
	
	private void updateInactiveAuthorizedRepoUsers(RepositoryDO repoDO) {
		EntityManager em = EntityManagerHelper.getEntityManager();
		try {
			Query q = em.createNamedQuery("AuthorizedRepoUserDO.updateInactiveUsers");
	        Date currentGMTDate = UtilMethods.getCurrentGMTDateTime();
			q.setParameter("repository", repoDO);
			q.setParameter("updatedDate", currentGMTDate);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Couldn't delete authorized user records for the repository. " + e.getMessage(), e);
			throw e;
		}		
	}

	public AuthorizedRepoUserDO getCurrentAuthorizedUser(RepositoryDO repoDO, TenantUser user) {
		AuthorizedRepoUserDO result = null;
		TypedQuery<AuthorizedRepoUserDO> q = EntityManagerHelper.getEntityManager()
				.createNamedQuery("AuthorizedRepoUserDO.getCurrentUser", AuthorizedRepoUserDO.class);
		q.setParameter("repository", repoDO);
		q.setParameter("user", user);
		List<AuthorizedRepoUserDO> resultList = q.getResultList();
		if (resultList != null && resultList.size() > 0) {
			result = resultList.get(0);
		}
		return result;
	}

    public void removeInactiveRecords() {
        try {
            // cutoff for removal of inactive records is midnight of sync file duration in days
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.DAY_OF_MONTH, -NightlyMaintenance.INACTIVE_RECORDS_CLEAR_THRESHOLD_IN_DAYS);
            Date cutoffDate = calendar.getTime();

            EntityManager em = EntityManagerHelper.getEntityManager();
            em.createNamedQuery("FavoriteFileDO.deleteInactiveFavorites")
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();
            em.createNamedQuery("OfflineFileDO.deleteInactiveOfflines")
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();
            em.createNamedQuery("AuthorizedRepoUserDO.deleteInactiveUsers")
    				.setParameter("cutoffDate", cutoffDate)
    				.executeUpdate();
            em.createNamedQuery("RepositoryDO.deleteInactiveRepos")
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();
            em.createNamedQuery("ServiceProviderDO.deleteInactiveProviders")
            		.setParameter("cutoffDate", cutoffDate)
            		.executeUpdate();
        } catch (Exception e) {
            logger.error("Error while removing all inactive records. " + e.getMessage(), e);
            throw e;
        }

        return;
    }
	
	public List<AuthorizedRepoUserDO> getUserAccessibleRepositories(TenantUser user) {
		List<AuthorizedRepoUserDO> personalAuthUsers = null;
		try {
			TypedQuery<AuthorizedRepoUserDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("AuthorizedRepoUserDO.findAll", AuthorizedRepoUserDO.class);
			q.setParameter("user", user);
			personalAuthUsers = q.getResultList();
		} catch (Exception e) {
			logger.error("Error while fetching repositories for User Id: " + user.getUserId() + ". " + e.getMessage(), e);
			throw e;
		}
		
		return personalAuthUsers;
	}	

	public Map<String, String> getSettings(String tenantId) {
		HashMap<String, String> setting = new HashMap<String, String>();

		try {
			TypedQuery<SettingDO> q = EntityManagerHelper.getEntityManager().createNamedQuery("SettingDO.findAllByTenantId", SettingDO.class);
			q.setParameter("tenantId", tenantId);
			List<SettingDO> settingsList = q.getResultList();

			for (SettingDO s : settingsList) {
				setting.put(s.getName(), Nvl.nvl(s.getValue()));
			}
		} catch (Exception e) {
			logger.error("Error occurred while getting settings." + e.getMessage(), e);
			throw e;
		}
		return setting;
	}

	public Map<String, String> updateSettingTable(String tenantId, Map<String, String> settingMap) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", "Unable to update the settings");
		
		Query query = EntityManagerHelper.getEntityManager().createNamedQuery("SettingDO.updateValueByNameAndTenantId");
		
		for(String property: settingMap.keySet()){
			String value = StringUtils.hasText(settingMap.get(property)) ? settingMap.get(property) : null;
			query.setParameter("setting_value", value);
			query.setParameter("setting_name", property);
			query.setParameter("tenantId", tenantId);
			query.executeUpdate();		
		}

		map.put("message", "Successfully updated the settings.");
		return map;
	}

	public RepositoryDO lookupRepoById(long repoId) {
		RepositoryDO repoDO = EntityManagerHelper.getEntityManager().find(RepositoryDO.class, repoId);
		return  repoDO!= null && repoDO.isActive()? repoDO : null;
	}
	
	public RepositoryDO getSharedRepositoryByTenantIdAndRepoName(String tenantId, String repoName) {
		RepositoryDO repoDO = null;
		try {
			TypedQuery<RepositoryDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("RepositoryDO.findSharedRepoByNameAndTenantId", RepositoryDO.class);
			q.setParameter("repoName", repoName);
			q.setParameter("tenantId", tenantId);
			List<RepositoryDO> repoList = q.getResultList();
			
			if (repoList.size() != 0){
				repoDO = repoList.get(0);
			}
		} catch (Exception e) {
			logger.error("Error while fetching shared repository. repoName:" + 
					repoName + " tenantId: "+tenantId + ". " + e.getMessage(), e);
			throw e;
		}
		return repoDO;
	}
	
	public RepositoryDO getPersonalRepoByTenantIdUserIdAndRepoName(String tenantId, String userId, String repoName) {
		RepositoryDO repoDO = null;
		try {
			TypedQuery<RepositoryDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("RepositoryDO.findPersonalRepoByNameUserIdAndTenantId", RepositoryDO.class);
			q.setParameter("repoName", repoName);
			q.setParameter("user", new TenantUser(tenantId, userId));
			List<RepositoryDO> repoList = q.getResultList();
			
			if (repoList.size() != 0){
				repoDO = repoList.get(0);
			}
		} catch (Exception e) {
			logger.error("Error while fetching Personal repository. repoName:" + 
					repoName + " tenantId: "+tenantId + ". UserId: " + userId + ". " + e.getMessage(), e);
			throw e;
		}
		return repoDO;
	}
	
	public List<RepositoryDO> getBoundRepositoriesUpdatedOnOrAfter(EntityManager em, TenantUser user, Date gmtTimestamp,
			boolean isDeltaUpdate) {
		List<RepositoryDO> result = null;
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<RepositoryDO> criteriaQuery = criteriaBuilder.createQuery(RepositoryDO.class);
		Root<RepositoryDO> repo = criteriaQuery.from(RepositoryDO.class);

		List<Predicate> predicates = new ArrayList<Predicate>();
		Predicate userPredicate = criteriaBuilder.equal(repo.get("createdBy"), user);
		
		Predicate sharedPredicate = criteriaBuilder.equal(repo.get("shared"), true);
		Predicate userOrSharedPredicate = criteriaBuilder.or(userPredicate, sharedPredicate);
		
		predicates.add(userOrSharedPredicate);

		if (isDeltaUpdate) {
			predicates.add(criteriaBuilder.greaterThanOrEqualTo(repo.<Date> get("updatedDate"), gmtTimestamp));
		} else {
			predicates.add(criteriaBuilder.and(criteriaBuilder.equal(repo.get("active"), true)));
		}
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		TypedQuery<RepositoryDO> query = em.createQuery(criteriaQuery);
		result = query.getResultList();
		return result;
	}

	public List<AuthorizedRepoUserDO> getAuthorizationsUpdatedOnOrAfter(EntityManager em, List<RepositoryDO> repositories, TenantUser user, Date gmtTimestamp,
			boolean isDeltaUpdate) {
		List<AuthorizedRepoUserDO> result = null;
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<AuthorizedRepoUserDO> criteriaQuery = criteriaBuilder.createQuery(AuthorizedRepoUserDO.class);
		Root<AuthorizedRepoUserDO> repo = criteriaQuery.from(AuthorizedRepoUserDO.class);

		List<Predicate> predicates = new ArrayList<Predicate>();
		Predicate userPredicate = criteriaBuilder.equal(repo.get("user"), user);
		predicates.add(userPredicate);

		if (isDeltaUpdate) {
			
			Predicate datePredicate = criteriaBuilder.greaterThanOrEqualTo(repo.<Date> get("updatedDate"), gmtTimestamp);
			
			if (repositories != null && repositories.size() > 0) {
				Expression<RepositoryDO> expression= repo.get("repository");
				Predicate repoPredicate = expression.in(repositories);
				
				Predicate dateOrRepoPredicate = criteriaBuilder.or(userPredicate, repoPredicate);
				predicates.add(dateOrRepoPredicate);
			} else {
				predicates.add(datePredicate);
			}
			
		} else {
			predicates.add(criteriaBuilder.and(criteriaBuilder.equal(repo.get("active"), true)));
		}
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		TypedQuery<AuthorizedRepoUserDO> query = em.createQuery(criteriaQuery);
		result = query.getResultList();
		return result;
	}

	public List<RepositoryDO> getUserAccessibleSharedRepositories(TenantUser user) {
		List<RepositoryDO> sharedAuth = null;
		try {
			TypedQuery<RepositoryDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("RepositoryDO.findSharedRepoByTenantId", RepositoryDO.class);
			q.setParameter("tenantId", user.getTenantId());
			sharedAuth = q.getResultList();
		} catch (Exception e) {
			logger.error("Error while fetching Shared repositories for User Id: " + user.getUserId()+ ". " + e.getMessage(), e);
			throw e;
		} 
		return sharedAuth;
	}

	public SettingDO fetchSettingByName(String tenantId, String name) {
		List<SettingDO> settings = null;
		try {
			TypedQuery<SettingDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("SettingDO.fetchSettingByNameAndTenantId", SettingDO.class);
			q.setParameter("tenantId", tenantId);
			q.setParameter("name", name);
			settings = q.getResultList();
			if (settings.size() > 0) {
				return settings.get(0);
			}
		} catch (Exception e) {
			logger.error("Error while fetching settings. TenantId: " + tenantId + " name:" + name + ". " + e.getMessage(), e);
			throw e;
		} 
		return null;
	}
	
	public List<ServiceProviderDO> fetchServiceProvidersByTenant(String tenantId) {
		List<ServiceProviderDO> settings = null;
		try {
			TypedQuery<ServiceProviderDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("ServiceProviderDO.getAllByTenantId", ServiceProviderDO.class);
			q.setParameter("tenantId", tenantId);
			settings = q.getResultList();
		} catch (Exception e) {
			logger.error("Error while fetching service providers. TenantId: " + tenantId + ". " + e.getMessage(), e);
			throw e;
		} 
		return settings;
	}
	
	/**
	 * This method also populates the attributes using FETCH JOIN
	 * @param tenantId
	 * @return
	 */
	public List<ServiceProviderDO> fetchServiceProvidersDetailsByTenant(String tenantId) {
		List<ServiceProviderDO> settings = null;
		try {
			TypedQuery<ServiceProviderDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("ServiceProviderDO.getAllDetailsByTenantId", ServiceProviderDO.class);
			q.setParameter("tenantId", tenantId);
			settings = q.getResultList();
		} catch (Exception e) {
			logger.error("Error while fetching service providers. TenantId: " + tenantId + ". " + e.getMessage(), e);
			throw e;
		} 
		return settings;
	}
	
	/**
	 * This method also populates the attributes using FETCH JOIN
	 * @param tenantId
	 * @return
	 */
	public ServiceProviderDO fetchServiceProviderDetailsByTenantIdAndId(String tenantId, long id) {
		try {
			List<ServiceProviderDO> settings = null;
			TypedQuery<ServiceProviderDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("ServiceProviderDO.getServiceProviderDetailsByTenantIdAndId", ServiceProviderDO.class);
			q.setParameter("tenantId", tenantId);
			q.setParameter("id", id);
			settings = q.getResultList();
			if (settings.size() > 0) return settings.get(0);
		} catch (Exception e) {
			logger.error("Error while fetching service providers. TenantId: " + tenantId + ". " + e.getMessage(), e);
			throw e;
		}
		return null;
	}
	
	public void deleteAttributesByServiceProvider(ServiceProviderDO provider) {
		try {
			Query q = EntityManagerHelper.getEntityManager().createNamedQuery("ServiceProviderAttributeDO.deleteAttributesByProviderId");
			q.setParameter("provider", provider);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while deleting service provider attributes for Id: " + provider.getId() + ". " + e.getMessage(), e);
			throw e;
		}
	}

	public ServiceProviderDO fetchServiceProviderByTenantIdAndType(String tenantId, String providerType) {
		try {
			List<ServiceProviderDO> settings = null;
			TypedQuery<ServiceProviderDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("ServiceProviderDO.getServiceProviderByTenantIdAndType", ServiceProviderDO.class);
			q.setParameter("tenantId", tenantId);
			q.setParameter("providerType", ServiceProviderType.valueOf(providerType));
			settings = q.getResultList();
			if (settings.size() > 0) return settings.get(0);
		} catch (Exception e) {
			logger.error("Error while fetching service provider by tenant and type. TenantId: " + tenantId + 
					" type: " + providerType + ". " + e.getMessage(), e);
			throw e;
		}
		return null;
	}

	public ServiceProviderDO fetchServiceProviderByTenantIdKeyAndValue(String tenantId, String key, String value) {
		try {
			List<ServiceProviderDO> settings = null;
			TypedQuery<ServiceProviderDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("ServiceProviderDO.getServiceProviderDetailsByTenantIdKeyAndValue", ServiceProviderDO.class);
			q.setParameter("tenantId", tenantId);
			q.setParameter("key", key);
			q.setParameter("value", value);
			settings = q.getResultList();
			if (settings.size() > 0) return settings.get(0);
		} catch (Exception e) {
			logger.error("Error while fetching service provider by tenant, key and value. TenantId: " + tenantId + 
					" key: " + key + "value: " + value+ ". " + e.getMessage(), e);
			throw e;
		}
		return null;
	}
	
	public ServiceProviderDO fetchServiceProviderByServiceProviderId(long serviceProviderId) {
		try {
			List<ServiceProviderDO> settings = null;
			TypedQuery<ServiceProviderDO> q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("ServiceProviderDO.getServiceProviderDetailsById", ServiceProviderDO.class);
			q.setParameter("id", serviceProviderId);
			settings = q.getResultList();
			if (settings.size() > 0) return settings.get(0);
		} catch (Exception e) {
			logger.error("Error while fetching service provider for serviceProvider by using serviceProviderId : "+serviceProviderId, e);
			throw e;
		}
		return null;
	}

	public void deleteServiceProviderAffectedAuthorizedUsers(String tenantId, ServiceProviderType serviceProviderType)
			throws Exception {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("AuthorizedRepoUserDO.updateAuthorizedUserByRepoTypeAndTenantId");
			q.setParameter("repoType", serviceProviderType);
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while deleting authorized user for repoType: " + serviceProviderType.toString()
					+ " tenantId: " + tenantId, e);
			throw e;
		}
	}

	public void deleteServiceProviderAffectedFavoriteFiles(String tenantId, ServiceProviderType serviceProviderType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("FavoriteFileDO.updateFavoriteFileByRepoTypeAndTenantId");
			q.setParameter("repoType", serviceProviderType);
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while deleting favorite files for repoType: " + serviceProviderType.toString()
					+ " tenantId: " + tenantId, e);
			throw e;
		}
	}

	public void deleteServiceProviderAffectedOfflineFiles(String tenantId, ServiceProviderType serviceProviderType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("OfflineFileDO.updateOfflineFileByRepoTypeAndTenantId");
			q.setParameter("repoType", serviceProviderType);
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while deleting offline files for repoType: " + serviceProviderType.toString()
					+ " tenantId: " + tenantId, e);
			throw e;
		}
	}

	public void deleteServiceProviderAffectedRepositories(String tenantId, ServiceProviderType serviceProviderType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("RepositoryDO.updateRepositoryByRepoTypeAndTenantId");
			q.setParameter("repoType", serviceProviderType);
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while deleting repositories for repoType: " + serviceProviderType.toString()
					+ " tenantId: " + tenantId, e);
			throw e;
		}
	}

	public void deleteAllowPersonalRepositoryAffectedAuthorizedUsers(String tenantId, String providerType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("AuthorizedRepoUserDO.updatePersonalAuthorizedUserByRepoTypeAndTenantId");
			q.setParameter("repoType", ServiceProviderType.valueOf(providerType));
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error(
					"Error while deleting authorized user for repoType: " + providerType + " tenantId: " + tenantId,
					e);
			throw e;
		}
	}

	public void deleteAllowPersonalRepositoryAffectedFavoriteFiles(String tenantId, String providerType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("FavoriteFileDO.updatePersonalFavoriteFileByRepoTypeAndTenantId");
			q.setParameter("repoType", ServiceProviderType.valueOf(providerType));
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error(
					"Error while deleting favorite files for repoType: " + providerType + " tenantId: " + tenantId,
					e);
			throw e;
		}
	}

	public void deleteAllowPersonalRepositoryAffectedOfflineFiles(String tenantId, String providerType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("OfflineFileDO.updatePersonalOfflineFileByRepoTypeAndTenantId");
			q.setParameter("repoType", ServiceProviderType.valueOf(providerType));
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error(
					"Error while deleting offline files for repoType: " + providerType + " tenantId: " + tenantId, e);
			throw e;
		}
	}

	public void deleteAllowPersonalRepositoryAffectedRepositories(String tenantId, String providerType) {
		try {
			Date currentGMTDateTime = UtilMethods.getCurrentGMTDateTime();
			Query q = EntityManagerHelper.getEntityManager()
					.createNamedQuery("RepositoryDO.updatePersonalRepositoryByRepoTypeAndTenantId");
			q.setParameter("repoType", ServiceProviderType.valueOf(providerType));
			q.setParameter("tenantId", tenantId);
			q.setParameter("updatedDate", currentGMTDateTime);
			q.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while deleting repositories for repoType: " + providerType + " tenantId: " + tenantId,
					e);
			throw e;
		}
	}
}
