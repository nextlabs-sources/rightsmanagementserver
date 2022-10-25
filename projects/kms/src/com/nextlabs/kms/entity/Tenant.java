package com.nextlabs.kms.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.nextlabs.kms.interceptor.ICreatedDate;

@Entity
@Table(name = "KMS_TENANT", uniqueConstraints = { @UniqueConstraint(columnNames = "code", name = "uk_km_tenant_1") })
public class Tenant implements Serializable, ICreatedDate {
	private static final long serialVersionUID = -6729903284691114082L;
	private Long id;
	private String code;
	private Date createdDate;
	private Provider provider;

	@Transient
	public void addProvider(Provider provider) {
		Assert.notNull(provider);
		provider.setTenant(this);
		setProvider(provider);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Tenant) {
			Tenant oth = (Tenant) obj;
			return ObjectUtils.nullSafeEquals(getId(), oth.getId());
		}
		return false;
	}

	@Column(name = "code", nullable = false, length = 128)
	public String getCode() {
		return code;
	}

	@Override
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	public Date getCreatedDate() {
		return createdDate;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "kms_tenant_gen")
	@SequenceGenerator(name = "kms_tenant_gen", sequenceName = "kms_tenant_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	public Long getId() {
		return id;
	}

	@OneToOne(fetch = FetchType.EAGER, mappedBy = "tenant", optional = false, cascade = { CascadeType.ALL })
	public Provider getProvider() {
		return provider;
	}

	@Override
	public int hashCode() {
		int hash = ObjectUtils.nullSafeHashCode(getId());
		return hash;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	protected void setProvider(Provider provider) {
		this.provider = provider;
	}
}
