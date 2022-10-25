package com.nextlabs.kms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nextlabs.kms.entity.Provider;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.exception.TenantNotFoundException;
import com.nextlabs.kms.service.IKeyRingManager;
import com.nextlabs.kms.service.KeyRingService;
import com.nextlabs.kms.service.TenantService;

@Service
public class ProviderService {
	@Autowired(required = true)
	private TenantService tenantService;
	@Autowired(required = true)
	private KeyRingService keyRingService;

	public IKeyRingManager getKeyRingManager(String tenantCode) throws KeyManagementException {
		Tenant tenant = tenantService.getTenantDetail(tenantCode);
		if (tenant == null) {
			throw new TenantNotFoundException(tenantCode);
		}
		Provider provider = tenant.getProvider();
		ProviderType providerType = provider.getProviderType();
		if (providerType == ProviderType.DEFAULT) {
			return new KeyRingManager(tenant, keyRingService);
		}
		throw new KeyManagementException("Unknown provider");
	}
}
