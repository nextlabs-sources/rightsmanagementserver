package com.nextlabs.kms.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.util.ObjectUtils;

@Entity
@Table(name = "KMS_PROVIDER_ATTRIBUTE")
public class ProviderAttribute implements Serializable {
	private static final long serialVersionUID = 3789001754447703609L;
	private Long id;
	private Provider provider;
	private String name;
	private String value;

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ProviderAttribute) {
			ProviderAttribute oth = (ProviderAttribute) obj;
			return ObjectUtils.nullSafeEquals(getId(), oth.getId());
		}
		return false;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "kms_provider_attr_gen")
	@SequenceGenerator(name = "kms_provider_attr_gen", sequenceName = "kms_provider_attr_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	public Long getId() {
		return id;
	}

	@Column(name = "name", length = 255, nullable = false)
	public String getName() {
		return name;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_provider_attr_1") )
	public Provider getProvider() {
		return provider;
	}

	@Column(name = "value", length = 500, nullable = false)
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int hash = ObjectUtils.nullSafeHashCode(getId());
		return hash;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
