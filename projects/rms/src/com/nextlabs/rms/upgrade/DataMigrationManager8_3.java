/**
 * 
 */
package com.nextlabs.rms.upgrade;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hsqldb.jdbc.JDBCPool;

import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.DBConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.repository.AuthorizedRepoUserDO;
import com.nextlabs.rms.entity.repository.RepositoryDO;
import com.nextlabs.rms.entity.repository.TenantUser;
import com.nextlabs.rms.entity.setting.ServiceProviderAttributeDO;
import com.nextlabs.rms.entity.setting.ServiceProviderDO;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.entity.setting.SettingDO;
import com.nextlabs.rms.persistence.EntityManagerHelper;
import com.nextlabs.rms.pojo.RepositoryDataWrapper;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UtilMethods;


/**
 * @author nnallagatla
 *
 */
public class DataMigrationManager8_3 {

	private final Logger logger = Logger.getLogger(DataMigrationManager8_3.class);
	
	private final String OLD_RMSDB_NAME = "RMSDB";
	
	private JDBCPool connPool;
	
	private final String DUMMY_USER_ID = "dummy";
	
	private boolean personalRepoEnabled = false;
	
	private Map<String, ServiceProviderType> repoTypeMap = new HashMap<>();
	private Map<String, ServiceProviderType> appTypeMap = new HashMap<>();
	
	/**
	 * 
	 */
	private DataMigrationManager8_3() {
	}
	
	private static final DataMigrationManager8_3 instance = new DataMigrationManager8_3();
	
	public static DataMigrationManager8_3 getInstance(){
		return instance;
	}
	
	public void migrateData(){
		
		if (!needsMigration()){
			logger.info("========No existing data found for migration=========");
			return;
		}		
			
		repoTypeMap.put("Dropbox", ServiceProviderType.DROPBOX);
		repoTypeMap.put("Google Drive", ServiceProviderType.GOOGLE_DRIVE);
		repoTypeMap.put("SharePoint", ServiceProviderType.SHAREPOINT_ONPREMISE);
		repoTypeMap.put("SharePoint Online", ServiceProviderType.SHAREPOINT_ONLINE);
		repoTypeMap.put("OneDrive", ServiceProviderType.ONE_DRIVE);
		repoTypeMap.put("Box", ServiceProviderType.BOX);
		
		appTypeMap.put("SharePoint Online", ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH);			
		appTypeMap.put("SharePoint On-Premise", ServiceProviderType.SHAREPOINT_CROSSLAUNCH);
		
		logger.info("========STARTING MIGRATION=========");
		
		try {
			try {
				EntityManagerHelper.beginTransaction();

				List<SettingDO> settingDOList = new ArrayList<>();
				List<ServiceProviderDO> repoSettingDOList = new ArrayList<>();
				Map<String, String> settings = getSettings();

				String value = settings.get(ConfigManager.ENABLE_PERSONAL_REPO);

				if (value != null && (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"))) {
					personalRepoEnabled = true;
				}

				populateSettingToMigrate(settings, settingDOList, repoSettingDOList);

				// insert settings
				insertEntities(settingDOList);
				logger.info("inserted " + settingDOList.size() + " settings");

				insertEntities(repoSettingDOList);
				logger.info("inserted " + repoSettingDOList.size() + " repository settings");

				EntityManagerHelper.commit();
			} catch (Exception e) {
				logger.error("Error occurred while migrating the settings: ", e);
				EntityManagerHelper.rollback();
			}

			try {
				EntityManagerHelper.beginTransaction();

				// sp app settings
				List<ServiceProviderDO> spAppList = getServiceProviderDOListForSPApps();
				insertEntities(spAppList);
				logger.info("inserted " + spAppList.size() + " SpApp settings");

				EntityManagerHelper.commit();
			} catch (Exception e) {
				logger.error("Error occurred while migrating SpApp settings: ", e);				
				EntityManagerHelper.rollback();
			}

			try {
				EntityManagerHelper.beginTransaction();

				List<RepositoryDO> repoDOList = new ArrayList<>();
				List<AuthorizedRepoUserDO> authUserDOList = new ArrayList<>();

				populateRepositoryAndUserAuthDOList(repoDOList, authUserDOList);

				// repositories
				insertEntities(repoDOList);
				logger.info("inserted " + repoDOList.size() + " repositories");

				insertEntities(authUserDOList);
				logger.info("inserted " + authUserDOList.size() + " user repo authorizations");

				EntityManagerHelper.commit();
			} catch (Exception e) {
				logger.error("Error occurred while migrating the repository list: ", e);				
				EntityManagerHelper.rollback();
			}
		} finally {
			EntityManagerHelper.closeEntityManager();
		}
		
		try{
			backupDB();
		} catch(Exception e){
			logger.error("Error occurred while backing up the DB");
			logger.error(e.getMessage(), e);
		}
		
		
		try {
			shutdown();
		} catch (SQLException e) {
			logger.error("Error while shutting down HSQL database", e);
		}
		
		try{
			deleteDB();
		} catch (Exception e) {
				logger.error("Error while deleting database. Please delete it manually", e);
		}
					
		logger.info("========ENDING MIGRATION=========");
	}
	
	private boolean needsMigration(){
		Connection conn = null;
		boolean flag = false;
		try {
			
			File file = new File(GlobalConfigManager.getInstance().getDataDir()
					+File.separator + OLD_RMSDB_NAME + ".script");
			
			if (!file.exists()){
				logger.info(file.getName() + " is missing. We assume there is no data to migrate");
				return false;
			}
			
			connPool = new JDBCPool(2);
			connPool.setUser("SA");
			connPool.setPassword("");
			connPool.setUrl("jdbc:hsqldb:"
					+ GlobalConfigManager.getInstance().getDataDir()
					+ File.separator
					+ OLD_RMSDB_NAME + ";ifexists=true"
					+ ";crypt_key=5d32da7b830d63c353b55ce37156529f;crypt_type=AES");
			
			conn = connPool.getConnection();
			flag = true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} finally {
			nullSafeClose(conn);
		}
		return flag;
	}
	
	private List<ServiceProviderDO> getServiceProviderDOListForSPApps(){
		List<List<String>> spAppSettings = getSPAppSettings();
		List<ServiceProviderDO> settingsDO = new ArrayList<>();
		
		if (spAppSettings == null || spAppSettings.isEmpty()){
			return settingsDO;
		}
		
		for (List<String> row: spAppSettings){
			settingsDO.add(getSPAppServiceProviderDO(row));
		}
		return settingsDO;
	}
	
	
	private ServiceProviderDO getSPAppServiceProviderDO(List<String> row){
		ServiceProviderDO spdo = new ServiceProviderDO();
		spdo.setCreatedDate(UtilMethods.getCurrentGMTDateTime());
		spdo.setUpdatedDate(spdo.getCreatedDate());
		spdo.setTenantId(GlobalConfigManager.DEFAULT_TENANT_ID);
		spdo.setServiceProviderType(appTypeMap.get(row.get(4)));
		
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.APP_ID, row.get(2)));
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.APP_SECRET, row.get(3)));
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.REMOTE_WEB_URL, row.get(5)));
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.APP_NAME, row.get(1)));
		return spdo;
	}
	
	
	private void populateSettingToMigrate(Map<String, String> settings, List<SettingDO> settingDOList, List<ServiceProviderDO> repoSettingDOList){
		
		populateRepoSettingDOList(settings, repoSettingDOList);
		
		SettingDO setting = null;
		for (Iterator<Map.Entry<String, String>> it = settings.entrySet().iterator(); it.hasNext() ;){
			Map.Entry<String, String> entry = it.next();
			
			setting = new SettingDO();
			setting.setTenantId(GlobalConfigManager.DEFAULT_TENANT_ID);
			setting.setName(entry.getKey());
			if (entry.getValue() != null && entry.getValue().trim().equalsIgnoreCase("YES")) {
				setting.setValue("true");
			}
			else if (entry.getValue() != null && entry.getValue().trim().equalsIgnoreCase("NO")) {
				setting.setValue("false");
			}
			else {
				setting.setValue(entry.getValue());
			}
			settingDOList.add(setting);
		}
	}
	
	/**
	 * This method will take populate reposettings and remove the corresponding properties from the incoming settings map
	 * @param settings
	 * @param repoSettingDOList
	 */
	private void populateRepoSettingDOList(Map<String, String> settings, List<ServiceProviderDO> repoSettingDOList) {
		
		Date updatedDate = UtilMethods.getCurrentGMTDateTime();
		
		String key = settings.remove(ConfigManager.SHAREPOINT_ONLINE_APP_KEY);
		String secret = settings.remove(ConfigManager.SHAREPOINT_ONLINE_APP_SECRET);
		String redirectURL = settings.remove(ConfigManager.SHAREPOINT_ONLINE_REDIRECT_URL);
		
		if (StringUtils.hasText(key) && StringUtils.hasText(secret) && StringUtils.hasText(redirectURL)) {
			repoSettingDOList.add(getRepoSetting(key, secret, redirectURL, ServiceProviderType.SHAREPOINT_ONLINE.name(), updatedDate));
		}
		
		key = settings.remove(ConfigManager.GOOGLEDRIVE_APP_KEY);
		secret = settings.remove(ConfigManager.GOOGLEDRIVE_APP_SECRET);
		redirectURL = settings.remove(ConfigManager.GOOGLEDRIVE_REDIRECT_URL);
		
		if (StringUtils.hasText(key) && StringUtils.hasText(secret) && StringUtils.hasText(redirectURL)) {
			repoSettingDOList.add(getRepoSetting(key, secret, redirectURL, ServiceProviderType.GOOGLE_DRIVE.name(), updatedDate));
		}
		
		key = settings.remove(ConfigManager.DROPBOX_APP_KEY);
		secret = settings.remove(ConfigManager.DROPBOX_APP_SECRET);
		redirectURL = settings.remove(ConfigManager.DROPBOX_REDIRECT_URL);
		
		if (StringUtils.hasText(key) && StringUtils.hasText(secret) && StringUtils.hasText(redirectURL)) {
			repoSettingDOList.add(getRepoSetting(key, secret, redirectURL, ServiceProviderType.DROPBOX.name(), updatedDate));
		}
		
		key = settings.remove(ConfigManager.BOX_APP_KEY);
		secret = settings.remove(ConfigManager.BOX_APP_SECRET);
		redirectURL = settings.remove(ConfigManager.BOX_REDIRECT_URL);
		
		if (StringUtils.hasText(key) && StringUtils.hasText(secret) && StringUtils.hasText(redirectURL)) {
			repoSettingDOList.add(getRepoSetting(key, secret, redirectURL, ServiceProviderType.BOX.name(), updatedDate));
		}
		
		key = settings.remove(ConfigManager.ONEDRIVE_APP_KEY);
		secret = settings.remove(ConfigManager.ONEDRIVE_APP_SECRET);
		redirectURL = settings.remove(ConfigManager.ONEDRIVE_REDIRECT_URL);
		
		if (StringUtils.hasText(key) && StringUtils.hasText(secret) && StringUtils.hasText(redirectURL)) {
			repoSettingDOList.add(getRepoSetting(key, secret, redirectURL, ServiceProviderType.ONE_DRIVE.name(), updatedDate));
		}
		
		//during upgrade add sharepoint by default
		ServiceProviderDO spdo = new ServiceProviderDO();
		spdo.setTenantId(GlobalConfigManager.DEFAULT_TENANT_ID);
		spdo.setServiceProviderType(ServiceProviderType.SHAREPOINT_ONPREMISE);
		spdo.setCreatedDate(updatedDate);
		spdo.setUpdatedDate(updatedDate);
		repoSettingDOList.add(spdo);
	}
	
	private ServiceProviderDO getRepoSetting(String key, String secret, String redirectURL, String type, Date updatedDate){
		ServiceProviderDO spdo = new ServiceProviderDO();
		spdo.setTenantId(GlobalConfigManager.DEFAULT_TENANT_ID);
		spdo.setServiceProviderType(ServiceProviderType.valueOf(type));
		spdo.setCreatedDate(updatedDate);
		spdo.setUpdatedDate(updatedDate);
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.APP_ID, key));
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.APP_SECRET, secret));
		spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.REDIRECT_URL, redirectURL));
		if (personalRepoEnabled) {
			spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.ALLOW_PERSONAL_REPO, "true"));
		} else {
			spdo.addAttribute(new ServiceProviderAttributeDO(ServiceProviderSetting.ALLOW_PERSONAL_REPO, "false"));
		}
		return spdo;
	}

	private List<List<String>> getRepositories() {
		Connection conn = null;
		Statement selectStatement = null;
		ResultSet rs = null;
		List<List<String>> list = null;
		try {
			conn = connPool.getConnection();
			selectStatement = conn.createStatement();
			rs = selectStatement.executeQuery("SELECT SID,REPOSITORY_TYPE,SEC_TOKEN,REPOSITORY_ID,REPOSITORY_NAME,ACCOUNT_NAME FROM REPOSITORYLIST");
			list = dump(rs);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			nullSafeClose(rs);
			nullSafeClose(selectStatement);
			nullSafeClose(conn);
		}
		return list;
	}
	
	private void populateRepositoryAndUserAuthDOList(List<RepositoryDO> repositoryList, List<AuthorizedRepoUserDO> authUsers){				
		List<List<String>> repos = getRepositories();
		
		for (List<String> repo : repos) {
			if ("Dropbox".equals(repo.get(1))) {
				continue;
			}
			RepositoryDataWrapper repoWrapper = getRepositoryWrapper(repo);
			repositoryList.add(repoWrapper.getRepository());
			if (repoWrapper.getAuthorizedUser() != null) {
				authUsers.add(repoWrapper.getAuthorizedUser());
			}
		}
	}
	
	private RepositoryDataWrapper getRepositoryWrapper(List<String> repo) {
		RepositoryDO repoDO = new RepositoryDO();
		//rs = selectStatement.executeQuery("SELECT SID,REPOSITORY_TYPE,SEC_TOKEN,REPOSITORY_ID,REPOSITORY_NAME,ACCOUNT_NAME FROM REPOSITORYLIST");
		
		String sid = repo.get(0);
		String repoType = repo.get(1);
		String token = repo.get(2);
		String repoDisplayName = repo.get(4);

		String accountName = repo.get(5);
		
		repoDO.setRepoType(repoTypeMap.get(repoType));
		
		AuthorizedRepoUserDO authUser = null; 
		
		Date currentGMTDate = UtilMethods.getCurrentGMTDateTime();
		
		if (accountName == null || accountName.isEmpty()){
			accountName = DUMMY_USER_ID;
		}
		
		repoDO.setCreatedDate(currentGMTDate);
		repoDO.setUpdatedDate(currentGMTDate);
		
		repoDO.setAccountId(accountName);
		repoDO.setRepoName(repoDisplayName);
		repoDO.setActive(true);
		
		if (sid == null || sid.isEmpty()){	
			sid = DUMMY_USER_ID;
			repoDO.setShared(true);
		}
		else{
			repoDO.setShared(false);
			
			authUser = new AuthorizedRepoUserDO();
			authUser.setAccountId(accountName);
			authUser.setAccountName(accountName);
			authUser.setSecToken(token);
			authUser.setActive(true);
			authUser.setUser(repoDO.getCreatedBy());
			authUser.setUpdatedDate(currentGMTDate);
			authUser.setRepository(repoDO);
		}
		
		TenantUser tUser = new TenantUser(GlobalConfigManager.DEFAULT_TENANT_ID, sid);
		
		repoDO.setCreatedBy(tUser);
		
		if (authUser != null) {
			authUser.setUser(tUser);
		}
		
		return new RepositoryDataWrapper(repoDO, authUser);
	}

	public <T> void insertEntities(List<T> doList){
		
		if (doList == null || doList.size() == 0){
			return;
		}
		
		EntityManager em = EntityManagerHelper.getEntityManager();
		int i = 0;
		for (T entity : doList){
			em.persist(entity);
			if ((++i) % DBConfigManager.DB_OPERATION_BATCH_SIZE == 0){
				em.flush();
			}
		}
		em.flush();
	}
	
	private List<List<String>> dump(ResultSet rs) {
		List<List<String>> result = null;
		try {
			result = new ArrayList<List<String>>();
			ResultSetMetaData meta = rs.getMetaData();
			int colmax = meta.getColumnCount();
			int i;
			for (; rs.next();) {
				List<String> repoProps = new ArrayList<String>();
				for (i = 0; i < colmax; ++i) {
					String prop = (String) rs.getObject(i + 1);
					repoProps.add(prop);
				}
				result.add(repoProps);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	private Map<String, String> getSettings() {
		Connection conn = null;
		Statement selectStatement = null;
		Map<String, String> setting = new HashMap<String, String>();
		ResultSet rs = null;
		try {
			conn = connPool.getConnection();
			selectStatement = conn.createStatement();
			rs = selectStatement.executeQuery("SELECT * FROM SETTING");
			while (rs.next()) {
				setting.put(rs.getString(1), rs.getString(2));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			nullSafeClose(rs);
			nullSafeClose(selectStatement);
			nullSafeClose(conn);
		}
		return setting;
	}
	

	private List<List<String>> getSPAppSettings() {
		Connection conn = null;
		Statement selectStatement = null;
		ResultSet rs = null;
		List<List<String>> list = null;
		try {
			conn = connPool.getConnection();
			selectStatement = conn.createStatement();
			rs = selectStatement.executeQuery("SELECT * FROM SPAPPSETTING");
			list = dump(rs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			nullSafeClose(rs);
			nullSafeClose(selectStatement);
			nullSafeClose(conn);
		}
		return list;
	}
		
	private void shutdown() throws SQLException {
		if (connPool != null){
			Connection conn = connPool.getConnection();
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");
			nullSafeClose(st);
			nullSafeClose(conn);
		}
	}
	
	
	public boolean backupDB() throws Exception{
		logger.debug("About to backup Database");
		Connection conn = null;
		PreparedStatement bkpStatement = null;
		String RMSDB_BKP_NAME = "OLD_RMSDB_" + System.currentTimeMillis() + ".tar.gz";
		File bkpFile = new File(GlobalConfigManager.getInstance().getBkpDir()
				+File.separator+RMSDB_BKP_NAME);
		try {
			conn = connPool.getConnection();
			String stmt = "BACKUP DATABASE TO '" + bkpFile.getAbsolutePath() +"' BLOCKING";
			bkpStatement = conn.prepareStatement(stmt);
			bkpStatement.executeUpdate();
			conn.commit();
			logger.debug("Database backed up successfully to "+GlobalConfigManager.getInstance().getBkpDir());
			return true;
		} catch (Exception e) {			
			logger.error("Error occurred while backing up database",e);
			throw e;
		} finally {
			nullSafeClose(bkpStatement);
			nullSafeClose(conn);
		}
	}
	
	public void deleteDB() {
		
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".properties"));
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".script"));
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".log"));
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".data"));
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".backup"));
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".lobs"));
		deleteIfExists(new File(GlobalConfigManager.getInstance().getDataDir()
				+File.separator+OLD_RMSDB_NAME + ".tmp"));
	}
	
	private void deleteIfExists(File file){
		if (file != null && file.exists()){
			file.delete();
		}
	}
	
	private void nullSafeClose(AutoCloseable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
