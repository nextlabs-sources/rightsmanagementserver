/**
 * 
 */
package com.nextlabs.rms.entity.repository;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author nnallagatla
 *
 */

@MappedSuperclass
public abstract class CachedFileDO {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "cache_file_gen")
	@Column(name = "id", nullable = false)
	private long id;
	
	@ManyToOne(optional = false)
	private RepositoryDO repository;
	
	@Embedded
	private TenantUser user;
	
	@Lob
	@Column(name = "file_id", nullable = false)
	private String fileId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	private Date createdDate; 
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_date", nullable = false)
	private Date updatedDate; 

	@Column(name = "is_active", nullable = false)
	private boolean active;

	public long getId() {
		return id;
	}
	
	protected void setId(long id) {
		this.id = id;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public RepositoryDO getRepository() {
		return repository;
	}

	public void setRepository(RepositoryDO repository) {
		this.repository = repository;
	}

	public TenantUser getUser() {
		return user;
	}

	public void setUser(TenantUser user) {
		this.user = user;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if ( !(other instanceof CachedFileDO) ){
    	return false;
    }

    final CachedFileDO cfile = (CachedFileDO) other;
    
    if (!Objects.equals(cfile.getUser(), getUser())){
    	return false;
    }
    
    if (!Objects.equals(cfile.getRepository(), getRepository())) {
    	return false;
    }
    
    if (!Objects.equals(cfile.getFileId(), getFileId())) {
    	return false;
    }
    
    if (!Objects.equals(cfile.getCreatedDate(), getCreatedDate())) {
    	return false;
    }

    return true;
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(getUser());
		result = 29*result + Objects.hashCode(getRepository());
		result = 29*result + Objects.hashCode(getFileId());
		result = 29*result + Objects.hashCode(getCreatedDate());
		return result;
	}
	
}
