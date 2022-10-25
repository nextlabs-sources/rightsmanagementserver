package com.nextlabs.kms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.nextlabs.kms.dao.CertificateDAO;
import com.nextlabs.kms.exception.AccessDeniedException;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.service.SecurityService;

@Service
public class SecurityServiceImpl implements SecurityService {
	@Autowired(required = true)
	private CertificateDAO certificateDAO;

	@Override
	public void verifyToken(String token, boolean secure) throws KeyManagementException {
		String encodedToken = null;
		if (secure) {
			encodedToken = certificateDAO.getSecureCertificate();
		} else {
			encodedToken = certificateDAO.getUnsecureCertificate();
		}
		if (token == null || !ObjectUtils.nullSafeEquals(token, encodedToken)) {
			throw new AccessDeniedException("Invalid token");
		}
	}

	@Override
	public String getKeyStoreSecureCertificate() {
		String certificate = certificateDAO.getSecureCertificate();
		return certificate;
	}
}
