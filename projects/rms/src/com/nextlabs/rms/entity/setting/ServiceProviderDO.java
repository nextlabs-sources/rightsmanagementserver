/**
 * 
 */
package com.nextlabs.rms.entity.setting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nextlabs.rms.entity.FieldConstants;


/**
 * @author nnallagatla
 *
 */
@Entity
@Table(name = "RMS_SVC_PROVIDER", indexes = {
		@Index(columnList = "tenant_id,provider_type", name = "idx_svc_prov_1", unique = false),
		@Index(columnList = "is_active,updated_date", name = "idx_svc_prov_2", unique = false)})
@NamedQueries({ 
	@NamedQuery(name = "ServiceProviderDO.getServiceProviderDetailsByTenantIdAndId", 
			query = "SELECT DISTINCT s FROM ServiceProviderDO s LEFT JOIN FETCH s.attributes WHERE s.tenantId=:tenantId AND s.id=:id AND s.active=TRUE"),
	@NamedQuery(name = "ServiceProviderDO.getServiceProviderDetailsById", 
	query = "SELECT DISTINCT s FROM ServiceProviderDO s LEFT JOIN FETCH s.attributes WHERE s.id=:id AND s.active=TRUE"),
	@NamedQuery(name = "ServiceProviderDO.getServiceProviderByTenantIdAndType",
	query = "SELECT s FROM ServiceProviderDO s WHERE s.tenantId=:tenantId AND s.serviceProviderType=:providerType AND s.active=TRUE"),
	@NamedQuery(name = "ServiceProviderDO.getAllByTenantId", 
	query = "SELECT s FROM ServiceProviderDO s WHERE s.tenantId=:tenantId AND s.active=TRUE"),
	@NamedQuery(name = "ServiceProviderDO.getAllDetailsByTenantId", 
			query = "SELECT DISTINCT s FROM ServiceProviderDO s LEFT JOIN FETCH s.attributes WHERE s.tenantId=:tenantId AND s.active=TRUE"),
    @NamedQuery(name = "ServiceProviderDO.deleteInactiveProviders",
    		query = "DELETE FROM ServiceProviderDO s WHERE s.active = FALSE AND s.updatedDate < :cutoffDate"),
	@NamedQuery(name = "ServiceProviderDO.getServiceProviderDetailsByTenantIdKeyAndValue",
	query = "SELECT DISTINCT s FROM ServiceProviderDO s LEFT JOIN FETCH s.attributes WHERE s.tenantId=:tenantId AND s.active=TRUE AND s.id IN "
			+ "(SELECT sa.serviceProvider.id FROM ServiceProviderAttributeDO sa WHERE sa.name=:key AND sa.value=:value) ")
	})

public class ServiceProviderDO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5891749088243707029L;

	/**
	 * 
	 */
	public ServiceProviderDO() {
		active = true;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "svc_provider_gen")
	@SequenceGenerator(name = "svc_provider_gen", sequenceName = "rms_svc_prov_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@Column(name = "tenant_id", length = FieldConstants.TENANT_ID_LENGTH, nullable = false)
	private String tenantId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type", nullable = false, length = FieldConstants.SVC_PROVIDER_TYPE_LENGTH)
	private ServiceProviderType serviceProviderType;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceProvider", cascade = { CascadeType.ALL }, orphanRemoval=true)
	private List<ServiceProviderAttributeDO> attributes = new ArrayList<>();

	@Column(name = "is_active", nullable = false)
	private boolean active;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	private Date createdDate; 
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_date", nullable = false)
	private Date updatedDate;
	
	public List<ServiceProviderAttributeDO> getAttributes() {
		return attributes;
	}

	public Long getId() {
		return id;
	}

	public ServiceProviderType getServiceProviderType() {
		return serviceProviderType;
	}
	
	public String getTenantId() {
		return tenantId;
	}

	@Override
	public int hashCode() {
		int hash = Objects.hashCode(getId());
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ServiceProviderDO) {
			ServiceProviderDO oth = (ServiceProviderDO) obj;
			return Objects.equals(getId(), oth.getId());
		}
		return false;
	}
	
	public void setAttributes(List<ServiceProviderAttributeDO> attributes) {
		this.attributes = attributes;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setServiceProviderType(ServiceProviderType providerType) {
		this.serviceProviderType = providerType;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	
	@Transient
	public void addAttribute(ServiceProviderAttributeDO attribute) {
		attribute.setServiceProvider(this);
		getAttributes().add(attribute);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

}
