package com.nextlabs.rms.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.command.CommandExecutor;

public class RMSServlet extends HttpServlet {

	private static final long serialVersionUID = -5553255427500833904L;

	private static Logger logger = Logger.getLogger(RMSServlet.class);
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}

	public void processReq(HttpServletRequest request, HttpServletResponse response) {
		try {
			CommandExecutor.executeCommand(request, response);				
		} catch (Exception e) {
			logger.error("Error occured while processing request",e);
		}
	}
}
