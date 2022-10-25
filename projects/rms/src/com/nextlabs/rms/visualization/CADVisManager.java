package com.nextlabs.rms.visualization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.EvaluationHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.conversion.CADDependencyManager;
import com.nextlabs.rms.conversion.ConverterFactory;
import com.nextlabs.rms.conversion.IFileConverter;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.license.LicenseManager;
import com.nextlabs.rms.license.LicensedFeature;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;

public class CADVisManager implements IVisManager {
	
	private static Logger logger = Logger.getLogger(CADVisManager.class);
	
	@Override 
	public String getVisURL( RMSUserPrincipal user,
			String sessionId, EvalResponse evalRes,
			byte[] fileContent, String fileNameWithoutNXL,String cacheId) throws RMSException, MissingDependenciesException {
		
		String redirectURL = null;
		String dataDir = GlobalConfigManager.getInstance().getTempDir()+ File.separator+sessionId;
		File inputFile = new File(dataDir,fileNameWithoutNXL);
		try {
			EvaluationHandler.writeContentsToFile(dataDir, fileNameWithoutNXL, fileContent);
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while processing the file: "+fileNameWithoutNXL+ e.getMessage());
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		} catch (IOException e) {
			logger.error("Error occurred while processing the file: "+fileNameWithoutNXL+ e.getMessage());
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		}
		redirectURL = getVisURL(user, sessionId, evalRes, inputFile, inputFile.getName(), cacheId);
		return redirectURL;
	}

	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, 
			File folderpath,String displayName, String cacheId) throws RMSException, MissingDependenciesException {
		
		if(!LicenseManager.getInstance().isFeatureLicensed(LicensedFeature.FEATURE_VIEW_CAD_FILE)){
			throw new RMSException(RMSMessageHandler.getClientString("licenseError"));
		}
		
		boolean isDirectory = false;
		
		List<String> assemblyFormats = GlobalConfigManager.getInstance().getSupportedHOOPSAssemblyFormatList();
		if(folderpath.isDirectory()) {
			folderpath = getAssemblyFile(folderpath, displayName);
			isDirectory = true;
		}
		else if (assemblyFormats.contains('.' + FilenameUtils.getExtension(displayName).toLowerCase())) {
			CADDependencyManager cadDepMgr = new CADDependencyManager();
			List<String> missingParts = cadDepMgr.getDependencies(folderpath.getAbsolutePath());
			if (!missingParts.isEmpty())
				throw new MissingDependenciesException(missingParts);
		}
		
		String redirectURL=null;
		String filePath=null;
		String fileParameters=null;
		String docIdWithExt=null;
		String fileNameWithoutNXL = folderpath.getName();
		boolean isFileNameAsDocId=GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID);
		try {
			//EvaluationHandler.writeContentsToFile(inputFile.getParent(),fileNameWithoutNXL, fileContent);
			docIdWithExt = cacheId+VisManagerFactory.SCS_FILE_EXTN;
			if(isFileNameAsDocId){
				//for testing purpose
				 filePath=GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID + "/" + cacheId + replaceFileType(fileNameWithoutNXL);
			}
			else{
				filePath=GlobalConfigManager.TEMPDIR_NAME+"/"+sessionId+"/"+cacheId+"/"+docIdWithExt;
			}
			String destinationPath=GlobalConfigManager.getInstance().getWebDir()+filePath;
			File destFile = new File(destinationPath); 
			//to create temp/session folder if it is not present
			destFile.getParentFile().mkdirs();
			IFileConverter cadconverter = ConverterFactory.getInstance().getConverter(ConverterFactory.CONVERTER_TYPE_CAD);
			boolean conversionResult=cadconverter.convertFile(folderpath.getAbsolutePath(),destinationPath);
			FileCacheId fileCacheId = new FileCacheId(sessionId,user.getUserName(),cacheId);
			CachedFile cachedFile = RMSCacheManager.getInstance().getCachedFile(fileCacheId);
			if(conversionResult){
				File convertedFile=new File(destinationPath);
				if(!convertedFile.exists()){
					if(getFileExtension(fileNameWithoutNXL).equalsIgnoreCase(GlobalConfigManager.PDF_FILE_EXTN)){
						logger.warn("3d conversion failed.About to start default conversion for file :"+fileNameWithoutNXL);
						GenericVisManager genericVisManager=new GenericVisManager();
						byte[] fileContent = Files.readAllBytes(folderpath.toPath());
						redirectURL=genericVisManager.getVisURL(user, sessionId, evalRes, fileContent, fileNameWithoutNXL, cacheId);
						return redirectURL;
					}
					logger.error("File conversion failed and not present in "+destinationPath);
					throw new RMSException("There was an error while processing the file.");
				}
				
				if(isFileNameAsDocId){
					//for testing purpose
					fileParameters=URLEncoder.encode(filePath+ "&cacheId=" +cacheId, "UTF-8");
				}
				else{
					fileParameters=URLEncoder.encode(filePath+ "&cacheId=" +cacheId ,"UTF-8");
				}
				
				logger.debug("Encoded file parameters :"+ filePath);
				cachedFile.setContentType(CachedFile.ContentType._3D);	
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+ "/CADViewer.jsp?file=" +fileParameters ;				
				logger.debug("ReDirectUrl: "+redirectURL);
			}
			else{
				logger.error("Error occurred while processing the file:"+fileNameWithoutNXL);
				throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
			}
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while processing the file: "+fileNameWithoutNXL+ e.getMessage());
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		} catch (IOException e) {
			logger.error("Error occurred while processing the file: "+fileNameWithoutNXL+ e.getMessage());
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		}

		if (isDirectory) {
			try {
				FileUtils.deleteDirectory(folderpath.getParentFile());
			} catch (IOException e) {
				logger.error("Failed to delete temp assembly directory.", e);
			}
		}

		return redirectURL;
	}
	
	private String replaceFileType(String fileNameWithoutNXL) {
		String fileExtension = getFileExtension(fileNameWithoutNXL);
		return fileNameWithoutNXL.replaceFirst(fileExtension,VisManagerFactory.SCS_FILE_EXTN);
	}
	
	
	private String getFileExtension(String fileNameWithoutNXL) {
		int index = fileNameWithoutNXL.toLowerCase().lastIndexOf(".");
		String fileExtension=fileNameWithoutNXL.substring(index, fileNameWithoutNXL.length());
		return fileExtension;
	}
	
	private File getAssemblyFile (File folder, String displayName) throws RMSException, MissingDependenciesException{	
		File assemblyFile = null;
		File[] folderEntries = folder.listFiles();

		for (File file : folderEntries) {
			String fileName = file.getName();
			if(fileName.equals(displayName)) {
				//shortening the assembly filename if it is greater than 50 characters as CADConverter cannot handle file path greater than 270 characters in windows
				if(fileName.length()>50 && !GlobalConfigManager.getInstance().isUnix){
					String shortName=fileName.substring(0,5)+"."+FilenameUtils.getExtension(fileName).toLowerCase();
					file.renameTo(new File(file.getParent()+File.separator+shortName));
					assemblyFile = new File(file.getParent()+File.separator+shortName);
					logger.debug("Detected and shortened the assembly file name from : " + file.getName()+" to :"+assemblyFile.getName());
					break;
				}
				logger.debug("Detected assembly file: " + file.getName());
				assemblyFile = file;
				break;
			}
		}
		
		if(assemblyFile == null){
			logger.error("Cannot find the assembly file to convert.");
			throw new RMSException("Cannot find the assembly file to convert.");
		}

		CADDependencyManager cadDepMgr = new CADDependencyManager();
		List<String> missingParts = cadDepMgr.getDependencies(assemblyFile.getAbsolutePath());
		String errorMessage = "";
		if(!missingParts.isEmpty()){
			errorMessage = "The following part files are missing from the assembly.";
			for(String part: missingParts)
				errorMessage = errorMessage.concat("\nMISSING: " + part);
			logger.error(errorMessage);
			throw new MissingDependenciesException(missingParts);
		}
		return assemblyFile;
	}
}
