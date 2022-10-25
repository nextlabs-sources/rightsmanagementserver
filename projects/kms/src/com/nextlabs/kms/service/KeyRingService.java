package com.nextlabs.kms.service;

import java.util.List;
import java.util.Set;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyManagementException;

public interface KeyRingService {
	IKeyRing createKeyRing(Tenant tenant, final String name) throws KeyManagementException;

	IKeyRing getKeyRing(Tenant tenant, final String name) throws KeyManagementException;

	List<IKeyRing> getKeyRings(Tenant tenant) throws KeyManagementException;

	void updateKeyRing(final IKeyRing keyRing) throws KeyManagementException;

	boolean deleteKeyRing(Tenant tenant, final String name) throws KeyManagementException;

	boolean deleteKeyRing(final IKeyRing keyRing) throws KeyManagementException;

	Set<String> getKeyRingNames(Tenant tenant) throws KeyManagementException;

	long getLatestModifiedDate(Tenant tenant) throws KeyManagementException;

	boolean disableKeyRing(Tenant tenant, String keyRingName) throws KeyManagementException;

	IKeyId generateKey(Tenant tenant, String keyRingName, KeyAlgorithm keyAlgorithm) throws KeyManagementException;

	boolean enableKeyRing(Tenant tenant, String keyRingName) throws KeyManagementException;

	IKey getKey(Tenant tenant, String keyRingName, IKeyId keyId) throws KeyManagementException;

	void importKeyRing(Tenant tenant, String keyRingName, List<IKey> keys) throws KeyManagementException;

	IKey getLatestKey(Tenant tenant, String keyRingName) throws KeyManagementException;
}
