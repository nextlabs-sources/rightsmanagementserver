package com.nextlabs.rms.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class LoginServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(LoginServlet.class);
	
	private static final long serialVersionUID = 2727571269678015970L;

	public void doGet(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);

	}

	private void processReq(HttpServletRequest request, HttpServletResponse response){
		try {
//			ODRMServlet serv = new ODRMServlet();
//			serv.processReq(request, response);
		}catch (Exception e) {
			logger.error("Error occurred while processing WrapperSvc Request",e);
		}
	}
}






