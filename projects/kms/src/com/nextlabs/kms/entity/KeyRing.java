package com.nextlabs.kms.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.Nationalized;

import com.nextlabs.kms.entity.enums.Status;
import com.nextlabs.kms.interceptor.ICreatedDate;
import com.nextlabs.kms.interceptor.IUpdatedDate;

@Entity
@Table(name = "KMS_KEYRING")
public class KeyRing implements Serializable, ICreatedDate, IUpdatedDate {
	private static final long serialVersionUID = 3199595189834864113L;
	private Long id;
	private String name;
	private Date lastUpdatedDate;
	private Date createdDate;
	private Long version;
	private byte[] data;
	private String type;
	private Tenant tenant;
	private Status status;
	private Boolean deleted = false;

	KeyRing() {
		// for hibernate
	}

	public KeyRing(String name, byte[] keyStoreData, String format, Tenant tenant) {
		setName(name);
		setType(format);
		setData(data);
		setStatus(Status.ACTIVE);
		setTenant(tenant);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "kms_keyring_gen")
	@SequenceGenerator(name = "kms_keyring_gen", sequenceName = "kms_keyring_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	public Long getId() {
		return id;
	}

	void setId(Long id) {
		this.id = id;
	}
	
	@Nationalized
	@Column(name = "name", nullable = false, length = 255)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_date", nullable = false)
	@Override
	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	@Override
	public void setLastUpdatedDate(Date lastUpdated) {
		this.lastUpdatedDate = lastUpdated;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	@Override
	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public void setCreatedDate(Date created) {
		this.createdDate = created;
	}

	@Version
	@Column(name = "version_lock", nullable = false)
	public Long getVersion() {
		return version;
	}

	void setVersion(Long version) {
		this.version = version;
	}

	@Lob
	@Column(name = "data", nullable = true, length = Integer.MAX_VALUE)
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Column(name = "keyring_type", nullable = false, length = 255)
	public String getType() {
		return type;
	}

	public void setType(String format) {
		this.type = format;
	}

	public void setKeyStoreData(byte[] keyStoreData) {
		this.data = keyStoreData;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_km_keyring_1") )
	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 255)
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Column(name = "deleted", nullable = false)
	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
