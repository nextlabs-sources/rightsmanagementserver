/**
 * 
 */
package com.nextlabs.rms.conversion;

import java.io.File;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.EvaluationHandler.EvalAndDecryptResponse;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;
import com.nextlabs.rms.visualization.IVisManager;
import com.nextlabs.rms.visualization.VisManagerFactory;

/**
 * @author nnallagatla
 *
 */
public class FileConversionHandler {
	private static Logger logger = Logger.getLogger(FileConversionHandler.class);
		 
	public String convertAndGetURL(RMSUserPrincipal user, EvalAndDecryptResponse evalDecryptResponse, File evaluatedFile) throws RMSException, MissingDependenciesException{
		logger.debug("Converting file " + evaluatedFile.getName());
		String fileNameWithoutNXL = EvaluationHandler.getRequestedFileNameWithoutNXL(evaluatedFile);
		IVisManager genericVisManager=VisManagerFactory.getInstance().getVisManager(fileNameWithoutNXL);
		
		FileCacheId fCacheId = evalDecryptResponse.getFileCacheId();
		EvalResponse evalRes = evalDecryptResponse.getEvalResponse();
		String displayName = evalDecryptResponse.getFileDisplayName();
		
		String redirectURL = "";
		
		if(evaluatedFile.isDirectory()){
			redirectURL=genericVisManager.getVisURL(user, fCacheId.getSessionId(), evalRes, evaluatedFile, displayName, fCacheId.getDocId());
		}
		else{
			CachedFile cachedFile= RMSCacheManager.getInstance().getCachedFile(fCacheId);
			redirectURL = genericVisManager.getVisURL(user, fCacheId.getSessionId(), evalRes, cachedFile.getFileContent(), displayName, fCacheId.getDocId());	
		}
		logger.debug("Returning URL for file " + evaluatedFile.getName());
		return redirectURL;
	}
}
