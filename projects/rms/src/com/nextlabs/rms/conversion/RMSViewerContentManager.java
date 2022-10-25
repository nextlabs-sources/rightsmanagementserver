package com.nextlabs.rms.conversion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import net.sf.ehcache.Ehcache;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.eval.EvalRequest;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.json.PrintFileUrl;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;
import com.nextlabs.rms.visualization.GenericVisManager;
import com.nextlabs.rms.visualization.IVisManager;
import com.nextlabs.rms.visualization.PDFVisManager;
import com.nextlabs.rms.visualization.VisManagerFactory;

public class RMSViewerContentManager {
	
	private static final Logger logger = Logger.getLogger(RMSViewerContentManager.class);

	private static final RMSViewerContentManager instance = new RMSViewerContentManager();
	
	private RMSViewerContentManager(){	
	}
	
	public static RMSViewerContentManager getInstance(){
		return instance;
	}
	
	public DocumentMetaData getMetaData(String documentId, String sessionId, String userName) {
		FileCacheId fileCacheId = new FileCacheId(sessionId, userName, documentId);
		CachedFile cachedFile = null;
		DocumentMetaData metaData;
		try {
			cachedFile = RMSCacheManager.getInstance().getCachedFile(fileCacheId);
			int numPages=-1;		
			IVisManager visManager = VisManagerFactory.getInstance().getVisManager(cachedFile.getFileName());
			if (visManager instanceof GenericVisManager
					|| (visManager instanceof PDFVisManager && CachedFile.ContentType._2D.equals(cachedFile.getContentType()))) {
				numPages = ImageProcessor.getInstance().getNumPages(cachedFile.getFileContent(), cachedFile.getFileName());
			}
			metaData = new DocumentMetaData();
			metaData.setDisplayName(cachedFile.getFileName());
			metaData.setPrintAllowed(cachedFile.isPrintAllowed());
			metaData.setNumPages(numPages);
			metaData.setPMIAllowed(cachedFile.isPMIAllowed());
			metaData.setRepoId(cachedFile.getRepoId());
			metaData.setRepoType(cachedFile.getRepoType());
			metaData.setFilePath(cachedFile.getFilePath());
			metaData.setWatermark(cachedFile.getWaterMark());
			metaData.setRightsLocaleMap(getRightsLocaleMap());
			metaData.setTagsMap(cachedFile.getTagMap());
			metaData.setLastModifiedDate(cachedFile.getLastModifiedDate());
			metaData.setFileSize(cachedFile.getFileSize());
			metaData.setRepoName(cachedFile.getRepoName());
			metaData.setDisplayPath(cachedFile.getDisplayPath());
			
		} catch (Exception e) {
			logger.error("Unable to get metadata (Document ID: " + documentId + "): " + e.getMessage(), e);
			metaData = new DocumentMetaData();
			metaData.setErrMsg(e.getMessage());
		}
		return metaData;
	}
	
	private Map<String, String> getRightsLocaleMap(){
		Map<String, String> rightsMap = new HashMap<String, String>();
		for (String str : EvalRequest.ALL_RIGHTS){
			rightsMap.put(str, RMSMessageHandler.getClientString(str));
		}
		return rightsMap;
	}
	
	public boolean removeDocumentFromCache(String documentId, String sessionId, String userName){
		logger.debug("About to remove documentID: "+documentId +" from cache");    	
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_FILECONTENT);
		FileCacheId fileCacheId=new FileCacheId(sessionId, userName, documentId);
		boolean res = cache.remove(fileCacheId);
		logger.debug("Result of removal for documentID: "+documentId +" is:"+res);
		return res;
	}
	
	public boolean removeDocumentFromTemp(String documentPath){
		logger.debug("About to remove documentID: "+documentPath +" from cache"); 
		File pdffile = new File(GlobalConfigManager.getInstance().getWebDir(),documentPath);
		if(pdffile.delete()){
			logger.debug("Result of removal for documentID: "+documentPath +" is:"+true);
			return true;
		}else{
			logger.debug("Result of removal for documentID: "+documentPath +" is:"+false);
			return false;
		}
		
	}
	
	public String getErrorMsg(String documentId, String sessionId, String userName) {
		String errMsg = null;
		FileCacheId fileCacheId=new FileCacheId(sessionId, userName, documentId);
		try {
			CachedFile cachedFile = RMSCacheManager.getInstance().getCachedFile(fileCacheId);
			errMsg = cachedFile.getErrorMsg();
		} catch (RMSException e) {
			errMsg = RMSMessageHandler.getClientString("unAuthView");
		}
		return errMsg;
	}

	public DocumentContent getContent(String documentId, int[] pageNumArr, String domainName, String userName, String sessionId) throws RMSException {
		CachedFile cachedFile = null;
		FileCacheId fileCacheId=new FileCacheId(sessionId, userName, documentId);
		try {
			cachedFile = RMSCacheManager.getInstance().getCachedFile(fileCacheId);
		} catch (RMSException e) {
			logger.error("Unable to find document in cache (Document ID: " + documentId + ")");
			DocumentContent content = new DocumentContent();
			content.setErrMsg(RMSMessageHandler.getClientString("notFoundInCacheErr"));
			return content;
		}

		logger.debug("Getting content from document:" +cachedFile.getFileName() + " with documentId:"+documentId);
		DocumentContent content = ImageProcessor.getInstance().getDocContent(cachedFile.getFileContent(), pageNumArr, domainName, userName, cachedFile.getWaterMark());
		return content;		
	}
	
	public PrintFileUrl generatePDF(String documentId,String folderPath,String domainName, String userName,String sessionId) throws RMSException {
		CachedFile cachedFile = null;
		FileCacheId fileCacheId=new FileCacheId(sessionId, userName, documentId);
		try {
			cachedFile = RMSCacheManager.getInstance().getCachedFile(fileCacheId);
		} catch (RMSException e) {
			logger.error("Unable to find document in cache (Document ID: " + documentId + ")", e);
		}
		PrintFileUrl url = ImageProcessor.getInstance().convertFileToPDF(cachedFile.getFileContent(),folderPath, cachedFile.getFileName(),domainName, userName,cachedFile.getWaterMark());
		return url;      
	}
	
	public boolean checkPrintPermission(String documentId,String sessionId,String userName) {
		CachedFile cachedFile = null;
		FileCacheId fileCacheId=new FileCacheId(sessionId, userName, documentId);
		try {
			cachedFile = RMSCacheManager.getInstance().getCachedFile(fileCacheId);
		} catch (RMSException e) {
			logger.error("Unable to find document in cache (Document ID: " + documentId + ")", e);
		}
		if(cachedFile.isPrintAllowed()){
			return true;
		}
		return false;      
	}

}
