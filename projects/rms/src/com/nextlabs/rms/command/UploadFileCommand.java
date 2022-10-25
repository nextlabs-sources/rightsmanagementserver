package com.nextlabs.rms.command;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;
import com.nextlabs.rms.wrapper.FormProcessorUtil;
import com.nextlabs.rms.wrapper.util.HTMLWrapperUtil;

public class UploadFileCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(UploadFileCommand.class);
	
	private String originalName = "";
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);
		String isCache = request.getParameter("cached");
		String sessionId=request.getSession().getId();
		String fileContent;
		if(isCache!=null&&isCache.equalsIgnoreCase("true")){
			fileContent = getFileContentStrFromCache(request);			
		}else{
			fileContent = getFileContentStr(request);
		}
		byte[] decodedBytes=Base64.decodeBase64(fileContent);
		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		String destFilePath = userTempDir + File.separator + originalName;
		File file = FormProcessorUtil.writeFileToDisk(decodedBytes, destFilePath);
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		EvaluationHandler evalHandler = new EvaluationHandler();
		String redirectURL = "";
		try {
			redirectURL = evalHandler.validateFileRequest(user, file,sessionId,UtilMethods.getIP(request), params);
			//redirectURL = evalHandler.getEncodedURL(redirectURL);
			//redirectURL=redirectURL.replace("\"", "\\\"");
			String url = GlobalConfigManager.RMS_CONTEXT_NAME+"/OpenLink.jsp?redirectURL="+redirectURL;
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(url);
		} catch (MissingDependenciesException missingException) {
				logger.error(missingException.getMessage());
		}
		
	}

	private String getFileContentStr(HttpServletRequest request) {
		Map<String, String[]> paramMap = (Map<String, String[]>)request.getParameterMap();
		return getFileContentStrFromMap(paramMap);
	}

    private String getFileContentStrFromCache(HttpServletRequest request) {
		Map<String, String[]> paramMap = (Map<String, String[]>)request.getSession().getAttribute(AuthFilter.CACHED_FILE_UPLD);
    	if(paramMap==null||paramMap.size()==0){
    		return null;
    	}
    	return getFileContentStrFromMap(paramMap);
	}

	private String getFileContentStrFromMap(Map<String, String[]> paramMap) {
		int parmId = 0;
    	StringBuffer str = new StringBuffer();
    	while(paramMap.get(HTMLWrapperUtil.FIELD_NAME+parmId)!=null){
    		str.append(paramMap.get(HTMLWrapperUtil.FIELD_NAME+parmId)[0]);
    		parmId++;
    	}
    	if(paramMap.get(HTMLWrapperUtil.ORIGFILENAME)!=null){
    		logger.debug("The name of the orignal file is "+paramMap.get(HTMLWrapperUtil.ORIGFILENAME)[0]);
    		originalName = paramMap.get(HTMLWrapperUtil.ORIGFILENAME)[0];
       	}
    	return str.toString();
	}

}
