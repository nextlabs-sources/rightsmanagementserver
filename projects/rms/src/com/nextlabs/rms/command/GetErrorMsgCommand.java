package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.conversion.RMSViewerContentManager;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.sharedutil.OperationResult;

public class GetErrorMsgCommand extends AbstractCommand{
	
	private static Logger logger = Logger.getLogger(GetErrorMsgCommand.class);
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String errMsg=null;
		String errId = request.getParameter("errorId");
		String sessionId=request.getSession().getId();
		RMSUserPrincipal userPrincipal=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		RMSViewerContentManager contentMgr = RMSViewerContentManager.getInstance();
		try {
			errMsg = contentMgr.getErrorMsg(errId, sessionId, userPrincipal.getUserName());
		} catch (Exception e) {
			logger.error("Error occurred while getting error message with error ID:"+errId);
			errMsg=RMSMessageHandler.getClientString("genericViewerErr");
		}
		OperationResult result = new OperationResult();
		result.setResult(true);
		result.setMessage(errMsg);
		JsonUtil.writeJsonToResponse(result, response);
	}
}
