package com.nextlabs.rms.services.manager.ssl;

import com.nextlabs.rms.entity.security.SecurityDO;
import com.nextlabs.rms.persistence.EntityManagerHelper;
import com.nextlabs.rms.services.crypt.ReversibleEncryptor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.engine.io.IoUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.persistence.EntityManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.*;

public class CommArtifactsCache {
	private static Log logger = LogFactory.getLog(CommArtifactsCache.class.toString());

	public static final boolean MODE_UNSECURE = false;
	public static final boolean MODE_SECURE = true;
	private KeyStore keyStoreSecure = null;
	private KeyStore trustStoreSecure = null;
	
	
	//public static String CERT_PATH = ConfigManager.getInstance().getDataDir() + File.separator + "cert"
			//+ File.separator;

	public KeyStore getTrustStoreSecure() {
		return trustStoreSecure;
	}
	public KeyStore getKeyStoreSecure() {
		return keyStoreSecure;
	}

	private HashMap<String, Artifact> artifactMap = new HashMap<>();

	private static CommArtifactsCache instance = new CommArtifactsCache();

	private CommArtifactsCache() {
	}

	public static CommArtifactsCache getInstance() {
		return instance;
	}

	public KeyManager[] getKeyManagersUnsecure() {
		try {
			File file = new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KEYSTORE_FILE_UNSECURE);
			long lastModifiedTime = file.lastModified();
			if (artifactMap.get(file.getAbsolutePath()) == null) {
				return cacheUnsecureKeyFile(file);
			} else {
				Artifact ar = artifactMap.get(file.getAbsolutePath());
				if (ar.getTimeStamp() != lastModifiedTime) {
					return cacheUnsecureKeyFile(file);
				} else {
					return (KeyManager[]) artifactMap.get(file.getAbsolutePath()).getKeyManagers();
				}
			}
		}
		catch(Exception e) {
			 logger.error("Error while opening temp agent keystore.");
			 return null;
		}
	}

	public KeyManager[] getKeyManagersSecure(String tenantId) {
		try {
			File certFile = new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KEYSTORE_FILE_SECURE);
			if (!certFile.exists()) {
				Map<String, File> files = Collections.singletonMap(SSLSocketFactoryGenerator.KEYSTORE_FILE_SECURE, certFile);
				readFileFromDB(files, tenantId);
			}
			long lastModifiedTime = certFile.lastModified();
			if (artifactMap.get(certFile.getAbsolutePath()) == null) {
				return cacheSecureKeyFile(certFile, tenantId);
			} else {
				Artifact ar = artifactMap.get(certFile.getAbsolutePath());
				if (ar.getTimeStamp() != lastModifiedTime) {
					return cacheSecureKeyFile(certFile, tenantId);
				} else {
					return (KeyManager[]) artifactMap.get(certFile.getAbsolutePath()).getKeyManagers();
				}
			}
		}
		catch(Exception e) {
			logger.error("Error while trying to read password file for client certificate.", e);
			return null;
		}
	}

	public TrustManager[] getTrustManagersSecure(String tenantId) {
		try {
			File certFile = new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.TRUSTSTORE_FILE_SECURE);
			if (!certFile.exists()) {
				Map<String, File> files = Collections.singletonMap(SSLSocketFactoryGenerator.TRUSTSTORE_FILE_SECURE, certFile);
				readFileFromDB(files, tenantId);
			}
			long lastModifiedTime = certFile.lastModified();
			if (artifactMap.get(certFile.getAbsolutePath()) == null) {
				return cacheTrustManagersSecureFile(certFile, tenantId);
			} else {
				Artifact ar = artifactMap.get(certFile.getAbsolutePath());
				if (ar.getTimeStamp() != lastModifiedTime) {
					return cacheTrustManagersSecureFile(certFile, tenantId);
				} else {
					return (TrustManager[]) artifactMap.get(certFile.getAbsolutePath()).getKeyManagers();
				}
			}
		}
		catch(Exception e) {
			logger.error("Error while trying to read truststore file.", e);
			return null;
		}
	}

	public String getEncodedCertificate(String keyStoreName, String alias, String password)  {
		try {
			File certFile = new File(SSLSocketFactoryGenerator.CERT_PATH, keyStoreName);
			long lastModifiedTime = certFile.lastModified();
			if (artifactMap.get(certFile.getAbsolutePath()) == null) {
				return cacheEncodedString(certFile, alias, password);
			} else {
				Artifact ar = artifactMap.get(certFile.getAbsolutePath());
				if (ar.getTimeStamp() != lastModifiedTime) {
					return cacheEncodedString(certFile, alias, password);
				} else {
					return (String) artifactMap.get(certFile.getAbsolutePath()).getKeyManagers();
				}
			}
		} catch(Exception e){
			logger.error("Error while opening hard coded NextLabs certificate.");
			return null;
		}
		
	}

	private KeyManager[] cacheUnsecureKeyFile(File file) throws GeneralSecurityException {
		InputStream istream = null;
		KeyManagerFactory instance = null;
		try {
			istream = new FileInputStream(file);
			KeyStore kstore = KeyStore.getInstance(SSLSocketFactoryGenerator.KEYSTORE_TYPE);
			kstore.load(istream, SSLSocketFactoryGenerator.KEYSTORE_PASSWORD_UNSECURE.toCharArray());
			instance = KeyManagerFactory.getInstance(SSLSocketFactoryGenerator.SSL_ALGORITHM);
			instance.init(kstore, SSLSocketFactoryGenerator.KEYSTORE_PASSWORD_UNSECURE.toCharArray());
		} catch (IOException ie) {
			logger.error("Error while opening temp agent keystore.");
		} finally {
			if (istream != null) {
				try {
					istream.close();
				} catch (IOException ioe) {
					logger.error("Error while trying to temp agent keystore file.", ioe);
				}
			}
		}

		if (instance != null) {
			synchronized (instance) {
				Artifact keyArtifact = new Artifact(file.lastModified(), instance.getKeyManagers());
				artifactMap.put(file.getAbsolutePath(), keyArtifact);
			}
			return instance.getKeyManagers();
		} else {
			return null;
		}
	}

	private KeyManager[] cacheSecureKeyFile(File certFile, String tenantId) throws GeneralSecurityException {
		InputStream istream = null;
		String password = null;
		try {
			File passwordFile = new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KEYSTORE_FILE_PASSWORD);
			if (!passwordFile.exists()) {
				Map<String, File> files = Collections.singletonMap(SSLSocketFactoryGenerator.KEYSTORE_FILE_PASSWORD, passwordFile);
				readFileFromDB(files, tenantId);
			}
			istream = new FileInputStream(passwordFile);
			password = IoUtils.toString(istream);
		} catch (IOException ie) {
			logger.error("Error while trying to read password file for client certificate.", ie);
		} finally {
			try {
				if (istream != null) {
					istream.close();
				}
			} catch (IOException ioe) {
				logger.error("Error while trying to close password file.", ioe);
			}
		}

		ReversibleEncryptor decryptor = new ReversibleEncryptor();
		try {
			password = decryptor.decrypt(password);
			password = password.substring(5);
		} catch (Exception e) {
			logger.error("Error while decrypting password.", e);
		}

		KeyStore kstore = KeyStore.getInstance(SSLSocketFactoryGenerator.KEYSTORE_TYPE);

		try {
			istream = new FileInputStream(certFile);
			kstore.load(istream, password.toCharArray());
			keyStoreSecure=kstore;
		} catch (IOException ie) {
			logger.error("Error while trying to read password file for client certificate.", ie);
		} finally {
			try {
				istream.close();
			} catch (IOException ioe) {
				logger.error("Error while trying to close password file.", ioe);
			}
		}
		KeyManagerFactory kmfSecure = KeyManagerFactory.getInstance(SSLSocketFactoryGenerator.SSL_ALGORITHM);
		kmfSecure.init(kstore, password.toCharArray());
		synchronized (kmfSecure) {
			Artifact tempArtifact = new Artifact(certFile.lastModified(), kmfSecure.getKeyManagers());
			artifactMap.put(certFile.getAbsolutePath(), tempArtifact);	
		}	
		return kmfSecure.getKeyManagers();
	}

	private String cacheEncodedString(File certFile, String alias, String password) {
		InputStream inStream = null;
		String encodedCertString = null;
		try {
			inStream = new FileInputStream(certFile);
			KeyStore ks = KeyStore.getInstance("jks");
			ks.load(inStream, password.toCharArray());
			Certificate cert = ks.getCertificate(alias);
			byte[] bytes = cert.getEncoded();
			encodedCertString = Base64.encodeBase64String(bytes);
		} catch (Exception ie) {
			logger.error("Error while opening hard coded NextLabs certificate.");
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
			} catch (IOException ioe) {
				logger.error("Error while trying to use hard coded NextLabs certificate", ioe);
			}
		}
		synchronized (artifactMap) {
			Artifact tempArtifact = new Artifact(certFile.lastModified(), encodedCertString);
			artifactMap.put(certFile.getAbsolutePath(), tempArtifact);
		}
	
		return encodedCertString;
	}

	private TrustManager[] cacheTrustManagersSecureFile(File certFile, String tenantId) throws GeneralSecurityException {
		InputStream istream = null;
		String password = null;
		try {
			File passwordFile = new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KEYSTORE_FILE_PASSWORD);
			if (!passwordFile.exists()) {
				Map<String, File> files = Collections.singletonMap(SSLSocketFactoryGenerator.KEYSTORE_FILE_PASSWORD, passwordFile);
				readFileFromDB(files, tenantId);
			}
			istream = new FileInputStream(passwordFile);
			password = IoUtils.toString(istream);
		} catch (IOException ie) {
			logger.error("Error while trying to read password file for client certificate.", ie);
		} finally {
			try {
				if (istream != null) {
					istream.close();
				}
			} catch (IOException ioe) {
				logger.error("Error while trying to close password file.", ioe);
			}
		}

		ReversibleEncryptor decryptor = new ReversibleEncryptor();
		try {
			password = decryptor.decrypt(password);
			password = password.substring(5);
		} catch (Exception e) {
			logger.error("Error while decrypting password.", e);
		}
		KeyStore kstore = KeyStore.getInstance(SSLSocketFactoryGenerator.KEYSTORE_TYPE);
		try {
			istream = new FileInputStream(certFile);
			kstore.load(istream, password.toCharArray());
			trustStoreSecure = kstore;
		} catch (IOException ie) {
			logger.error("Error while trying to read truststore file.", ie);
		} finally {
			try {
				if (istream != null) {
					istream.close();
				}
			} catch (IOException ioe) {
				logger.error("Error while trying to close truststore file.", ioe);
			}
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(kstore);
		synchronized (tmf) {
			Artifact tempArtifact = new Artifact(certFile.lastModified(), tmf.getTrustManagers());
			artifactMap.put(certFile.getAbsolutePath(), tempArtifact);
		}
		return tmf.getTrustManagers();
	}

	@SuppressWarnings("unchecked")
	public void readFileFromDB(Map<String, File> files, String tenantId) throws IOException {
		if (files.size() == 0) {
			return;
		}
		byte[] fileContent = null;
		try {
			EntityManager em = EntityManagerHelper.getEntityManager();
			List<String> fileNames = new ArrayList<>(files.keySet());
			List<SecurityDO> resultList = em.createNamedQuery("SecurityDO.findFileByNameAndTenant")
			                                .setParameter("fileNames", fileNames)
			                                .setParameter("tenantId", tenantId)
			                                .getResultList();
			if (resultList == null) {
				return;
			}
			for (SecurityDO securityDO : resultList) {
				String fileName = securityDO.getFileName();
				fileContent = securityDO.getFileContent();
				if (fileContent != null) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(files.get(fileName).getPath());
						fos.write(fileContent);
					} finally {
						if (fos != null) {
							IOUtils.closeQuietly(fos);
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Error querying security table." + e.getMessage(), e);
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}

		return;
	}

	@SuppressWarnings("unchecked")
	public void writeFileToDB(Map<String, File> files, String tenantId) throws IOException {
		try {
			EntityManagerHelper.beginTransaction();
			EntityManager em = EntityManagerHelper.getEntityManager();
			Iterator<Map.Entry<String, File>> iterator = files.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<String, File> entry = iterator.next();
				List<SecurityDO> resultList = em.createNamedQuery("SecurityDO.findFileByNameAndTenant")
				        .setParameter("fileNames", Collections.singletonList(entry.getKey()))
				        .setParameter("tenantId", tenantId)
				        .getResultList();

				if (resultList == null || resultList.isEmpty()) {
					Path path = Paths.get(entry.getValue().getPath());
					byte[] content = Files.readAllBytes(path);
					SecurityDO securityDO = new SecurityDO(tenantId, entry.getKey(), content);
					em.persist(securityDO);
				}
			}
			EntityManagerHelper.commit();
		}
		catch (Exception e) {
			logger.error("Error writing to security table." + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}

		return;
	}
}