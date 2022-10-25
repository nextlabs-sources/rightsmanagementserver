package com.nextlabs.kms.dao.impl;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import com.nextlabs.kms.dao.CertificateDAO;
import com.nextlabs.kms.service.secure.CommArtifactsCache;

@Repository
public class FileSystemCertificateDAOImpl implements CertificateDAO {
	public static final String KMS_KEYSTORE_FILE_SECURE = "kms-keystore.jks";
	public static final String KMS_KEYSTORE_FILE_UNSECURE = "kms-temp-keystore.jks";
	public static final String KMS_KEYSTORE_SECURE_ALIAS = "kms";
	public static final String KMS_KEYSTORE_UNSECURE_ALIAS = "kms-temp";
	@Autowired(required = true)
	@Qualifier(value="kms.datadir")
	private Resource resource;

	@Override
	public String getSecureCertificate() {
		try {
			File f = new File(resource.getFile().getAbsolutePath() + File.separator + "cert", KMS_KEYSTORE_FILE_SECURE);
			if (!f.exists() || !f.isFile()) {
				throw new FileNotFoundException("Unable to find certificate: " + f.getAbsolutePath());
			}
			String encodedCertificate = CommArtifactsCache.getInstance().getEncodedCertificate(f, KMS_KEYSTORE_SECURE_ALIAS,
					"123next!");
			return encodedCertificate;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String getUnsecureCertificate() {
		try {
			File f = new File(resource.getFile().getAbsolutePath() + File.separator + "cert", KMS_KEYSTORE_FILE_UNSECURE);
			if (!f.exists() || !f.isFile()) {
				throw new FileNotFoundException("Unable to find certificate: " + f.getAbsolutePath());
			}
			String encodedCertificate = CommArtifactsCache.getInstance().getEncodedCertificate(f, KMS_KEYSTORE_UNSECURE_ALIAS,
					"123next!");
			return encodedCertificate;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
