package com.nextlabs.rms.restlets;

import java.util.Map;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.filters.AuthFilter;

public class GetUserNameResource extends AbstractResource {

	@Override
	public String processRequest(Map json, String method) {
		RMSUserPrincipal principal = (RMSUserPrincipal)RMSServerServlet.getCurrentSessionRef().getAttribute(AuthFilter.LOGGEDIN_USER);
		String name = principal.getUserName();
		return name;
	}

}
