package com.nextlabs.rms.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.leafdigital.iptocountry.IpToCountry;
import com.leafdigital.iptocountry.IpToCountry.Informer;
import com.nextlabs.rms.config.GlobalConfigManager;

public class UserLocationDBUpdater {

	private Informer informer = null;
	
	private static Logger logger = Logger
			.getLogger(UserLocationDBUpdater.class);

	private Object monitorObject = new Object(); 
	
	public boolean downloadUpdate() {

		boolean databaseUpdated = false;
		long updateDBFrequency =UserLocationProvider.getInstance().getUpdateFrequency();
		try {
			File tempDIR = new File(GlobalConfigManager.getInstance().getTempDir());
			File currentDBFile = new File(GlobalConfigManager.getInstance()
					.getDataDir(), GlobalConfigManager.LOCATION_DATABASE);
			logger.debug("About to download IP2Country Database Update at:"+tempDIR);
			FileUtils.copyFileToDirectory(currentDBFile, tempDIR);
			logger.debug("IP2Country Database copied from "+currentDBFile+" to "+tempDIR);
			logger.debug("IP2Country Database download URL "+UserLocationProvider.DATABASE_DOWNLOAD_URL);
			informer=new DownloadInformer(monitorObject);
			IpToCountry ip = new IpToCountry(tempDIR,
					UserLocationProvider.DATABASE_DOWNLOAD_URL,
					updateDBFrequency, informer);
			synchronized (monitorObject) {
				logger.debug("Waiting for the IP2Country Database download to complete");
				monitorObject.wait();
			}
			File tempDBFile = new File(tempDIR,
					"IpToCountry.csv.download");
			while(tempDBFile.exists()){
				logger.debug("Temp Download file exists");
				}
			File downloadedDBFile = new File(tempDIR,
					GlobalConfigManager.LOCATION_DATABASE);
			logger.debug("IP2Country Database in datadir "+currentDBFile.getAbsolutePath());
			logger.debug("IP2Country Database in temp "+downloadedDBFile.getAbsolutePath());
			logger.debug("IP2Country Database in datadir file Time "+currentDBFile.lastModified());
			logger.debug("IP2Country Database in temp "+downloadedDBFile.lastModified());
			if (currentDBFile.lastModified() != downloadedDBFile.lastModified()) {
				FileUtils.copyFileToDirectory(downloadedDBFile, new File(
						GlobalConfigManager.getInstance().getDataDir()), true);
				databaseUpdated = true;
				downloadedDBFile.delete();
			}
		} catch (IOException e) {
			logger.error("Error occured while loading the Location database");
		} catch (InterruptedException e) {
			logger.error("Error occured in UserLocationProvider");
		} catch (IllegalArgumentException e) {
			logger.error("Error occured in instantiating UserLocationProvider");
		}
		logger.debug("Database update  result" +databaseUpdated);
		return databaseUpdated;
	}
	
	
	private class DownloadInformer implements Informer{

		private Object monitorObj = null;
		
		boolean downloaded=false;
		
		public DownloadInformer(Object monitorObject){
			this.monitorObj = monitorObject; 
		}
		
		@Override
		public void downloadFailed(Throwable arg0) {
			// TODO Auto-generated method stub
			synchronized (monitorObj) {
				logger.debug("Download IP2Country Database failed ");
				monitorObj.notify();
			}
		}

		@Override
		public void fileDownloaded(int arg0, int arg1) {
			synchronized (monitorObj) {
				logger.debug("IP2Country Database downloaded Successfully ");
				downloaded=true;
				
			}
		}

		@Override
		public void fileLoaded(int arg0, int arg1, int arg2) {
			synchronized (monitorObj) {
				logger.debug("IP2Country Database loaded Successfully ");
				if(downloaded){
					monitorObj.notify();
				}
				
			}
		}

		@Override
		public void lineError(int arg0, String arg1) {
			synchronized (monitorObj) {
				logger.debug("Download IP2Country Database lineError ");
				monitorObj.notify();
			}
		}

		@Override
		public void loadFailed(Throwable arg0) {
			synchronized (monitorObj) {
				logger.debug("IP2Country Database Loading failed ");
			}
		}
		
	}
	
}