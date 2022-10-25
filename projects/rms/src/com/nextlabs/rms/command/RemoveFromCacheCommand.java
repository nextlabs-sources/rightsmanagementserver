package com.nextlabs.rms.command;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.conversion.RMSViewerContentManager;
import com.nextlabs.rms.filters.AuthFilter;

public class RemoveFromCacheCommand extends AbstractCommand {

	private Logger logger = Logger.getLogger(RemoveFromCacheCommand.class);
	
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String documentId = request.getParameter("documentId");
		String sessionId=request.getSession().getId();
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		if(documentId==null || documentId.length()==0){
			return;
		}
		RMSViewerContentManager mgr = RMSViewerContentManager.getInstance();
		boolean res = mgr.removeDocumentFromCache(documentId,sessionId,user.getUserName());
		String docTempDirPath = GlobalConfigManager.getInstance().getWebDir()+File.separator+ GlobalConfigManager.TEMPDIR_NAME+File.separator+sessionId+File.separator+documentId;
		File docTempDir = new File(docTempDirPath);
		if(docTempDir.exists()){
			try{
				FileUtils.deleteDirectory(docTempDir);
				logger.debug("Deleted doc temp dir:"+docTempDirPath);
			}catch(Exception e){
				logger.error("Error occurred while deleting doc temp dir:"+docTempDirPath, e);
			}			
		}
		
	}

}