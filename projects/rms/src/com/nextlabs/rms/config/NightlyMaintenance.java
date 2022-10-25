package com.nextlabs.rms.config;

import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.util.UserLocationDBUpdater;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

public class NightlyMaintenance extends TimerTask {

	private final static long inactiveFileDuration = 12 * 60 * 60 * 1000;
	public final static int INACTIVE_RECORDS_CLEAR_THRESHOLD_IN_DAYS = 7;

	private Logger logger = Logger.getLogger(NightlyMaintenance.class);

	@Override
	public void run() {
		logger.info("Starting Nightly Maintenance");
		try {
			cleanupTempFolders();
		} catch (Exception e) {
			logger.error("Error occurred while cleaning up temp folders", e);
		}
        try {
            cleanupDB();
        } catch (Exception e) {
            logger.error("Error occurred while cleaning up DB", e);
        }
		try {
			backupDBAndConfig();
			updateLocationDatabase();
		} catch (Exception e) {
			logger.error("Error occurred while backing up DB and Config", e);
		}
		logger.info("Nightly Maintenance completed");
	}

	public void updateLocationDatabase() {
			UserLocationDBUpdater userLocationDBUpdater = new UserLocationDBUpdater();
			if (userLocationDBUpdater.downloadUpdate()) {
				logger.debug(GlobalConfigManager.LOCATION_DATABASE
						+ " updated successfully");
			}
	}
	
	private void backupDBAndConfig() {
		File bkpDir = new File(GlobalConfigManager.getInstance().getBkpDir());
		if (!bkpDir.exists()) {
			bkpDir.mkdirs();
		}
		File configFile = new File(GlobalConfigManager.getInstance().getDataDir(),
				GlobalConfigManager.CONFIG_FILENAME);
		
		try {
			FileUtils.copyFileToDirectory(configFile, bkpDir, true);
			logger.debug(GlobalConfigManager.CONFIG_FILENAME
					+ " backed up successfully");

			File locationDB = new File(GlobalConfigManager.getInstance()
					.getDataDir(), GlobalConfigManager.LOCATION_DATABASE);
			FileUtils.copyFileToDirectory(locationDB, bkpDir, true);
			logger.debug(GlobalConfigManager.LOCATION_DATABASE
					+ " backed up successfully");
			backupCert(bkpDir);
		} catch (Exception e) {
			logger.error("Error occurred while backing up config file", e);
		}
	}
	
	private void backupCert(File bkpDir) throws IOException{
		
		File certDir = new File(GlobalConfigManager.getInstance().getDataDir(),
				"cert");
		File bkpCertDir=new File(bkpDir,"cert");
		if (!bkpCertDir.exists()) {
			bkpCertDir.mkdirs();
		}
		FileUtils.copyDirectory(certDir, bkpDir,true);
		
	}

	private void cleanupTempFolders() {
		String tempDirPath = GlobalConfigManager.getInstance().getTempDir();
		cleanUpFolder(tempDirPath);
		String tempWebDirPath = GlobalConfigManager.getInstance().getWebDir()
				+ File.separator + GlobalConfigManager.TEMPDIR_NAME;
		cleanUpFolder(tempWebDirPath);
	}

	private void cleanUpFolder(String tempDirPath) {
		File tempDir = new File(tempDirPath);
		if(tempDir.exists()){
			File[] tempDirFiles = tempDir.listFiles();
			for (File file : tempDirFiles) {
				if (file.isDirectory()) {
					if (!hasRecentFiles(file)
							&& file.lastModified() > inactiveFileDuration) {
						try {
							FileUtils.deleteDirectory(file);
							logger.debug("Deleted directory:"
									+ file.getAbsolutePath());
						} catch (Exception e) {
							logger.error("Error occurred while deleting directory:"
									+ file.getAbsolutePath());
						}
					}
				}
			}
		}
	}

	private boolean hasRecentFiles(File file) {
		File filesInDir[] = file.listFiles();
		if (filesInDir.length == 0) {
			return false;
		}
		for (File fileInDir : filesInDir) {
			if ((System.currentTimeMillis() - fileInDir.lastModified()) < inactiveFileDuration) {
				return true;
			}
		}
		return false;
	}

    private void cleanupDB() {
        RepositoryManager.getInstance().removeInactiveRecords();
        return;
    }
}
