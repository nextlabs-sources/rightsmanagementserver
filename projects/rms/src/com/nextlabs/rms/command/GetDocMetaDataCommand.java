package com.nextlabs.rms.command;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.conversion.DocumentMetaData;
import com.nextlabs.rms.conversion.RMSViewerContentManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;

public class GetDocMetaDataCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(GetDocMetaDataCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String documentId=URLDecoder.decode(request.getParameter("documentId"), "UTF-8");
		String sessionId=request.getSession().getId();
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		logger.debug("Getting metadata for documentId:"+documentId);
		RMSViewerContentManager contentMgr = RMSViewerContentManager.getInstance();
		DocumentMetaData metaData = contentMgr.getMetaData(documentId,sessionId,user.getUserName());
		JsonUtil.writeJsonToResponse(metaData, response);
	}

}
