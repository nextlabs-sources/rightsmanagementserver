package com.nextlabs.kms.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.service.IKeyRingManager;
import com.nextlabs.kms.service.KeyRingService;

public class KeyRingManager implements IKeyRingManager {
	private static final Logger log = LoggerFactory.getLogger(KeyRingManager.class);
	private final Tenant tenant;
	private final KeyRingService keyRingService;

	public KeyRingManager(Tenant tenant, KeyRingService keyRingService) {
		this.tenant = tenant;
		this.keyRingService = keyRingService;
	}

	@Override
	public IKeyRing createKeyRing(final String name) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("createKeyRing ({})", name);
		}
		IKeyRing keyRing = keyRingService.createKeyRing(tenant, name);
		return keyRing;
	}

	@Override
	public IKeyRing getKeyRing(final String name) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("getKeyRing ({})", name);
		}
		IKeyRing keyRing = keyRingService.getKeyRing(tenant, name);
		return keyRing;
	}

	@Override
	public List<IKeyRing> getKeyRings() throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("getKeyRings");
		}
		List<IKeyRing> results = keyRingService.getKeyRings(tenant);
		return results;
	}

	@Override
	public boolean deleteKeyRing(final String name) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("deleteKeyRing ({})", name);
		}
		boolean result = keyRingService.deleteKeyRing(tenant, name);
		return result;
	}

	@Override
	public boolean deleteKeyRing(final IKeyRing keyRing) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("deleteKeyRing keyRing(" + toShortName(keyRing) + ")");
		}
		boolean result = keyRingService.deleteKeyRing(keyRing);
		return result;
	}

	@Override
	public Set<String> getKeyRingNames() throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("getKeyRings");
		}
		Set<String> results = keyRingService.getKeyRingNames(tenant);
		return results;
	}

	private String toShortName(IKeyRing keyRing) {
		return keyRing != null ? keyRing.getName() : "null";
	}

	@Override
	public long getLatestModifiedDate() throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("getLastModifiedDate");
		}
		long timeInMillis = keyRingService.getLatestModifiedDate(tenant);
		return timeInMillis;
	}

	@Override
	public boolean disableKeyRing(String keyRingName) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("disableKeyRing ({})", keyRingName);
		}
		boolean result = keyRingService.disableKeyRing(tenant, keyRingName);
		return result;
	}

	@Override
	public IKeyId generateKey(String keyRingName, KeyAlgorithm keyAlgorithm) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("generateKey for keyring ({}) with {} {}", keyRingName, keyAlgorithm.getName(), keyAlgorithm.getLength());
		}
		IKeyId result = keyRingService.generateKey(tenant, keyRingName, keyAlgorithm);
		return result;
	}

	@Override
	public boolean enableKeyRing(String keyRingName) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("enableKeyRing ({})", keyRingName);
		}
		boolean result = keyRingService.enableKeyRing(tenant, keyRingName);
		return result;
	}

	@Override
	public IKey getKey(String keyRingName, IKeyId keyId) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("getKey with id {} and timestamp {} from ({}) ", keyId.getHash(), keyId.getCreationTimeStamp(),
					keyRingName);
		}
		IKey result = keyRingService.getKey(tenant, keyRingName, keyId);
		return result;
	}

	@Override
	public IKey getLatestKey(String keyRingName) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("getLatestKey ({}) ", keyRingName);
		}
		IKey result = keyRingService.getLatestKey(tenant, keyRingName);
		return result;
	}

	@Override
	public void importKeyRing(String keyRingName, List<IKey> keys) throws KeyManagementException {
		if (log.isTraceEnabled()) {
			log.trace("importKeyRing with keyRingName {} ", keyRingName);
		}
		keyRingService.importKeyRing(tenant, keyRingName, keys);
	}

	@Override
	public void close() throws IOException {
	}
}
