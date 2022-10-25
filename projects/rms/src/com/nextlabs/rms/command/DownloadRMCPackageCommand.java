package com.nextlabs.rms.command;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.ZipUtil;


public class DownloadRMCPackageCommand extends AbstractCommand{
	
	private static Logger logger = Logger.getLogger(DownloadRMCPackageCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		String rmcZipPath=GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RMC_ZIP_PATH);
		File zipFile = new File(rmcZipPath);
		if(!zipFile.exists()){
			logger.error("Invalid RMC Package Directory: " + rmcZipPath);		
			OperationResult errorResult = new OperationResult();
			errorResult.setResult(false);
			errorResult.setMessage(RMSMessageHandler.getClientString("rmcDownloadErr"));
			JsonUtil.writeJsonToResponse(errorResult, response);
			return;
		}
		String rmcFolderName = FilenameUtils.getBaseName(rmcZipPath);
		String tempUnzipPath=(String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		File tempUnzipDir=new File(tempUnzipPath);
		tempUnzipDir.mkdirs();				
		String tempUnzippedRMCFolder=tempUnzipPath+File.separator+rmcFolderName;
		ZipUtil.getInstance().unZip(rmcZipPath,tempUnzippedRMCFolder);
		String loginMode = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RMC_AUTH_MODE);
		StringBuilder registerStringBuilder= new StringBuilder();
		registerStringBuilder.append("<RegistrationInfo>");
		registerStringBuilder.append("<URL>");
		registerStringBuilder.append(UtilMethods.getServerUrl(request));
		registerStringBuilder.append(GlobalConfigManager.RMS_CONTEXT_NAME);
		registerStringBuilder.append("/service");
		registerStringBuilder.append("</URL>");
		registerStringBuilder.append("<TenantId>");
		registerStringBuilder.append("NA");
		registerStringBuilder.append("</TenantId>");
		registerStringBuilder.append("<Cert>");
		registerStringBuilder.append(SSLSocketFactoryGenerator.getKeyStoreUnSecureCertificate());
		registerStringBuilder.append("</Cert>");
		registerStringBuilder.append("<Domains>");
		String[] domains = GlobalConfigManager.getInstance().getDomainNames();
		if(domains!=null && domains.length>0){
			for (String domain : domains) {
				registerStringBuilder.append("<Domain>");
				registerStringBuilder.append(domain);
				registerStringBuilder.append("</Domain>");				
			}
		}
		registerStringBuilder.append("</Domains>");
		if (loginMode != null && loginMode.trim().length() > 0) {
			registerStringBuilder.append("<AuthnType>").append(loginMode.trim()).append("</AuthnType>");
		}
		registerStringBuilder.append("</RegistrationInfo>");
		FileUtils.writeStringToFile(new File(tempUnzippedRMCFolder+File.separator+GlobalConfigManager.RMC_CONFIG_FILE_NAME), registerStringBuilder.toString());
		
		String sessionId = request.getSession().getId();
		String webDir = GlobalConfigManager.getInstance().getWebDir();
		File tempWebDir = new File(webDir, GlobalConfigManager.TEMPDIR_NAME+File.separator+sessionId+File.separator);
		if(!tempWebDir.exists()){
			tempWebDir.mkdirs();
		}
		File outputZipFile = new File(tempWebDir,rmcFolderName+".zip");	
		ZipUtil.zipFolder(tempUnzippedRMCFolder, outputZipFile.getAbsolutePath());
		
		String redirectUrl = GlobalConfigManager.RMS_CONTEXT_NAME+ "/" + GlobalConfigManager.TEMPDIR_NAME + "/" + sessionId + "/" +outputZipFile.getName();
		JsonObject json = new JsonObject();
		json.addProperty("result", true);
		json.addProperty("filePath", redirectUrl);
		JsonUtil.writeJsonToResponse(json, response);
		return;
	}

}
