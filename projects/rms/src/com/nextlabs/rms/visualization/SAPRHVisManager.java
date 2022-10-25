package com.nextlabs.rms.visualization;

import java.io.File;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.license.LicenseManager;
import com.nextlabs.rms.license.LicensedFeature;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;

public class SAPRHVisManager implements IVisManager {

	private static Logger logger = Logger.getLogger(SAPRHVisManager.class);

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
		
		File sapRHViewer=new File(GlobalConfigManager.getInstance().getSAPViewerDir());
		
		if(!sapRHViewer.exists()){
			throw new RMSException(RMSMessageHandler.getClientString("missingViewerPackageErr", "SAP Viewer", "RH"));
		}
		
		boolean isFileNameAsDocId = GlobalConfigManager.getInstance()
				.getBooleanProperty(GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID);
		String redirectURL = null;
		String fileParameters = null;
		String filePath = null;
		String docIdWithExt = null;
		String dataDir = GlobalConfigManager.getInstance().getTempDir() + File.separator + sessionId;
		docIdWithExt = cacheId+GlobalConfigManager.RH_FILE_EXTN;
		File inputFile = new File(dataDir, docIdWithExt);
		try {
			EvaluationHandler.writeContentsToFile(inputFile.getParent(), docIdWithExt, fileContent);
			// at this moment, watermark is not supported by RH viewer
			
			// Obligation obligation = evalRes.getObligation(EvalRequest.OBLIGATION_WATERMARK);
			// WaterMark waterMark = WatermarkUtil.build(obligation);
			// if (waterMark != null) {
			// waterMark.setWaterMarkStr(WatermarkUtil.updateWaterMark(waterMark.getWaterMarkStr(), user.getUserName(), waterMark));
			// }
			if (isFileNameAsDocId) {
				filePath = GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID + "/" + cacheId;
			} else {
				filePath = GlobalConfigManager.TEMPDIR_NAME + "/" + sessionId + "/" + cacheId;
			}
			File destinationPath = new File(GlobalConfigManager.getInstance().getWebDir() + filePath);
			FileUtils.copyFileToDirectory(inputFile, destinationPath);
			
			fileParameters = URLEncoder.encode(filePath + "/" + docIdWithExt + "&cacheId=" + cacheId, "UTF-8");
			fileParameters = fileParameters.replaceAll("[+]", "%20");
			CachedFile cachedFile = RMSCacheManager.getInstance().getCachedFile(new FileCacheId(sessionId,user.getUserName(),cacheId));
			cachedFile.setContentType(CachedFile.ContentType._3D);	
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/RHViewer.jsp?filePath=" + fileParameters;
			logger.debug("ReDirectUrl: " + redirectURL);
		} catch (Exception e) {
			logger.error("Failed while processing file : " +fileNameWithoutNXL+ " with docId: " +docIdWithExt, e);
		}
		return redirectURL;
	}


}
