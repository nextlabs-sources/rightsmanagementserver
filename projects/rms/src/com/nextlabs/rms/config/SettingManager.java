package com.nextlabs.rms.config;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.nextlabs.rms.entity.setting.ServiceProviderAttributeDO;
import com.nextlabs.rms.entity.setting.ServiceProviderDO;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.entity.setting.SettingDO;
import com.nextlabs.rms.exception.ServiceProviderAlreadyExists;
import com.nextlabs.rms.exception.ServiceProviderDuplicateAppNameException;
import com.nextlabs.rms.exception.ServiceProviderNotFoundException;
import com.nextlabs.rms.persistence.EntityManagerHelper;
import com.nextlabs.rms.persistence.PersistenceManager;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.RepositoryFactory;
import com.nextlabs.rms.util.UtilMethods;

public class SettingManager {

	private static Logger logger = Logger.getLogger(SettingManager.class);
	private static List<String> crossLaunchServiceProviderTypes = Arrays.asList(RepositoryFactory.crossLaunchApps);
	
	public static List<String> getPropertyNameList() {
		List<String> propertyNameList = new ArrayList<String>();
		propertyNameList.add(ConfigManager.KM_POLICY_CONTROLLER_HOSTNAME);
		propertyNameList.add(ConfigManager.KM_RMI_PORT_NUMBER);
		propertyNameList.add(ConfigManager.KM_RMI_KEYSTOREFILE);
		propertyNameList.add(ConfigManager.KM_RMI_KEYSTOREPASSWORD);
		propertyNameList.add(ConfigManager.KM_RMI_TRUSTSTOREFILE);
		propertyNameList.add(ConfigManager.KM_RMI_TRUSTSTOREPASSWORD);
		propertyNameList.add(ConfigManager.EVAL_RMI_PORT_NUMBER);
		propertyNameList.add(ConfigManager.EVAL_POLICY_CONTROLLER_HOSTNAME);
		propertyNameList.add(ConfigManager.SMTP_HOST);
		propertyNameList.add(ConfigManager.SMTP_USER_NAME);
		propertyNameList.add(ConfigManager.SMTP_PASSWORD);
		propertyNameList.add(ConfigManager.SMTP_ENABLE_TTLS);
		propertyNameList.add(ConfigManager.SMTP_PORT);
		propertyNameList.add(ConfigManager.SMTP_AUTH);
		propertyNameList.add(ConfigManager.REGN_EMAIL_SUBJECT);
		propertyNameList.add(ConfigManager.RMS_ADMIN_EMAILID);
		propertyNameList.add(ConfigManager.SESSION_TIMEOUT_MINS);
		propertyNameList.add(ConfigManager.ENABLE_PERSONAL_REPO);
		propertyNameList.add(ConfigManager.ALLOW_REGN_REQUEST);
		propertyNameList.add(ConfigManager.POLICY_USER_LOCATION_IDENTIFIER);
		propertyNameList.add(ConfigManager.LOCATION_UPDATE_FREQUENCY);
		propertyNameList.add(ConfigManager.ENABLE_USER_LOCATION);
		propertyNameList.add(ConfigManager.ICENET_URL);
		propertyNameList.add(ConfigManager.RMC_CURRENT_VERSION);
		propertyNameList.add(ConfigManager.RMC_UPDATE_URL_32BITS);
		propertyNameList.add(ConfigManager.RMC_CHECKSUM_32BITS);
		propertyNameList.add(ConfigManager.RMC_UPDATE_URL_64BITS);
		propertyNameList.add(ConfigManager.RMC_CHECKSUM_64BITS);
		propertyNameList.add(ConfigManager.ENABLE_REMOTE_PC);
		propertyNameList.add(ConfigManager.SP_ONLINE_APP_CONTEXT_ID);
		return propertyNameList;
	}

	public static Map<String, String> getSettingValues(String tenantId) {
		try {
			Map<String, String> configMap = PersistenceManager.getInstance().getSettings(tenantId);
			configMap.put("locationDBUpdateTime", getLocationDBUpdateTime());
			return configMap;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
	}
		
	public static String getLocationDBUpdateTime() {
		String localTime="";
		File currentDBFile = new File(GlobalConfigManager.getInstance()
						.getDataDir(), GlobalConfigManager.LOCATION_DATABASE);
		if(currentDBFile.exists()){
			Date date = new Date(currentDBFile.lastModified());
			DateFormat df=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM, Locale.getDefault());
			localTime=df.format(date);
		}
		return localTime;
	}

	public static void saveSetting(String tenantId, String name, String value) {
		try {
			EntityManagerHelper.beginTransaction();
			SettingDO setting = PersistenceManager.getInstance().fetchSettingByName(tenantId, name);
			
			if (setting != null) {
				setting.setValue(value);
			} else {
				setting = new SettingDO(name, value);
				setting.setTenantId(tenantId);
				EntityManagerHelper.getEntityManager().persist(setting);
			}
			EntityManagerHelper.commit();
		} catch (Exception e) {
			logger.error("Error occurred while saving Setting." + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}
		finally{
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	public static Map<String, String> saveSettingValues(String tenantId, Map<String, String> updateMap)
	{
		Map<String, String> resultMap = null;
		try {
			EntityManagerHelper.beginTransaction();
			resultMap = PersistenceManager.getInstance().updateSettingTable(tenantId, updateMap);	
			logger.info("All SP APP settings table records are updated.");
			EntityManagerHelper.commit();
			return resultMap;
		} catch (Exception e) {
			logger.error("Error occurred while saving SpAppSettings."  + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}
		finally{
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	public static Map<String, String> saveConfiguration(String tenantId, Map<String,String> configMap) {
		try {
			EntityManagerHelper.beginTransaction();
			PersistenceManager.getInstance().updateSettingTable(tenantId, configMap);
			EntityManagerHelper.commit();
			Map<String, String> dbConfigMap = getSettingValues(tenantId);
			return dbConfigMap;
		} catch (Exception e) {
			logger.error("Error updating settings table." + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	public static void saveServiceProviderSetting(ServiceProviderSetting setting) 
			throws ServiceProviderNotFoundException, ServiceProviderAlreadyExists, ServiceProviderDuplicateAppNameException {
		if (setting == null) {
			return;
		}
		try {
			EntityManagerHelper.beginTransaction();
			
			long id = setting.getId();
			ServiceProviderDO spDO = null;
			boolean insert = true;
			if (id != -1) {
				insert = false;
			}
			
			if (ServiceProviderType.SHAREPOINT_CROSSLAUNCH.toString().equals(setting.getProviderType()) 
					|| ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.toString().equals(setting.getProviderType())) {
				ServiceProviderDO fromDB = PersistenceManager.getInstance().fetchServiceProviderByTenantIdKeyAndValue(setting.getTenantId(), 
						ServiceProviderSetting.APP_NAME, setting.getAttributes().get(ServiceProviderSetting.APP_NAME));	
				if (fromDB !=null && (insert ||	fromDB.getId() != id)) {
					throw new ServiceProviderDuplicateAppNameException("Service provider with APP_NAME: " + 
						setting.getAttributes().get(ServiceProviderSetting.APP_NAME) + 
						" already exists for type: " + setting.getProviderType() + ". TenantId: " + setting.getTenantId());
				}
			}

			if (!insert) {
				spDO = PersistenceManager.getInstance().fetchServiceProviderDetailsByTenantIdAndId(setting.getTenantId(), id);
				if (spDO == null) {
					throw new ServiceProviderNotFoundException("id: " + id);
				}
				List<ServiceProviderAttributeDO> attributes = spDO.getAttributes();
				boolean existingAllowRepository = false;
				boolean newAllowRepository = Boolean.parseBoolean(setting.getAttributes().get(ServiceProviderSetting.ALLOW_PERSONAL_REPO));
				for(ServiceProviderAttributeDO attribute: attributes){
					if(attribute.getName().equals(ServiceProviderSetting.ALLOW_PERSONAL_REPO)){
						if(attribute.getValue()!=null){
							existingAllowRepository = Boolean.parseBoolean(attribute.getValue());
						}
						break;
					}
				}
				if(existingAllowRepository && !newAllowRepository){
					//Delete all the existing personal repos here
					deleteExistingPersonalRepoByRepoType(setting.getTenantId(), setting.getProviderType());
				}
			} 
			else {
				spDO = new ServiceProviderDO();
				spDO.setCreatedDate(UtilMethods.getCurrentGMTDateTime());
				if (violatesUniqueProviderType(setting)) {
					throw new ServiceProviderAlreadyExists(setting.getProviderType());
				}
			}
			populateServiceProviderDO(spDO, setting);
			spDO.setUpdatedDate(UtilMethods.getCurrentGMTDateTime());
			
			EntityManagerHelper.getEntityManager().persist(spDO);
			EntityManagerHelper.commit();
		} catch (Exception e) {
			logger.error("Error inserting service provider setting table." + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}
		finally {
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	/**
	 * Dropbox, google drive, one drive, sp online and sp can have only one service provider configured
	 * @param setting
	 * @return
	 */
	private static boolean violatesUniqueProviderType(ServiceProviderSetting setting) {
		String serviceProviderType = setting.getProviderType();
		if (ServiceProviderType.DROPBOX.toString().equals(serviceProviderType)
				|| ServiceProviderType.GOOGLE_DRIVE.toString().equals(serviceProviderType)
				|| ServiceProviderType.ONE_DRIVE.toString().equals(serviceProviderType)
				|| ServiceProviderType.SHAREPOINT_ONLINE.toString().equals(serviceProviderType)
				|| ServiceProviderType.SHAREPOINT_ONPREMISE.toString().equals(serviceProviderType) 
				|| ServiceProviderType.BOX.toString().equals(serviceProviderType)) {
			ServiceProviderDO provider = PersistenceManager.getInstance()
					.fetchServiceProviderByTenantIdAndType(setting.getTenantId(), serviceProviderType);
			return provider != null;
		}
		return false;
	}
	
	private static ServiceProviderType deleteServiceProviderSetting(String tenantId, long id) throws ServiceProviderNotFoundException {
		ServiceProviderType serviceProviderType;
		ServiceProviderDO spDO = PersistenceManager.getInstance().fetchServiceProviderDetailsByTenantIdAndId(tenantId, id);
		if (spDO == null) {
			throw new ServiceProviderNotFoundException("id: " + id);
		}
		serviceProviderType = spDO.getServiceProviderType();
		PersistenceManager.getInstance().deleteAttributesByServiceProvider(spDO);
		spDO.setUpdatedDate(UtilMethods.getCurrentGMTDateTime());
		spDO.setActive(false);
		EntityManagerHelper.getEntityManager().persist(spDO);
		return serviceProviderType;
	}
	
	public static List<ServiceProviderSetting> getServiceProviderSettingsByTenant(String tenantId) {
		try {
			List<ServiceProviderDO> serviceProviderList = PersistenceManager.getInstance().fetchServiceProvidersDetailsByTenant(tenantId);
			return toServiceProviderSettingList(serviceProviderList);
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	private static List<ServiceProviderSetting> toServiceProviderSettingList(List<ServiceProviderDO> doList) {
		List<ServiceProviderSetting> settings = new ArrayList<>();
		if (doList == null) {
			return settings;
		}
		for (ServiceProviderDO spdo : doList) {
			if(spdo.getServiceProviderType().toString().equals(ServiceProviderType.DROPBOX.toString()) || 
					spdo.getServiceProviderType().toString().equals(ServiceProviderType.GOOGLE_DRIVE.toString())||
					spdo.getServiceProviderType().toString().equals(ServiceProviderType.ONE_DRIVE.toString()) || 
					spdo.getServiceProviderType().toString().equals(ServiceProviderType.BOX.toString())){
				List<ServiceProviderAttributeDO> attributes = spdo.getAttributes();
				for(ServiceProviderAttributeDO attr : attributes){
					if(attr.getName().equals(ServiceProviderSetting.ALLOW_PERSONAL_REPO)){
						attr.setValue("true");
						break;
					}
				}
			}
			settings.add(toServiceProviderSetting(spdo));
		}
		return settings;
	}

	/**
	 * Call this method only if ServiceProviderAttributesDO are already loaded in to ServiceProviderDO
	 * else 
	 * 	if associated EntityManager is still open, it will result in N queries to load N attributes
	 * 	if associated EntityManager is closed, it will throw exception
	 * @param spdo
	 * @return
	 */
	private static ServiceProviderSetting toServiceProviderSetting(ServiceProviderDO spdo) {
		if (spdo == null) {
			return null;
		}
		ServiceProviderSetting sps = new ServiceProviderSetting();
		sps.setId(spdo.getId());
		sps.setProviderType(spdo.getServiceProviderType().toString());
		sps.setTenantId(spdo.getTenantId());
		sps.setProviderTypeDisplayName(ServiceProviderSetting.getProviderTypeDisplayName(sps.getProviderType()));
		
		if (crossLaunchServiceProviderTypes.contains(sps.getProviderType()) || sps.getProviderType().equals(ServiceProviderType.SHAREPOINT_ONLINE.name())) {
			sps.setDownloadable(true);
		}
		
		List<ServiceProviderAttributeDO> attrDOList = spdo.getAttributes();
		
		if (attrDOList != null) {
			for (ServiceProviderAttributeDO attr : attrDOList) {
				sps.getAttributes().put(attr.getName(), attr.getValue());
			}
		}
		return sps;
	}
	
	private static void populateServiceProviderDO(ServiceProviderDO spDO, ServiceProviderSetting spSetting) {
		if (spDO == null) {
			return;
		}
		
		spDO.setServiceProviderType(ServiceProviderType.valueOf(spSetting.getProviderType()));
		spDO.setTenantId(spSetting.getTenantId());
		
		Map<String, String> attrMap = spSetting.getAttributes();
		spDO.getAttributes().clear();
		for (Iterator<Map.Entry<String, String>> it = attrMap.entrySet().iterator(); it.hasNext();) {
			ServiceProviderAttributeDO spado = new ServiceProviderAttributeDO();
			Map.Entry<String, String> entry = it.next();
			spado.setName(entry.getKey());
			spado.setValue(entry.getValue());
			spDO.addAttribute(spado);
		}
	}
	
	public static ServiceProviderSetting fetchServiceProviderByTenantIdKeyAndValue(String tenantId, String key, String value) {
		try {
			ServiceProviderDO serviceProvider = PersistenceManager.getInstance().fetchServiceProviderByTenantIdKeyAndValue(tenantId, key, value);
			return toServiceProviderSetting(serviceProvider);
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
	}

	public static void deleteServiceProvider(String tenantId, long id) throws Exception {
		try{
			EntityManagerHelper.beginTransaction();
			ServiceProviderType serviceProviderType = deleteServiceProviderSetting(tenantId,id);
			PersistenceManager.getInstance().deleteServiceProviderAffectedAuthorizedUsers(tenantId, serviceProviderType);
			PersistenceManager.getInstance().deleteServiceProviderAffectedFavoriteFiles(tenantId, serviceProviderType);
			PersistenceManager.getInstance().deleteServiceProviderAffectedOfflineFiles(tenantId, serviceProviderType);
			PersistenceManager.getInstance().deleteServiceProviderAffectedRepositories(tenantId,serviceProviderType);
			EntityManagerHelper.commit();
		}
		catch(Exception e){
			logger.error("Couldn't delete the service provider. Id: " + id + ". " + e.getMessage(), e);
			EntityManagerHelper.rollback();
			throw e;
		}finally{
			EntityManagerHelper.closeEntityManager();
		}
	}
	
	private static void deleteExistingPersonalRepoByRepoType(String tenantId, String providerType) {
		PersistenceManager.getInstance().deleteAllowPersonalRepositoryAffectedAuthorizedUsers(tenantId, providerType);
		PersistenceManager.getInstance().deleteAllowPersonalRepositoryAffectedFavoriteFiles(tenantId, providerType);
		PersistenceManager.getInstance().deleteAllowPersonalRepositoryAffectedOfflineFiles(tenantId, providerType);
		PersistenceManager.getInstance().deleteAllowPersonalRepositoryAffectedRepositories(tenantId,providerType);
	}
}
