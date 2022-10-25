package com.nextlabs.kms.service;

import com.nextlabs.kms.exception.KeyManagementException;

public interface SecurityService {
	void verifyToken(String token, boolean secure) throws KeyManagementException;

	String getKeyStoreSecureCertificate();
}
