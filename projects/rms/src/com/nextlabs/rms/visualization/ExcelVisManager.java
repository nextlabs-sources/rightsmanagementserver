package com.nextlabs.rms.visualization;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.nextlabs.rms.eval.EvalRequest;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.Obligation;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.eval.Rights;
import com.nextlabs.rms.eval.EvaluationHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.conversion.HTMLFileGenerator;
import com.nextlabs.rms.conversion.WaterMark;
import com.nextlabs.rms.license.LicenseManager;
import com.nextlabs.rms.license.LicensedFeature;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;
import com.nextlabs.rms.util.WatermarkUtil;

public class ExcelVisManager implements IVisManager{

	private static final String HTML_FILE_EXTN = ".html";
	
	private static Logger logger = Logger.getLogger(ExcelVisManager.class);
	
	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, File folderpath,
			String displayName, String cacheId) throws RMSException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId,
			 EvalResponse evalRes, byte[] fileContent,
			String displayName,String cacheId) throws RMSException {

		if(!LicenseManager.getInstance().isFeatureLicensed(LicensedFeature.FEATURE_VIEW_GENERIC_FILE)){
			throw new RMSException(RMSMessageHandler.getClientString("licenseError"));
		}
		if (!EvaluationHandler.hasDocConversionJarFiles()) {
			throw new RMSException(RMSMessageHandler.getClientString("missingViewerPackageErr", "Document Viewer", FilenameUtils.getExtension(displayName).toUpperCase()));
		}

		String redirectURL = null;
		boolean isPrintAllowed=true;
		String htmlFileName=getHtmlFileName(displayName);
		try {
			Rights rights = evalRes.getRights();
			boolean hasPrintRights = rights.hasRights(EvalRequest.ATTRIBVAL_ACTION_PRINT);
			if (!hasPrintRights) {
				isPrintAllowed = false;
			}
			Obligation obligation = evalRes.getObligation(EvalRequest.OBLIGATION_WATERMARK);
			WaterMark waterMark = null;
			String waterMarkStr = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.IMAGE_WATERMARK);
			if (obligation != null || (waterMarkStr != null && waterMarkStr.length() >0)) {
				waterMark = WatermarkUtil.build(obligation);
				waterMark.setWaterMarkStr(WatermarkUtil.updateWaterMark(waterMark.getWaterMarkStr(), user.getDomain(), user.getUserName(), waterMark));
			}
			HTMLFileGenerator.handleExcelFile(sessionId, cacheId, htmlFileName, fileContent, user,isPrintAllowed, waterMark);
			String outFile = URLEncoder.encode(htmlFileName,"UTF-8");
			outFile = outFile.replaceAll("[+]", "%20");
			CachedFile cachedFile = RMSCacheManager.getInstance().getCachedFile(new FileCacheId(sessionId,user.getUserName(),cacheId));
			cachedFile.setContentType(CachedFile.ContentType._2D);	
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/"+ GlobalConfigManager.TEMPDIR_NAME 
					+ "/" + sessionId + "/" +cacheId +"/"+ outFile;
			
		} catch (UnsupportedEncodingException e) {
			logger.error("Error occured while encoding  File:" +htmlFileName);
		} catch (IOException e) {	
			logger.error("Error occured while converting file to html:" +htmlFileName);
		}
		return redirectURL;
	}
	
	private String getHtmlFileName(String origFileName) {
		return origFileName + HTML_FILE_EXTN;
	}


}
