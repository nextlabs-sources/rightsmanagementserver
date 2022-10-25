package com.nextlabs.rms.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.leafdigital.iptocountry.IpToCountry;
import com.leafdigital.iptocountry.IpToCountry.Informer;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;

public class UserLocationProvider {

	private static Logger logger = Logger.getLogger(UserLocationProvider.class);
	private static UserLocationProvider instance = new UserLocationProvider();
	private Informer informer = null;
	private InetAddress address;
	private IpToCountry ip;
	private String countryCode;
	public static String DATABASE_DOWNLOAD_URL = "http://software77.net/geo-ip/?DL=1";
	public static final long DAILY = 24 * 60 * 60 * 1000;
	public static final long WEEKLY = 7 * DAILY;
	public static final long MONTHLY = 30 * DAILY;

	private UserLocationProvider() {
		initUserLocationDb();
	}

	public void initUserLocationDb() {
		if(GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.USERLOCATION_DB_URL)!=null && GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.USERLOCATION_DB_URL).length()>0){
			DATABASE_DOWNLOAD_URL=GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.USERLOCATION_DB_URL);
		}
		File file = new File(GlobalConfigManager.getInstance().getDataDir());
		long updateDBFrequency = getUpdateFrequency();
		try {
			ip = new IpToCountry(file, DATABASE_DOWNLOAD_URL,
					updateDBFrequency, informer);
		} catch (IllegalArgumentException e) {
			logger.error("Error in the arguments passed to IPToCountry");
		} catch (IOException e) {
			logger.error("Problem loading the file");
		} catch (InterruptedException e) {
			logger.error("Location Database constructor is interrupted");
		}
	}

	public long getUpdateFrequency() {
		long updateFreq;
		String updateFrequency = ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(
				ConfigManager.LOCATION_UPDATE_FREQUENCY);

		if (updateFrequency.equalsIgnoreCase("Monthly")) {
			updateFreq = MONTHLY;
		} else if (updateFrequency.equalsIgnoreCase("Weekly")) {
			updateFreq = WEEKLY;
		} else {
			updateFreq = DAILY;
		}
		return updateFreq;
	}

	public static UserLocationProvider getInstance() {
		return instance;
	}
	
	public IpToCountry getIp() {
		return ip;
	}


	public String getCountry(String ipAddress) {
		try {
			address = InetAddress.getByName(ipAddress);
			countryCode = ip.getCountryCode(address);

		} catch (UnknownHostException e) {
			logger.error("Unknown Host");
		}
		logger.debug("Country of IP " + ipAddress + "is :" + countryCode);
		return countryCode;
	}

}
