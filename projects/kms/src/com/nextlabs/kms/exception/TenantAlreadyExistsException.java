package com.nextlabs.kms.exception;

public class TenantAlreadyExistsException extends KeyManagementException {
	private static final long serialVersionUID = 1380853831458670984L;
	private String tenantId;

	protected TenantAlreadyExistsException(String msg, String tenantId) {
		super(msg);
		this.tenantId = tenantId;
	}
	
	public TenantAlreadyExistsException(String tenantId) {
		this("Tenant already exists :" + tenantId, tenantId);
	}

	public String getTenantId() {
		return tenantId;
	}
}
