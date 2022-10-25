package com.nextlabs.rms.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSTCLoginContext;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ADServerInfo;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.util.RepositoryFileUtil;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.RepositoryFileUtil.RepositoryFileParams;
import com.nextlabs.rms.teamcenter.TCSession;
import com.teamcenter.services.strong.administration.PreferenceManagementService;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.FSCStreamingUtility;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ImanFile;

public class DisplayTCFileCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(DisplayTCFileCommand.class);

	// TC preferences
	private static final String PREF_FMS_BOOTSTRAP_URLS = "Fms_BootStrap_Urls";

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		HttpSession httpSession = request.getSession(true);
		
		RepositoryFileParams params = RepositoryFileUtil.getRepositoryFileQueryStringParams(request);
		String tcHost = request.getParameter("tchost");
		String userId = request.getParameter("userid");
		String token = request.getParameter("token");
		String fileUid = request.getParameter("fileuid");
		String domain = request.getParameter("domain");
		String popup = request.getParameter("popup");
		String redirectURL = null;

		logger.debug("tcSOAHost:" + tcHost);
		logger.debug("userid:" + userId);
		logger.debug("fileuid:" + fileUid);
		logger.debug("domain:" + domain);
		logger.debug("popup:" + popup);

		if (token == null) {
			redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME
					+ "/ShowError.jsp?errMsg="
					+ RMSMessageHandler.getClientString("tcAuthErr");
		} else {
			// Initiate TC session
			TCSession tcSession = new TCSession(
						tcHost, userId, token, "", "", "");

			// Initiate RMS login context with directory service
			RMSUserPrincipal user;
			Object objUser = request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			
			if (objUser == null) {
				user = createLoginContext(domain, userId, httpSession);
			} else {
				logger.debug("Reusing existing loginContext from session");
				user = (RMSUserPrincipal) objUser;
			}
			
			if (user == null) {
				redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME
						+ "/ShowError.jsp?errMsg="
						+ RMSMessageHandler.getClientString("tcDSAuthErr");
			} else {
				String userTempFolder = (String) request.getSession().getAttribute(
						AuthFilter.USER_TEMP_DIR);
				String sessionId = request.getSession().getId();
				
				File folder = new File(userTempFolder);
				if(!folder.exists()){
					folder.mkdirs();
				}
				String outputPath =  folder.getAbsolutePath();
	
				File fileFromTC = downloadFile(tcSession, tcHost, fileUid,
						outputPath);
				
				if (fileFromTC == null) {
					redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME
							+ "/ShowError.jsp?errMsg="
							+ RMSMessageHandler.getClientString("tcDownloadFileErr");
				} else {
					EvaluationHandler evalHandler = new EvaluationHandler();
					redirectURL = evalHandler.validateFileRequest(user, fileFromTC, sessionId, UtilMethods.getIP(request), params);
					
					if (popup != null && popup.equalsIgnoreCase("true")) {
						redirectURL = GlobalConfigManager.RMS_CONTEXT_NAME
								+ "/OpenLink.jsp?redirectURL=" + redirectURL;
					}
				}
			}

		}

		logger.debug("Redirecting to URL:" + redirectURL);
		response.sendRedirect(redirectURL);
	}

	private File downloadFile(TCSession tcSession, String tchost,
			String fileuid, String outputPath) {
		String[] fileUid = { fileuid };
		ImanFile[] manFiles = new ImanFile[1];

		logger.debug("Getting TC DataManagementService");
		DataManagementService dmService = DataManagementService
				.getService(tcSession.getConnection());
		
		try {
			logger.debug("Loading ImanFile object: " + fileUid);
			ServiceData serviceData = dmService.loadObjects(fileUid);
			String[] pros = new String[] { "original_file_name" };
			
			logger.debug("Loading ImanFile object's properties");
			dmService.getProperties(
					new ModelObject[] { serviceData.getPlainObject(0) }, pros);
			ImanFile file = (ImanFile) serviceData.getPlainObject(0);
			
			manFiles[0] = file;

			logger.debug("Retrieving BootstrapFSCURIs");
			String[] assignedFSCURIs = {};
			String[] bootstrapFSCURIs = getBootstrapFSCURIs(tcSession);
			
			if (logger.isDebugEnabled()) {
				StringBuffer strBuffer = new StringBuffer();
				for (String value : bootstrapFSCURIs) {
					strBuffer.append(value + ",");
				}

				logger.debug("BootstrapFSCURIs:" + strBuffer.toString());
			}

			logger.debug("Initializing FSCStreamingUtility");
			FSCStreamingUtility fsc = new FSCStreamingUtility(
					tcSession.getConnection(), InetAddress.getLocalHost()
							.getHostAddress(), assignedFSCURIs,
					bootstrapFSCURIs);
			
			OutputStream[] outputStream = new OutputStream[1];
			File desFile = new File(outputPath, file.get_original_file_name());
			outputStream[0] = new FileOutputStream(desFile);
			
			logger.debug("Downloading the file");
			fsc.download(manFiles, outputStream);
			logger.debug("Download complete for " + file.get_original_file_name() + " [" + desFile.length() + "]");

			// Close FMS connection since done
			outputStream[0].close();			
			fsc.term();
			
			return desFile;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return null;
	}

	private String[] getBootstrapFSCURIs(TCSession tcSession) {
		PreferenceManagementService prefService = PreferenceManagementService
				.getService(tcSession.getConnection());

		GetPreferencesResponse prefResponse = prefService.getPreferences(
				new String[] { PREF_FMS_BOOTSTRAP_URLS }, false);

		if (prefResponse.response.length <= 0) {
			logger.error("No preferences response for "
					+ PREF_FMS_BOOTSTRAP_URLS);
			return new String[] {};
		} else {
			// expecting only one response
			if (prefResponse.response[0] != null
					&& prefResponse.response[0].values != null) {

				return prefResponse.response[0].values.values;
			} else {
				if (prefResponse.response[0] == null) {
					logger.error("Preferences response is NULL for "
							+ PREF_FMS_BOOTSTRAP_URLS);
				} else if (prefResponse.response[0].values == null) {
					logger.error("Preferences response value is NULL for "
							+ PREF_FMS_BOOTSTRAP_URLS);
				}

				return new String[] {};
			}
		}
	}
	
	private RMSUserPrincipal createLoginContext(String domain, String userid,
			HttpSession httpSession) throws Exception {
		ADServerInfo adSrvr = GlobalConfigManager.getInstance().getAdServerInfo(
				domain);

		if (adSrvr == null) {
			return null;
		}

		RMSTCLoginContext ctxt = new RMSTCLoginContext(adSrvr);
		try {
			boolean res = ctxt.authenticate(userid);
			
			logger.info("Authentication result for user " + userid + " : " + res);
		} catch (Exception ex) {
			logger.info("Authentication resulr for user " + userid + " failed with exception: " + ex.getMessage());
			return null;
		}
		
		
		AuthFilter.addSessionParameters(httpSession, ctxt.getUserPrincipal());

		return ctxt.getUserPrincipal();
	}

}
