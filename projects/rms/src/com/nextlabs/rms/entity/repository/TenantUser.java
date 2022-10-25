/**
 * 
 */
package com.nextlabs.rms.entity.repository;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.nextlabs.rms.entity.FieldConstants;

/**
 * @author nnallagatla
 *
 */
@Embeddable
public class TenantUser implements Serializable {

	private static final long serialVersionUID = 669596571793405576L;

	@Column(name = "tenant_id", length = FieldConstants.TENANT_ID_LENGTH, nullable = false)
	private String tenantId;

	@Column(name = "user_id", length = FieldConstants.USER_ID_LENGTH, nullable = false)
	private String userId;

	public TenantUser() {

	}

	public TenantUser(String tenantId, String userId) {
		this.tenantId = tenantId;
		this.userId = userId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(getTenantId());
		result = 29 * result + Objects.hashCode(getUserId());
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof TenantUser)) {
			return false;
		}

		final TenantUser user = (TenantUser) other;

		if (!Objects.equals(user.getTenantId(), getTenantId())) {
			return false;
		}

		if (!Objects.equals(user.getUserId(), getUserId())) {
			return false;
		}

		return true;
	}
	
	@Override
	public String toString() {
		return "TenantID: " + tenantId + ", UserID: " + userId;
	}

}
