package com.nextlabs.kms.exception;

public class TenantNotFoundException extends KeyManagementException {
	private static final long serialVersionUID = 1380853831458670984L;
	private String tenantId;

	protected TenantNotFoundException(String msg, String tenantId) {
		super(msg);
		this.tenantId = tenantId;
	}

	public TenantNotFoundException(String tenantId) {
		this("Tenant is not found :" + tenantId, tenantId);
	}

	public String getTenantId() {
		return tenantId;
	}
}
