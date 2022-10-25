package com.nextlabs.kms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.model.ProviderTypeAttribute;
import com.nextlabs.kms.service.IKeyRingManager;
import com.nextlabs.kms.service.TenantService;
import com.nextlabs.kms.service.impl.ProviderService;

@Component
public class KMSInitializationManager {
	
	public static final String DEFAULT_TENANT_ID = "-1";
	public static final String DEFAULT_KR_NAME = "NL_SHARE";
	public static final String KMS_INSTALLER_PROPERTIES = ".kms_tmp_file.properties";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired(required = true)
	private ProviderService providerService;
	@Autowired(required = true)
	private TenantService tenantService;
	@Autowired(required = true)
	@Qualifier(value="kms.datadir")
	private Resource resource;
	
	
	@EventListener({ContextRefreshedEvent.class})
	void contextRefreshedEvent() throws IOException {
		File kmsConfigFile = new File(resource.getFile(), KMS_INSTALLER_PROPERTIES);
		if(kmsConfigFile.exists()) {
			Tenant tenant = createDefaultTenant(kmsConfigFile);
			if(tenant!=null) {
				createDefaultKeyRingAndKey();
			}
		}
	}
	
	private Tenant createDefaultTenant(File kmsConfigFile){
		Tenant tenant = tenantService.getTenant(KMSInitializationManager.DEFAULT_TENANT_ID);
    if(tenant==null){
    	logger.info("Creating default tenant with ID: " + KMSInitializationManager.DEFAULT_TENANT_ID);
	    try {
	    	List<ProviderTypeAttribute> attributes = readProviderAttributes(kmsConfigFile);
	    	if(attributes != null) {
	    		tenant = tenantService.createTenant(KMSInitializationManager.DEFAULT_TENANT_ID, ProviderType.DEFAULT, attributes);
	    	}
			} catch (KeyManagementException e) {
				logger.error("Failed to create default tenant.",e);
			}
    }
    return tenant;
	}
	
	private void createDefaultKeyRingAndKey(){
		try {
    	IKeyRingManager keyRingManager = providerService.getKeyRingManager(KMSInitializationManager.DEFAULT_TENANT_ID);
    	IKeyRing keyRing = keyRingManager.getKeyRing(KMSInitializationManager.DEFAULT_KR_NAME);
    	if(keyRing == null) {
    		logger.info("Creating default keyring with name: " + KMSInitializationManager.DEFAULT_KR_NAME);
    		keyRing = keyRingManager.createKeyRing(KMSInitializationManager.DEFAULT_KR_NAME);
    	}
  		if(keyRing.getKeys().isEmpty()){
  			logger.info("Creating a new key in default keyring: " + KMSInitializationManager.DEFAULT_KR_NAME);
    		keyRingManager.generateKey(KMSInitializationManager.DEFAULT_KR_NAME, KeyAlgorithm.DEFAULT_KEY_ALGORITHM);
  		}
  	
    } catch (KeyManagementException e){
    	logger.error("Failed to retrieve default keyring.", e);
    }
	}
	
	private List<ProviderTypeAttribute> readProviderAttributes(File kmsConfigFile){
		Properties prop = new Properties();
		InputStream inputStream = null;
		try {
			logger.debug("Reading Provider attributes from " + kmsConfigFile.getAbsolutePath());
			inputStream = new FileInputStream(kmsConfigFile);
			prop.load(inputStream);
			inputStream.close();
			List<ProviderTypeAttribute> attributes = new ArrayList<>();
			for (String key : prop.stringPropertyNames()) {
				if(!key.equals("provider")) {
					String value = prop.getProperty(key);
					attributes.add(new ProviderTypeAttribute(key, String.valueOf(value)));
				}
			}
			kmsConfigFile.delete();
			return attributes;
		} catch (IOException e) {
			logger.error("Error occured while reading " + kmsConfigFile.getAbsolutePath(), e);
		} finally {
			if (inputStream != null) {
				try{
					inputStream.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return null;
	} 
}


