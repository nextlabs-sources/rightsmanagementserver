package com.nextlabs.rms.license;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;

public class LicenseManager {
	
	private static LicenseManager instance = new LicenseManager();

	private static final String LICENSE_JARCHECKER_GETPROPERTIES_METHOD_NAME = "getProperties";
  
	private static final String LICENSE_JARCHECKER_CHECK_METHOD_NAME = "check";
  
	private static final String LICENSE_JARCHECKER_SETCLASSLOADER_METHOD_NAME = "setClassLoader";
  
	private static final String LICENSE_JARCHECKER_SETJARFILENAME_METHOD_NAME = "setJarFileName";
  
	private static final String LICENSE_JARCHECKER_CLASS_NAME = "com.wald.license.checker.JarChecker";
  
	private static final String LICENSE_CLASSLOADER_CLASS_NAME = "com.wald.license.checker.LicenseClassLoader";
	
	private static final String LICENSE_JAR_NAME = "license.jar";
	
	private static final String LICENSE_FILE_NAME = "license.dat";
	
	private static final String LICENSE_STRING_CAD_VIEWER = "rms_cad_viewer";
	
	private static final String LICENSE_STRING_SAP_3D_VIEWER = "rms_sap_3d_viewer";
	
	private static final String LICENSE_STRING_SECURE_VIEWER = "rms_secure_viewer";

	private Properties licenseProperties = new Properties();
	
	private Logger logger = Logger.getLogger(LicenseManager.class);
	
	private LicenseManager(){
		try {			
			init();
		} catch (Exception e) {
			logger.debug("Error occurred while initializing License Manager.");
		}
	}
	
	private void init() throws Exception {
		String licenseJarFileLocation = GlobalConfigManager.getInstance().getLicDir() + File.separator + LICENSE_JAR_NAME;						
		File licJarFile = new File(licenseJarFileLocation);
		if(!licJarFile.exists()){
			logger.error("License jar file not found. Not Initializing License Manager.");
			return;
		}
		File licFile = new File(GlobalConfigManager.getInstance().getLicDir(), LICENSE_FILE_NAME);
		if(!licFile.exists()){
			logger.info("License file not found. Not Initializing License Manager.");
			return;
		}
    URL jarLocation = licJarFile.toURI().toURL();
    URL dataFileParentFolderLocation = new File(GlobalConfigManager.getInstance().getLicDir()).toURI().toURL();
    URL[] classLoaderURLs = { jarLocation, dataFileParentFolderLocation };
    ClassLoader licenseLocationClassLoader = new URLClassLoader(classLoaderURLs, this.getClass().getClassLoader());

    Class<?> licenseClassLoaderClass = licenseLocationClassLoader.loadClass(LICENSE_CLASSLOADER_CLASS_NAME);
    Constructor<?> parentClassLoaderConstructor = licenseClassLoaderClass.getConstructor(ClassLoader.class);
    Object licenseClassLoader = parentClassLoaderConstructor.newInstance(licenseLocationClassLoader);

    Class<?> jarCheckerClass = licenseLocationClassLoader.loadClass(LICENSE_JARCHECKER_CLASS_NAME);
    Object jarCheckerInstance = jarCheckerClass.newInstance();
    Method setJarFileMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_SETJARFILENAME_METHOD_NAME, java.lang.String.class);
    setJarFileMethod.invoke(jarCheckerInstance, licenseJarFileLocation);

    Class<?> setClassLoaderMethodParams = licenseClassLoader.getClass();
    Method setClassLoaderMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_SETCLASSLOADER_METHOD_NAME, setClassLoaderMethodParams);
    setClassLoaderMethod.invoke(jarCheckerInstance, licenseClassLoader);

    Method checkMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_CHECK_METHOD_NAME);
    checkMethod.invoke(jarCheckerInstance);

    Method getPropertiesMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_GETPROPERTIES_METHOD_NAME);
    this.licenseProperties = (Properties) getPropertiesMethod.invoke(jarCheckerInstance);
    
    StringBuilder sb = new StringBuilder("Licensed Viewers: ");
    if(isFeatureLicensed(LicensedFeature.FEATURE_VIEW_CAD_FILE))
    	sb.append("CAD Viewer, ");
    if(isFeatureLicensed(LicensedFeature.FEATURE_VIEW_SAP_3D_FILE))
    	sb.append("SAP 3D Viewer, ");
    if(isFeatureLicensed(LicensedFeature.FEATURE_VIEW_GENERIC_FILE))
    	sb.append("Secure Viewer");
    if(sb.toString().equals("Licensed Viewers: ")){
    	logger.info("No valid License found for viewer");
    }else{
      logger.info(sb.toString());    	
    }
	}
	
	public static LicenseManager getInstance(){
		return instance;
	}
	
	public boolean isFeatureLicensed(LicensedFeature featureName){
		switch (featureName) {
		case FEATURE_VIEW_CAD_FILE:
			if(licenseProperties.get(LICENSE_STRING_CAD_VIEWER)!=null && licenseProperties.get(LICENSE_STRING_CAD_VIEWER).equals("1")){
				return true;
			}
			break;
		case FEATURE_VIEW_SAP_3D_FILE:
			if(licenseProperties.get(LICENSE_STRING_SAP_3D_VIEWER)!=null && licenseProperties.get(LICENSE_STRING_SAP_3D_VIEWER).equals("1")){
				return true;
			}
			break;
		case FEATURE_VIEW_GENERIC_FILE:
			if(licenseProperties.get(LICENSE_STRING_SECURE_VIEWER)!=null && licenseProperties.get(LICENSE_STRING_SECURE_VIEWER).equals("1")){
				return true;
			}
			break;
		}
		return false;
	}

}