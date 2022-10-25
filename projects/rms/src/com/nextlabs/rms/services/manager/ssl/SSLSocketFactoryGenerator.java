package com.nextlabs.rms.services.manager.ssl;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.nxl.sharedutil.EncryptionUtil;
import com.nextlabs.rms.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SSLSocketFactoryGenerator {

    public static final boolean MODE_UNSECURE = false;
    public static final boolean MODE_SECURE = true;
    public static final String KEYSTORE_FILE_UNSECURE = "temp_agent-keystore.jks";
    public static final String RMS_KEYSTORE_FILE_SECURE = "rms-keystore.jks";
    public static final String RMS_KEYSTORE_FILE_UNSECURE = "rms-temp-keystore.jks";
    public static final String RMS_KEYSTORE_SECURE_ALIAS = "rms";
    public static final String RMS_KEYSTORE_UNSECURE_ALIAS = "rms-temp";
    public static final String KEYSTORE_FILE_SECURE = "agent-keystore.jks";
    public static final String KEYSTORE_FILE_PASSWORD = "agent-key.jks";
    public static final String TRUSTSTORE_FILE_SECURE = "agent-truststore.jks";
    public static final String KEYSTORE_PASSWORD_UNSECURE = "password";
    public static final String KEYSTORE_TYPE = "JKS";
    public static final String SSL_ALGORITHM = "SunX509";
	
    public static final String KMS_KEYSTORE_FILE_SECURE = "kms-keystore.jks";
    public static final String KMS_KEYSTORE_FILE_UNSECURE = "kms-temp-keystore.jks";
    public static final String KMS_KEYSTORE_SECURE_ALIAS = "kms";
    public static final String KMS_KEYSTORE_UNSECURE_ALIAS = "kms-temp";
    public static final String KMS_KEYSTORE_DAT_SECURE = "kms-keystore.dat";
    private static String kmsKeyStoreSecurePass;
	
    public static String CERT_PATH = GlobalConfigManager.getInstance().getDataDir() + File.separator + "cert" + File.separator;
    private static Log logger = LogFactory.getLog(SSLSocketFactoryGenerator.class.toString());
    
    private boolean isSecureMode;

    public SSLSocketFactoryGenerator (boolean mode) {
        isSecureMode = mode;
    }

    public KeyManager[] getKeyManagers(String tenantId) throws IOException, GeneralSecurityException {
        if (!isSecureMode) {
            return getKeyManagersUnsecure();
        } else {
            return getKeyManagersSecure(tenantId);
        }
    }

    public TrustManager[] getTrustManagers(String tenantId) throws IOException, GeneralSecurityException {
        if (!isSecureMode) {
            return getTrustManagersUnsecure();
        } else {
            return getTrustManagersSecure(tenantId);
        }
    }

    private KeyManager[] getKeyManagersUnsecure() throws IOException, GeneralSecurityException {
    	return CommArtifactsCache.getInstance().getKeyManagersUnsecure();
    }

    private KeyManager[] getKeyManagersSecure(String tenantId) throws IOException, GeneralSecurityException {
    	return CommArtifactsCache.getInstance().getKeyManagersSecure(tenantId);
    }

    private TrustManager[] getTrustManagersUnsecure() {
        return new TrustManager[ ] {
                new X509TrustManager() {
                    public X509Certificate[ ] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[ ] certs, String t) { }
                    public void checkServerTrusted(X509Certificate[ ] certs, String t) { }
                }
        };
    }

    private TrustManager[] getTrustManagersSecure(String tenantId) throws GeneralSecurityException {
    	return CommArtifactsCache.getInstance().getTrustManagersSecure(tenantId);
    }

    public KeyStore getKeyStoreSecure() {
        return CommArtifactsCache.getInstance().getKeyStoreSecure();
    }

    public KeyStore getTrustStoreSecure() {
        return CommArtifactsCache.getInstance().getTrustStoreSecure();
    }
    
    public static String getEncodedCertificate(String keyStoreName, String alias, String password){
    	return CommArtifactsCache.getInstance().getEncodedCertificate(keyStoreName, alias, password);
    }
    
    public static String getKeyStoreUnSecureCertificate(){
    	return getEncodedCertificate(RMS_KEYSTORE_FILE_UNSECURE, RMS_KEYSTORE_UNSECURE_ALIAS, "123next!");
    }

    public static String getKeyStoreSecureCertificate(){
    	return getEncodedCertificate(RMS_KEYSTORE_FILE_SECURE, RMS_KEYSTORE_SECURE_ALIAS, "123next!");
    }
    
    public static String getKMSKeyStoreUnsecureCertificate() throws RMSException{
    	String cert = getEncodedCertificate(KMS_KEYSTORE_FILE_UNSECURE, KMS_KEYSTORE_UNSECURE_ALIAS, "123next!");
    	if(!StringUtils.hasText(cert)) {
  			logger.error("Failed to read KMS keystore file.");
  			throw new RMSException("Failed to read KMS keystore file.");
  		}
    	return cert;
    }
    
    public static String getKMSKeyStoreSecureCertificate() throws RMSException{
    	if(kmsKeyStoreSecurePass==null){
    		 try {
					String encryptedPass = FileUtils.readFileToString(new File(CERT_PATH + KMS_KEYSTORE_DAT_SECURE));
					EncryptionUtil util = new EncryptionUtil();
					kmsKeyStoreSecurePass = util.decrypt(encryptedPass);
				} catch (IOException e) {
					logger.error(e);
					throw new RMSException(e.getMessage());
				}
    	}
  		String cert = getEncodedCertificate(KMS_KEYSTORE_FILE_SECURE, KMS_KEYSTORE_SECURE_ALIAS, kmsKeyStoreSecurePass);
  		if(!StringUtils.hasText(cert)) {
  			logger.error("Failed to read KMS keystore file.");
  			throw new RMSException("Failed to read KMS keystore file.");
  		}
    	return cert;
    }
}