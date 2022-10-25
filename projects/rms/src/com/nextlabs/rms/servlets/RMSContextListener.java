/**
 * 
 */
package com.nextlabs.rms.servlets;

import com.bluejungle.destiny.agent.pdpapi.PDPSDK;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.RMSCacheManager;
import com.nextlabs.rms.config.RMSInitializationManager;
import com.nextlabs.rms.config.RMSNightlyMaintenanceManager;
import com.nextlabs.rms.nxl.decrypt.CryptManager;
import com.nextlabs.rms.persistence.EntityManagerHelper;

import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author nnallagatla
 *
 */
public class RMSContextListener implements ServletContextListener {
	private final Logger logger = Logger.getLogger(getClass());
	private static final Object LOCK = new Object();
	private static ExecutorService execSvc = null;	
	
	/**
	 * 
	 */
	public RMSContextListener() {
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			execSvc.shutdownNow();
			CryptManager.cleanup();
			RMSCacheManager.getInstance().shutdown();
			EntityManagerHelper.closeEntityManagerFactory();
		} catch (Exception e) {
			logger.error("Error occurred while shutting down Application ",e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		EntityManagerHelper.createEntityManagerFactory();
		String webDir=arg0.getServletContext().getRealPath("/");
		RMSInitializationManager initMgr = new RMSInitializationManager();
		initMgr.initRMS(webDir);
		initializePDP();
        RMSNightlyMaintenanceManager nmMgr = new RMSNightlyMaintenanceManager();
        nmMgr.scheduleNightlyMaintenance();
        execSvc = Executors.newCachedThreadPool();
	}
	
	public static ExecutorService getExecutorSvc(){
		return execSvc;
	}

	private void initializePDP() {
		synchronized (LOCK) {
			ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				String dataDir = GlobalConfigManager.getInstance().getDataDir();
				final String dpcPath = dataDir + (dataDir.endsWith("\\") || dataDir.endsWith("/") ? "" : File.separator)
						+ GlobalConfigManager.EMBEDDEDJPC_FOLDER_NAME;
				File f = new File(dpcPath);
				if ((f.exists()) && (f.isDirectory())) {
					Thread t = new Thread("Thread-embedded-PDP") {
						public void run() {
							try {
								logger.info("Initializing Embedded JavaPC: " + dpcPath);
								PDPSDK.initializePDP(dpcPath);
								logger.info("Embedded JavaPC initialized successfully");
							} catch (Exception e) {
								logger.error("Unable to initialize PC: " + e.getMessage(), e);
							}
						}
					};
					t.start();
				} else {
					logger.error("Embedded JavaPC folder is not found: " + f.getAbsolutePath());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				Thread.currentThread().setContextClassLoader(originalClassLoader);
			}
		}
	}
}