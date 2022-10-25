/**
 * 
 */
package com.nextlabs.rms.entity.repository;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author psheoran
 *
 */

@Entity
@Table(name = "RMS_AUTH_REPO_USER", indexes = {
		//Come up with an index
		//@Index(columnList = "account_id", name = "idx_repository_1", unique = false)
})
@NamedQueries({ 
	@NamedQuery(name = "AuthorizedRepoUserDO.findAll", 
		query = "SELECT r FROM AuthorizedRepoUserDO r WHERE r.user=:user AND r.active = TRUE"),
	@NamedQuery(name = "AuthorizedRepoUserDO.deleteInactiveUsers", 
	query = "DELETE FROM AuthorizedRepoUserDO r WHERE r.active = FALSE AND r.updatedDate < :cutoffDate"),
	@NamedQuery(name = "AuthorizedRepoUserDO.getCurrentUser", 
	query = "Select r FROM AuthorizedRepoUserDO r WHERE r.repository=:repository AND r.user=:user AND r.active = TRUE"),
	@NamedQuery(name = "AuthorizedRepoUserDO.updateInactiveUsers", 
	query = "UPDATE AuthorizedRepoUserDO r SET r.active = FALSE, r.updatedDate=:updatedDate WHERE r.repository =:repository AND r.active=TRUE"),
	@NamedQuery(name = "AuthorizedRepoUserDO.getExistingIdenticalPersonalRepository", 
	query = "SELECT r FROM AuthorizedRepoUserDO r,IN (r.repository) t WHERE LOWER(r.userAccountId)=LOWER(:userAccountId) AND t.createdBy.tenantId=:tenantId AND t.repoType=:repoType AND LOWER(t.accountId)=LOWER(:accountId) AND r.user=:user AND t.active = TRUE"),
	@NamedQuery(name = "AuthorizedRepoUserDO.updateAuthorizedUserByRepoTypeAndTenantId", 
	query = "UPDATE AuthorizedRepoUserDO a SET a.active = FALSE, a.updatedDate=:updatedDate WHERE a.user.tenantId=:tenantId AND a.repository IN (SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId )"),
	@NamedQuery(name = "AuthorizedRepoUserDO.updatePersonalAuthorizedUserByRepoTypeAndTenantId", 
	query = "UPDATE AuthorizedRepoUserDO a SET a.active = FALSE, a.updatedDate=:updatedDate WHERE a.user.tenantId=:tenantId AND a.repository IN (SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId AND r.shared = FALSE)"),
	})

public class AuthorizedRepoUserDO {

	public AuthorizedRepoUserDO() {
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "auth_repo_user_gen")
	@SequenceGenerator(name = "auth_repo_user_gen", sequenceName = "rms_auth_repo_user_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private long id;

	@Column(name = "user_account_id", length=1024, nullable = false)
	private String userAccountId;
	
	@Embedded
	private TenantUser user;
	
	public String getAccountId() {
		return userAccountId;
	}

	public void setAccountId(String userAccountId) {
		this.userAccountId = userAccountId;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_date", nullable = false)
	private Date updatedDate; 
	
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	@Column(name = "is_active", nullable = false)
	private boolean active;
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "repo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_authorized_2"))
	private RepositoryDO repository;
	
	public RepositoryDO getRepository() {
		return repository;
	}

	public void setRepository(RepositoryDO onedriveRepo) {
		this.repository = onedriveRepo;
	}

	public TenantUser getUser() {
		return user;
	}

	public void setUser(TenantUser user) {
		this.user = user;
	}

	@Lob
	@Column(name = "sec_token", length=2048)
	private String secToken;
	
	@Column(name = "account_name", length=256)
	private String accountName;
	
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public long getId() {
		return id;
	}

	public String getSecToken() {
		return secToken;
	}

	public void setSecToken(String secToken) {
		this.secToken = secToken;
	}

	@Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if ( !(other instanceof AuthorizedRepoUserDO) ){
    	return false;
    }

    final AuthorizedRepoUserDO repoUser = (AuthorizedRepoUserDO) other;
    
    if (!Objects.equals(getUser(), repoUser.getUser())) {
    	return false;
    }
    
    if (!Objects.equals(getAccountName(), repoUser.getAccountName())){
    	return false;
    }

    if (!Objects.equals(getRepository().getId(), repoUser.getRepository().getId())){
    	return false;
    }
    
    return true;
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(getUser());
		result = 29*result + Objects.hashCode(getAccountName());
		result = 29*result + Objects.hashCode(getRepository().getId());
		return result;
	}
	
}
