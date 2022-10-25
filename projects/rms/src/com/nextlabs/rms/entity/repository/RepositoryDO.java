/**
 *
 */
package com.nextlabs.rms.entity.repository;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.nextlabs.rms.entity.FieldConstants;
import com.nextlabs.rms.entity.setting.ServiceProviderType;

/**
 * @author psheoran
 *
 */
@Entity
@Table(name = "RMS_REPOSITORY", indexes = {
		@Index(columnList = "updated_date, is_active", name = "idx_repository_1", unique = false),
		@Index(columnList = "tenant_id, user_id, repository_type, is_active", name = "idx_repository_2", unique = false)})
@NamedQueries({ 
	@NamedQuery(name = "RepositoryDO.findSharedRepoByTenantId", 
			query = "SELECT r FROM RepositoryDO r WHERE r.createdBy.tenantId=:tenantId AND r.active = TRUE AND r.shared = TRUE"),
	@NamedQuery(name = "RepositoryDO.findSharedRepoByNameAndTenantId", 
			query = "SELECT r FROM RepositoryDO r WHERE r.createdBy.tenantId=:tenantId AND LOWER(r.repoName)=LOWER(:repoName) AND r.active = TRUE AND r.shared = TRUE"),
	@NamedQuery(name = "RepositoryDO.findPersonalRepoByNameUserIdAndTenantId", 
			query = "SELECT r FROM RepositoryDO r WHERE r.createdBy=:user AND LOWER(r.repoName)=LOWER(:repoName) AND r.active = TRUE AND r.shared = FALSE"),
	@NamedQuery(name = "RepositoryDO.findSharedRepoByTenantIdAccountIdRepoType", 
			query = "SELECT r FROM RepositoryDO r WHERE r.repoType=:repoType AND LOWER(r.accountId)=LOWER(:accountId) AND r.createdBy.tenantId=:tenantId AND r.active = TRUE AND r.shared = TRUE"),
	@NamedQuery(name = "RepositoryDO.findById", 
			query = "SELECT r FROM RepositoryDO r WHERE r.id=:id AND r.active = TRUE" ),
    @NamedQuery(name = "RepositoryDO.deleteInactiveRepos",
    		query = "DELETE FROM RepositoryDO r WHERE r.active = FALSE AND r.updatedDate < :cutoffDate"),
    @NamedQuery(name = "RepositoryDO.updateRepositoryByRepoTypeAndTenantId",
    		query = "UPDATE RepositoryDO r SET r.active = FALSE, r.updatedDate=:updatedDate WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId" ),
	@NamedQuery(name = "RepositoryDO.updatePersonalRepositoryByRepoTypeAndTenantId",
	query = "UPDATE RepositoryDO r SET r.active = FALSE, r.updatedDate=:updatedDate WHERE r.repoType=:repoType AND r.active = TRUE AND r.createdBy.tenantId=:tenantId AND r.shared = FALSE" )
	})

public class RepositoryDO {

	public RepositoryDO() {
		setActive(true);
		setShared(false);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "repository_gen")
	@SequenceGenerator(name = "repository_gen", sequenceName = "rms_repo_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private long id;

	@Column(name = "repository_name", length = 256, nullable = false)
	private String repoName;

	@Enumerated(EnumType.STRING)
	@Column(name = "repository_type", length=FieldConstants.SVC_PROVIDER_TYPE_LENGTH, nullable=false)
	private ServiceProviderType repoType;

	@Column(name = "is_shared", nullable = false)
	private boolean shared;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	private Date createdDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_date", nullable = false)
	private Date updatedDate;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Embedded
	private TenantUser createdBy;

	@Column(name = "account_id", length=1024, nullable = false)
	private String accountId;

	@Version
	@Column(name = "version_lock", nullable = false)
	private long version = 0L;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
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

	public ServiceProviderType getRepoType() {
		return repoType;
	}

	public void setRepoType(ServiceProviderType repoType) {
		this.repoType = repoType;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof RepositoryDO)) {
			return false;
		}

		final RepositoryDO repo = (RepositoryDO) other;

		if (!Objects.equals(repo.getRepoType(), getRepoType())) {
			return false;
		}

		if (!Objects.equals(repo.getCreatedDate(), getCreatedDate())) {
			return false;
		}

		if (!Objects.equals(repo.getCreatedBy(), getCreatedBy())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(getCreatedBy());
		result = 29*result + Objects.hashCode(getRepoType());
		result = 29*result + Objects.hashCode(getCreatedDate());
		result = 29*result + Objects.hashCode(getCreatedBy());
		return result;
	}

	public TenantUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(TenantUser createdBy) {
		this.createdBy = createdBy;
	}

	public long getVersion() {
		return version;
	}

	protected void setVersion(long version) {
		this.version = version;
	}

}
