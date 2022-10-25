package com.nextlabs.rms.visualization;

import java.io.File;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.eval.EvalResponse;

public interface IVisManager {
	
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, byte[] fileContent,
			String displayName, String cacheId) throws RMSException, MissingDependenciesException;
	
	public String getVisURL(RMSUserPrincipal user, String sessionId, EvalResponse evalRes, File folderpath,
			String displayName, String cacheId) throws RMSException, MissingDependenciesException;
}
