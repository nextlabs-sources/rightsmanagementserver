package com.nextlabs.kms;

import java.util.Collection;

import com.nextlabs.kms.entity.KeyRing;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.exception.KeyManagementException;

public interface IKeyRing {

	KeyRing getKeyRingDO();

	String getName();

	IKey getKey(IKeyId keyId) throws KeyManagementException;

	Collection<IKey> getKeys() throws KeyManagementException;

	void setKey(IKey key) throws KeyManagementException;

	/**
	 * close the keyring
	 * 
	 * @throws KeyManagementException
	 */
	void close() throws KeyManagementException;

	boolean isClosed();

	Tenant getTenant();
}
