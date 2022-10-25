package com.nextlabs.kms.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.nextlabs.kms.entity.KeyRing;
import com.nextlabs.kms.entity.Tenant;

public interface KeyRingDAO extends BaseDAO<KeyRing> {
	void deleteKeyRing(Tenant tenant, String keyRingName);

	KeyRing getNonDeletedKeyRing(Tenant tenant, String keyRingName);

	KeyRing getActiveKeyRing(Tenant tenant, String keyRingName);

	Set<String> getNonDeletedKeyRings(Tenant tenant);

	Date getLatestModifiedDate(Tenant tenant);

	List<KeyRing> getActiveKeyRings(Tenant tenant);
}
