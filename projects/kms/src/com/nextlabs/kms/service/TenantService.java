package com.nextlabs.kms.service;

import java.util.List;

import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.model.ProviderTypeAttribute;

public interface TenantService {
	Tenant getTenant(String code);

	Tenant createTenant(String code, ProviderType providerType, List<ProviderTypeAttribute> attributes)
			throws KeyManagementException;

	Tenant getTenantDetail(String code);
}
