package com.nextlabs.rms.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;

public class LogoutCommand extends AbstractCommand{

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		StringBuilder redirectUrl = new StringBuilder(GlobalConfigManager.RMS_CONTEXT_NAME).append(AuthFilter.LOGIN_PAGE).append("?action=logout");
		response.sendRedirect(redirectUrl.toString());
		return;
		
	}

}
