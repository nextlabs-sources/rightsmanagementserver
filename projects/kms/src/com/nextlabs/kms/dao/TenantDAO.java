package com.nextlabs.kms.dao;

import com.nextlabs.kms.entity.Tenant;

public interface TenantDAO extends BaseDAO<Tenant> {
	Tenant getTenant(String code);

	Tenant getTenantDetail(String code);
}
