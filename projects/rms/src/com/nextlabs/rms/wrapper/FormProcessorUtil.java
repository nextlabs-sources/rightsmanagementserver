package com.nextlabs.rms.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;

public class FormProcessorUtil {
	
	private static Logger logger = Logger.getLogger(FormProcessorUtil.class);

	public static Iterator<?> getFormItemsFromReq(HttpServletRequest request) throws FileUploadException,
			Exception, IOException, FileNotFoundException {
		GlobalConfigManager configManager=GlobalConfigManager.getInstance();
		int thresholdSize=configManager.getIntProperty(GlobalConfigManager.FILE_UPLD_THRESHOLD_SIZE);
		long requestSize=configManager.getIntProperty(GlobalConfigManager.FILE_UPLD_MAX_REQUEST_SIZE);
		
		if(thresholdSize<=0){
			thresholdSize=WrapperServiceProcessor.THRESHOLD_SIZE;
		}
		if(requestSize<=0){
			requestSize=WrapperServiceProcessor.REQUEST_SIZE;
		}
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(thresholdSize);
		String userTempDir = (String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR);
		if(userTempDir==null || userTempDir.length()==0){
			Random r = new Random();
			long random = r.nextLong();			
			userTempDir = GlobalConfigManager.getInstance().getTempDir() + System.currentTimeMillis() + random;
		}
		File tmpDir = new File(userTempDir);
		factory.setRepository(tmpDir);
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(requestSize);
		// parses the request's content to extract file data
		List<?> formItems = upload.parseRequest(request);
		Iterator<?> iter = formItems.iterator();
		return iter;
	}
	
	public static File writeFileToDisk(byte[] bytes, String destFilePath) {
		File file = null;
		FileOutputStream fos = null;
		try{
			file=new File(destFilePath);	
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			fos = new FileOutputStream(file);
			fos.write(bytes);
		}
		catch(Exception e) {
			logger.error("Error occurred while writing encrypted file to disk ",e);
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				logger.error("Error occurred while closing stream", e);
			}
		}
		return file;
	}
	
}
