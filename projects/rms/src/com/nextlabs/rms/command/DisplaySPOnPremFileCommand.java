package com.nextlabs.rms.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.SPViewRequest;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.SharePointClient;
import com.nextlabs.rms.util.HTTPUtil;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;

public class DisplaySPOnPremFileCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(DisplaySPOnPremFileCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String redirectURL = null;
		SPViewRequest spViewRequest = null;
		try {
			String requestData = request.getParameter("data");
			if (requestData == null){
				logger.error("Missing required parameters while viewing sharepoint onpremise file");
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("500_error_msg");
				response.sendRedirect(redirectURL);
				return;
			} 
			Gson gson = new Gson();
			spViewRequest = gson.fromJson(requestData, SPViewRequest.class);
			logger.debug("path:" + spViewRequest.getPath());
			logger.debug("siteName:" + spViewRequest.getSiteName());
			logger.debug("uid:" + spViewRequest.getUid());
			logger.debug("userName:" + spViewRequest.getUserName());
			RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);	
			if(spViewRequest.getToken() == null || spViewRequest.getPath() == null || spViewRequest.getSiteName() == null || spViewRequest.getUid() == null){
				logger.error("Missing required parameters while viewing sharepoint onpremise file");
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("500_error_msg");
				response.sendRedirect(redirectURL);
				return;
			} 
			RMSUserPrincipal user = createUserPrincipal(request, spViewRequest);
			String userTempFolder = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
			String sessionId=request.getSession().getId();
			File folder = new File(userTempFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
			String outputPath =  folder.getAbsolutePath();
			File fileFromSP=downloadFile(request, outputPath, spViewRequest);
			if(fileFromSP!=null){
				EvaluationHandler evalHandler = new EvaluationHandler();
				redirectURL = evalHandler.validateFileRequest(user, fileFromSP,sessionId,UtilMethods.getIP(request), params);
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/OpenLink.jsp?redirectURL="+redirectURL+"&closeWindow=true";
			} else{
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("fileDownloadErr");                                                      
			}	

		} catch (Exception e ) {
			logger.error("Error occured while displaying Sharepoint on premise file", e);
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("500_error_msg");
		}	
		logger.debug("Redirecting to URL:"+redirectURL);
		response.sendRedirect(redirectURL);
	}

	private RMSUserPrincipal createUserPrincipal(HttpServletRequest request, SPViewRequest spViewRequest) {
		RMSUserPrincipal principal =new RMSUserPrincipal();
		principal.setUid(spViewRequest.getUid());
		principal.setUserAttributes(spViewRequest.getUserAttributes());
		String userName = spViewRequest.getUserName() != null ? spViewRequest.getUserName() : spViewRequest.getUid();
		principal.setUserName(userName);
		AuthFilter.addSessionParameters(request.getSession(), principal);
		return principal;
	}



	private File downloadFile(HttpServletRequest req, String outputPath, SPViewRequest sprequest){
		CloseableHttpResponse response = null;
		FileOutputStream fileOS = null;
		try{ 
			String path = sprequest.getPath();
			String spServer=sprequest.getSiteName();
			String protocol = "https";
			if(spServer.startsWith("https://")){
				spServer=spServer.trim().substring(8);
			}
			else if(spServer.startsWith("http://")){
				protocol = "http";
				spServer=spServer.trim().substring(7);
			}                                              
			String fileName=path.substring(path.lastIndexOf('/')+1);
			String folder=path.substring(0,path.lastIndexOf('/'));
			logger.debug("SharePoint FileName:"+fileName);
			logger.debug("SharePoint folder:"+folder);
			CloseableHttpClient client = HTTPUtil.getHTTPClient();
			URL url=SharePointClient.convertToURLEscapingIllegalCharacters(protocol+"://"+spServer+"/_api/web/GetFolderByServerRelativeUrl('"+folder+"')/Files('"+fileName+"')/$value");
			logger.debug("URL to download file: "+url.toString());
			HttpGet request = new HttpGet(url.toString());
			request.addHeader("Cookie", "FedAuth="+sprequest.getToken()); 
			request.addHeader("binaryStringResponseBody", "true");
			request.addHeader("accept", "application/json;odata=verbose");
			request.addHeader("X-FORMS_BASED_AUTH_ACCEPTED", "f");
			response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.debug("Response Code : " + statusCode);		
			if(statusCode>=400){
				logger.debug("Reason Phrase : " + response.getStatusLine().getReasonPhrase());
				logger.error("Error occurred while downloading file");
				return null;
			}
			File file =new File(outputPath, fileName);
			HttpEntity entity = response.getEntity();
			fileOS  = new FileOutputStream(file);
			entity.writeTo(fileOS);
			EntityUtils.consume(entity);
			fileOS.flush();
			return file;
		}catch(Exception e){
			logger.error("Error occurred while downloading file from SharePoint", e);
		}
		finally{
			try {
				if(response!=null){
					response.close();                                                                             
				}
				if(fileOS!=null){
					fileOS.close();                                                                            
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return null;
	}
}
