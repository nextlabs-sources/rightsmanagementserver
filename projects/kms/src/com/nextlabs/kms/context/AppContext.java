package com.nextlabs.kms.context;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Interceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.util.StringUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.nextlabs.kms.controller.service.ServiceExceptionHandler;
import com.nextlabs.kms.interceptor.HibernateInterceptor;
import com.nextlabs.nxl.sharedutil.EncryptionUtil;

@Configuration
@EnableTransactionManagement
@ComponentScan(value = {"com.nextlabs.kms.service", "com.nextlabs.kms.dao"})
public class AppContext implements TransactionManagementConfigurer {

	public static final String KEY_KMSDATADIR = "KMSDATADIR";
	public static final String KEY_KMSINSTALLDIR = "KMSINSTALLDIR";
	public static final String DATA_SOURCE_FILE = "KMSDBConfig.properties";
	public static final String LOG4J_CONFIG_FILE = "KMSLog.properties";
	public static final String DEFAULT_KMS_DB_NAME = "DEFAULT_KMSDB";

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return txManager();
	}

	private PlatformTransactionManager txManager() {
		HibernateTransactionManager tx = new HibernateTransactionManager();
		tx.setSessionFactory(sessionFactory().getObject());
		try {
			tx.setDataSource(dataSource(dataSourceResource().getObject()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return tx;
	}

	@Bean
	@DependsOn(value = { "kms.ds.properties" })
	public LocalSessionFactoryBean sessionFactory() {
		LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
		sf.setPackagesToScan("com.nextlabs.kms.entity");

		try {
			Properties dataSourceResource = dataSourceResource().getObject();
			sf.setHibernateProperties(hibernateProperties(dataSourceResource));
			sf.setDataSource(dataSource(dataSourceResource));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		sf.setEntityInterceptor(entityInterceptor());
		return sf;
	}

	@Bean
	public Interceptor entityInterceptor() {
		HibernateInterceptor interceptor = new HibernateInterceptor();
		return interceptor;
	}

	@Bean
	@DependsOn(value = { "kms.msg.resource" })
	public ServiceExceptionHandler serviceExceptionHandler() {
		return new ServiceExceptionHandler();
	}

	@Bean(name = { "kms.msg.resource" })
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames("com.nextlabs.kms.locale.KMSMessages");
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setFallbackToSystemLocale(true);
		messageSource.setAlwaysUseMessageFormat(true);
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setCacheSeconds(30);
		return messageSource;
	}
	
	@Bean(name = { "kms.encryptionutil" })
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public EncryptionUtil encryptionUtil(){
		return new EncryptionUtil();
	}

	@Bean(name = { "kms.ds.properties" })
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
	@DependsOn(value = { "kms.logging.properties", "kms.encryptionutil" })
	public PropertiesFactoryBean dataSourceResource() throws IOException {
		PropertiesFactoryBean configurer = new PropertiesFactoryBean();
		configurer.setSingleton(true);
		File f = new File(dataDirPath().getFile(), DATA_SOURCE_FILE);
		if (!f.exists()) {
			configurer.setProperties(getDefaultDBProperties());
		} else {
			configurer.setLocation(new FileSystemResource(f));
		}
		return configurer;
	}

	@Bean(name = { "kms.logging.properties" })
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
	@DependsOn(value = { "kms.datadir" })
	public PropertiesFactoryBean loggingResource() throws IOException {
		PropertiesFactoryBean configurer = new PropertiesFactoryBean();
		configurer.setSingleton(true);
		File f = new File(dataDirPath().getFile(), LOG4J_CONFIG_FILE);
		loadLoggingProperties(f);
		PropertyConfigurator.configureAndWatch(f.getAbsolutePath());
		configurer.setLocation(new FileSystemResource(f));
		return configurer;
	}

	private void loadLoggingProperties(File f) throws IOException {
		final String defaultLoggingFile = "/log4j.properties";
		final String filePathKey = "log4j.appender.file.File";
		final Properties defaultLoggingProps = new Properties();
		InputStream in = AppContext.class.getResourceAsStream(defaultLoggingFile);
		try {
			if (in == null) {
				throw new FileNotFoundException("Unable to find default logging file");
			}
			defaultLoggingProps.load(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		if (!f.exists()) {
			System.out.println(f.getName() + " file not found. Creating " + f.getName() + " with default configurations.");
			Properties newProps = new Properties();
			newProps.putAll(defaultLoggingProps);
			File logsPath = new File(f.getParent() + File.separator + "logs", "KMS.log");
			newProps.setProperty(filePathKey, logsPath.getAbsolutePath());
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(f));
				newProps.store(writer, null);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			return;
		} else if (!f.isFile()) {
			throw new RuntimeException("Unable to read file: " + f.getAbsolutePath());
		} else if (!f.canRead()) {
			throw new RuntimeException("Unable to read file: " + f.getAbsolutePath());
		}
		final Properties loggingProps = new Properties();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			loggingProps.load(br);
		} finally {
			if (br != null) {
				br.close();
			}
		}
		Enumeration<?> propertyNames = defaultLoggingProps.propertyNames();
		boolean changed = false;
		while (propertyNames.hasMoreElements()) {
			String key = (String) propertyNames.nextElement();
			String defaultValue = defaultLoggingProps.getProperty(key);
			String existingValue = loggingProps.getProperty(key);
			if (!StringUtils.hasText(existingValue)) {
				changed = true;
				loggingProps.setProperty(key, defaultValue);
			}
		}
		if (changed) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(f));
				loggingProps.store(writer, null);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
	}

	@Bean(name = { "kms.installdir" })
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Resource installDirPath() {
		String installDir = System.getenv(KEY_KMSINSTALLDIR);
		if (!StringUtils.hasText(installDir)) {
			if (isWindows()) {
				installDir = "C:\\Program Files\\nextlabs\\RMS";
			} else {
				installDir = "/opt/nextlabs/RMS/";
			}
		}
		File f = new File(installDir);
		if (!f.isDirectory()) {
			throw new RuntimeException("Invalid install directory: " + f.getAbsolutePath());
		} else if (!f.canRead()) {
			throw new RuntimeException("Unable to access install directory: " + f.getAbsolutePath());
		}
		Resource resource = new FileSystemResource(f);
		return resource;
	}

	@Bean(name = { "kms.datadir" })
	@Order(value = Ordered.HIGHEST_PRECEDENCE)
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Resource dataDirPath() {
		String dataDir = System.getenv(KEY_KMSDATADIR);
		if (!StringUtils.hasText(dataDir)) {
			if (isWindows()) {
				dataDir = "C:\\ProgramData\\nextlabs\\RMS\\datafiles\\KMS";
			} else {
				dataDir = "/var/opt/nextlabs/RMS/datafiles/KMS";
			}
		}
		File f = new File(dataDir);
		if (!f.exists()) {
			f.mkdirs();
		}
		if (!f.isDirectory()) {
			throw new RuntimeException("Invalid data directory: " + f.getAbsolutePath());
		} else if (!f.canRead()) {
			throw new RuntimeException("Unable to access data directory: " + f.getAbsolutePath());
		}
		Resource resource = new FileSystemResource(f);
		return resource;
	}

	private Properties hibernateProperties(final Properties dataSourceResource) {
		return new Properties() {
			private static final long serialVersionUID = 331825139553782739L;
			{
				setProperty("hibernate.hbm2ddl.auto", dataSourceResource.getProperty("hibernate.hbm2ddl.auto"));
				setProperty("hibernate.globally_quoted_identifiers", "false");
			}
		};
	}

	private DataSource dataSource(Properties dataSourceResource) {
		String driver = dataSourceResource.getProperty("javax.persistence.jdbc.driver");
		String url = dataSourceResource.getProperty("javax.persistence.jdbc.url");
		String user = dataSourceResource.getProperty("javax.persistence.jdbc.user");
		String pass = dataSourceResource.getProperty("javax.persistence.jdbc.password");
		if(StringUtils.hasText(pass)) {
			pass = encryptionUtil().decrypt(pass);
		}
		Integer minPoolSize = Integer.valueOf(dataSourceResource.getProperty("hibernate.c3p0.min_size"));
		Integer maxPoolSize = Integer.valueOf(dataSourceResource.getProperty("hibernate.c3p0.max_size"));
		Integer idleConnectionTestPeriod = Integer.valueOf(dataSourceResource.getProperty("hibernate.c3p0.idle_test_period"));
		ComboPooledDataSource ds = new ComboPooledDataSource();
		try {
			ds.setDriverClass(driver);
			ds.setJdbcUrl(url);
			ds.setUser(user);
			ds.setPassword(pass);
			if (minPoolSize != null) {
				ds.setMinPoolSize(minPoolSize);
			}
			if (maxPoolSize != null) {
				ds.setMaxPoolSize(maxPoolSize);
			}
			if (idleConnectionTestPeriod != null) {
				ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
			}
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
		return ds;
	}

	private Properties getDefaultDBProperties() throws IOException {
		File dbProp = new File(dataDirPath().getFile().getAbsolutePath(), DATA_SOURCE_FILE);
		Properties prop = new Properties();
		if (!dbProp.exists()) {
			InputStream in = AppContext.class.getResourceAsStream("/datasource.properties");
			if (in == null) {
				throw new FileNotFoundException("Unable to find default datasource");
			}
			String dbDir = dataDirPath().getFile().getAbsolutePath() + File.separator + DEFAULT_KMS_DB_NAME;
			System.out.println(DATA_SOURCE_FILE + " file not found. Loading " + dbDir + " with default configurations.");
			try {
				prop.load(in);
				prop.setProperty("javax.persistence.jdbc.url", "jdbc:hsqldb:file:" + dbDir + ";crypt_key=5d32da7b830d63c353b55ce37156529f;crypt_type=AES");
			} catch (Exception e) {
				throw e;
			} finally {
			}
		}
		return prop;
	}

	private boolean isWindows() {
		String os = System.getProperty("os.name", "").toLowerCase();
		return (os.indexOf("win") >= 0);
	}
}
