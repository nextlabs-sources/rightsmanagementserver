/**
 * 
 */
package com.nextlabs.rms.config;

import com.nextlabs.nxl.sharedutil.EncryptionUtil;
import com.nextlabs.rms.util.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author nnallagatla
 *
 */
public class DBConfigManager {
	
	private static Logger logger = Logger.getLogger(DBConfigManager.class);

	private Properties databaseProperties = new Properties();
	
	public static final String DB_CONFIG_FILENAME = "DBConfig.properties";
	
	public static final int DB_OPERATION_BATCH_SIZE = 20;
	
	public static final String DEFAULT_DB_NAME = "DEFAULT_RMSDB";
	
	private DBConfigManager() {
	}
	
	private static final DBConfigManager INSTANCE = new DBConfigManager();
	
	static DBConfigManager getInstance(){
		return INSTANCE;
	}
	
	void loadDBProperties(String dataDir){
		logger.info("loading DBConfig.properties");
		BufferedInputStream inStream = null;
		try {
			File file = new File(dataDir, DB_CONFIG_FILENAME);
			if(!file.exists()){
				logger.info("DB Config file '"+ DB_CONFIG_FILENAME +"' not found in datadir. Defaults will be used for initializing EnityManagerFactory");
				loadDefaultDBProperties(dataDir);
			}else{
				inStream = new BufferedInputStream(new FileInputStream(new File(dataDir, DB_CONFIG_FILENAME)));
				databaseProperties.load(inStream);				
				String encryptedPass = databaseProperties.getProperty("javax.persistence.jdbc.password");
				logger.info(databaseProperties);
				if(StringUtils.hasText(encryptedPass)){
					EncryptionUtil eu = new EncryptionUtil();
					String decryptedPass = eu.decrypt(encryptedPass);					
					databaseProperties.setProperty("javax.persistence.jdbc.password", decryptedPass);
				}
			}
		} catch (IOException e) {
			logger.error("Error occurred while reading DB Config file ",e);
		} finally{
			try {
				if(inStream!=null){					
					inStream.close();
				}
			} catch (IOException e) {
				logger.error("Error occurred while closing stream");
			}
		}
	}
	
	
	Properties getDatabaseProperties() {
		return databaseProperties;
	}

	private void loadDefaultDBProperties(String dataDir){
		databaseProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		databaseProperties.setProperty("javax.persistence.jdbc.driver", "org.hsqldb.jdbc.JDBCDriver");
		databaseProperties.setProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:file:" + dataDir + File.separator
				+ DEFAULT_DB_NAME + ";crypt_key=5d32da7b830d63c353b55ce37156529f;crypt_type=AES;hsqldb.write_delay=false;shutdown=true");
		databaseProperties.setProperty("javax.persistence.jdbc.user", "SA");
		databaseProperties.setProperty("javax.persistence.jdbc.password", "");
	}
}
