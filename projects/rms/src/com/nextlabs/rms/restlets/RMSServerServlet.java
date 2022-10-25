package com.nextlabs.rms.restlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.restlet.ext.servlet.ServerServlet;

public class RMSServerServlet extends ServerServlet {

	private static final long serialVersionUID = -6862372119053535118L;
	
	private static ThreadLocal<HttpSession> currSessionRef = new ThreadLocal<HttpSession>();
	
	@Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		currSessionRef.set(request.getSession());
		super.service(request, response);
		currSessionRef.remove();
    }
	
	public static HttpSession getCurrentSessionRef(){
		return currSessionRef.get();
	}

}
