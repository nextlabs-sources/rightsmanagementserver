
package com.nextlabs.rms.wrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.wrapper.util.HTMLWrapperUtil;

public class WrapperServiceProcessor {
	
	public static final int THRESHOLD_SIZE = 1024 * 1024 * 70; // 70MB
	
	public static final long REQUEST_SIZE = 1024 * 1024 * 150L; // 150MB
	
	private static Logger logger = Logger.getLogger(WrapperServiceProcessor.class);
	
	private String originalName;
	
	public void getHTMLWithFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		Iterator<?> iter=FormProcessorUtil.getFormItemsFromReq(request);
		FileItem item= getFileItem(iter, response);
		if(item==null){
			return;
		}

		String protocol = getProtocol(request);
		String hostName = getHostName(request);
		String portNum = getPortNumber(request);				
		byte[] encoded = HTMLWrapperUtil.getEncodedBytes(item.get());
		String HTMLStr = HTMLWrapperUtil.generateHTML(encoded, protocol, hostName, portNum, GlobalConfigManager.RMS_CONTEXT_NAME, originalName);
		writeHTMLContentToResponse(HTMLStr,response);
		response.setStatus(HttpServletResponse.SC_OK);		
	}
	
	private String getProtocol(HttpServletRequest request) {
		StringTokenizer st=new StringTokenizer(request.getProtocol(),"/");
		String protocol=st.nextToken();
		return protocol.toLowerCase();
	}

	private String getHostName(HttpServletRequest request) {
		StringTokenizer st=new StringTokenizer(request.getRequestURL().toString(),"/");
		st.nextToken();
		String hostName = st.nextToken();
		if(hostName.contains(":")){
			String[] arr = hostName.split(":");
			hostName = arr[0];
		}
		return hostName;
	}

	private String getPortNumber(HttpServletRequest request) {
		return ""+request.getLocalPort();
	}

	private void writeHTMLContentToResponse(String HTMLStr,HttpServletResponse response) {
		response.setContentType("text/html");
		//Look at this later
		//response.setCharacterEncoding(arg0);
		byte[] arBytes = HTMLStr.getBytes();
		OutputStream os = null;
		try{
			os = response.getOutputStream();
			os.write(arBytes);
			os.flush();
		}
		catch(Exception e){
			logger.error("Error occurred while writing bytes to Output Stream",e);
		}
		finally
		{
			try{
				os.close();
			}catch(Exception e){
				logger.error("Error occurred while writing bytes to Output Stream",e);
			}
		}
	}
	
	private FileItem getFileItem(Iterator<?> iter,
			HttpServletResponse response) throws Exception, IOException,
			FileNotFoundException {
		FileItem item = null;
		int count = 0;
		String name = null;
		FileItem selectedItem = null;
		while (iter.hasNext()) {
			item = (FileItem) iter.next();
			logger.debug(item.getName());
			if (!item.isFormField()) {
				name = item.getName();
				selectedItem = item;
			} else {
				/*
				if (!HTMLWrapperUtil.checkAllowedFormat(name)) {
					StringTokenizer extTokenizer=new StringTokenizer(name,".");
					String extn="";
					while(extTokenizer.hasMoreTokens())
					{
						extn=extTokenizer.nextToken();
					}
					response.sendError(400, "ERROR: File extension '" + extn + "' not supported");
					return null;
				}*/
				count++;
				originalName = name;
			}
		}
		if (count == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ERROR: No files found in the request");
			return null;
		}
		else if(count>1){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ERROR: Multiple files found in the request");
			return null;
		}
		return selectedItem;
	}


}