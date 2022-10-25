package com.nextlabs.rms.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.wrapper.WrapperServiceProcessor;

public class WrapperSvcServlet extends HttpServlet {

	private static final long serialVersionUID = -1478649171707845243L;
	
	private static Logger logger = Logger.getLogger(WrapperSvcServlet.class);
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}

	private void processReq(HttpServletRequest request, HttpServletResponse response){
		try {
				WrapperServiceProcessor htmlWrapper=new WrapperServiceProcessor();
				long startTime=System.currentTimeMillis();
				htmlWrapper.getHTMLWithFile(request,response);	
				long difference=System.currentTimeMillis()-startTime;
				logger.info("Time taken for processing WrapperSvc request:"+difference + " ms");				
		} catch (Exception e) {
			logger.error("Error occurred while processing WrapperSvc Request ",e);
		}
	}
	

}

