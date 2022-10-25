/**
 * 
 */
package com.nextlabs.rms.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.DBConfigManager;
import com.nextlabs.rms.entity.repository.CachedFileDO;
import com.nextlabs.rms.entity.repository.FavoriteFileDO;
import com.nextlabs.rms.entity.repository.OfflineFileDO;
import com.nextlabs.rms.entity.repository.RepositoryDO;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.rmc.types.CachedFileIdListType;
import com.nextlabs.rms.util.UtilMethods;

/**
 * @author nnallagatla
 *
 */
public class RMCCachedFilePersistenceManager {

	private static Logger logger = Logger.getLogger(RMCCachedFilePersistenceManager.class);
	
	/**
	 * 
	 */
	private RMCCachedFilePersistenceManager() {
	}
	
	private static RMCCachedFilePersistenceManager instance = new RMCCachedFilePersistenceManager();
	
	public static RMCCachedFilePersistenceManager getInstance(){
		return instance;
	}
	/**
	 * 
	 * @param em
	 * @param repo
	 */
	void deleteCachedFilesByRepoId(EntityManager em, RepositoryDO repo){
		Query q = em.createNamedQuery("FavoriteFileDO.deleteByRepoId");
		q.setParameter("repo", repo);
		q.setParameter("updatedDate", UtilMethods.getCurrentGMTDateTime());
		q.executeUpdate();
	}

	/**
	 * 
	 * @param em
	 * @param repo
	 */
	void deleteOfflineFilesByRepoId(EntityManager em, RepositoryDO repo){
		Query q = em.createNamedQuery("OfflineFileDO.deleteByRepoId");
		q.setParameter("repo", repo);
		q.setParameter("updatedDate", UtilMethods.getCurrentGMTDateTime());
		q.executeUpdate();		
	}

	public boolean unMarkFavoriteFiles(CachedFileIdListType cachedFileIdList){
		return unMarkCachedFiles(cachedFileIdList, "FavoriteFileDO");
	}

	private boolean unMarkCachedFiles(CachedFileIdListType cachedFileIdList, String entity){
		boolean flag = false;
		
		try {
			EntityManagerHelper.beginTransaction();
			String queryStr = "UPDATE " + entity
					+ " e SET e.active=FALSE, e.updatedDate=:updatedDate WHERE e.id IN :idList AND e.active=TRUE";
			Query q = EntityManagerHelper.getEntityManager().createQuery(queryStr);
			
			Date currentDate = UtilMethods.getCurrentGMTDateTime();
			List<Long> idList = new ArrayList<>();
			
			for (long id : cachedFileIdList.getCachedFileIdArray()){
				idList.add(id);
			}
			
			q.setParameter("idList", idList);
			q.setParameter("updatedDate", currentDate);
            q.executeUpdate();

            logger.debug("All CachedFiles have been unmarked");
			EntityManagerHelper.commit();
			flag=true;
		} catch (Exception e) {
			logger.error("Error occurred while unmarking cached Files in " + entity + ". " + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}
		return flag;
	}
	
	
	public boolean unMarkOfflineFiles(CachedFileIdListType cachedFileIdList){
		return unMarkCachedFiles(cachedFileIdList, "OfflineFileDO");
	}

	public <T> void markCachedFiles(List<? extends CachedFileDO> cachedFileList){
		
		Date currentDate = UtilMethods.getCurrentGMTDateTime();
		
		try {
			EntityManagerHelper.beginTransaction();
			int i = 0;
			for (CachedFileDO fav : cachedFileList) {
					fav.setCreatedDate(currentDate);
					fav.setUpdatedDate(currentDate);
					EntityManagerHelper.getEntityManager().persist(fav); 
					if ((++i) % DBConfigManager.DB_OPERATION_BATCH_SIZE == 0){
						EntityManagerHelper.getEntityManager().flush();
						EntityManagerHelper.getEntityManager().clear();
					}
			}
			logger.debug("All CachedFiles have been saved");
			EntityManagerHelper.commit();
		} catch (Exception e) {
			logger.error("Error occurred while persisting cachedFiles. " + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}
	}
	
	
	public List<FavoriteFileDO> getFavoriteFilesUpdatedOnOrAfter(EntityManager em, TenantUser user, Date gmtTimestamp, boolean isDeltaUpdate){
		List<FavoriteFileDO> result = null;
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<FavoriteFileDO> criteriaQuery = criteriaBuilder.createQuery(FavoriteFileDO.class);
		Root<FavoriteFileDO> fav = criteriaQuery.from(FavoriteFileDO.class);

		List<Predicate> predicates = new ArrayList<Predicate>();
		Predicate userPredicate = criteriaBuilder.equal(fav.get("user"), user);

		predicates.add(userPredicate);

		if (isDeltaUpdate) {
			predicates.add(criteriaBuilder.greaterThanOrEqualTo(fav.<Date> get("updatedDate"), gmtTimestamp));
		} else {
			predicates.add(criteriaBuilder.and(criteriaBuilder.equal(fav.get("active"), true)));
		}
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		TypedQuery<FavoriteFileDO> query = em.createQuery(criteriaQuery);
		result = query.getResultList();
		return result;
	}

	public List<OfflineFileDO> getOfflineFilesUpdatedOnOrAfter(EntityManager em, TenantUser user, Date gmtTimestamp, boolean isDeltaUpdate){
		List<OfflineFileDO> result = null;
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<OfflineFileDO> criteriaQuery = criteriaBuilder.createQuery(OfflineFileDO.class);
		Root<OfflineFileDO> off = criteriaQuery.from(OfflineFileDO.class);

		List<Predicate> predicates = new ArrayList<Predicate>();
		Predicate userPredicate = criteriaBuilder.equal(off.get("user"), user);

		predicates.add(userPredicate);

		if (isDeltaUpdate) {
			predicates.add(criteriaBuilder.greaterThanOrEqualTo(off.<Date> get("updatedDate"), gmtTimestamp));
		} else {
			predicates.add(criteriaBuilder.and(criteriaBuilder.equal(off.get("active"), true)));
		}
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		TypedQuery<OfflineFileDO> query = em.createQuery(criteriaQuery);
		result = query.getResultList();
		return result;
	}
	
}
