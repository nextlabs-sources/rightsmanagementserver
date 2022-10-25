package com.nextlabs.rms.command;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.MissingDependenciesException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.json.UploadFileResponse;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.util.CADAssemblyUtil;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;
import com.nextlabs.rms.wrapper.FormProcessorUtil;
import com.nextlabs.rms.wrapper.util.HTMLWrapperUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;

public class UploadAndViewCommand extends AbstractCommand {

	private static final String FILE="file";
	
	private String originalName = "";
	
	private static Logger logger = Logger.getLogger(UploadAndViewCommand.class);
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		processReq(request, response);
	}
	
	public void processReq(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Iterator<?> iter=FormProcessorUtil.getFormItemsFromReq(request);
		RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);
		byte[] bytes = readFile(iter);
		UploadFileResponse fileResponse;
		String value="";
	/*	String protocol="https://";
		if(!request.isSecure()){
			protocol="http://";
		}
	*/	
		String origin=request.getRequestURL().substring(0,(request.getRequestURL().indexOf(GlobalConfigManager.RMS_CONTEXT_NAME)));
		String sessionId=request.getSession().getId();
		response.setHeader("Access-Control-Allow-Origin",origin);// protocol+request.getRemoteHost());
		if(request.getHeader("Accept")!=null){
			value = request.getHeader("Accept");
		}

		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		String destFilePath = userTempDir + File.separator + originalName;
		File file = FormProcessorUtil.writeFileToDisk(bytes, destFilePath);

		if (originalName.toLowerCase().endsWith(ConfigManager.ZIP_EXTN)) {
			try{
				file = CADAssemblyUtil.getCADFileFromZIP(userTempDir, originalName);
			}
			catch(RMSException e){
				sendErrorResponse(e.getMessage(), request.getHeader("Accept"),response);
				return;
			}
		}
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		EvaluationHandler evalHandler = new EvaluationHandler();
		String redirectURL = "";
		try {
			redirectURL = evalHandler.validateFileRequest(user, file, sessionId, UtilMethods.getIP(request), params);
		} catch (MissingDependenciesException missingException) {
			String errorMessage = HTMLWrapperUtil.generateHTMLUnorderedList(
					RMSMessageHandler.getClientString("missingDependenciesErr"), missingException.getMissingDependencies());
			logger.error(missingException.getMessage());
			String cacheId = EvaluationHandler.setError(user, originalName, sessionId, errorMessage);
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME + "/ShowError.jsp?errId=" + cacheId;
			
		}
		response.setStatus(HttpServletResponse.SC_OK);
		fileResponse=new UploadFileResponse();
		fileResponse.setName(originalName);
		fileResponse.setViewerUrl(redirectURL);
		/*if(redirectURL.contains("errMsg")){
			redirectURL=redirectURL.substring(redirectURL.lastIndexOf("errMsg=")+7);
			fileResponse.setError(redirectURL);
		}*/
		UploadFileResponse[] fileArray=new UploadFileResponse[1];
		fileArray[0]=fileResponse;
		JsonUtil.writeJsonToResponse(fileArray, response);
		if(value.equals("")||!value.contains("application/json")){
			response.setContentType("text/plain");	
		}
	}
	
	private void sendErrorResponse(String errorMessage, String value, HttpServletResponse response){

		UploadFileResponse fileResponse=new UploadFileResponse();
		fileResponse.setName(originalName);
		fileResponse.setError(errorMessage);
		UploadFileResponse[] fileArray=new UploadFileResponse[1];
		fileArray[0]=fileResponse;
		JsonUtil.writeJsonToResponse(fileArray, response);
		if(value.equals("")||!value.contains("application/json")){
			response.setContentType("text/plain");	
		}
	}

	private byte[] readFile(Iterator<?> iter) throws RMSException{
		FileItem item = null;
		byte[] bytes = null;
		while (iter.hasNext()) {
			item = (FileItem) iter.next();			
			if (!item.isFormField()) {
				
				bytes=new byte[(int) item.getSize()];
				try {
					item.getInputStream().read(bytes);
					if(FILE.compareTo(item.getFieldName())==0){
						StringTokenizer fileTokenizer=new StringTokenizer(item.getName(),"\\");	
						while(fileTokenizer.hasMoreTokens()){
							originalName=fileTokenizer.nextToken();
						}
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
			}
		return bytes;	
	}
}