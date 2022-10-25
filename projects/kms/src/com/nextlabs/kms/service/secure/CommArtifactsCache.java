package com.nextlabs.kms.service.secure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommArtifactsCache {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, Artifact> artifactMap = new HashMap<>();

	private static final CommArtifactsCache instance = new CommArtifactsCache();

	private CommArtifactsCache() {
	}

	public static CommArtifactsCache getInstance() {
		return instance;
	}

	public String getEncodedCertificate(File certFile, String alias, String password) {
		try {
			long lastModifiedTime = certFile.lastModified();
			Artifact artifact = artifactMap.get(certFile.getAbsolutePath());
			if (artifact == null) {
				return cacheEncodedString(certFile, alias, password);
			} else {
				if (artifact.getTimeStamp() != lastModifiedTime) {
					return cacheEncodedString(certFile, alias, password);
				} else {
					return (String) artifactMap.get(certFile.getAbsolutePath()).getKeyManagers();
				}
			}
		} catch (Exception e) {
			logger.error("Error while opening hard coded NextLabs certificate.");
			return null;
		}
	}

	private String cacheEncodedString(File certFile, String alias, String password) {
		InputStream inStream = null;
		String encodedCertString = null;
		try {
			inStream = new FileInputStream(certFile);
			KeyStore ks = KeyStore.getInstance("jks");
			ks.load(inStream, password.toCharArray());
			Certificate cert = ks.getCertificate(alias);
			if (cert == null) {
				throw new RuntimeException("Unable to find certificate with specified alias");
			}
			byte[] bytes = cert.getEncoded();
			encodedCertString = Base64.encodeBase64String(bytes);
		} catch (Exception e) {
			logger.error("Error occurred when accessing certificate: {}", e.getMessage(), e);
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
			} catch (IOException ioe) {
				logger.error("Error while trying to use hard coded NextLabs certificate", ioe);
			}
		}
		synchronized (encodedCertString) {
			Artifact tempArtifact = new Artifact(certFile.lastModified(), encodedCertString);
			artifactMap.put(certFile.getAbsolutePath(), tempArtifact);
		}

		return encodedCertString;
	}

}
