package com.nextlabs.rms.nxl.decrypt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.nextlabs.nxl.crypt.RightsManager;
import com.nextlabs.nxl.exception.NXRTERROR;
import com.nextlabs.nxl.legacy.FileMetaDataForEvaluation;
import com.nextlabs.nxl.pojos.ConnectionResult;
import com.nextlabs.nxl.pojos.NXLFileMetaData;
import com.nextlabs.nxl.pojos.PolicyControllerDetails;
import com.nextlabs.nxl.util.DecryptionUtil;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.nxl.legacy.OldNXLFileMetaData;
import com.nextlabs.nxl.legacy.OldNxlDecryptionHandler;
import com.nextlabs.nxl.legacy.OldNxlFile;

public class CryptManager {
		
	private static Logger logger = Logger.getLogger(CryptManager.class);
	
	private static RightsManager rightsManager;
	
	private static String kmsurl=GlobalConfigManager.getInstance().getKMSUrl();
	
	private static boolean useKmsKeys=GlobalConfigManager.getInstance().useKmsKeys();
	
	public static long fileSizeLimit=1024*1024*1024*2L;

	static {
		PolicyControllerDetails pcObject = loadPolicyControllerObject();
		try {
			rightsManager=kmsurl!=null && kmsurl.length()>0 && useKmsKeys?new RightsManager(kmsurl):new RightsManager(pcObject);
		} catch (NXRTERROR e) {
			logger.debug("Error occured while creating an instance of RightsManager",e);
		}
    }

	public static PolicyControllerDetails loadPolicyControllerObject() {
		boolean isRemotePC=ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getBooleanProperty(ConfigManager.ENABLE_REMOTE_PC);
		PolicyControllerDetails pcObject=new PolicyControllerDetails();
		if(isRemotePC){
			pcObject.setKeyStoreName(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.KM_RMI_KEYSTOREFILE));
			pcObject.setKeyStorePassword(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.KM_RMI_KEYSTOREPASSWORD));
			pcObject.setTrustStoreName(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.KM_RMI_TRUSTSTOREFILE));
			pcObject.setTrustStorePasswd(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.KM_RMI_TRUSTSTOREPASSWORD));
			pcObject.setPcHostName(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.KM_POLICY_CONTROLLER_HOSTNAME));
			pcObject.setRmiPortNum(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getIntProperty(ConfigManager.KM_RMI_PORT_NUMBER));
			return pcObject;
		}
		else{
			pcObject.setKeyStoreName(GlobalConfigManager.getInstance().getDataDir()+File.separator+GlobalConfigManager.CERT_FOLDER_NAME+File.separator+GlobalConfigManager.EMBEDDEDJPC_KEYSTOREKMC_FILE);
			pcObject.setKeyStorePassword(GlobalConfigManager.EMBEDDEDJPC_KEYSTOREKMC_PASSWORD);
			pcObject.setTrustStoreName(GlobalConfigManager.getInstance().getDataDir()+File.separator+GlobalConfigManager.CERT_FOLDER_NAME+File.separator+GlobalConfigManager.EMBEDDEDJPC_TRUSTSTOREKMC_FILE);
			pcObject.setTrustStorePasswd(GlobalConfigManager.EMBEDDEDJPC_TRUSTSTOREKMC_PASSWORD);
			pcObject.setPcHostName(GlobalConfigManager.EMBEDDEDJPC_HOSTNAME);
			pcObject.setRmiPortNum(GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.EMBEDDEDJPC_RMI_PORT_NUMBER ));
			return pcObject;
		}
	}
	
	private int getNXLVersion(RandomAccessFile file) throws Exception {
		String nxlSignatureCode=DecryptionUtil.readUnsignedChar(file,0,8);
		String majVer = DecryptionUtil.readUnsignedChar(file, 0, 1);
		if(nxlSignatureCode.equals("NXLFMT!")){
			logger.debug("New nxl file format detected");
			return 1;
		} else if(majVer.trim().equals("2")){
			logger.debug("Old Nxl file format detected");
			return 0;
		}
		return -1;
	}
	
	public byte[] getDecryptedBytes(File inputFile) throws RMSException{
		RandomAccessFile file=null;
		try{
			file=new RandomAccessFile(inputFile, "r");
			if(file.length()>fileSizeLimit){
				throw new RMSException("The maximum supported file size is 2GB. Please try opening a smaller file");
			}
			int version=getNXLVersion(file);
			if(version==0){
				OldNxlDecryptionHandler handler=new OldNxlDecryptionHandler(file);
				return ((OldNxlFile)handler.parseContent()).getNxlFilePart().getDecryptedFileContent();
			}else if(version==1){
				byte[][] decryptedArray= rightsManager.decrypt(inputFile, null,ConfigManager.KMS_DEFAULT_TENANT_ID).getDecryptedBytes();
				return decryptedArray[0];
			}
		}
		catch(Exception ex){
			logger.error("Couldn't decrypt the file ",ex);
			if(ex.getCause() != null && ex.getCause() instanceof java.security.InvalidKeyException){
				throw new RMSException(RMSMessageHandler.getClientString("invalidKeySize"));
			}
		}
		finally{			
//			file=null;
			try {
				file.close();
			} catch (IOException e) {
				logger.error("Error occured while closing the file: ",e);
			}
		}
		return null;	
	}
	
	public FileMetaDataForEvaluation readMetaData(File inputFile){
		RandomAccessFile file=null;
		try{
			file=new RandomAccessFile(inputFile, "r");
			int version=getNXLVersion(file);
			FileMetaDataForEvaluation metadata=new FileMetaDataForEvaluation();
			if(version==0){
				OldNxlDecryptionHandler handler=new OldNxlDecryptionHandler(file);
				Map<String,String> tags=((OldNXLFileMetaData)handler.readMeta()).getNlTagPart().getTags();
				Map<String,List<String>> tagsNewFormat=new HashMap<>();
				Set<String> keys = tags.keySet();
				for (String key: keys){
					List<String> value=new ArrayList<String>();
					value.add(tags.get(key));
					tagsNewFormat.put(key, value);
				}
				metadata.setTags(tagsNewFormat);
			}else if(version==1){
				NXLFileMetaData fileMeta= rightsManager.readMeta(inputFile);
				metadata.setAttr(fileMeta.getAttr());
				metadata.setRights(fileMeta.getRights());
				metadata.setTags(fileMeta.getTags());
			}
			return metadata;
		}
		catch(Exception e){
			logger.error("Couldn't read metadata of the file ",e);
			return null;
		}
		finally{
			try{
				file.close();
			}
			catch(IOException e){
				logger.error("Cannot close the file",e);
			}
			file=null;
		}
	}
	
	public static ConnectionResult testConnection(PolicyControllerDetails pcDetails) throws NXRTERROR{
		if(rightsManager==null){
			rightsManager = kmsurl!=null&&kmsurl.length()>0?new RightsManager(GlobalConfigManager.getInstance().getKMSUrl()):new RightsManager(pcDetails);
		}else{
		    if(kmsurl!=null&&kmsurl.length()>0){
				rightsManager.reloadConfig(GlobalConfigManager.getInstance().getKMSUrl());
			}else{
				rightsManager.reloadConfig(pcDetails);
			}			
		}
		ConnectionResult res = rightsManager.testConnection();
		try{
			refresh();			
		}catch(NXRTERROR ex){
			logger.error("Error occurred while re-initializing RIghtsManager");
		}
		return res;
	}

	public static void refresh() throws NXRTERROR {
		if(kmsurl!=null && kmsurl.length()>0 && useKmsKeys){
			rightsManager.reloadConfig(GlobalConfigManager.getInstance().getKMSUrl());
		}else{
			rightsManager.reloadConfig(loadPolicyControllerObject());
		}
	}

	public static void cleanup() {
		rightsManager.cleanup();		
	}
}
