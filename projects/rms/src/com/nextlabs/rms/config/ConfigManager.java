package com.nextlabs.rms.config;

import com.nextlabs.rms.pojo.ServiceProviderSetting;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

public class ConfigManager implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2568021502529515959L;
	
	public static final String SP_ONLINE_APP_CONTEXT_ID = "SP_ONLINE_APP_CONTEXT_ID";
	
	private Properties properties = new Properties();
	
	public static final String ZIP_EXTN = ".zip";
	
	public static final String ALLOW_REGN_REQUEST= "ALLOW_REGN_REQUEST"; 
	
	public static final String POLICY_USER_LOCATION_IDENTIFIER = "POLICY_USER_LOCATION_IDENTIFIER";
	
	public static final String LOCATION_UPDATE_FREQUENCY = "LOCATION_UPDATE_FREQUENCY";
	
	public static final String DB_USERNAME="DB_USERNAME";
	
	public static final String DB_PASSWORD="DB_PASSWORD";

	public static final String DB_MAX_CONN="DB_MAX_CONN";
	
	public static final String KM_POLICY_CONTROLLER_HOSTNAME="KM_POLICY_CONTROLLER_HOSTNAME";

	public static final String KM_RMI_PORT_NUMBER="KM_RMI_PORT_NUMBER";
	
	public static final String KM_RMI_KEYSTOREFILE = "KM_RMI_KEYSTOREFILE";

	public static final String KM_RMI_KEYSTOREPASSWORD = "KM_RMI_KEYSTORE_PASSWORD";

	public static final String KM_RMI_TRUSTSTOREFILE = "KM_RMI_TRUSTSTOREFILE";

	public static final String KM_RMI_TRUSTSTOREPASSWORD = "KM_RMI_TRUSTSTORE_PASSWORD";
	
	public static final String EVAL_RMI_PORT_NUMBER="EVAL_RMI_PORT_NUMBER";

	public static final String EVAL_POLICY_CONTROLLER_HOSTNAME = "EVAL_POLICY_CONTROLLER_HOSTNAME";

	public static final String LOG4J_CONFIG_FILE = "RMSLog.properties";

	public static final String SMTP_HOST="SMTP_HOST";
	
	public static final String SMTP_USER_NAME="SMTP_USER_NAME";
	
	public static final String SMTP_PASSWORD="SMTP_PASSWORD";
	
	public static final String SMTP_ENABLE_TTLS="SMTP_ENABLE_TTLS";
	
	public static final String SMTP_PORT="SMTP_PORT";
	
	public static final String SMTP_AUTH="SMTP_AUTH";
	
	public static final String REGN_EMAIL_SUBJECT="REGN_EMAIL_SUBJECT";

	public static final String RMS_ADMIN_EMAILID = "RMS_ADMIN_EMAILID";
	
	public static final String DROPBOX_APP_KEY = "DROPBOX_APP_KEY";
	
	public static final String DROPBOX_APP_SECRET = "DROPBOX_APP_SECRET";
	
	public static final String DROPBOX_REDIRECT_URL = "DROPBOX_REDIRECT_URL";
	
	public static final String BOX_APP_KEY = "BOX_APP_KEY";
	
	public static final String BOX_APP_SECRET = "BOX_APP_SECRET";
	
	public static final String BOX_REDIRECT_URL = "BOX_REDIRECT_URL";
	
	public static final String GOOGLEDRIVE_APP_KEY = "GOOGLEDRIVE_APP_KEY";
	
	public static final String GOOGLEDRIVE_APP_SECRET = "GOOGLEDRIVE_APP_SECRET";
	
	public static final String GOOGLEDRIVE_REDIRECT_URL = "GOOGLEDRIVE_REDIRECT_URL";
	
	public static final String ONEDRIVE_APP_KEY = "ONEDRIVE_APP_KEY";
	
	public static final String ONEDRIVE_APP_SECRET = "ONEDRIVE_APP_SECRET";
	
	public static final String ONEDRIVE_REDIRECT_URL = "ONEDRIVE_REDIRECT_URL";
	
	public static final String SESSION_TIMEOUT_MINS = "SESSION_TIMEOUT_MINS";
	
	public static final String ENABLE_PERSONAL_REPO = "ENABLE_PERSONAL_REPO";
	
	public static final String ENABLE_USER_LOCATION ="ENABLE_USER_LOCATION";
	
	public static final String ENABLE_REMOTE_PC ="ENABLE_REMOTE_PC";
	
	private static Logger logger = Logger.getLogger(ConfigManager.class);

	public static final String SP_APP_PATH_ONLINE="SP_APP_PATH_ONLINE";
	
	private Properties smtpServerProps = null;  

	public static final String SP_OAUTH_TOKEN = "SP_OAUTH_TOKEN";
	
	public static final String ICENET_URL="ICENET_URL";
	
	public static final String RMC_CURRENT_VERSION="RMC_CURRENT_VERSION";
	
	public static final String RMC_UPDATE_URL_32BITS="RMC_UPDATE_URL_32BITS";

	public static final String RMC_CHECKSUM_32BITS="RMC_CHECKSUM_32BITS";
	
	public static final String RMC_UPDATE_URL_64BITS="RMC_UPDATE_URL_64BITS";

	public static final String RMC_CHECKSUM_64BITS="RMC_CHECKSUM_64BITS";

	public static final String SHAREPOINT_ONLINE_APP_KEY = "SHAREPOINT_ONLINE_APP_KEY";

	public static final String SHAREPOINT_ONLINE_APP_SECRET = "SHAREPOINT_ONLINE_APP_SECRET";

	public static final String SHAREPOINT_ONLINE_REDIRECT_URL = "SHAREPOINT_ONLINE_REDIRECT_URL";
	
	public static final String SHAREPOINTONLINE_SEARCHLIMITCOUNT = "SHAREPOINTONLINE_SEARCHLIMITCOUNT";
	
	public static final String KMS_URL_KEY = "KMS_URL";
	
	public static final String KMS_CONTEXT_NAME = "/KMS"; 
	
	public static final String KMS_DEFAULT_TENANT_ID = "-1";

	public static Map<Long, ServiceProviderSetting> idToServiceProviderMap  = new HashMap<Long, ServiceProviderSetting>();
	
	public static Map<String, ServiceProviderSetting> serviceProviderMap  = new HashMap<String, ServiceProviderSetting>();
	
	private String tenantId;
	
	private ConfigManager(String tenantId){
		this.tenantId = tenantId;
		logger.info("ConfigManager Created for tenant: " + tenantId);
		loadConfigFromDB();
	}
	
	public static ConfigManager getInstance(String tenantId) {
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_CONFIG);
		Element element = cache.get(tenantId);
		
		if (element != null) {
			return (ConfigManager) element.getObjectValue();
		}

		ConfigManager configManager = null;
		
		synchronized (ConfigManager.class) {
			element = cache.get(tenantId);
			if (element == null) {
				configManager = new ConfigManager(tenantId);
				cache.put(new Element(tenantId, configManager));
			} else {
				configManager = (ConfigManager) element.getObjectValue();
			}
		}
		return configManager;
	}

	public void loadConfigFromDB() {
		Map<String, String> configValues = SettingManager.getSettingValues(tenantId);
		properties.putAll(configValues);
		loadServiceProviderSettingsFromDB();
		initSMTP();
	}
	
	/**
	 * 
	 */
	public void loadServiceProviderSettingsFromDB() {
		List<ServiceProviderSetting> settings = SettingManager.getServiceProviderSettingsByTenant(tenantId);
		
		//TODO should we block clients on reading from accessing these maps until we reload?
		
		idToServiceProviderMap.clear();
		serviceProviderMap.clear();
		
		for (ServiceProviderSetting spSetting : settings) {
			idToServiceProviderMap.put(spSetting.getId(), spSetting);
			serviceProviderMap.put(spSetting.getProviderType(), spSetting);
		}
	}

	private void initSMTP() {
		smtpServerProps = new Properties();
		String enableTTLS=getStringProperty(SMTP_ENABLE_TTLS).trim();
		String auth=getStringProperty(SMTP_AUTH).trim();
		if(!enableTTLS.equals("")){
			smtpServerProps.put("mail.smtp.starttls.enable", getStringProperty(SMTP_ENABLE_TTLS));
		}
		if(!auth.equals("")){
			smtpServerProps.put("mail.smtp.auth", getStringProperty(SMTP_AUTH));
		}
		smtpServerProps.put("mail.smtp.host", getStringProperty(SMTP_HOST));
		smtpServerProps.put("mail.smtp.user", getStringProperty(SMTP_USER_NAME));
		smtpServerProps.put("mail.smtp.password", getStringProperty(SMTP_PASSWORD));
		smtpServerProps.put("mail.smtp.port", getStringProperty(SMTP_PORT));
	}
	
	public boolean getBooleanProperty(String key) {
		String val = properties.getProperty(key);
		if (val == null) {
			return false;
		}
		if (val.trim().equalsIgnoreCase("true") || val.trim().equalsIgnoreCase("yes")) {
			return true;
		}
		return false;
	}

	public String getStringProperty(String key) {
		String val = properties.getProperty(key, "").trim();
		return val;
	}

	public int getIntProperty(String key) {
		int val = -1;
		try {
			String strVal = properties.getProperty(key);
			if (strVal != null && strVal.length() > 0) {
				val = Integer.parseInt(strVal.trim());
			}
			return val;
		} catch (Exception e) {
			logger.error("Error occurred while getting value for key:"+key);
			return val;
		}
	}
	
	public long getLongProperty(String key) {
		long val = -1;
		try {
			String strVal = properties.getProperty(key);
			if (strVal != null && strVal.length() > 0) {
				val = Long.parseLong(strVal.trim());
			}
			return val;
		} catch (Exception e) {
			logger.error("Error occurred while getting value for key:"+key);
			return val;
		}
	}
	
	public Properties getSmtpServerProps() {
		return smtpServerProps;
	}

	public Map<String, ServiceProviderSetting> getServiceProviderMap() {
		return serviceProviderMap;
	}	
}
