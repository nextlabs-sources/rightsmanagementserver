/**
 * 
 */
package com.nextlabs.rms.dto.repository;

/**
 * @author nnallagatla
 *
 */
public class RepositoryDTO {
	public RepositoryDTO() {
	}

	private long id;

	private String repoName;

	private String repoType;

	private boolean shared;

	private String tenantId;

	private String createdByUserId;
	
	private String currentUser;

	private String repoAccountId;

	private AuthorizedRepoUserDTO authorizedUser;

	public String getRepoAccountId() {
		return repoAccountId;
	}

	public void setRepoAccountId(String accountId) {
		this.repoAccountId = accountId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getRepoType() {
		return repoType;
	}

	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public String getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public AuthorizedRepoUserDTO getAuthorizedUser() {
		return authorizedUser;
	}

	public void setAuthorizedUser(AuthorizedRepoUserDTO authorizedUser) {
		this.authorizedUser = authorizedUser;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

}
