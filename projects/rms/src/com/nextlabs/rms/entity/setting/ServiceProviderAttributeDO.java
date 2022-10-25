/**
 * 
 */
package com.nextlabs.rms.entity.setting;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.nextlabs.rms.entity.FieldConstants;


/**
 * @author nnallagatla
 *
 */
@Entity
@Table(name = "RMS_SVC_PROVIDER_ATTR")
@NamedQueries({ 
	@NamedQuery(name = "ServiceProviderAttributeDO.deleteAttributesByProviderId", 
			query = "DELETE FROM ServiceProviderAttributeDO s WHERE s.serviceProvider = :provider"),})
public class ServiceProviderAttributeDO implements Serializable {
	private static final long serialVersionUID = -4069175024787375598L;

	public ServiceProviderAttributeDO() {
		
	}
	
	public ServiceProviderAttributeDO(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "svc_provider_attr_gen")
	@SequenceGenerator(name = "svc_provider_attr_gen", sequenceName = "rms_svc_prov_attr_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_provider_attr_1") )
	private ServiceProviderDO serviceProvider;
	
	@Column(name = "name", length = FieldConstants.ATTR_NAME_LENGTH, nullable = false)
	private String name;
	
	@Column(name = "value", length = FieldConstants.ATTR_VALUE_LENGTH, nullable = false)
	private String value;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ServiceProviderAttributeDO) {
			ServiceProviderAttributeDO oth = (ServiceProviderAttributeDO) obj;
			return Objects.equals(getId(), oth.getId());
		}
		return false;
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ServiceProviderDO getServiceProvider() {
		return serviceProvider;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int hash = Objects.hash(getId());
		return hash;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setServiceProvider(ServiceProviderDO provider) {
		this.serviceProvider = provider;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
