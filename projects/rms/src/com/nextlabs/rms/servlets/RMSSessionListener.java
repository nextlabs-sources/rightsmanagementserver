package com.nextlabs.rms.servlets;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.repository.RepositoryManager;

public class RMSSessionListener implements HttpSessionListener {

	private Logger logger = Logger.getLogger(RMSSessionListener.class);
	
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		try{
			HttpSession session = sessionEvent.getSession();
			List<String> repoCacheIds=(List<String>) session.getAttribute(RepositoryManager.REPO_CACHE_ID_LIST);
			boolean res = RepositoryManager.getInstance().deleteRepositoryListFromCache(repoCacheIds);
			String userTempDir = (String)session.getAttribute(AuthFilter.USER_TEMP_DIR);
			if(userTempDir==null||userTempDir.length()==0){
				return;
			}
			File tempDir = new File(userTempDir);
			if(tempDir.exists()){
				FileUtils.deleteDirectory(tempDir);
				logger.debug("Deleted temp dir due to session time out:"+userTempDir);
			}
			String webDir = GlobalConfigManager.getInstance().getWebDir();
			File tempWebDir = new File(webDir, "temp"+File.separator+session.getId());
			if(tempWebDir.exists()){
				FileUtils.deleteDirectory(tempWebDir);
				logger.debug("Deleted temp dir due to session time out:"+tempWebDir.getAbsolutePath());
			}
		}catch(Exception e){
			logger.error("Error occurred while cleaning up user temp dir", e);
		}
	}

}
