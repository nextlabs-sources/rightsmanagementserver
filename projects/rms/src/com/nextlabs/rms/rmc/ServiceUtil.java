/**
 * 
 */
package com.nextlabs.rms.rmc;

import javax.persistence.EntityManager;

import com.nextlabs.rms.entity.repository.RepositoryDO;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.rmc.types.StatusType;

/**
 * @author nnallagatla
 *
 */
public class ServiceUtil {


	public static RepositoryDO getRepoReference(EntityManager em, long repoId){
		/*
		 * we cannot use getReference for Repo as we have logical deletion for it.
		 * EntityManager can return repo records with is_active set to false
		 */
		RepositoryDO repo = em.find(RepositoryDO.class, repoId);
		if (!repo.isActive()) {
			return null;
		}
		return repo;
	}	
	
	public static StatusType getStatus(int statusCode, String statusMessageLabel){
		StatusType status = StatusType.Factory.newInstance();
		status.setCode(statusCode);
		status.setMessage(RMSMessageHandler.getClientString(statusMessageLabel));
		return status;
	}
	
}
