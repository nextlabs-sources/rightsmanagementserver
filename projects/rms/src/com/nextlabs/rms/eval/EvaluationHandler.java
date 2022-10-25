package com.nextlabs.rms.eval;

import com.nextlabs.destiny.sdk.CESdk;
import com.nextlabs.destiny.sdk.CESdkException;
import com.nextlabs.destiny.sdk.ICESdk;
import com.nextlabs.nxl.legacy.FileMetaDataForEvaluation;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.conversion.FileConversionHandler;
import com.nextlabs.rms.conversion.WaterMark;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.nxl.decrypt.CryptManager;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryFactory;
import com.nextlabs.rms.util.CADAssemblyUtil;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UserLocationProvider;
import com.nextlabs.rms.util.WatermarkUtil;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class EvaluationHandler {

	public static final String LOCALHOST_IP = "127.0.0.1";

	private static Logger logger = Logger.getLogger(EvaluationHandler.class);

	public static final String NXL_FILE_EXTN = ".nxl";

	private EvalResponse processRequest(EvalRequest req){
		IEvalAdapter evalAdapter = EvaluationAdapterFactory.getInstance().getAdapter(false);
		EvalResponse res = evalAdapter.evaluate(req);
		return res;
	}

	private static Resource getResource(String fileName, String resDimension){
		Resource res = new Resource(resDimension, fileName, EvalRequest.ATTRIBVAL_RES_ID_FSO);
		return res;
	}

	public String validateFileRequest(RMSUserPrincipal user, File fileFromRepo, String sessionId,String ipAddress, RepositoryFileParams params) throws MissingDependenciesException{
		logger.info("IP Address of the request is "+ipAddress);
		String redirectURL = "";
		String cacheId= null;
		if (fileFromRepo == null || (fileFromRepo.length() == 0 && !fileFromRepo.isDirectory())) {
			logger.error("File from Repository null or 0KB in size. Returning..");
			cacheId=setError(user, fileFromRepo.getName(), sessionId,RMSMessageHandler.getClientString("fileDownloadErr"));
			redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errId="+cacheId;
			return redirectURL;
		}
		try {
			EvalAndDecryptResponse evalDecryptRes = null;
			evalDecryptRes = evaluateAndDecrypt(fileFromRepo, user, sessionId, ipAddress, params);
			FileConversionHandler conversionHandler = new FileConversionHandler();
			redirectURL = conversionHandler.convertAndGetURL(user, evalDecryptRes, fileFromRepo);
		} catch(PolicyEvalException evalException){
			cacheId=setError(user, fileFromRepo.getName(), sessionId,evalException.getMessage());
			redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errId="+cacheId;
			logger.error(evalException);
		} catch (RMSException rmsException){
			cacheId=setError(user, fileFromRepo.getName(), sessionId,rmsException.getMessage());
			redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errId="+cacheId;
			logger.error(rmsException);
		} catch (MissingDependenciesException missingException){
			throw missingException;
		} catch (Throwable e){
			cacheId=setError(user, fileFromRepo.getName(), sessionId,RMSMessageHandler.getClientString("fileProcessErr"));
			redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errId="+cacheId;
			logger.error(e);
		}
		if(!GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.RETAIN_NXL_FILE)){
			fileFromRepo.delete();
		}
		return redirectURL;
	}

	public static String setError(RMSUserPrincipal user, String fileName,
			String sessionId,String errMsg) {
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_FILECONTENT);
		CachedFile cachedFile = new CachedFile(fileName, null, false, null,errMsg,false);
		String cacheId = System.currentTimeMillis() + UUID.randomUUID().toString();
		FileCacheId fileCacheId=new FileCacheId(sessionId,user.getUserName(),cacheId);
		cache.put(new Element(fileCacheId, cachedFile));
		return cacheId;
	}

	private EvalResponse evaluatePolicy(String fileName, RMSUserPrincipal user, Map<String, List<String>> attribs,String ipAddress,String countryCode) {
		int ip = 0;
		try {
			ip = CESdk.ipAddressToInteger(ipAddress);
			logger.debug("Inet address of the request:" + ip);
		} catch (Exception e) {
			logger.error("Error occured while converting Client IP address into integer. Using localhost instead.", e);
			try {
				ip = CESdk.ipAddressToInteger(LOCALHOST_IP);
			} catch (CESdkException ex) {
			}
		}
		User rmsUser = new User(user.getUid(), user.getUserName() != null ? user.getUserName() : user.getUid());
		populateUserAttribute(rmsUser, user, countryCode);

		EvalRequest evalRequest = new EvalRequest();
		evalRequest.setHost(new Host(ip));
		Resource srcResource = EvaluationHandler.getResource(fileName, EvalRequest.ATTRIBVAL_RES_DIMENSION_FROM);
		evalRequest.setSrcResource(srcResource);
		populateAttributes(srcResource, attribs);
		evalRequest.setUser(rmsUser);
		evalRequest.setApplication(new Application(user.getLoginContext()));
		evalRequest.setRights(getEvaluatedRights());
		evalRequest.setNoiseLevel(ICESdk.CE_NOISE_LEVEL_USER_ACTION);

		NamedAttributes[] envAttrs = new NamedAttributes[1];
		envAttrs[0] = new NamedAttributes("environment");
		envAttrs[0].addAttribute("dont-care-acceptable", "yes");
		evalRequest.setEnvironmentAttributes(envAttrs);
		EvalResponse evalRes = processRequest(evalRequest);
		return evalRes;
	}

	private void populateUserAttribute(User rmsUser, RMSUserPrincipal user, String countryCode) {
		Map<String, List<String>> userAttributes = user.getUserAttributes();
		if (userAttributes != null && !userAttributes.isEmpty()) {
			Set<Entry<String, List<String>>> entrySet = userAttributes.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> values = entry.getValue();
				for (String value : values) {
					if(value==null){
						rmsUser.addAttribute(entry.getKey(), "");
					} else{
						rmsUser.addAttribute(entry.getKey(), value);
					}
				}
			}
		}
		if (ConfigManager.getInstance(user.getTenantId()).getBooleanProperty(ConfigManager.ENABLE_USER_LOCATION)) {
			String policyUserLocationIdentifier = ConfigManager.getInstance(user.getTenantId()).getStringProperty(ConfigManager.POLICY_USER_LOCATION_IDENTIFIER);
			if (policyUserLocationIdentifier == null || policyUserLocationIdentifier.trim().length() == 0) {
				policyUserLocationIdentifier = EvalRequest.POLICY_USER_LOCATION_IDENTIFIER_DEFAULT;
			}
			rmsUser.addAttribute(policyUserLocationIdentifier, countryCode);
		}
	}

	private void populateAttributes(INamedAttribute namedAttribute, Map<String, List<String>> attributes) {
		if (attributes != null && !attributes.isEmpty()) {
			Set<Entry<String, List<String>>> entrySet = attributes.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					for (String value : values) {
						namedAttribute.addAttribute(entry.getKey(), value);
					}
				}
			}
		}
	}

	
	public EvalResAttributes evaluate(File fileToEvaluate, RMSUserPrincipal user, String sessionId, String ipAddress) {
		String userLocation="";
		boolean hasPrintRights=true;
		boolean hasPMIRights=true;
		boolean hasOpenRights=true;
		CryptManager cryptManager=new CryptManager();
		EvalResponse evalRes=null;
		String fileNameWithoutNXL = getRequestedFileNameWithoutNXL(fileToEvaluate);
		String unAuthorizedFileName = "";
		
		//This is used only for Testing.
		if(!((GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.USER_IP_ADDRESS)==null)||(GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.USER_IP_ADDRESS).equals("")))){
			ipAddress=GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.USER_IP_ADDRESS);
			logger.debug("Ip address overriden to :" +ipAddress );
		}
		
		//Get CountryCode only if it is configured
		if(ConfigManager.getInstance(user.getTenantId()).getBooleanProperty(ConfigManager.ENABLE_USER_LOCATION)){
			if(UserLocationProvider.getInstance().getIp()==null){
				UserLocationProvider.getInstance().initUserLocationDb();
			}
			userLocation=UserLocationProvider.getInstance().getCountry(ipAddress);
		}
		
		if(fileToEvaluate.isDirectory()){
			File[] zipFileEntries=fileToEvaluate.listFiles();
			List<String> assemblyFormats = GlobalConfigManager.getInstance().getSupportedHOOPSAssemblyFormatList();
			List<Obligation> obligations = null;
			Map<String, List<String>> tagMapForAssemblyFile = null;
			if(zipFileEntries != null && zipFileEntries.length > 0) {
				for(int i=0;i<zipFileEntries.length;i++){	
					String zipFileEntryExtension = '.' + FilenameUtils.getExtension(zipFileEntries[i].getName());
					if(!zipFileEntryExtension.equalsIgnoreCase(NXL_FILE_EXTN))
						continue;
					String zipEntryNameWithoutNXL = FilenameUtils.getBaseName(zipFileEntries[i].getName());
					String zipEntryNameWithoutExtension = FilenameUtils.getBaseName(zipEntryNameWithoutNXL);
					String zipEntryExtension = '.' + FilenameUtils.getExtension(zipEntryNameWithoutNXL).toLowerCase();
					FileMetaDataForEvaluation metaData = cryptManager.readMetaData(zipFileEntries[i]);
					Map<String, List<String>> tagMap = metaData.getTags();
					EvalResponse resp=evaluatePolicy(zipFileEntries[i].getName(), user, tagMap, ipAddress, userLocation);
					fileNameWithoutNXL=zipEntryNameWithoutNXL;
					if((fileToEvaluate.getName().equalsIgnoreCase(zipEntryNameWithoutExtension)) && (assemblyFormats.contains(zipEntryExtension))){
						obligations = resp.getObligations();
						tagMapForAssemblyFile=tagMap;
					}
					if(!(resp.getRights().hasRights(EvalRequest.ATTRIBVAL_ACTION_OPEN))){
						hasOpenRights =false;
						unAuthorizedFileName = zipFileEntries[i].getName();
						logger.debug("Exiting as User "+ user.getUserName() + " with SID "+ user.getUid() + " not allowed to view the file:"+fileNameWithoutNXL);
						break;
					}
					if(!(resp.getRights().hasRights(EvalRequest.ATTRIBVAL_ACTION_PRINT))){
						hasPrintRights=false;
						logger.debug("Print denied for "+ user.getUserName() + " with SID "+ user.getUid() + " for the file:"+fileNameWithoutNXL);
					}
					if(!(resp.getRights().hasRights(EvalRequest.ATTRIBVAL_ACTION_PMI))){
						hasPMIRights=false;
						logger.debug("PMI denied for "+ user.getUserName() + " with SID "+ user.getUid() + " for the file:"+zipFileEntries[i].getName());
					}
				}
			} else {
				hasOpenRights = hasPMIRights = hasPrintRights = false;
			}
			evalRes = new EvalResponse();
			
			if (hasOpenRights) {
				evalRes.addRights(EvalRequest.ATTRIBVAL_ACTION_OPEN);
			}
			if (hasPrintRights) {
				evalRes.addRights(EvalRequest.ATTRIBVAL_ACTION_PRINT);
			}
			if (hasPMIRights) {
				evalRes.addRights(EvalRequest.ATTRIBVAL_ACTION_PMI);
			}
			if (obligations != null) {
				for (Obligation obligation : obligations) {
					evalRes.addObligation(obligation);
				}
			}
			if(tagMapForAssemblyFile!=null){
				evalRes.setTagMap(tagMapForAssemblyFile);
			}
		} else {// It is a single file.
			Map<String, List<String>> tagMap = getTags(fileToEvaluate, cryptManager);
			evalRes = evaluatePolicy(fileToEvaluate.getName(), user, tagMap, ipAddress, userLocation);
			evalRes.setTagMap(tagMap);
			unAuthorizedFileName = evalRes.getRights().hasRights(EvalRequest.ATTRIBVAL_ACTION_OPEN) ? ""
				: fileToEvaluate.getName(); 
		}
		
		HashMap<String,List<String>> attributes = new HashMap<>();
		List<String> fileList = new ArrayList<>();
		fileList.add(unAuthorizedFileName);
		attributes.put("unauthorizedFiles", fileList);
		EvalResAttributes evalResAttr = new EvalResAttributes(attributes, evalRes);
		
		return evalResAttr;
	}

	private Map<String, List<String>> getTags(File fileToEvaluate, CryptManager cryptManager) {
		if(fileToEvaluate.isDirectory()) {
			String assemblyFileName = CADAssemblyUtil.getAssemblyFileNameFromFolder(fileToEvaluate);
			fileToEvaluate = new File(fileToEvaluate, assemblyFileName);
		}
		FileMetaDataForEvaluation metaData = cryptManager.readMetaData(fileToEvaluate);
		Map<String, List<String>> tagMap = metaData.getTags();
		return tagMap;
	}
	
	public FileInfo getFileInfo(File fileToEvaluate){
		CryptManager cryptManager=new CryptManager();
		Map<String, List<String>> tagMap =getTags(fileToEvaluate, cryptManager);
		FileInfo fileInfo=new FileInfo();
		fileInfo.setTagMap(tagMap);
		return fileInfo;
	}
	
	public static class EvalAndDecryptResponse {
		public EvalResponse getEvalResponse() {
			return evalResponse;
		}

		public FileCacheId getFileCacheId() {
			return fileCacheId;
		}

		public String getFileDisplayName() {
			return fileDisplayName;
		}

		private EvalResponse evalResponse;
		private FileCacheId fileCacheId;
		private String fileDisplayName;
		
		public EvalAndDecryptResponse(EvalResponse response, FileCacheId cacheId, String fDisplayName){
			evalResponse = response;
			fileCacheId = cacheId;
			fileDisplayName = fDisplayName;
		}
	}
	
	public static class EvalResAttributes {
		private Map<String, ?> map;
		private EvalResponse evalResponse;
		
		public EvalResponse getEvalResponse(){
			return evalResponse;
		}
		
		public Map<String,?> getAttributesMap(){
			return map;
		}
		
		public EvalResAttributes (Map<String,?> map, EvalResponse evalResponse) {
			this.map = map;
			this.evalResponse = evalResponse;
		}
	}

    @SuppressWarnings("unchecked")
	public EvalAndDecryptResponse evaluateAndDecrypt(File fileToEvaluate, RMSUserPrincipal user, String sessionId, String ipAddress,
																									RepositoryFileParams params) throws Exception{
		boolean hasPrintRights=true;
		boolean hasPMIRights=true;
		boolean hasOpenRights=true;
		Map<String, List<String>> tagMap = null;
		String cacheId=null;
		byte[] fileContent=null;
		EvalResponse evalRes=null;
		EvalResAttributes evalResAttr=null;
		CryptManager cryptManager=new CryptManager();
		String fileNameWithoutNXL = getRequestedFileNameWithoutNXL(fileToEvaluate);
		boolean isNXLFile = StringUtils.endsWithIgnoreCase(fileToEvaluate.getName(), EvaluationHandler.NXL_FILE_EXTN);

		if (fileToEvaluate.isDirectory() || isNXLFile) {
			evalResAttr = evaluate(fileToEvaluate, user, sessionId, ipAddress);
			evalRes = evalResAttr.getEvalResponse();
		}
		else {
			evalRes = getAllRights();
		}

		Rights rights = evalRes.getRights();
		hasOpenRights = rights.hasRights(EvalRequest.ATTRIBVAL_ACTION_OPEN);
		hasPrintRights = rights.hasRights(EvalRequest.ATTRIBVAL_ACTION_PRINT);
		hasPMIRights = rights.hasRights(EvalRequest.ATTRIBVAL_ACTION_PMI);

		if (hasOpenRights) {
			boolean isWriteFileToDisk = GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.WRITE_DECRYPTED_FILE_TO_DISK);
			if (fileToEvaluate.isDirectory()) {
				decryptFolder(fileToEvaluate);
			}
			else {
				fileContent = getFileContent(cryptManager, fileToEvaluate, isWriteFileToDisk, fileNameWithoutNXL, isNXLFile);
			}
			String displayName = fileNameWithoutNXL;
			boolean isFileNameAsDocId = GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID);
			if (isFileNameAsDocId) {
				String username=user.getUserName().replace("\\","");
				cacheId = username+fileNameWithoutNXL;
			} else {
				cacheId = System.currentTimeMillis() + UUID.randomUUID().toString();
			}
			Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_FILECONTENT);
			Obligation obligation = evalRes.getObligation(EvalRequest.OBLIGATION_WATERMARK);
			WaterMark waterMark = null;
			String waterMarkStr = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.IMAGE_WATERMARK);
			if (obligation != null || (waterMarkStr != null && waterMarkStr.length() >0)) {
				waterMark = WatermarkUtil.build(obligation);
				waterMark.setWaterMarkStr(WatermarkUtil.updateWaterMark(waterMark.getWaterMarkStr(), user.getDomain(), user.getUserName(), waterMark));
			}
			tagMap=getTags(fileToEvaluate, cryptManager);
			long fileSize=getFileSize(fileToEvaluate);
			IRepository repo = null;
			if(params.getRepoId()!=IRepository.INVALID_REPOSITORY_ID) {
				repo = RepositoryFactory.getInstance().getRepository(user, params.getRepoId());
			}
			CachedFile cachedFile = new CachedFile(fileNameWithoutNXL, fileContent, hasPrintRights, waterMark, null, hasPMIRights, params, repo, fileSize, tagMap);
			FileCacheId fileCacheId=new FileCacheId(sessionId,user.getUserName(),cacheId);
			cache.put(new Element(fileCacheId, cachedFile));
			return new EvalAndDecryptResponse(evalRes, fileCacheId, displayName);
		} else {
			List<String> unauthorizedFiles = (List<String>) evalResAttr.getAttributesMap().get("unauthorizedFiles");
			StringBuilder builder = new StringBuilder();
			int size = unauthorizedFiles != null ? unauthorizedFiles.size() : 0;
			int idx = 0;
			if (unauthorizedFiles != null) {
				for (String unauthorizedFile: unauthorizedFiles) {
					builder.append(unauthorizedFile);
					if (idx++ < size-1) {
						builder.append(", ");
					}
				}
			}
			String fileList = builder.toString();
			logger.error("User "+ user.getUserName() + " with SID "+ user.getUid() + " not allowed to view the file:"+ fileList);
			String errorMsg = getUnauthorizedMsg(evalRes,fileList);
			throw new PolicyEvalException(errorMsg);
		}
	}
    
  private long getFileSize(File fileToEvaluate){
  	if(fileToEvaluate.isDirectory()) {
			String assemblyFileName = CADAssemblyUtil.getAssemblyFileNameFromFolder(fileToEvaluate);
			fileToEvaluate = new File(fileToEvaluate, assemblyFileName);
		}
  	return fileToEvaluate.length();
  }

    private byte[] getFileContent(CryptManager cryptManager, File fileToEvaluate, boolean isWriteFileToDisk,
                                  String fileNameWithoutNXL, boolean isEncrypted) throws Exception {
        byte[] fileContent = null;

        if (isEncrypted) {
            fileContent = cryptManager.getDecryptedBytes(fileToEvaluate);
        }
        else {
            Path path = Paths.get(fileToEvaluate.getPath());
            fileContent = Files.readAllBytes(path);
        }

        if (isWriteFileToDisk) {
            writeContentsToFile(fileToEvaluate.getParent(), fileNameWithoutNXL, fileContent);
        }
        if (fileContent == null) {
            throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
        }
        if (fileContent.length == 0) {
            logger.error("The file - '"+fileNameWithoutNXL+ "' is empty");
            throw new RMSException(RMSMessageHandler.getClientString("emptyFileErr"));
        }

        return fileContent;
    }

	private String getUnauthorizedMsg(EvalResponse response,String fileName) {
		String errorMsg = RMSMessageHandler.getClientString("unAuthView")+" ("+fileName+")";
		Obligation notifyObligation = response.getObligation(EvalRequest.OBLIGATION_NOTIFY);
		if (notifyObligation != null) {
			List<Attribute> attributes = notifyObligation.getAttributes();
			// notify obligation only have single attribute
			if (!attributes.isEmpty()) {
				Attribute attribute = attributes.get(0);
				String value = attribute.getValue();
				if (value != null && value.trim().length() > 0) {
					errorMsg = value;
					if (errorMsg.contains("<a href")) {
						if (!(errorMsg.contains("http") || errorMsg.contains("https") || errorMsg.contains("mailto"))) {
							errorMsg = errorMsg.replace("<a href=\"", "<a href=\"http://");
						}
					}
				}
			}
		}
		return errorMsg;
	}

	public static String getRequestedFileNameWithoutNXL (File filePath) {
		if(filePath.isDirectory()){
			return CADAssemblyUtil.getAssemblyFileNameFromFolder(filePath);
		} else {
			return getFileNameWithoutNXL(filePath);
		}
	}

	public static String getFileNameWithoutNXL(File filePath) {
		String fileName = filePath.getName();
		return getFileNameWithoutNXL(fileName);
		
	}
	
	public static String getFileNameWithoutNXL(String fileName){
		if(fileName==null|| fileName.length()==0){
			return fileName;
		}
		if(!fileName.toLowerCase().endsWith(NXL_FILE_EXTN)){
			return fileName;			
		}
		int index = fileName.toLowerCase().lastIndexOf(NXL_FILE_EXTN);
		String name = fileName.substring(0, index);
		return name;
	}
	
	public static String getOriginalFileExtension(String fileName){
		String fileExtension="";
		if(fileName==null|| fileName.length()==0){
			return fileExtension;
		}
		if(fileName.toLowerCase().endsWith(NXL_FILE_EXTN)){
			fileExtension=FilenameUtils.getExtension(getFileNameWithoutNXL(fileName));			
		}
		else{
			fileExtension=FilenameUtils.getExtension(fileName);
		}
		return fileExtension;
	}

	public static void writeContentsToFile(String targetDirPath,
			String fileName, byte[] fileContent)
					throws FileNotFoundException, IOException {
		FileOutputStream ostr = null;
		try{
			File targetDir = new File(targetDirPath);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			File decryptedFile = new File(targetDir, fileName);
			if (decryptedFile.exists()) {
				decryptedFile.delete();
			}
			ostr = new FileOutputStream(decryptedFile);
			ostr.write(fileContent);
			ostr.flush();
			if (logger.isDebugEnabled()) {
				logger.debug("File written to:" + decryptedFile.getAbsolutePath());
			}
		} finally {
			if (ostr != null) {
				ostr.close();
			}
		}
	}
	private void decryptFolder(File fileToEvaluate) throws RMSException {
		CryptManager cryptManager=new CryptManager();
		File[] zipFileEntries=fileToEvaluate.listFiles();
		if (zipFileEntries != null) {
			for(File file:zipFileEntries){
				String fileExt = '.' + FilenameUtils.getExtension(file.getName());
				if(!fileExt.equalsIgnoreCase(NXL_FILE_EXTN))
					continue;
				try {
					byte[] fileContent = cryptManager.getDecryptedBytes(file);
					if(fileContent == null){
						throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
					}
					if(fileContent.length==0){
						logger.error("The file - '"+file.getName()+ "' is empty");
						throw new RMSException(RMSMessageHandler.getClientString("emptyFileErr"));
					}
					writeContentsToFile(file.getParent(), getFileNameWithoutNXL(file), fileContent);
					file.delete();
				} catch (Exception e){
					logger.error("Error while decrypting the file - '"+file.getName());
					throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
				}
			}
		}
	}

	public List<String> getEvaluatedRights() {
		List<String> rights = new ArrayList<String>(3) {
			private static final long serialVersionUID = 1L;

			{
				add(EvalRequest.ATTRIBVAL_ACTION_OPEN);
				add(EvalRequest.ATTRIBVAL_ACTION_PRINT);
				add(EvalRequest.ATTRIBVAL_ACTION_PMI);
			}
		};
		return rights;
	}

	public EvalResponse getAllRights() {
		EvalResponse resp = new EvalResponse();
		List<String> rightsList = getEvaluatedRights();
		for (String rights : rightsList) {
			resp.addRights(rights);
		}
		return resp;
	}

	public static boolean hasDocConversionJarFiles() {
		String perceptiveJarPath = GlobalConfigManager.getInstance().getDocConverterDir() + File.separator + GlobalConfigManager.ISYS11DF_JAR;
		String memoryStreamJarPath = GlobalConfigManager.getInstance().getDocConverterDir() + File.separator + GlobalConfigManager.MEMORY_STREAM_JAR;
		File perceptiveJar = new File(perceptiveJarPath);
		File memoryStreamJar = new File(memoryStreamJarPath);

		return (perceptiveJar.exists() && memoryStreamJar.exists());
	}
}