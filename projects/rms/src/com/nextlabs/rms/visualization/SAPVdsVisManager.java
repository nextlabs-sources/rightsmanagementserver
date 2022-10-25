package com.nextlabs.rms.visualization;

import java.io.File;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.license.LicenseManager;
import com.nextlabs.rms.license.LicensedFeature;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;

public class SAPVdsVisManager implements IVisManager {

	private static Logger logger = Logger.getLogger(SAPVdsVisManager.class);
	
	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, File folderpath,
			String displayName, String cacheId) throws RMSException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, byte[] fileContent,
			String fileNameWithoutNXL, String cacheId) throws RMSException {
		if(!LicenseManager.getInstance().isFeatureLicensed(LicensedFeature.FEATURE_VIEW_SAP_3D_FILE)){
			throw new RMSException(RMSMessageHandler.getClientString("licenseError"));
		}
		
		File sapVDSViewer=new File(GlobalConfigManager.getInstance().getSAPViewerDir());
		if(!sapVDSViewer.exists()){
			throw new RMSException(RMSMessageHandler.getClientString("missingViewerPackageErr", "SAP Viewer", "VDS"));
		}
		
		String filePath = null;
		String docIdWithExt  = null;
		boolean isFileNameAsDocId = GlobalConfigManager.getInstance()
				.getBooleanProperty(GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID);
		String redirectURL = null;
		String fileParameters = null;
		String dataDir = GlobalConfigManager.getInstance().getTempDir() + File.separator + sessionId;
		docIdWithExt = cacheId+GlobalConfigManager.VDS_FILE_EXTN;
		File inputFile = new File(dataDir, docIdWithExt);
		try {
			EvaluationHandler.writeContentsToFile(inputFile.getParent(), docIdWithExt, fileContent);
			if (isFileNameAsDocId) {
				// for testing purpose
				filePath = GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID + "/" + cacheId;
			} else {
				filePath = GlobalConfigManager.TEMPDIR_NAME + "/" + sessionId + "/" + cacheId;
			}
			File destinationPath = new File(GlobalConfigManager.getInstance().getWebDir() + filePath);
			FileUtils.copyFileToDirectory(inputFile, destinationPath);
			if (isFileNameAsDocId) {
				fileParameters = URLEncoder.encode(filePath + "/" + docIdWithExt + "&cacheId=" + cacheId+ "&originalFileName="+fileNameWithoutNXL, "UTF-8");
			} else {
				fileParameters = URLEncoder.encode(filePath + "/" + docIdWithExt + "&cacheId="+cacheId, "UTF-8");
			}
			fileParameters = fileParameters.replaceAll("[+]", "%20");
			CachedFile cachedFile = RMSCacheManager.getInstance().getCachedFile(new FileCacheId(sessionId,user.getUserName(),cacheId));
			cachedFile.setContentType(CachedFile.ContentType._3D);	

			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/VDSViewer.jsp?filePath=" + fileParameters; 
			logger.debug("ReDirectUrl: " + redirectURL);
		} catch (Exception e) {
			logger.error("Error occurred while processing the file: "+fileNameWithoutNXL+ " with docId: " +docIdWithExt, e);
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		}
		return redirectURL;
	}


}
