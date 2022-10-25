package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;

public abstract class AbstractCommand {
	
	private static final Logger logger = Logger.getLogger(AbstractCommand.class);
	
	public static AbstractCommand createCommand(HttpServletRequest request) {
		try {
			String uri = request.getRequestURI();
			String relativeUri = uri;
			String ctxt = GlobalConfigManager.RMS_CONTEXT_NAME + "/RMSViewer";
			String kmsCtxt = ctxt + "/KMSSettings";
			StringBuffer clsNameBuf = null;
			if (uri.startsWith(kmsCtxt)){
				relativeUri = uri.substring(kmsCtxt.length()+1);
				clsNameBuf = new StringBuffer("com.nextlabs.rms.command.kms");
			} else if(uri.startsWith(ctxt)){
				relativeUri = uri.substring(ctxt.length()+1);
				clsNameBuf = new StringBuffer("com.nextlabs.rms.command");
			}else {
				throw new Exception("Unrecognized command");
			}
			String[] paths = relativeUri.split("/");
			
			for (String str : paths) {
				clsNameBuf.append(".");
				clsNameBuf.append(str);
			}
			clsNameBuf.append("Command");
			String clsName = clsNameBuf.toString();
			if (logger.isTraceEnabled()) {
				logger.trace("Command class to be instantiated: " + clsName);
			}
			Class<?> cls = Class.forName(clsName);
			AbstractCommand serverAction = (AbstractCommand) cls.newInstance();
	        return serverAction;			
		} catch (Exception e) {
			logger.error("Error occured while creating command object: " + e.getMessage(), e);
			return null;
		}
    }
	
	public abstract void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
