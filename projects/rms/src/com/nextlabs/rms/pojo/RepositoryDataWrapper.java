/**
 * 
 */
package com.nextlabs.rms.pojo;

import com.nextlabs.rms.entity.repository.AuthorizedRepoUserDO;
import com.nextlabs.rms.entity.repository.RepositoryDO;

/**
 * @author nnallagatla
 *
 */
public class RepositoryDataWrapper {

	public RepositoryDO getRepository() {
		return repository;
	}
	public AuthorizedRepoUserDO getAuthorizedUser() {
		return authorizedUser;
	}
	
	public RepositoryDataWrapper(RepositoryDO repo, AuthorizedRepoUserDO authUser) {
		this.repository = repo;
		this.authorizedUser = authUser;
	}
	
	private RepositoryDO repository;
	private AuthorizedRepoUserDO authorizedUser;
	
}
