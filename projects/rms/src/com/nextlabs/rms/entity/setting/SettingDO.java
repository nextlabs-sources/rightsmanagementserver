/**
 * 
 */
package com.nextlabs.rms.entity.setting;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
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
@Table(name = "RMS_SETTING", indexes = {
		@Index(columnList = "tenant_id,setting_name", name = "idx_setting_1", unique = true)})
@NamedQueries({ 
	@NamedQuery(name = "SettingDO.findAllByTenantId",
			query = "SELECT s FROM SettingDO s WHERE s.tenantId=:tenantId"),
	@NamedQuery(name = "SettingDO.updateValueByNameAndTenantId",
			query = "UPDATE SettingDO s SET s.value=:setting_value WHERE s.name=:setting_name AND s.tenantId=:tenantId"),
	@NamedQuery(name = "SettingDO.fetchSettingByNameAndTenantId",
			query = "SELECT s FROM SettingDO s WHERE s.tenantId=:tenantId AND s.name=:name")})
public class SettingDO {

	/**
	 * 
	 */
	public SettingDO() {
	}
	
	public SettingDO(String name, String value){
		this.name= name;
		this.value = value;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "setting_gen")
	@SequenceGenerator(name = "setting_gen", sequenceName = "rms_setting_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@Column(name = "tenant_id", length = FieldConstants.TENANT_ID_LENGTH, nullable = false)
	private String tenantId;

	@Column(name = "setting_name", nullable = false, length = FieldConstants.ATTR_NAME_LENGTH)
	private String name;

	@Column(name = "setting_value", nullable = true, length = FieldConstants.ATTR_VALUE_LENGTH)
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Long getId() {
		return id;
	}
	
	@Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if ( !(other instanceof SettingDO) ) return false;

    final SettingDO setting = (SettingDO) other;

    if ( !Objects.equals(setting.getName(), getName())) {
    	return false;
    }

    return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName());
	}
}
