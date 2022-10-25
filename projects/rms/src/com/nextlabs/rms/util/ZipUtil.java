package com.nextlabs.rms.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.log4j.Logger;

public class ZipUtil {
	
	private static Logger logger = Logger.getLogger(ZipUtil.class);
    
	private static ZipUtil instance=new ZipUtil();
    
	private ZipUtil(){
    	//Singleton
    }
    
    public static ZipUtil getInstance(){
    	return instance;
    }
  
    
	public void unZip(String zippedFile, String outputFolder){
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile(zippedFile);
			// Extracts all files to the path specified
			zipFile.extractAll(outputFolder);
		} catch (ZipException e) {
			logger.error("Error occurred while unzipping file:"+zippedFile, e);
		}
	}

    
    public static void zipFolder (String sourceFolder, String destinationFolder) throws FileNotFoundException, ZipException {
		File base = new File(sourceFolder);
		ArrayList<File> filesToZip = new ArrayList<File>();
		// get all files/folders under sourceFolder
		if (base.isDirectory()) {
			File[] files = base.listFiles();
			if (files != null && files.length > 0) {
				filesToZip.addAll(Arrays.asList(files));
			}
		}
		// init zip file
		ZipFile zipFile = new ZipFile(destinationFolder);

		// init zip parameters
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		parameters.setEncryptFiles(false);

		// add files/folders into zipFile
		for (File f : filesToZip) {
			if (f.isDirectory()) {
				zipFile.addFolder(f.getAbsolutePath(), parameters);
			} else {
				zipFile.addFile(f, parameters);
			}
		}
	}
}
