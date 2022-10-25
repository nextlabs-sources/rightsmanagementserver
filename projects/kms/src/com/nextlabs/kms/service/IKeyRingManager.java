package com.nextlabs.kms.service;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyManagementException;

public interface IKeyRingManager extends Closeable {

	IKeyRing createKeyRing(String keyRingName) throws KeyManagementException;

	IKeyId generateKey(String keyRingName, KeyAlgorithm keyAlgorithm) throws KeyManagementException;

	IKeyRing getKeyRing(String name) throws KeyManagementException;

	List<IKeyRing> getKeyRings() throws KeyManagementException;

	IKey getKey(String keyRingName, IKeyId keyId) throws KeyManagementException;
	
	IKey getLatestKey(String keyRingName) throws KeyManagementException;

	boolean deleteKeyRing(String name) throws KeyManagementException;

	boolean deleteKeyRing(IKeyRing keyRing) throws KeyManagementException;

	Set<String> getKeyRingNames() throws KeyManagementException;

	long getLatestModifiedDate() throws KeyManagementException;

	boolean disableKeyRing(String keyRingName) throws KeyManagementException;

	boolean enableKeyRing(String keyRingName) throws KeyManagementException;

	void importKeyRing(String keyRingName, List<IKey> keys) throws KeyManagementException;
}
