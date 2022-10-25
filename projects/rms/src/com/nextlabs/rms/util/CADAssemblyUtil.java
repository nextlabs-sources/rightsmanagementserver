package com.nextlabs.rms.util;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CADAssemblyUtil {

	private static Logger logger = Logger.getLogger(CADAssemblyUtil.class);
	
	public enum ValidationResult {INVALID_CONTENTS, MULTIPLE_ASSEMBLY_FILES, ASSEMBLY_FILE_NOT_FOUND, ASSEMBLY_FILE_FOUND};

	public static File getCADFileFromZIP(String userTempDir, String fileName) throws RMSException {
		String destFilePath = userTempDir + File.separator + fileName;
		File file = null;
		String zipFileNameWithoutExtension = FilenameUtils.getBaseName(fileName);
		String unzippedFolder = userTempDir + File.separator + zipFileNameWithoutExtension;
		String errorMessage = "";
		boolean assemblyFileFound = false;

		switch (unzipAndValidateZipFile(destFilePath, unzippedFolder)) {
			case INVALID_CONTENTS:
				errorMessage = RMSMessageHandler.getClientString("nonNXLInZip");
				break;
			case MULTIPLE_ASSEMBLY_FILES:
				errorMessage = RMSMessageHandler.getClientString("multipleAssemblyFiles");
				break;
			case ASSEMBLY_FILE_FOUND:
				file = new File(unzippedFolder);
				assemblyFileFound = true;
				break;
			default:
				errorMessage = RMSMessageHandler.getClientString("assemblyFileNotFound");
		}
		if (!assemblyFileFound) {
			throw new RMSException(errorMessage);
		}
		return file;
	}
	
	public static String getAssemblyFileNameFromFolder (File unzippedFolder){
		File [] folderContents = unzippedFolder.listFiles();
		List<String> assemblyFormats = GlobalConfigManager.getInstance().getSupportedHOOPSAssemblyFormatList();
		if (folderContents != null) {
			for(int i=0;i<folderContents.length;i++){	
				String zipFileEntryExtension = '.' + FilenameUtils.getExtension(folderContents[i].getName());
				String zipEntryNameWithoutNXL;
				String zipEntryNameWithoutExtension;
				String zipEntryExtension;
				if(!zipFileEntryExtension.equalsIgnoreCase(EvaluationHandler.NXL_FILE_EXTN)) {
					zipEntryNameWithoutNXL = folderContents[i].getName();
					zipEntryNameWithoutExtension = FilenameUtils.getBaseName(zipEntryNameWithoutNXL);
					zipEntryExtension = zipFileEntryExtension.toLowerCase();
				}
				else {
					zipEntryNameWithoutNXL = FilenameUtils.getBaseName(folderContents[i].getName());
					zipEntryNameWithoutExtension = FilenameUtils.getBaseName(zipEntryNameWithoutNXL);
					zipEntryExtension = '.' + FilenameUtils.getExtension(zipEntryNameWithoutNXL).toLowerCase();
				}
				if((unzippedFolder.getName().equalsIgnoreCase(zipEntryNameWithoutExtension)) && (assemblyFormats.contains(zipEntryExtension))){
					return zipEntryNameWithoutNXL;
				}
			}
		}
		return null;
	}
	
	public static ValidationResult unzipAndValidateZipFile (String zipFile, String unzippedFolderPath){
		
		File unzippedFolder = new File(unzippedFolderPath);
		try {
			FileUtils.deleteDirectory(unzippedFolder);	
		} catch (IOException e) {
			logger.error(e);
		}
		
		ZipUtil zipUtil = ZipUtil.getInstance();
		logger.debug("Unzipping " + zipFile + " to " + unzippedFolderPath);
		zipUtil.unZip(zipFile, unzippedFolderPath);
		
		if(!new File(zipFile).delete())
			logger.error("Failed to delete ZIP file.");
		
		boolean assemblyFileFound = false;
		List<String> assemblyFormats = GlobalConfigManager.getInstance().getSupportedHOOPSAssemblyFormatList();
		List<String> cadFormats = GlobalConfigManager.getInstance().getSupportedCADFileFormatList();
		File [] zipEntries = unzippedFolder.listFiles();
		String unzippedFolderName = unzippedFolder.getName();
		
		if (zipEntries != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Zip file contains the following files");
				for (File zipEntry : zipEntries) {
					logger.debug(zipEntry.getName());
				}
			}
			
			for (File zipEntry : zipEntries) {
                String zipEntryNameWithoutNXL = EvaluationHandler.getFileNameWithoutNXL(zipEntry);
                String zipEntryOriginalExtension = '.' + FilenameUtils.getExtension(zipEntryNameWithoutNXL).toLowerCase();
                if (!cadFormats.contains(zipEntryOriginalExtension)) {
                    logger.error("ZIP file contains non-CAD content.");
                    return ValidationResult.INVALID_CONTENTS;
                }
				else {
					String zipEntryNameWithoutExtension = FilenameUtils.getBaseName(zipEntryNameWithoutNXL);	
					if(zipEntryNameWithoutExtension.equals(unzippedFolderName)){
						if(assemblyFormats.contains(zipEntryOriginalExtension)){
							if(assemblyFileFound){
								assemblyFileFound = false;
								logger.error("Multiple assembly files with same file name detected.");
								return ValidationResult.MULTIPLE_ASSEMBLY_FILES;
							}
							else {
								assemblyFileFound=true;
							}
						}
					}
				}				
			}	
		}
		if(assemblyFileFound)
			return ValidationResult.ASSEMBLY_FILE_FOUND;
					
		return ValidationResult.ASSEMBLY_FILE_NOT_FOUND;	
	}
}