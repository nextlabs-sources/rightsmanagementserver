/**
 *
 */
package com.nextlabs.rms.config;

import com.nextlabs.nxl.sharedutil.EncryptionUtil;
import com.nextlabs.rms.util.StringUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.*;

/**
 * @author nnallagatla
 *
 */
public class GlobalConfigManager {

	public static final String SHAREPOINT_SEARCHRESULT_COUNT="SHAREPOINT_SEARCHRESULT_COUNT";

	public static final String SP_ONLINE_APP_CONTEXT_ID = "SP_ONLINE_APP_CONTEXT_ID";

	public static final String RMS_CONTEXT_NAME = "/RMS";

	public static final String RMS_AUTH_URL = "/RMS/auth/login";

	private Properties properties = new Properties();

	private String dataDir = "";

	private String installDir="";

	private String cadConverterDir = "";

	private String cadBinDir = "";

	private String unixCadConverter = "";

	private String winCadConverter ="";

	private String viewerPlugInDir;

	private String webViewerDir = "";

	private String docConverterDir = "";

	private String sapViewerDir = "";

	private String tempDir = "";

	private String webDir = "";

	private String bkpDir = "";

	private String licDir = "";

	public static final String CONFIG_FILENAME = "RMSConfig.properties";

	public static final String LOCATION_DATABASE = "IpToCountry.csv";

	public static final String KEY_RMSDATADIR = "RMSDATADIR";

	public static final String KEY_RMSINSTALLDIR="RMSINSTALLDIR";

	public static final String TEMPDIR_NAME = "temp";

	public static final String BKPDIR_NAME = "bkp";

	public static final String ENABLE_HTTP = "ENABLE_HTTP";

	public static final String USE_2D_PDF_VIEWER = "USE_2D_PDF_VIEWER";

	public static final String SHOW_DEFAULTLIBRARY_SHAREPOINT = "SHOW_DEFAULTLIBRARY_SHAREPOINT";

	public static final String SUPPORTED_FILE_FORMATS = "SUPPORTED_FILE_FORMATS";

	public static final String SUPPORTED_HOOPSASSEMBLY_FORMATS = "SUPPORTED_HOOPSASSEMBLY_FORMATS";

	public static final String SUPPORTED_HOOPSNONASSEMBLY_FORMATS = "SUPPORTED_HOOPSNONASSEMBLY_FORMATS";

	public static final String ALLOWED_FILE_EXTN = ".doc,.docx,.ppt,.pptx,.xls,.xlsx,.pdf,.txt,.jpg,.png,.vsd,.vsdx,.tif,.tiff,.dxf";

	public static final String ALLOWED_HOOPSNONASSEMBLY_EXTN = ".jt,.prt,.sldprt,.sldasm,.catpart,.catshape,.cgr,.neu,.par,.psm,.x_b,.x_t,.xmt_txt,.pdf,.ipt,.igs,.stp,.stl,.step,.3dxml,.dwg,.iges,.model";

	public static final String ALLOWED_HOOPSASSEMBLY_EXTN = ".asm,.catproduct,.iam";

	public static final String ZIP_EXTN = ".zip";

	public static final String ALLOW_REGN_REQUEST= "ALLOW_REGN_REQUEST";

	public static final String POLICY_USER_LOCATION_IDENTIFIER = "POLICY_USER_LOCATION_IDENTIFIER";

	public static final String LOCATION_UPDATE_FREQUENCY = "LOCATION_UPDATE_FREQUENCY";

	public static final String FILE_UPLD_THRESHOLD_SIZE="FILE_UPLD_THRESHOLD_SIZE";

	public static final String FILE_UPLD_MAX_REQUEST_SIZE="FILE_UPLD_MAX_REQUEST_SIZE";

	public static final String DB_USERNAME="DB_USERNAME";

	public static final String DB_PASSWORD="DB_PASSWORD";

	public static final String DB_MAX_CONN="DB_MAX_CONN";

	public static final String KM_POLICY_CONTROLLER_HOSTNAME="KM_POLICY_CONTROLLER_HOSTNAME";

	public static final String EVAL_POLICY_CONTROLLER_HOSTNAME = "EVAL_POLICY_CONTROLLER_HOSTNAME";

	public static final String LOG4J_CONFIG_FILE = "RMSLog.properties";

	public static final String REPO_CACHE_TIMEOUT_MINS = "REPO_CACHE_TIMEOUT_MINS";

	public static final String CONFIG_CACHE_TIMEOUT_MINS = "CONFIG_CACHE_TIMEOUT_MINS";

	public static final String FILECONTENT_CACHE_TIMEOUT_MINS = "FILECONTENT_CACHE_TIMEOUT_MINS";

	public static final String FILECONTENT_CACHE_MAXMEM_MB = "FILECONTENT_CACHE_MAXMEM_MB";

	public static final String WRITE_DECRYPTED_FILE_TO_DISK = "WRITE_DECRYPTED_FILE_TO_DISK";

	public static final String SP_DOCLIB_FLAGID="SP_DOCLIB_FLAGID";

	public static final String USE_IMG_FOR_EXCEL="USE_IMG_FOR_EXCEL";

	public static final String RMC_AUTH_MODE="RMC_AUTH_MODE";

	public static final String ENABLE_CLUSTERED_MODE = "ENABLE_CLUSTERED_MODE";

	public boolean isUnix = false;

	private static Logger logger = Logger.getLogger(GlobalConfigManager.class);

	public static final String SP_APP_PATH_ONLINE="SP_APP_PATH_ONLINE";

	public static final String RH_FILE_EXTN = ".rh";

	public static final String VDS_FILE_EXTN = ".vds";

	public static final String PDF_FILE_EXTN = ".pdf";

	public static final String DWG_FILE_EXTN = ".dwg";

	public static final String TIFF_FILE_EXTN = ".tiff";

	public static final String USE_FILENAME_AS_DOCUMENTID = "USE_FILENAME_AS_DOCUMENTID";

	public static final String CONVERTER_IMAGE_DPI = "CONVERTER_IMAGE_DPI";

	public static final String CONVERTER_PAGE_LIMIT = "CONVERTER_PAGE_LIMIT";

	public static final String SHAREPOINT_CONN_TIMEOUT_SECONDS = "SHAREPOINT_CONN_TIMEOUT_SECONDS";

	public static final String SHAREPOINT_MAX_CONNECTIONS = "SHAREPOINT_MAX_CONNECTIONS";

	public static final String SP_OAUTH_TOKEN = "SP_OAUTH_TOKEN";

	public static final String SP_APP_PATH_ON_PREMISE = "SP_APP_PATH_ON_PREMISE";

	public static final String SHAREPOINT_CONN_RETRY_ATTEMPTS = "SHAREPOINT_CONN_RETRY_ATTEMPTS";

	public static final String SP_OAUTH_TOKEN_EXPIRY_TIME = "SP_OAUTH_TOKEN_EXPIRY_TIME";

	public static final String TRUST_SELF_SIGNED_CERTS = "TRUST_SELF_SIGNED_CERTS";

	public static final String SHAREPOINT2013_SEARCHWITHCOUNT = "SHAREPOINT2013_SEARCHWITHCOUNT";

	public static final String SHAREPOINT2013_SEARCHLIMITCOUNT = "SHAREPOINT2013_SEARCHLIMITCOUNT";

	public static final String RETAIN_NXL_FILE = "RETAIN_NXL_FILE";

	public static final String IMAGE_WATERMARK = "IMAGE_WATERMARK";

	public static final String WATERMARK_USERNAME = "$(User)";

	public static final String WATERMARK_LOCALTIME = "$(Time)";

	public static final String WATERMARK_LOCALDATE = "$(Date)";

	public static final String WATERMARK_HOST = "$(Host)";

	public static final String WATERMARK_GMTTIME = "%gmtTime";

	public static final String WATERMARK_DATE_FORMAT = "WATERMARK_DATE_FORMAT";

	public static final String WATERMARK_FONT_SIZE = "WATERMARK_FONT_SIZE";

	public static final String WATERMARK_REPEAT = "WATERMARK_REPEAT";

	public static final String WATERMARK_FONT_NAME = "WATERMARK_FONT_NAME";

	public static final String WATERMARK_TEXT_KEY = "WATERMARK_TEXT_KEY";

	public static final String WATERMARK_FONT_COLOR = "WATERMARK_FONT_COLOR";

	public static final String WATERMARK_FONT_TRANSPARENCY = "WATERMARK_FONT_TRANSPARENCY";

	public static final String WATERMARK_ROTATION = "WATERMARK_ROTATION";

	public static final String WATERMARK_DENSITY = "WATERMARK_DENSITY";

	public static final String USER_IP_ADDRESS = "USER_IP_ADDRESS";

	public static final String USERLOCATION_DB_URL = "USERLOCATION_DB_URL";

	public static final String CAD_CONVERTER_ZIP_REGEX = "RightsManagementServer-CADViewer";

	public static final String CAD_DEPLOYED_PATH = "RMSCADCONVERTER";

	public static final String CADVIEWER_WEBDIR_NAME = "cadviewer";

	public static final String INSTALL_EXTERNAL = "external";

	public static final String INSTALL_PLUGINS = "viewers";

	public static final String POLICY_EVALUATION_DEFAULT_ALLOW="POLICY_EVALUATION_DEFAULT_ALLOW";

	public static final String DABS_AGENT_SERVICE = "DABS_AGENT_SERVICE";

	public static final String DABS_LOG_SERVICE = "DABS_LOG_SERVICE";

	public static final String IS_CAD_PMI_ALLOWED = "IS_CAD_PMI_ALLOWED";

	public static final String CERT_FOLDER_NAME = "cert";

	public static final String SAP_CONVERTER_ZIP_REGEX = "RightsManagementServer-SAPViewer";

	public static final String PERCEPTIVE_ZIP_REGEX = "RightsManagementServer-DocViewer";

	public static final String SAPVIEWER_WEBDIR_NAME = "SAPViewer";

	public static final String RM_ACTION_PREFIX = "RM_ACTION_PREFIX";

	public static final String RMC_ZIP_PATH = "RMC_PACKAGE_ZIP_PATH";

	public static final String RMC_CONFIG_FILE_NAME = "register.xml";

	public static final String DISABLE_WEBSVC_AUTH = "DISABLE_WEBSVC_AUTH";

	public static final String ENABLE_WEBSVC_DEBUG_LOGS = "ENABLE_WEBSVC_DEBUG_LOGS";

	public static final String RM_RIGHTS_GRANTED_LOGGING_KEY = "RM_RIGHTS_GRANTED";

	public static final String RM_EVALUATED_POLICY_IDS = "RM_EVALUATED_POLICY_IDS";

	public static final String RM_EVALUATED_POLICY_NAMES = "RM_EVALUATED_POLICY_NAMES";

	public static final String RM_APPLICATION_PUBLISHER = "RM_APPLICATION_PUBLISHER";

	private static final String RMS_BUILD_NUMBER = "RMS_BUILD_NUMBER";

	public static final String SUPPORTED_POLICY_EVAL_WEBSVC_RIGHTS = "SUPPORTED_POLICY_EVAL_WEBSVC_RIGHTS";

	public static final String DEFAULT_POLICY_EVAL_WEBSVC_RIGHTS = "RIGHT_VIEW, RIGHT_EDIT, RIGHT_PRINT, RIGHT_CLIPBOARD, RIGHT_SAVEAS, RIGHT_DECRYPT, RIGHT_SCREENCAP, RIGHT_SEND, RIGHT_CLASSIFY";

	public static final String USE_OLD_POLICY_PARSER = "USE_OLD_POLICY_PARSER";

	public static final String EMBEDDEDJPC_RMI_PORT_NUMBER = "EMBEDDEDJPC_RMI_PORT_NUMBER";

	public static final String EMBEDDEDJPC_KEYSTOREKMC_FILE = "rmskmc-keystore.jks";

	public static final String EMBEDDEDJPC_TRUSTSTOREKMC_FILE="rmskmc-truststore.jks";

	public static final String EMBEDDEDJPC_KEYSTOREKMC_PASSWORD= "123next!";

	public static final String EMBEDDEDJPC_TRUSTSTOREKMC_PASSWORD="123next!";

	public static final String EMBEDDEDJPC_HOSTNAME="localhost";

	private static final String LICENSE_FOLDER_NAME = "license";

	public static final String EMBEDDEDJPC_FOLDER_NAME = "javapc";

	public static final String INSTALL_DB_SETTINGS_FILENAME = "install_db_settings.txt";

	public static final String SHAREPOINT_ONLINE_APP_KEY = "SHAREPOINT_ONLINE_APP_KEY";

	public static final String SHAREPOINT_ONLINE_APP_SECRET = "SHAREPOINT_ONLINE_APP_SECRET";

	public static final String SHAREPOINT_ONLINE_REDIRECT_URL = "SHAREPOINT_ONLINE_REDIRECT_URL";

	public static final String SHAREPOINTONLINE_SEARCHLIMITCOUNT = "SHAREPOINTONLINE_SEARCHLIMITCOUNT";

	public static final String KMS_URL_KEY = "KMS_URL";

	private static final String RMS_VERSION_NUMBER= "8.4.3";

	public static final String[] RMS_FILES_TO_CACHE = new String[] {"agent-keystore.jks",
	                                                                "agent-truststore.jks",
	                                                                "agent-key.jks"};

	private String versionNumWithBuild;

	public static final String KMS_CONTEXT_NAME = "/KMS";

	public static final String USE_KMS_KEYS = "USE_KMS_KEYS";

	public static final String DEFAULT_TENANT_ID = "-1";

	public static final String KMS_INSTALLER_PROPERTIES = ".kms_tmp_file.properties";

	private static final String LDAP_HOST_SUFFIX = "HOST_NAME";

	private static final String LDAP_DOMAIN_SUFFIX = "DOMAIN";

	private static final String LDAP_SEARCH_BASE_SUFFIX = "SEARCH_BASE";

	private static final String LDAP_USER_SEARCH_QUERY_SUFFIX = "USER_SEARCH_QUERY";

	private static final String LDAP_RMS_ADMIN_SUFFIX = "RMS_ADMIN";

	private static final String LDAP_RMS_USERGROUP_SUFFIX = "RMS_USERGROUP";

	private static final String LDAP_TYPE = "LDAP_TYPE";

	private static final String LDAP_SECURITY_PRINCIPAL_USE_USERID= "SECURITY_PRINCIPAL_USE_USERID";

	private static final String LDAP_RMS_USER_UNIQUEID="RMS_USER_UNIQUEID";

	private static final String LDAP_AD_SSL="LDAP_AD_SSL";

	private static final String LDAP_AD_TRUSTSTORE="LDAP_AD_TRUSTSTORE";

	private static final String LDAP_AD_TRUSTSTOREPASSWORD="LDAP_AD_TRUSTSTOREPASSWORD";

	// TC AD query
	private static final String LDAP_RMS_QUERYDN_SUFFIX = "QUERYDN";

	// TC AD query
	private static final String LDAP_RMS_QUERYDN_PASSWORD_SUFFIX = "QUERYDN_PASSWORD";

	private static final String SAML_SP_ENTITY_ID = "SAML_SP_ENTITY_ID";

	private static final String SAML_SP_ACS_URL = "SAML_SP_ACS_URL";

	private static final String SAML_SP_NAME_ID_FORMAT = "SAML_SP_NAME_ID_FORMAT";

	private static final String SAML_SP_X509_CERT = "SAML_SP_X509_CERT";

	private static final String SAML_SP_PRIV_KEY = "SAML_SP_PRIV_KEY";

	public static final String SAML_IDP_ENTITY_ID = "SAML_IDP_ENTITY_ID";

	private static final String SAML_IDP_SSO_URL = "SAML_IDP_SSO_URL";

	private static final String SAML_IDP_X509_CERT = "SAML_IDP_X509_CERT";

	private static final String SAML_SETTINGS_SIGN_ALGO = "SAML_SETTINGS_SIGN_ALGO";

	private static final String SAML_SETTINGS_AUTHN_CONTEXT = "SAML_SETTINGS_AUTHN_CONTEXT";

	private static final String SAML_SETTINGS_STRICT = "SAML_SETTINGS_STRICT";

	private static final String SAML_SETTINGS_DEBUG = "SAML_SETTINGS_DEBUG";

	public static final String SAML_RMS_ADMIN = "SAML_RMS_ADMIN";

	public static final String SAML_LOGIN_BTN_TEXT = "SAML_LOGIN_BTN_TEXT";

	private static final String OKTA_CLIENT_ID = "OKTA_CLIENT_ID";

	private static final String OKTA_CLIENT_SECRET = "OKTA_CLIENT_SECRET";

	private static final String OKTA_SERVER_URL = "OKTA_SERVER_URL";

	private static final String OKTA_AUTHORIZATION_SERVER_ID = "OKTA_AUTHORIZATION_SERVER_ID";

	public static final String OKTA_RMS_ADMIN = "OKTA_RMS_ADMIN";

	private static GlobalConfigManager instance = new GlobalConfigManager();

	private ADServerInfo[] adServerArr = null;

	private OktaServerInfo oktaServer = null;

	private Properties samlConfig = null;

	private List<String> supportedFileFormatList = null;

	private List<String> supportedHOOPSAssemblyFormatList = null;

	private List<String> supportedHOOPSFileFormatList = null;

	private List<String> supportedCADFileFormatList = null;

	private List<String> supportedHOOPSNonAssemblyFormatList = null;

	public static final String ISYS11DF_JAR = "ISYS11df.jar";

	public static final String MEMORY_STREAM_JAR = "RMS-Perceptive-Lib.jar";

	private static String kmsUrl;

	private static boolean useKmsKeys;

	public static final String INFINISPAN_CONFIG_FILE_NAME = "infinispan.xml";

	public static final String INFINISPAN_CLUSTERED_CONFIG_FILE_NAME = "infinispan_clustered.xml";

	public static final String PERCEPTIVE_DIR = "perceptive";

	public static GlobalConfigManager getInstance(){
		return instance;
	}

	private GlobalConfigManager(){
		System.out.println("GlobalConfigManager Created");
		init();
		loadConfigParams();
		loadDBProperties();
		initKMSUrl();
	}

	private void init(){
		checkOSType();
		setInstallDir();
		setDataDir();
		setCartridgeDirs();
		initLogging();
		setTempDir();
	}

	private void initLogging() {
		System.out.println("about to initialize logging");
		File logProp = new File(dataDir, LOG4J_CONFIG_FILE);
		if(!logProp.exists()){
			//create log prop file..
			//set the properties value
			Properties prop = new Properties();
    		prop.setProperty("log4j.rootLogger", "INFO, file");
    		prop.setProperty("log4j.appender.file", "org.apache.log4j.RollingFileAppender");
    		prop.setProperty("log4j.appender.file.MaxFileSize", "5MB");
    		prop.setProperty("log4j.appender.file.MaxBackupIndex", "5");
    		prop.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
    		prop.setProperty("log4j.appender.file.layout.ConversionPattern", "%d{MM-dd-yyyy HH:mm:ss} %-5p %c{1}: - %m%n");
    		prop.setProperty("log4j.appender.file.encoding", "UTF-8");
    		prop.setProperty("log4j.category.org.apache","WARN");
    		FileOutputStream opStr = null;
    		try {
    			opStr = new FileOutputStream(logProp);
				prop.store(opStr, null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				try {
					if (opStr != null){
						opStr.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("RMSLog.Properties file not found. Creating RMSLog.properties with default configuration");
		}
		String rmsLogDir=dataDir+File.separator+"logs";
		System.out.println("RMS Log dir is "+rmsLogDir);
		Properties defaultProps = readLogPropsFile(logProp);
		String logDir=(String)defaultProps.get("log4j.appender.file.File");
		if(logDir==null || logDir.trim().length()==0){
			System.out.println("Output log file set to default configuration");
			defaultProps.put("log4j.appender.file.File",rmsLogDir+File.separator+"RMS.log");
		}
		String maxFileSize = (String)defaultProps.get("log4j.appender.file.MaxFileSize");
		if(maxFileSize!=null && maxFileSize.length()>0){
			if(maxFileSize.startsWith("0")){
				defaultProps.put("log4j.appender.file.MaxFileSize", "5MB");
			}
		}
		String maxBackup = (String)defaultProps.get("log4j.appender.file.MaxBackupIndex");
		if(maxBackup!=null && maxBackup.length()>0){
			if(maxBackup.startsWith("0")){
				defaultProps.put("log4j.appender.file.MaxBackupIndex", "5");
			}
		}
		String encoding = defaultProps.getProperty("log4j.appender.file.encoding");
		if (encoding == null || encoding.trim().length() == 0) {
			System.out.println("Enconding log file set to default configuration");
			defaultProps.setProperty("log4j.appender.file.encoding", "UTF-8");
		}
		FileOutputStream fos=null;
		try {
			fos = new FileOutputStream(logProp);
			defaultProps.store(fos, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if (fos != null){
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		PropertyConfigurator.configure(defaultProps);
		PropertyConfigurator.configureAndWatch(logProp.getAbsolutePath());
		logger = Logger.getLogger(GlobalConfigManager.class);
		logger.info("Log file initialized..");

	}

	public String getRMSVersionNumber(){
		if(versionNumWithBuild==null){
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			ResourceBundle bundle = ResourceBundle.getBundle("com.nextlabs.rms.config.RMSVersion",
					Locale.getDefault(), cl);
			String buildNumber = bundle.getString(RMS_BUILD_NUMBER);
			versionNumWithBuild = RMS_VERSION_NUMBER+" ( Build : "+buildNumber+" )";
		}
		return versionNumWithBuild;
	}

	private Properties readLogPropsFile(File logProp) {
		Properties defaultProps = new Properties();
		FileInputStream in = null;
		try{
			in = new FileInputStream(logProp);
			defaultProps.load(in);
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if (in != null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return defaultProps;
	}

	private void setTempDir() {
		tempDir = dataDir + File.separator + "temp";
		File tempDirFile = new File(tempDir);
		if(!tempDirFile.exists()){
			tempDirFile.mkdirs();
		}
		logger.info("Temp dir set to :"+tempDir);
	}

	private void checkOSType() {
        String OSName = System.getProperty("os.name");
        if (OSName != null && !OSName.toLowerCase().startsWith("win")) {
            isUnix = true;
        }
	}

	private void setInstallDir(){
		installDir=System.getenv(KEY_RMSINSTALLDIR);
		if(installDir==null|| installDir.length()==0){
			if(isUnix){
				installDir = "/opt/nextlabs/RMS/";
			}else{
				installDir = "C:\\Program Files\\nextlabs\\RMS\\";
			}
		}
	}

	private void setDataDir() {
		//logger.debug("Setting data directory");
		dataDir = System.getenv(KEY_RMSDATADIR);
		if(dataDir==null || dataDir.length()==0){
			//If not specified in ServletContext also, then go for hardcoded path for datadir.
			if(isUnix){
				dataDir = "/var/opt/nextlabs/RMS/datafiles/";
			}else{
				dataDir = "C:\\ProgramData\\nextlabs\\RMS\\datafiles\\";
			}
		}
		File dataDirFile = new File(dataDir);
		if(!dataDirFile.exists()){
			dataDirFile.mkdirs();
		}
		File bkpDirFolder = new File(dataDir, BKPDIR_NAME);
		if(!bkpDirFolder.exists()){
			bkpDirFolder.mkdirs();
		}
		bkpDir = bkpDirFolder.getAbsolutePath();
		//logger.info("Datadir set to:" + dataDir);
		licDir = dataDir + File.separator + LICENSE_FOLDER_NAME;
	}


	public String getInstallDir() {
		return installDir;
	}

	public String getDataDir() {
		return dataDir;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setWebDir(String webDir){
		this.webDir = webDir;
	}

	public String getWebDir(){
		return webDir;
	}

	public String getBkpDir(){
		return bkpDir;
	}

	public String getLicDir(){
		return licDir;
	}

	public String getCadConverterDir(){
		return cadConverterDir;
	}

	public String getWebViewerDir(){
		return webViewerDir;
	}

	public String getViewerPlugInDir(){
		return viewerPlugInDir;
	}

	public String getWinCadConverter() {
		return winCadConverter;
	}

	public String getUnixCadConverter() {
		return unixCadConverter;
	}

	public String getCadBinDir() {
		return cadBinDir;
	}

	public String getSAPViewerDir(){
		return sapViewerDir;
	}

	public void setWebViewerDir(){
		webViewerDir = new StringBuilder(webDir).append("ui").append(File.separator)
														.append("app").append(File.separator)
														.append("viewers").toString();
		sapViewerDir =  new StringBuilder(webViewerDir).append(File.separator).append(SAPVIEWER_WEBDIR_NAME).toString();
	}

	public void setCartridgeDirs(){
		cadConverterDir = new StringBuilder(installDir).append(INSTALL_EXTERNAL).append(File.separator)
																.append(CAD_DEPLOYED_PATH).toString();
		cadBinDir = new StringBuilder().append(cadConverterDir).append(File.separator).append("bin").toString();
		unixCadConverter = new StringBuilder(cadBinDir).append(File.separator)
																	.append("linux64").append(File.separator)
																	.append("converter").toString();
		winCadConverter = new StringBuilder(cadBinDir).append(File.separator)
																 .append("win64").append(File.separator)
																 .append("converter.exe").toString();
		viewerPlugInDir = new StringBuilder(installDir).append(INSTALL_PLUGINS).toString();
		docConverterDir = new StringBuilder(installDir).append(INSTALL_EXTERNAL).append(File.separator).append(PERCEPTIVE_DIR).toString();
	}

	private void loadDBProperties(){
		DBConfigManager.getInstance().loadDBProperties(getDataDir());
	}

	private void loadConfigParams() {
		BufferedInputStream inStream = null;
		StringBuffer propString = new StringBuffer();
		try {
			File file = new File(dataDir, CONFIG_FILENAME);
			if(!file.exists()){
				logger.info("Config file '"+ CONFIG_FILENAME +"' not found in datadir. Will use defaults.");
				String lineSeperator=System.getProperty("line.separator");
				propString.append("#LDAP_NUM_SERVERS=1"+lineSeperator);
				propString.append("#LDAP.1.LDAP_TYPE=<LDAP_TYPE>"+lineSeperator);
	    		propString.append("#LDAP.1.HOST_NAME=<LDAP_SERVER_HOSTNAME>"+lineSeperator);
	    		propString.append("#LDAP.1.DOMAIN=<DOMAINNAME>e.g., abc.mycompany.com"+lineSeperator);
	    		propString.append("#LDAP.1.SEARCH_BASE=e.g,DC=abc,DC=mycompany,DC=com"+lineSeperator);
	    		propString.append("#LDAP.1.USER_SEARCH_QUERY=(&(objectClass=user)(sAMAccountName=$USERID$))"+lineSeperator);
	    		propString.append("#LDAP.1.RMS_USERGROUP=<USERGROUP_NAME>"+lineSeperator);
	    		propString.append("#LDAP.1.RMS_ADMIN=<ADMIN_USERNAME>"+lineSeperator);
	    		// TC AD Query
	    		propString.append("#LDAP.1.RMS_QUERYDN=<USERNAME>"+lineSeperator);
	    		// TC AD Query
	    		propString.append("#LDAP.1.RMS_QUERYDN_PASSWORD=<USERNAME_PASSWORD>"+lineSeperator);

	    		BufferedWriter bwr = new BufferedWriter(new FileWriter(file));
	    		try {
	    			bwr.write(propString.toString());
				} catch (Exception e) {
					logger.error("Error occurred while creating sample config file", e);
				}finally{
					 bwr.flush();
					 bwr.close();
				}
			}else{
				inStream = new BufferedInputStream(new FileInputStream(new File(dataDir, CONFIG_FILENAME)));
				properties.load(inStream);
			}
		} catch (IOException e) {
			logger.error("Error occurred while reading Config file ",e);
		} finally{
			try {
				if(inStream!=null){
					inStream.close();
				}
			} catch (IOException e) {
				logger.error("Error occurred while closing stream");
			}
		}
		setSupportedFileFormats();
		setSupportedNonAssemblyHOOPSFileFormats();
		setSupportedHOOPSAssemblyFormats();
		setSupportedHOOPSFileFormats();
		setSupportedCADFileFormats();
		readADServerConfig();
		readOktaServerConfig();
		readSAMLConfig();
	}

	private void setSupportedFileFormats() {
		String fileFormats=getStringProperty(SUPPORTED_FILE_FORMATS).trim();
		if(fileFormats.equals("")){
			supportedFileFormatList=Arrays.asList(ALLOWED_FILE_EXTN.split("\\s*,\\s*"));
			logger.info("Supported file formats are "+ supportedFileFormatList.toString());
			return;
		}
		supportedFileFormatList=Arrays.asList(fileFormats.split("\\s*,\\s*"));;
		logger.info("Supported file formats are "+ supportedFileFormatList.toString());
	}

	private void setSupportedNonAssemblyHOOPSFileFormats() {
		String hoopsNonAssemblyFormats=getStringProperty(SUPPORTED_HOOPSNONASSEMBLY_FORMATS).trim();
		supportedHOOPSNonAssemblyFormatList=new ArrayList<String>();
		if(hoopsNonAssemblyFormats.equals("")){
			List<String> supportedNonAssemblyFormats=Arrays.asList(ALLOWED_HOOPSNONASSEMBLY_EXTN.split("\\s*,\\s*"));
			supportedHOOPSNonAssemblyFormatList.addAll(supportedNonAssemblyFormats);
			logger.info("Supported HOOPS non assembly formats are "+ supportedHOOPSNonAssemblyFormatList.toString());
			return;
		}
		supportedHOOPSNonAssemblyFormatList=Arrays.asList(hoopsNonAssemblyFormats.split("\\s*,\\s*"));;
		logger.info("Supported HOOPS non assembly formats are "+ supportedHOOPSNonAssemblyFormatList.toString());
	}

	private void setSupportedHOOPSAssemblyFormats(){
		String hoopsAssemblyFormats=getStringProperty(SUPPORTED_HOOPSASSEMBLY_FORMATS).trim();
		supportedHOOPSAssemblyFormatList=new ArrayList<String>();
		if(hoopsAssemblyFormats.equals("")){
			List<String> supportedAssemblyFormats=Arrays.asList(ALLOWED_HOOPSASSEMBLY_EXTN.split("\\s*,\\s*"));
			supportedHOOPSAssemblyFormatList.addAll(supportedAssemblyFormats);
			logger.info("Supported HOOPS assembly formats are "+ supportedHOOPSAssemblyFormatList.toString());
			return;
		}
		supportedHOOPSAssemblyFormatList=Arrays.asList(hoopsAssemblyFormats.split("\\s*,\\s*"));
		logger.info("Supported HOOPS assembly formats are "+ supportedHOOPSAssemblyFormatList.toString());
	}

	private void setSupportedHOOPSFileFormats() {
		supportedHOOPSFileFormatList=new ArrayList<String>();
		supportedHOOPSFileFormatList.addAll(supportedHOOPSNonAssemblyFormatList);
		supportedHOOPSFileFormatList.addAll(supportedHOOPSAssemblyFormatList);
		logger.info("Supported HOOPS file formats are "+ supportedHOOPSFileFormatList.toString());
	}

	private void setSupportedCADFileFormats(){
		supportedCADFileFormatList=new ArrayList<String>();
		supportedCADFileFormatList.addAll(supportedHOOPSFileFormatList);
		supportedCADFileFormatList.add(RH_FILE_EXTN);
		supportedCADFileFormatList.add(VDS_FILE_EXTN);
		logger.info("Supported CAD file formats are "+ supportedCADFileFormatList.toString());

	}

	private void readADServerConfig(){
		int numADServers = getIntProperty("LDAP_NUM_SERVERS");
		if(numADServers <= 0){
			logger.error("LDAP_NUM_SERVERS attribute should be set to a value of 1 or above");
			numADServers = 1;
		}
		adServerArr = new ADServerInfo[numADServers];
		for (int i = 1;i<=numADServers;i++) {
			adServerArr[i-1]=new ADServerInfo();
			adServerArr[i-1].setLdapHost(getStringProperty("LDAP."+i+"."+LDAP_HOST_SUFFIX));
			adServerArr[i-1].setDomain(getStringProperty("LDAP."+i+"."+LDAP_DOMAIN_SUFFIX));
			adServerArr[i-1].setSearchBase(getStringProperty("LDAP."+i+"."+LDAP_SEARCH_BASE_SUFFIX));
			adServerArr[i-1].setUserSearchQuery(getStringProperty("LDAP."+i+"."+LDAP_USER_SEARCH_QUERY_SUFFIX));
			adServerArr[i-1].setRmsAdmin(getStringProperty("LDAP."+i+"."+LDAP_RMS_ADMIN_SUFFIX));
			adServerArr[i-1].setRmsGroup(getStringProperty("LDAP."+i+"."+LDAP_RMS_USERGROUP_SUFFIX));
			adServerArr[i-1].setLdapType(getStringProperty("LDAP."+i+"."+LDAP_TYPE));
			adServerArr[i-1].setUniqueId(getStringProperty("LDAP."+i+"."+LDAP_RMS_USER_UNIQUEID));
			adServerArr[i-1].setSecurityPrincipalUseUserID(getBooleanProperty("LDAP."+i+"."+LDAP_SECURITY_PRINCIPAL_USE_USERID));
			adServerArr[i-1].setLdapSSL(getBooleanProperty("LDAP."+i+"."+LDAP_AD_SSL));
			adServerArr[i-1].setTrustStore(getStringProperty("LDAP."+i+"."+LDAP_AD_TRUSTSTORE));
			adServerArr[i-1].setTrustStorePassword(getStringProperty("LDAP."+i+"."+LDAP_AD_TRUSTSTOREPASSWORD));

			// TC AD Query
			adServerArr[i-1].setQueryDN(getStringProperty("LDAP."+i+"."+LDAP_RMS_QUERYDN_SUFFIX));
			String encryptedPasswod = getStringProperty("LDAP."+i+"."+LDAP_RMS_QUERYDN_PASSWORD_SUFFIX);
			EncryptionUtil eu = new EncryptionUtil();
			if(encryptedPasswod!=null && encryptedPasswod.length()>0){
				adServerArr[i-1].setQueryDNPassword(eu.decrypt(encryptedPasswod));
			}

			logger.debug("LDAPHost : "+adServerArr[i-1].getLdapHost()+"\t searchBase : "+
					adServerArr[i-1].getSearchBase()+"\t Domain : "+adServerArr[i-1].getDomain()+
					"\t userSearchQuery : "+adServerArr[i-1].getUserSearchQuery()+
					"\t sslKeyStore : "+adServerArr[i-1].getTrustStore() +
					"\t RMSUserGroup : "+adServerArr[i-1].getRmsGroup() +
					"\t RMSQueryDN : " + adServerArr[i-1].getQueryDN() );
		}
	}

	private void readOktaServerConfig() {
	    String clientId = getStringProperty(OKTA_CLIENT_ID);
	    String clientSecret = getStringProperty(OKTA_CLIENT_SECRET);
	    String serverUrl = getStringProperty(OKTA_SERVER_URL);
	    String authServerName = getStringProperty(OKTA_AUTHORIZATION_SERVER_ID);
	    if(!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret) || !StringUtils.hasText(serverUrl) || !StringUtils.hasText(authServerName)){
	        logger.debug("Okta Server is not configured");
	        return;
	    }
	    oktaServer = new OktaServerInfo(clientId, clientSecret, serverUrl, authServerName);
	}

	private void readSAMLConfig() {
        String idpId = getStringProperty(SAML_IDP_ENTITY_ID);
        String idpSsoUrl = getStringProperty(SAML_IDP_SSO_URL);
        String idpCert = getStringProperty(SAML_IDP_X509_CERT);
        String spId = getStringProperty(SAML_SP_ENTITY_ID);
        String spAcsUrl = getStringProperty(SAML_SP_ACS_URL);

        if (!StringUtils.hasText(idpId) || !StringUtils.hasText(idpSsoUrl) || !StringUtils.hasText(idpCert) || !StringUtils.hasText(spId) || !StringUtils.hasText(spAcsUrl)) {
            logger.debug("SAML authentication is not configured");
            return;
        }

        String strict = getStringProperty(SAML_SETTINGS_STRICT);
        String debug = getStringProperty(SAML_SETTINGS_DEBUG);

        String spNameIdFormat = getStringProperty(SAML_SP_NAME_ID_FORMAT);
        if ("emailAddress".equalsIgnoreCase(spNameIdFormat)) {
            spNameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
        } else if ("persistent".equalsIgnoreCase(spNameIdFormat)) {
            spNameIdFormat = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
        } else if ("transient".equalsIgnoreCase(spNameIdFormat)) {
            spNameIdFormat = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
        } else {
            spNameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
        }

        String spLogoutUrl = "";
        String spCert = getStringProperty(SAML_SP_X509_CERT);
        String spKey = getStringProperty(SAML_SP_PRIV_KEY);
        String signRequests = String.valueOf(StringUtils.hasText(spCert) && StringUtils.hasText(spKey));

        String idpLogoutUrl = "";
        String idpLogoutSloUrl = "";

        String signatureAlgorithm = getStringProperty(SAML_SETTINGS_SIGN_ALGO);
        if ("sha1".equalsIgnoreCase(signatureAlgorithm)) {
            signatureAlgorithm = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
        } else if ("sha384".equalsIgnoreCase(signatureAlgorithm)) {
            signatureAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
        } else if ("sha512".equalsIgnoreCase(signatureAlgorithm)) {
            signatureAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
        } else {
            signatureAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
        }
        String authnContext = getStringProperty(SAML_SETTINGS_AUTHN_CONTEXT);

        strict = ("false".equalsIgnoreCase(strict) || "no".equalsIgnoreCase(strict)) ? "false" : "true";
        debug = ("true".equalsIgnoreCase(debug) || "yes".equalsIgnoreCase(debug)) ? "true" : "false";

        samlConfig = new Properties();
        samlConfig.setProperty("onelogin.saml2.strict", strict);
        samlConfig.setProperty("onelogin.saml2.debug", debug);

        samlConfig.setProperty("onelogin.saml2.sp.entityid", spId);
        samlConfig.setProperty("onelogin.saml2.sp.assertion_consumer_service.url", spAcsUrl);
        samlConfig.setProperty("onelogin.saml2.sp.assertion_consumer_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        samlConfig.setProperty("onelogin.saml2.sp.single_logout_service.url", spLogoutUrl);
        samlConfig.setProperty("onelogin.saml2.sp.single_logout_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
        samlConfig.setProperty("onelogin.saml2.sp.nameidformat", spNameIdFormat);
        samlConfig.setProperty("onelogin.saml2.sp.x509cert", spCert);
        samlConfig.setProperty("onelogin.saml2.sp.privatekey", spKey);

        samlConfig.setProperty("onelogin.saml2.idp.entityid", idpId);
        samlConfig.setProperty("onelogin.saml2.idp.single_sign_on_service.url", idpSsoUrl);
        samlConfig.setProperty("onelogin.saml2.idp.single_sign_on_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
        samlConfig.setProperty("onelogin.saml2.idp.single_logout_service.url", idpLogoutUrl);
        samlConfig.setProperty("onelogin.saml2.idp.single_logout_service.response.url", idpLogoutSloUrl);
        samlConfig.setProperty("onelogin.saml2.idp.single_logout_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
        samlConfig.setProperty("onelogin.saml2.idp.x509cert", idpCert);

        samlConfig.setProperty("onelogin.saml2.security.nameid_encrypted", signRequests);
        samlConfig.setProperty("onelogin.saml2.security.authnrequest_signed", signRequests);
        samlConfig.setProperty("onelogin.saml2.security.logoutrequest_signed", signRequests);
        samlConfig.setProperty("onelogin.saml2.security.logoutresponse_signed", signRequests);
        samlConfig.setProperty("onelogin.saml2.security.sign_metadata", signRequests);

        samlConfig.setProperty("onelogin.saml2.security.want_messages_signed", "false");
        samlConfig.setProperty("onelogin.saml2.security.want_assertions_signed", "false");
        samlConfig.setProperty("onelogin.saml2.security.want_assertions_encrypted", "false");
        samlConfig.setProperty("onelogin.saml2.security.want_nameid_encrypted", "false");

        samlConfig.setProperty("onelogin.saml2.security.requested_authncontext", authnContext);
        samlConfig.setProperty("onelogin.saml2.security.onelogin.saml2.security.requested_authncontextcomparison", "exact");
        samlConfig.setProperty("onelogin.saml2.security.want_xml_validation", "true");
        samlConfig.setProperty("onelogin.saml2.security.signature_algorithm", signatureAlgorithm);
    }

	public List<String> getSupportedFileFormat() {
		return supportedFileFormatList;
	}

	public List<String> getSupportedHOOPSAssemblyFormatList(){
		return supportedHOOPSAssemblyFormatList;
	}

	public List<String> getSupportedHOOPSFileFormatList() {
		return supportedHOOPSFileFormatList;
	}

	public List<String> getSupportedCADFileFormatList() {
		return supportedCADFileFormatList;
	}

	public List<String> getSupportedHOOPSNonAssemblyFormatList() {
		return supportedHOOPSNonAssemblyFormatList;
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

	public ADServerInfo[] getAdServerArr() {
		return adServerArr;
	}

	public ADServerInfo getAdServerInfo(String domainName){
		for (ADServerInfo adSrvr : adServerArr) {
			if(adSrvr.getDomain().equalsIgnoreCase(domainName)){
				return adSrvr;
			}
		}
		return null;
	}

	public OktaServerInfo getOktaServerInfo(){
	    return oktaServer;
	}

	public Properties getSAMLConfig(){
	    return samlConfig;
	}

	public String[] getDomainNames(){
		if(adServerArr==null || adServerArr.length==0){
			return null;
		}
		String[] domainNames = new String[adServerArr.length];
		int i=0;
		for (ADServerInfo adServerInfo : adServerArr) {
			domainNames[i] = adServerInfo.getDomain();
			i++;
		}
		return domainNames;
	}

	public String getCopyrightYear(){
		return  "2014 - " + Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
	}

	public Properties getDatabaseProperties(){
		return DBConfigManager.getInstance().getDatabaseProperties();
	}

	private void initKMSUrl(){
		kmsUrl= getStringProperty(KMS_URL_KEY).trim();
		if(kmsUrl!=null && kmsUrl.length()>0)
			kmsUrl = kmsUrl.endsWith("/") ? (kmsUrl.substring(0, kmsUrl.length()-1)+KMS_CONTEXT_NAME) : (kmsUrl+KMS_CONTEXT_NAME);

		logger.info("KMS Url is " + kmsUrl);
		useKmsKeys=getBooleanProperty(USE_KMS_KEYS);
	}

	public boolean useKmsKeys(){
		return useKmsKeys;
	}

	public String getKMSUrl(){
		return kmsUrl;
	}

	public Locale getCurrentUserLocale() {
		// This can be used in the future for MUI(Multilingual UI). The client locale should be set when the session is initialized.
		// For now, we'll just use the server locale.
		return Locale.getDefault();
	}

	public String getDocConverterDir() {
		return docConverterDir;
	}

	public void setDocConverterDir(String docViewerDir) {
		this.docConverterDir = docViewerDir;
	}

}
