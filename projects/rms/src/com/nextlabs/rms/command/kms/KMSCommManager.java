package com.nextlabs.rms.command.kms;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;

public class KMSCommManager {	

	private static final Object LOCK = new Object();
	private static final File kmsDatFile = new File(SSLSocketFactoryGenerator.CERT_PATH, SSLSocketFactoryGenerator.KMS_KEYSTORE_DAT_SECURE);
	private static final File kmsJksFile = new File(SSLSocketFactoryGenerator.CERT_PATH, SSLSocketFactoryGenerator.KMS_KEYSTORE_FILE_SECURE);
	private static Logger logger = Logger.getLogger(KMSCommManager.class);
	
	public static Map<String,String> constructHeader() throws RMSException{
		if(!kmsDatFile.exists() || !kmsJksFile.exists()){
			registerClient();
		}
		String localCert = SSLSocketFactoryGenerator.getKMSKeyStoreSecureCertificate();
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(KMSWebSvcUrl.WEBSVC_KMS_CERTIFICATE_NAME, localCert);
		return headerMap;
	}
	
	private static void registerClient(){
		synchronized (LOCK) {
			File kmsUnsecureCert= new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KMS_KEYSTORE_FILE_UNSECURE);
			if(!kmsUnsecureCert.exists()) {
				logger.error("KMS Certificates not found.");
				return;
			}
			
			File kmsDatFile = new File(SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KMS_KEYSTORE_DAT_SECURE);
			if(!kmsDatFile.exists()) {
				try {
					RegisterClientService.registerClient();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.info("RMS Client is already registered to KMS.");
			}
		}
	}
	
	public enum KMSWebSvcUrl {
		CREATE_KEYRING ("createKeyRing"),
		DELETE_KEYRING ("deleteKeyRing"),
		DISABLE_KEYRING ("disableKeyRing"),
		ENABLE_KEYRING ("enableKeyRing"),
		GENERATE_KEY ("generateKey"),
		GET_ALL_KEYRINGS_WITH_KEYS ("getAllKeyRingsWithKeys"),
		GET_KEY ("getKey"),
		GET_KEYRING_NAMES ("getKeyRingNames"),
		GET_KEYRING ("getKeyRing"),
		GET_KEYRING_WITH_KEYS ("getKeyRingWithKeys"),
		GET_KEYRINGS ("getKeyRings"),
		GET_LATEST_KEY ("getLatestKey"),
		GET_TENANT_DETAIL ("getTenantDetail"),
		IMPORT_KEYRING ("importKeyRing"),
		REGISTER_CLIENT ("registerClient"),
		REGISTER_TENANT ("registerTenant");
		
		private final String serviceName;
		public static final String WEBSVC_KMS_CERTIFICATE_NAME = "X-AUTH-CERT";
	
	  private KMSWebSvcUrl (String serviceName) {
	      this.serviceName = serviceName;
	  }
	
	  public boolean equalsName(String otherName) {
	      return (otherName == null) ? false : serviceName.equals(otherName);
	  }
	
	  public String getServiceUrl() {
	     return GlobalConfigManager.getInstance().getKMSUrl() + "/service/" + this.serviceName;
	  }
	}

}
