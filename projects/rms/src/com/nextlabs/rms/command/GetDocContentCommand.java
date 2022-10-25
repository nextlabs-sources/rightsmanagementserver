package com.nextlabs.rms.command;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.conversion.DocumentContent;
import com.nextlabs.rms.conversion.RMSViewerContentManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;

public class GetDocContentCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(GetDocContentCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) {
		DocumentContent content = new DocumentContent();
		String documentId=null;		
		try {
			documentId = URLDecoder.decode(request.getParameter("documentId"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Error occurred while getting decoding documentId: " + documentId);
			content.setErrMsg(RMSMessageHandler.getClientString("genericViewerErr"));
		}
		String sessionId=request.getSession().getId();
		String[] pageNumStrArr = request.getParameterValues("pageNum[]");
		if(pageNumStrArr!=null && pageNumStrArr.length>0 && documentId!=null){
			int[] pgNum = new int[pageNumStrArr.length]; 
			for(int i=0;i<pageNumStrArr.length;i++){
				pgNum[i] = Integer.valueOf(pageNumStrArr[i]);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Getting content for documentId: " + documentId);
			}
			RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			RMSViewerContentManager contentMgr = RMSViewerContentManager.getInstance();
			try {
				content = contentMgr.getContent(documentId, pgNum, userPrincipal.getDomain(), userPrincipal.getUserName(),sessionId);
			} catch (Exception e) {
				logger.error("Error occurred while getting contents of page number: " + Arrays.toString(pgNum) + " of document: "+documentId);
				content.setErrMsg(RMSMessageHandler.getClientString("genericViewerErr"));
			}
		}
		JsonUtil.writeJsonToResponse(content, response);
	}

}