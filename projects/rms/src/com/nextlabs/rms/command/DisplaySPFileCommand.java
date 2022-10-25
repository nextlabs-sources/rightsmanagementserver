package com.nextlabs.rms.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.SharePointClient;
import com.nextlabs.rms.util.HTTPUtil;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;

public class DisplaySPFileCommand extends AbstractCommand{

	private static Logger logger = Logger.getLogger(DisplaySPFileCommand.class);
    

	@Override
	public void doAction(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);
		
		String accessToken =(String) request.getSession().getAttribute(ConfigManager.SP_OAUTH_TOKEN);
		String redirectURL = null;
		if(accessToken==null){
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("spAuthErr");
		}else{
			String userTempFolder = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
			String sessionId=request.getSession().getId();
			File folder = new File(userTempFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
			String outputPath =  folder.getAbsolutePath();
			File fileFromSP=downloadFile(accessToken,request,outputPath);
			if(fileFromSP!=null){
				RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
				EvaluationHandler evalHandler = new EvaluationHandler();
				redirectURL = evalHandler.validateFileRequest(user, fileFromSP,sessionId,UtilMethods.getIP(request), params);
				String popup = request.getParameter("popup");
				if(popup!=null&&popup.equalsIgnoreCase("true")){
					redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/OpenLink.jsp?redirectURL="+redirectURL;
				}
			}else{
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("fileDownloadErr");				
			}
		}
		logger.debug("Redirecting to URL:"+redirectURL);
		response.sendRedirect(redirectURL);
	}

	

	private File downloadFile(String accessToken, HttpServletRequest req, String outputPath){
		CloseableHttpResponse response = null;
		try{ 
//			String path=URLDecoder.decode(req.getParameter("path"),"UTF-8");
			String path=req.getParameter("path");
			String spServer=URLDecoder.decode(req.getParameter("siteName"),"UTF-8");
			String protocol= URLDecoder.decode(req.getParameter("protocol"),"UTF-8");
			String fileName=path.substring(path.lastIndexOf('/')+1);
			String folder=path.substring(0,path.lastIndexOf('/'));
			String[] folderArray=folder.split("/");
			String[] siteArray=spServer.split("/");
			boolean overlap=false;
			int i=0;
			for(;i<siteArray.length;i++){
				if(siteArray[i].equals(folderArray[1])){
					overlap = checkOverlap(i,folderArray,siteArray);
					if(overlap){
						break;
					}
				}
			}
			String filteredFolderPath="";
			for(int k=siteArray.length-i+1;k<folderArray.length;k++){
				filteredFolderPath+=folderArray[k]+"/";
			}
			filteredFolderPath=filteredFolderPath.substring(0,filteredFolderPath.length()-1);
			CloseableHttpClient client = HTTPUtil.getHTTPClient();
			URL url=SharePointClient.convertToURLEscapingIllegalCharacters(protocol+"://"+spServer+"/_api/web/GetFolderByServerRelativeUrl('"+filteredFolderPath+"')/Files('"+fileName+"')/$value");
			logger.debug("URL to download file: "+url.toString());
			HttpGet request = new HttpGet(url.toString());
			request.addHeader("Authorization", "Bearer "+accessToken);
			request.addHeader("accept", "application/json;odata=verbose");
			response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.debug("Response Code : " + statusCode);
			if(statusCode>=400){
				logger.error("Error occurred while downloading file");
				return null;
			}
 			File file =new File(outputPath, fileName);
			HttpEntity entity = response.getEntity();
	        FileOutputStream fileOS  = new FileOutputStream(file);
	        entity.writeTo(fileOS);
	        EntityUtils.consume(entity);
	        fileOS.flush();
	        fileOS.close();
	        return file;
		}catch(Exception e){
			logger.error("Error occurred while downloading file from SharePoint", e);
		}
		finally{
			try {
				if(response!=null){
					response.close();					
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return null;
	}
	
	private static boolean checkOverlap(int i, String[] folderArray, String[] siteArray) {
		int j=1;
		for(;i<siteArray.length;i++){
			if(j>=folderArray.length){
				return false;
			}
			if(!siteArray[i].equals(folderArray[j++])){
				return false;
			}
		}
		return true;
	}
}
