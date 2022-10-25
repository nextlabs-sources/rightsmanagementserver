package com.nextlabs.kms.controller.service;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.servlet.ServletAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RestletController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private Restlet root;
	@Autowired
	private Context context;
	@Autowired
	private ServletContext servletContext;
	private ServletAdapter adapter;

	public RestletController() {
	}

	@PostConstruct
	public final void postConstruct() {
		final Application application = new Application(this.context);
		application.setInboundRoot(this.root);
		this.adapter = new ServletAdapter(this.servletContext);
		this.adapter.setNext(application);
	}

	@RequestMapping(value = { "/service/**" }, consumes = { MediaType.APPLICATION_XML_VALUE })
	public final void request(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("Serving " + request.getRequestURL().toString());
		this.adapter.service(request, response);
	}
}