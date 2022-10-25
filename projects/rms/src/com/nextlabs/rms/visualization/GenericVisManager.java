package com.nextlabs.rms.visualization;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import com.nextlabs.rms.eval.EvaluationHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.license.LicenseManager;
import com.nextlabs.rms.license.LicensedFeature;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;

public class GenericVisManager implements IVisManager{
	
	private static Logger logger = Logger.getLogger(GenericVisManager.class);

	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, File folderpath,
			String displayName, String cacheId) throws RMSException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getVisURL(RMSUserPrincipal user, String sessionId,
			EvalResponse evalRes, byte[] fileContent, String displayName,String cacheId)
			throws RMSException {
		
		if(!LicenseManager.getInstance().isFeatureLicensed(LicensedFeature.FEATURE_VIEW_GENERIC_FILE)){
			throw new RMSException(RMSMessageHandler.getClientString("licenseError"));
		}
		if (!EvaluationHandler.hasDocConversionJarFiles()) {
			throw new RMSException(RMSMessageHandler.getClientString("missingViewerPackageErr", "Document Viewer", FilenameUtils.getExtension(displayName).toUpperCase()));
		}
		URI uri = null;
		try {
			uri = new URI(GlobalConfigManager.RMS_CONTEXT_NAME
					+ "/DocViewer.jsp?documentid="
					+ URLEncoder.encode(cacheId, "UTF-8"));      	 
		} catch (UnsupportedEncodingException e) {
			logger.error("Error occured while encoding file :" + displayName);
		} catch (URISyntaxException e) {
			logger.error("Error occured while encoding file :" + displayName);
		}
		CachedFile cachedFile = RMSCacheManager.getInstance().getCachedFile(new FileCacheId(sessionId,user.getUserName(),cacheId));
		cachedFile.setContentType(CachedFile.ContentType._2D);	
		return uri.toString();
	}

}
