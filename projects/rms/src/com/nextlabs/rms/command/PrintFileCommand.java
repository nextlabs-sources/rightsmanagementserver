package com.nextlabs.rms.command;

import java.io.File;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.conversion.RMSViewerContentManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.PrintFileUrl;
import com.nextlabs.rms.locale.RMSMessageHandler;

public class PrintFileCommand extends AbstractCommand{

		private static Logger logger = Logger.getLogger(PrintFileCommand.class);
	
		@Override
	    public void doAction(HttpServletRequest request,
	                  HttpServletResponse response) throws Exception {
	           String documentId = request.getParameter("documentId");
	           String sessionId=request.getSession().getId();
	           if(documentId==null || documentId.length()==0){
	                  return;
	           }
	           RMSViewerContentManager contentMgr = RMSViewerContentManager.getInstance();
	           RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
	           /*Element element = contentMgr.getDocumentFromCache(documentId);
	           CachedFile cachedFile = (CachedFile)element.getObjectValue();*/
	           boolean isPrintAllowed=contentMgr.checkPrintPermission(documentId, sessionId, userPrincipal.getUserName());
	           PrintFileUrl printFileUrl = null;
	           String error=null;
	           if(!isPrintAllowed){
	        	   error=RMSMessageHandler.getClientString("printPermissionErr");
    			   printFileUrl=new PrintFileUrl(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+error,error);
    			   JsonUtil.writeJsonToResponse(printFileUrl, response);
    			   return;
	           }
	           String webDir = GlobalConfigManager.getInstance().getWebDir();
	           String folderPath = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
	           String[] folders=folderPath.split(Pattern.quote(File.separator));
	           String sessionFolderName=folders[folders.length-1];
	           logger.debug("sessionFolderName "+sessionFolderName);
	           File tempWebDir = new File(webDir, GlobalConfigManager.TEMPDIR_NAME+File.separator+sessionFolderName+File.separator+documentId);
	           if(!tempWebDir.exists()){
	                  tempWebDir.mkdirs();
	           }
	           try {
	        	   printFileUrl = contentMgr.generatePDF(documentId,tempWebDir.getAbsolutePath(),userPrincipal.getDomain(), userPrincipal.getUserName(),sessionId);
	        	   printFileUrl.getUrl().replace("\\", "/");
	           } catch (Exception e) {
	        	   logger.error(e.getMessage());
	        	   error=e.getMessage();
	           }
	           printFileUrl.setError(error);
	           JsonUtil.writeJsonToResponse(printFileUrl, response);   
	    }
}
