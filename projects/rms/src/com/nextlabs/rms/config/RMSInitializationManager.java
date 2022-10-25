package com.nextlabs.rms.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.license.LicenseManager;
import com.nextlabs.rms.persistence.PersistenceManager;
import com.nextlabs.rms.services.manager.ssl.CommArtifactsCache;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;
import com.nextlabs.rms.upgrade.DataMigrationManager8_3;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.ZipUtil;

public class RMSInitializationManager {

	private static final String RMS_PERCEPTIVE_VERSION_TXT = "rms-perceptive-version.txt";
	private static Logger logger = Logger.getLogger(RMSInitializationManager.class);
	public void initRMS(String webDir) {
		GlobalConfigManager.getInstance();
		DataMigrationManager8_3.getInstance().migrateData();
		GlobalConfigManager.getInstance().setWebDir(webDir);
		GlobalConfigManager.getInstance().setWebViewerDir();
		syncCachedFileDataWithDB();
		PersistenceManager.getInstance().checkAndInsertNewSettings();
		insertInstallationDataIntoDB();
		initLicenseManager();
		try {
			deployCADConverter();
		} catch (Exception e){
			logger.error("Error occured while deploying CAD Converter", e);
		}
		try {
			deployVDSFiles();
		} catch (Exception e){
			logger.error("Error occured while deploying VDS Converter", e);
		}
		try {
			deployDocConverter();
		} catch (Exception e){
			logger.error("Error occured while deploying Doc Converter", e);
		}
	}


	private void syncCachedFileDataWithDB() {
		// ToDo: We will at some point need to persist files from multiple tenants (for now we assume only one)
		Map<String, File> filesToCache = new HashMap<>();
		Map<String, File> filesToRead = new HashMap<>();
		for (String fileName : GlobalConfigManager.RMS_FILES_TO_CACHE) {
			File file = new File(SSLSocketFactoryGenerator.CERT_PATH + fileName);
			if (file.exists()) {
				filesToCache.put(fileName, file);
			}
			else {
				filesToRead.put(fileName, file);
			}
		}

		try {
			CommArtifactsCache.getInstance().writeFileToDB(filesToCache, GlobalConfigManager.DEFAULT_TENANT_ID);
			CommArtifactsCache.getInstance().readFileFromDB(filesToRead, GlobalConfigManager.DEFAULT_TENANT_ID);
		} catch (IOException e) {
			logger.error("Error occurred while attempting to load cached files into the database.");
		}
	}

	private void insertInstallationDataIntoDB() {
		File installSettingsFile = new File(GlobalConfigManager.getInstance().getDataDir(), GlobalConfigManager.INSTALL_DB_SETTINGS_FILENAME);
		if(!installSettingsFile.exists()){
			//No DB Updates required from installation. Get out of here..
			return;
		}
		Properties prop = new Properties();		
		BufferedInputStream inStream = null;
		boolean updateSuccessful = false;
		try {
			inStream = new BufferedInputStream(new FileInputStream(installSettingsFile));
			prop.load(inStream);
			logger.info("About to insert values from file:"+GlobalConfigManager.INSTALL_DB_SETTINGS_FILENAME + " into the database.");
			Map<String, String> propertyMap = new HashMap<String, String>((Map)prop);
			SettingManager.saveSettingValues(GlobalConfigManager.DEFAULT_TENANT_ID, propertyMap);
			logger.info("Updated the database successfully");
			updateSuccessful = true;
		} catch (Exception e) {
			logger.error("Error occurred while reading icenet url file");
		}finally{
			if(inStream!=null){
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error("Error occurred while closing stream", e);
				}				
			}
		}
		if(updateSuccessful){
			installSettingsFile.delete();
			logger.info("Deleted the file" + GlobalConfigManager.INSTALL_DB_SETTINGS_FILENAME);
		}
	}

	private void initLicenseManager() {
		LicenseManager.getInstance();
	}

	private void deployCADConverter() throws IOException {
		String targetFolder = new File(GlobalConfigManager.getInstance().getCadConverterDir()).getAbsolutePath();
		String currentViewerVer = "";
		File viewerInfoFile = new File(GlobalConfigManager.getInstance().getCadBinDir(),"rms-cad-version.txt");

		if (viewerInfoFile.exists())
			currentViewerVer = FileUtils.readFileToString(viewerInfoFile);

		boolean converterDeployed = extractZipFile(new File(targetFolder), GlobalConfigManager.CAD_CONVERTER_ZIP_REGEX, currentViewerVer);
		if(converterDeployed) {
			logger.info("Deployed CAD Converter.");
			if(GlobalConfigManager.getInstance().isUnix){
				String unixCadConverterDir = GlobalConfigManager.getInstance().getCadBinDir() + "/linux64/";
				setPOSIXFilePermissions(unixCadConverterDir + "converter");
			}
		}

		File cadViewer = new File(targetFolder, GlobalConfigManager.CADVIEWER_WEBDIR_NAME);
		File webCadViewer = new File(GlobalConfigManager.getInstance().getWebViewerDir(),GlobalConfigManager.CADVIEWER_WEBDIR_NAME);
		if(webCadViewer.exists()) {
			if(converterDeployed) {
				FileUtils.deleteDirectory(webCadViewer);
				FileUtils.copyDirectory(cadViewer, webCadViewer);
				logger.info("Deployed CAD Viewer.");
			}
		} else {	//cadviewer absent in webapps
			if(cadViewer.exists()){
				FileUtils.copyDirectory(cadViewer, webCadViewer);
				logger.info("Deployed CAD Viewer.");
			}
		}
	}

	private void deployVDSFiles() throws IOException {
		String targetFolder = new File(GlobalConfigManager.getInstance().getWebViewerDir()).getAbsolutePath();
		String currentViewerVer = "";
		File viewerInfoFile = new File(targetFolder+File.separator+GlobalConfigManager.SAPVIEWER_WEBDIR_NAME,"rms-sap-version.txt");

		if (viewerInfoFile.exists())
			currentViewerVer = FileUtils.readFileToString(viewerInfoFile);

		boolean converterDeployed = extractZipFile(new File(targetFolder), GlobalConfigManager.SAP_CONVERTER_ZIP_REGEX, currentViewerVer);
		if(converterDeployed) {
			logger.info("Deployed SAP Viewer.");
		}
	}

	private void deployDocConverter(){
		String targetFolder = GlobalConfigManager.getInstance().getDocConverterDir();
		String currentViewerVer = "";
		File viewerInfoFile = new File(targetFolder+File.separator,RMS_PERCEPTIVE_VERSION_TXT);

		if (viewerInfoFile.exists()){
			try {
				currentViewerVer = FileUtils.readFileToString(viewerInfoFile);
			} catch (Exception e) {
				logger.error("Error occurred while reading version info of Doc Viewer",e);
			}
		}
		boolean viewerDeployed = false;
		try {
			String tempPath = GlobalConfigManager.getInstance().getViewerPlugInDir()+File.separator+GlobalConfigManager.PERCEPTIVE_DIR;
			viewerDeployed = extractZipFile(new File(tempPath), GlobalConfigManager.PERCEPTIVE_ZIP_REGEX, currentViewerVer);
			if(viewerDeployed){
				FileUtils.copyDirectory(new File(tempPath+File.separator+(GlobalConfigManager.getInstance().isUnix?("linux"+File.separator+"intel-64"):("windows"+File.separator+"intel-64"))), new File(targetFolder));
				FileUtils.copyFile(new File(tempPath+File.separator+RMS_PERCEPTIVE_VERSION_TXT), new File(targetFolder,RMS_PERCEPTIVE_VERSION_TXT));
				FileUtils.copyFile(new File(tempPath+File.separator+GlobalConfigManager.ISYS11DF_JAR), new File(targetFolder,GlobalConfigManager.ISYS11DF_JAR));
				FileUtils.copyFile(new File(tempPath+File.separator+GlobalConfigManager.MEMORY_STREAM_JAR), new File(targetFolder,GlobalConfigManager.MEMORY_STREAM_JAR));
				FileUtils.deleteDirectory(new File(tempPath));
				if(GlobalConfigManager.getInstance().isUnix){
					setPOSIXFilePermissions(targetFolder);
				}
			}
		} catch (IOException e) {
			logger.error("Error occured while deploying doc viewer ",e);
		}
		if(viewerDeployed) {
			logger.info("Deployed Doc Viewer.");
		}
	}
	
	
	private boolean extractZipFile(File targetFolder, String converterZipRegex, String currentZipVer){
		File pluginDir = new File(GlobalConfigManager.getInstance().getViewerPlugInDir());
		File [] zipFiles = pluginDir.listFiles(new RegexFileFilter(converterZipRegex));
		if (zipFiles == null || zipFiles.length == 0) {
			logger.debug("No " + converterZipRegex + " zip file is found.");
			return false;
		}
		File latestZipFile = zipFiles[0];
		String converterPath = latestZipFile.getAbsolutePath();
		String converterName = latestZipFile.getName();
		if(converterPath==null || converterPath.length()==0){
			return false;
		}
		if(converterName.compareTo(currentZipVer) != 0) {
			ZipUtil zipUtil = ZipUtil.getInstance();
			if(!StringUtils.hasText(currentZipVer)) {
				logger.debug("Overwriting " + currentZipVer + " with " + converterName);
			}
			logger.debug("Unzipping " + converterPath + " to " + targetFolder);
			zipUtil.unZip(converterPath, targetFolder.getAbsolutePath());
			return true;
		}
		return false;
	}
	


	private void setPOSIXFilePermissions(String filePath){
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);

		try {
			java.nio.file.Files.setPosixFilePermissions(Paths.get(filePath), perms);
		} catch (IOException e) {
			logger.error("Failed to set file permission.", e);
		}
	}

	class RegexFileFilter implements java.io.FileFilter {
		final java.util.regex.Pattern pattern;
		public RegexFileFilter(String regex) {
			pattern = java.util.regex.Pattern.compile(regex);
		}
		public boolean accept(java.io.File f) {
			return pattern.matcher(f.getName()).find();
		} 
	}
}
