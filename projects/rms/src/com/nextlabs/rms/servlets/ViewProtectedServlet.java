package com.nextlabs.rms.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.wrapper.FormProcessorUtil;

public class ViewProtectedServlet extends HttpServlet {
	private static final long serialVersionUID = 1200391221606974161L;
	private static final String FILE="file";
	private String originalName = "";
	private static Logger logger = Logger.getLogger(ViewProtectedServlet.class);
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		try {
			processReq(request, response);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response){
		try {
			processReq(request, response);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void processReq(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		Iterator<?> iter=FormProcessorUtil.getFormItemsFromReq(request);
		byte[] bytes=readFile(iter,response);
		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		writeFileToDisk(bytes, userTempDir);
		response.setStatus(HttpServletResponse.SC_OK);
		String redirectURL=GlobalConfigManager.RMS_CONTEXT_NAME+"/DocViewer.jsp?documentid="+originalName;
		response.sendRedirect(redirectURL);
	}

	private byte[] readFile(Iterator<?> iter, HttpServletResponse response) {
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
						while(fileTokenizer.hasMoreTokens())
						{
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
	
	private void writeFileToDisk(byte[] bytes, String destDirPath) {
		FileOutputStream fos = null;
		try{
			File file=new File(destDirPath+File.separator+originalName);	
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(bytes);
		}catch(Exception e){
			logger.error("Error occurred while writing encrypted file to disk ",e);
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
}
