package com.nextlabs.kms.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.nextlabs.kms.entity.enums.ProviderType;

@Entity
@Table(name = "KMS_PROVIDER", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "tenant_id" }, name = "uk_km_provider_1") })
public class Provider implements Serializable {
	private static final long serialVersionUID = -116296836090048272L;
	private Long id;
	private Tenant tenant;
	private ProviderType providerType;
	private List<ProviderAttribute> attributes = new ArrayList<>();
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Provider) {
			Provider oth = (Provider) obj;
			return ObjectUtils.nullSafeEquals(getId(), oth.getId());
		}
		return false;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "provider", cascade = { CascadeType.ALL })
	public List<ProviderAttribute> getAttributes() {
		return attributes;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "kms_provider_gen")
	@SequenceGenerator(name = "kms_provider_gen", sequenceName = "kms_provider_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	public Long getId() {
		return id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type", nullable = false, length = 50)
	public ProviderType getProviderType() {
		return providerType;
	}

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	public Tenant getTenant() {
		return tenant;
	}

	@Override
	public int hashCode() {
		int hash = ObjectUtils.nullSafeHashCode(getId());
		return hash;
	}

	public void setAttributes(List<ProviderAttribute> attributes) {
		this.attributes = attributes;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setProviderType(ProviderType providerType) {
		this.providerType = providerType;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	
	@Transient
	public void addAttribute(ProviderAttribute attribute) {
		Assert.notNull(attribute);
		attribute.setProvider(this);
		getAttributes().add(attribute);
	}
}
