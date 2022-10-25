package com.nextlabs.kms.service;

import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyManagementException;

public interface IKeyGeneratorManager {
	byte[] generateKey(KeyAlgorithm keyAlgorithm) throws KeyManagementException;
}
