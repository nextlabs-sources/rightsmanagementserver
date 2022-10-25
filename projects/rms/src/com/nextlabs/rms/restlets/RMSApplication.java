package com.nextlabs.rms.restlets;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RMSApplication extends Application {
	
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/GetUserName",GetUserNameResource.class );
		return router;
	}

}
