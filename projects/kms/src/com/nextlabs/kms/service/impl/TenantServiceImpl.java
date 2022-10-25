package com.nextlabs.kms.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextlabs.kms.controller.KMSInitializationManager;
import com.nextlabs.kms.dao.TenantDAO;
import com.nextlabs.kms.entity.Provider;
import com.nextlabs.kms.entity.ProviderAttribute;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.exception.TenantAlreadyExistsException;
import com.nextlabs.kms.model.ProviderTypeAttribute;
import com.nextlabs.kms.service.TenantService;

@Service
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {
	@Autowired
	private TenantDAO tenantDAO;

	@Override
	public Tenant getTenant(String code) {
		String tenantId = (code == null || code.length()<=0) ? KMSInitializationManager.DEFAULT_TENANT_ID : code;
		return tenantDAO.getTenant(tenantId);
	}

	@Transactional(readOnly = false)
	@Override
	public Tenant createTenant(String code, ProviderType providerType, List<ProviderTypeAttribute> attributes)
			throws KeyManagementException {
		Tenant tenant = getTenant(code);
		if (tenant != null) {
			throw new TenantAlreadyExistsException(code);
		}
		//TODO add validation 
		Provider provider = new Provider();
		provider.setProviderType(providerType);
		if (attributes != null) {
			for (ProviderTypeAttribute attribute : attributes) {
				ProviderAttribute attr = new ProviderAttribute();
				attr.setName(attribute.getName());
				attr.setValue(attribute.getValue());
				provider.addAttribute(attr);
			}
		}
		tenant = new Tenant();
		tenant.setCode(code);
		tenant.addProvider(provider);
		
		tenantDAO.save(tenant);
		return tenant;
	}

	@Override
	public Tenant getTenantDetail(String code) {
		String tenantId = (code == null || code.length()<=0) ? KMSInitializationManager.DEFAULT_TENANT_ID : code;
		return tenantDAO.getTenantDetail(tenantId);
	}
}
