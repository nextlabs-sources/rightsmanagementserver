package com.test.nextlabs.kms.testAutomation;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

public class TestingConfigManager {
	
	private static TestingConfigManager manager = new TestingConfigManager();
	
	private Properties properties = new Properties();
	
	private String dataDir = "";
	
	public static final String KMS_FOLDER_NAME = "KMS";
	
	public static final String TEST_AUTOMATION_DATA_FOLDER="TestAutomation";
	
	public static final String TEST_AUTOMATION_DATA_FOLDER_NAME =TEST_AUTOMATION_DATA_FOLDER+File.separator+"KMSTestData";
	
	public static final String DEFAULT_IMPL_FOLDER_NAME = "Default_Impl";
	
	public static final String CONFIG_FILENAME = "KMSConfigTest.properties";
	
	public static final String TEST_RESULT_FILENAME = "KMSTestResult.log";

	private static final String KEY_RMS_TEST_DATADIR = "RMS_TEST_DIR";
	
	private Properties smtpServerProps = null;

	public static final String UNTRUSTED_CERTIFICATE_VALUE = "MIIDfzCCAmegAwIBAgIEezATwTANBgkqhkiG9w0BAQsFADBwMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTETMBEGA1UEBxMKQ2FsaWZvcm5pYTERMA8GA1UEChMITmV4dGxhYnMxETAPBgNVBAsTCE5leHRsYWJzMREwDwYDVQQDEwhOZXh0bGFiczAeFw0xNjAxMTEwNTA5NDRaFw0yNjAxMDgwNTA5NDRaMHAxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRMwEQYDVQQHEwpDYWxpZm9ybmlhMREwDwYDVQQKEwhOZXh0bGFiczERMA8GA1UECxMITmV4dGxhYnMxETAPBgNVBAMTCE5leHRsYWJzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtlh8euUmqDrcbXTRNYAOpfytS7TyBRXXjExZ+k+lAj/dxCZTvDirU90YVyze8hD8f5cbRb3/ZDENvFdGnjEKUY7A0fIdku6bmlqdCQwxhI3bWFk3d7G8ZWU9t5PjfSPo/XjQMDScw7CYqj02AYMIbbJiI6AFUiG5q49Gx0WAMfpskFsWfTra2L93OFN4U1c3euhWJhlP72GZI/Kswc9wBi/pi6rKkr213Qog2srgtAvh+MMKpjt4U6tCUJD0Gm1u8j7E1fbuAYFZhOI//C35SS70q4/snVT7YtxA3VIy5RaWc0Bct7N32xgqFsEiDziBfuv5tAouOuyfORmggyPAQQIDAQABoyEwHzAdBgNVHQ4EFgQUyehM/ZFgpAokDNMrQcYnH4oaV2IwDQYJKoZIhvcNAQELBQADggEBAEd8W/Gum1EJcT0RA7lHIS/5Dbxuvmjdvn95EIP6XWY1Hte7KJdIudFRk8p/iN5eE1YicEF3x3dZ61x2VvMzLwpMzmLj9wbdoEYF7l71bdE2+ROK4qs9JvFn+rGu7AxsrOhjBPUA37OZtK6hCorpWwUSR4c9MfmWkOBvQrmezvu4K8Q6Mf+djHRcBGVvPWQk/eYJ0SJjNPSQ63LFLEJv7QVGlrtIleAzHDmtqrgismT7ulcrayR51I/2f4KtXSLEOwqBUbDJtYWvIVO+o4hslYwNThVPce9h9GoH4zallaUpyVwF45eaeDa4Is/XmrfvqWzBkNq4WVKVNLCsupaBMt8=";

	public static final String WEBSVC_CERTIFICATE_NAME = "X-AUTH-CERT"; //"X-NXL-S-CERT";
	
	public static final String TRUSTED_CERTIFICATE_VALUE = "MIIDfzCCAmegAwIBAgIEJ3zkijANBgkqhkiG9w0BAQsFADBwMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTETMBEGA1UEBxMKQ2FsaWZvcm5pYTERMA8GA1UEChMITmV4dGxhYnMxETAPBgNVBAsTCE5leHRsYWJzMREwDwYDVQQDEwhOZXh0bGFiczAeFw0xNjAxMTEwNTEyMzJaFw0yNjAxMDgwNTEyMzJaMHAxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRMwEQYDVQQHEwpDYWxpZm9ybmlhMREwDwYDVQQKEwhOZXh0bGFiczERMA8GA1UECxMITmV4dGxhYnMxETAPBgNVBAMTCE5leHRsYWJzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkfC884jG73jSmriXbEwJoreJP98uaLZakrSkd71lmhYQG3nFcafe/iQYbxgwaIwGj9JwRqmBzJwsNjgCyldWobBnqfIoNa7fq3OxAV6vkOUaQ3xOfmYx34iYiFjYDLce+ZTrYTCps1KCIUJF7u00Ls8f1n1zdwwL9vtt+YHSqFtA7IE4NAqKTVUOmv2sYh1bf0DVbv9j3rsvWqucz6YnlT4rw6+h1doytmGQ4x4MD9Ij3gSHAA3tOxs+bF7naM+kdWjKEdn13fTNGSWDowIi3yYHg6h1/pDrgDCv62Y7LzGSxfABWpV4XwL9+4SXiVcfvRUUQDP3ZVFsAcKYdUwgoQIDAQABoyEwHzAdBgNVHQ4EFgQUlQmL0QOmCsulmMLXazmG+8HlhTowDQYJKoZIhvcNAQELBQADggEBAI74EeWFwXXZ3sXVJQxgMgTGgy0z4QDZLAuytiGv9Pkucv3BZyaPuBGEtSFUkv1+HZxizHI8tmORzndUFy8lLh0DyRLVKMtas2jbt5x+J1b8qgQ/WMgi9x3jJUH/nVEDXCKA/hvAixZdsmrLpUu3ZeJSRLjodpvXsqqv6lkoavL3afFqTOX/cJDM2KdMKTimf7l0wLO9qfZsLrxTCHHPBg5JFFwFkPghIEzLSs++EHyKAKst6nhVw6rnNFa9ABDJXnD47V9Vowfblr2K9KQVRcW3muy2DMfDgMfHVzizy+dSn8b0SyYefWXQ9pllnXWGgFmdUcp85WjQWw/CGP8wnFM=";

	public static final String KMS_URL = "KMS_URL";
			
	public static final String LOCALHOST_URL = "https://localhost:8443/KMS";
	
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
	}
	
	private void setDataDir() throws Exception {
		dataDir = System.getenv(KEY_RMS_TEST_DATADIR);
		if(dataDir == null){
			throw new Exception("Test data directory environment variable not set");
		}
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
