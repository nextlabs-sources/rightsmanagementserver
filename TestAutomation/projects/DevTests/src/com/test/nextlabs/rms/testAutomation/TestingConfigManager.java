package com.test.nextlabs.rms.testAutomation;

import java.io.*;
import java.util.Properties;
import java.util.StringTokenizer;

public class TestingConfigManager {
	
	private static TestingConfigManager manager = new TestingConfigManager();
	
	private Properties properties = new Properties();
	
	private String dataDir = "";
	
	public static final String RMS_FOLDER_NAME = "RMS";
	
	public static final String TEST_AUTOMATION_DATA_FOLDER = "RMSTestAutomation";
	
	public static final String TEST_AUTOMATION_DATA_FOLDER_NAME = TEST_AUTOMATION_DATA_FOLDER+File.separator + "TestData";
	
	public static final String LOGGING_FOLDER_NAME = "Logging";
	
	public static final String HEARTBEAT_FOLDER_NAME = "HeartBeat";
	
	public static final String REGISTER_AGENT_FOLDER_NAME = "RegisterAgent";

	public static final String ADD_REPO_FOLDER_NAME = "AddRepo";

	public static final String UPDATE_REPO_FOLDER_NAME = "UpdateRepo";

	public static final String GET_REPOSITORY_DETAILS_FOLDER_NAME = "GetRepositoryDetails";

	public static final String REMOVE_REPO_FOLDER_NAME = "RemoveRepo";

	public static final String MARK_FAVORITE_FOLDER_NAME = "MarkFavorite";

	public static final String UNMARK_FAVORITE_FOLDER_NAME = "UnmarkFavorite";

	public static final String MARK_OFFLINE_FOLDER_NAME = "MarkOffline";

	public static final String UNMARK_OFFLINE_FOLDER_NAME = "UnmarkOffline";

    public static final String USER_PROFILE_FOLDER_NAME = "UserProfile";

    public static final String CONFIG_FILENAME = "RMSConfigTest.properties";

	private static final String KEY_RMS_TEST_DATADIR = "RMS_TEST_DIR";
	
	public static final String CONVERT_FILE_FOLDER_NAME = "ConvertFile";
	
	public static final String POLICY_EVAL_FOLDER_NAME = "PolicyEval";
	
	private Properties smtpServerProps = null;

	public static final String UNTRUSTED_CERTIFICATE_VALUE = "MIIDmzCCAoOgAwIBAgIEI8+FCTANBgkqhkiG9w0BAQsFADB+MQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCVNhbiBNYXRlbzERMA8GA1UEChMITmV4dExhYnMxGjAYBgNVBAsTEVJpZ2h0cyBNYW5hZ2VtZW50MR8wHQYDVQQDExZSaWdodHMgTWFuYWdlbWVudCBUZW1wMB4XDTE1MDUyNzE0MDI0MVoXDTI1MDUyNDE0MDI0MVowfjELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRIwEAYDVQQHEwlTYW4gTWF0ZW8xETAPBgNVBAoTCE5leHRMYWJzMRowGAYDVQQLExFSaWdodHMgTWFuYWdlbWVudDEfMB0GA1UEAxMWUmlnaHRzIE1hbmFnZW1lbnQgVGVtcDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAPUFAKjEmwL/oWQpafK5vOz8/dOEf9mmcjKawhxNhJJ5R+4olKHF9ZaKMve542ehSKngA53buaPsvP96ix7j8O8E2DHnYqbR5I9jNfZarIWjAwfO94TDvQovuIFBCWMnJXsRUtVm36cF6WpQdqctIbgvSbjGbgYacqGZ6QaaWrySGxupjyB8lfD6dYBG5lXFRcFA7QQVbNAGM7Xis2S3sPZOch4VJK7faX2xRyW6sIKL0FU8W9HCbm2PjG+XBr+dmsP3lk6HOqlSEy55HRYldMI/KCSlTGIcUHjH0qpiBxceSHILgY+YOqJ3l6/d8k9ui3MK2XGUhNFgwwLYFruk1l8CAwEAAaMhMB8wHQYDVR0OBBYEFGKcZJB9ZVJ6q/T2DNJIUoQEqx6sMA0GCSqGSIb3DQEBCwUAA4IBAQA9oFoR9GYVvba1WTdq2sl7kqTxqTPkUtD5LGi5A7q1yxMkAwsR2kW00L5dbRmADT7PjE3x42V2ZHHuYhDjGg/zm+2xHVrUWl2ZxHodmHz6+qDbdAZ3+9U4Zz7nt2oxDFghp/eE1adXa2kfAIZzn8VVamD6TS9O0R/KyXToYgpjLmz6QD9GFsz5wGbVsnJGWTxfiNjX3LnFIkqJU8rHn1DcMyB3/xd3ytUJzKrAnD8f46JpfR1amJOQAxiDy5+kW1OnclGBImS9iisvCmwU3+UNixbFAAxymBA9VvAO90sw0tHcLN7M1NSpenVlAnJTHhGuLSepk8gv4jAEsa9+DPKR";

	public static final String WEBSVC_CERTIFICATE_NAME = "X-NXL-S-CERT";
	
	public static final String TRUSTED_CERTIFICATE_VALUE = "MIIDkTCCAnmgAwIBAgIEUUgJizANBgkqhkiG9w0BAQsFADB5MQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCVNhbiBNYXRlbzERMA8GA1UEChMITmV4dExhYnMxGjAYBgNVBAsTEVJpZ2h0cyBNYW5hZ2VtZW50MRowGAYDVQQDExFSaWdodHMgTWFuYWdlbWVudDAeFw0xNTA1MjcxNDAzNTBaFw0yNTA1MjQxNDAzNTBaMHkxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTESMBAGA1UEBxMJU2FuIE1hdGVvMREwDwYDVQQKEwhOZXh0TGFiczEaMBgGA1UECxMRUmlnaHRzIE1hbmFnZW1lbnQxGjAYBgNVBAMTEVJpZ2h0cyBNYW5hZ2VtZW50MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl5HKn8BZ0yyY0JxpvQT0IkRPfKeXnvR53L0y5Nu1DkdAwc8mcd4H7ePUe3QiToKgzVZUJatUQ+bOlCDIQB/bgBO88Pi5ozNGEk4iobpIR0NYi5PHuYqNlsiUXD3IARle1XpiE3m4JU6lk1p9y8YimX3dE+B05lS58MEVya998Q1yh2C0NYOfECkQv4w8FtbPJbn4VTfqanokKjcx0L5aSpS/q06AG24SruwU4sX6pMspHME8fOUbBTm+VnhpArZ35FyJj1o+BoCVt4Pp0UG+lIiBMj3E50m/Ii/YnuWrtozsUZhd6w7/QIfpTn51ZehKOhHAhsRgWjeDvp7EOp45WwIDAQABoyEwHzAdBgNVHQ4EFgQUWnITlK2cVYTVHF2J1mEns/BOP38wDQYJKoZIhvcNAQELBQADggEBAAndNhd3Shu6b1sk69LsLMccxttFP+43gfapDsyefdqdybwQwdw8vgJOikbMgJFhPHNR+0HX0Oh/u3nIqYxjGTwE5jFy6fu/P1iAyinYECuGmj1MCpsjEtLjIClaWDRKETlk5mGeRxtHtexMHITkJv/6Qunh7rYC+RvwBGAAvgIlQQbMzMRAzAY4ttnscmIVPaU+ofED8YOE+pR8MWkYLG9EJViDIVDKZO9w9PLSqFu2wvLOg58Bg7sEfHXEvikYR0SQdm2Teouw0iQCYJipbMtCl6EwzgjuZtKGEAy2w5WareoTkvQdHr8Gp7lkofRwPjdUWHqfI7GOVLZwiKLdkhg=";
	
	public static final String SMTP_HOST = "SMTP_HOST";
	
	public static final String SMTP_USER_NAME = "SMTP_USER_NAME";
	
	public static final String SMTP_PASSWORD = "SMTP_PASSWORD";
	
	public static final String SMTP_ENABLE_TTLS = "SMTP_ENABLE_TTLS";
	
	public static final String SMTP_PORT = "SMTP_PORT";
	
	public static final String SMTP_AUTH = "SMTP_AUTH";

	public static final String REPORT_EMAIL_SUBJECT = "REPORT_EMAIL_SUBJECT";

	public static final String RMS_TEST_TO_EMAIL = "RMS_TEST_TO_EMAIL";

	public static final String RMS_TEST_CC_EMAIL = "RMS_TEST_CC_EMAIL";

	public static final String SERVICE_URL = "SERVICE_URL";
			
	public static final String LOCALHOST_SERVICE_URL = "https://localhost:8443/RMS/service/";
	
	private TestingConfigManager(){
		try {
			loadConfigParams();
		} catch (Exception e) {
			System.out.println("Exception occured while initializing data directory");
		}
	}

	public static TestingConfigManager getInstance(){
		return manager;
	}

	private void loadConfigParams() throws Exception {
		setDataDir();
		BufferedInputStream inStream = null;
		try {
			File file = new File(dataDir, CONFIG_FILENAME);
			StringBuffer propString = new StringBuffer();
			if(!file.exists()){
				System.out.println("Config file '"+ CONFIG_FILENAME +"' not found in datadir. Will use defaults.");
				String lineSeperator=System.getProperty("line.separator");
				propString.append("#SMTP_HOST=smtp.gmail.com"+lineSeperator);
	    		propString.append("#SMTP_USER_NAME=test"+lineSeperator);
	    		propString.append("#SMTP_PASSWORD=password"+lineSeperator);
	    		propString.append("#ENABLE_TTLS=true"+lineSeperator);
	    		propString.append("#SMTP_PORT=587"+lineSeperator);
	    		propString.append("#SMTP_AUTH=true"+lineSeperator);
	    		propString.append("#REPORT_EMAIL_SUBJECT=RMS Test Report"+lineSeperator);
	    		propString.append("#RMS_TEST_TO_EMAIL=admin@gmail.com"+lineSeperator);
	    		
	    		BufferedWriter bwr = new BufferedWriter(new FileWriter(file));
	    		try {	    			
	    			bwr.write(propString.toString());
				} catch (Exception e) {
					System.out.println("Error occurred while creating sample config file"+ e.getMessage());
				}finally{
					 bwr.flush();
					 bwr.close();
				}	    		
			}else{
				inStream = new BufferedInputStream(new FileInputStream(new File(dataDir, CONFIG_FILENAME)));
				properties.load(inStream);				
			}
		} catch (IOException e) {
			System.out.println("Error occurred while reading Config file ");
			e.printStackTrace();
		} finally{
			try {
				if(inStream!=null){					
					inStream.close();
				}
			} catch (IOException e) {
				System.out.println("Error occurred while closing stream");
			}
		}
		initSMTP();
	}
	
	private void setDataDir() throws Exception {
		//logger.debug("Setting data directory");
		dataDir = System.getenv(KEY_RMS_TEST_DATADIR);
		if(dataDir == null){
			throw new Exception("Test data directory environment variable not set");
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
			System.out.println("Error occurred while getting value for key:"+key);
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
			System.out.println("Error occurred while getting value for key:"+key);
			return val;
		}
	}

	public String getDataDir() {
		return dataDir;
	}
	
	public Properties getSmtpServerProps() {
		return smtpServerProps;
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
	
	public String[] getStringPropertyArray(String key) {
		String val = properties.getProperty(key);
		if(val == null){
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(val,",");
		String[] propArray = new String[tokenizer.countTokens()];
		int count = 0;
		while(tokenizer.hasMoreTokens()){
			propArray[count++]=tokenizer.nextToken().trim();
		}
		return propArray;
	}
}
