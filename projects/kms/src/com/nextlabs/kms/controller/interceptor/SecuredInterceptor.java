package com.nextlabs.kms.controller.interceptor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.nextlabs.kms.entity.enums.ErrorCode;
import com.nextlabs.kms.exception.AccessDeniedException;
import com.nextlabs.kms.service.MessageBundleService;
import com.nextlabs.kms.service.SecurityService;
import com.nextlabs.kms.types.Error;
import com.nextlabs.kms.util.XMLUtil;

public class SecuredInterceptor implements HandlerInterceptor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	public static final String HTTP_CERT_HEADER = "X-AUTH-CERT";
	private final PathMatcher matcher = new AntPathMatcher();
	@Autowired(required = true)
	private SecurityService securityService;
	@Autowired(required = true)
	private MessageBundleService bundle;
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpServletRequest req = (HttpServletRequest) request;
		String uri = request.getRequestURI();
		String token = req.getHeader(HTTP_CERT_HEADER);
		String address = request.getRemoteAddr();
		String contextPath = request.getContextPath();
		boolean match = matcher.match(contextPath + "/service/**", uri);
		if (match) {
			if (token == null) {
				logger.error("Unable to find token in the request (remote address: {})", address);
				sendAccessDeniedError(response);
				return false;
			} else {
				boolean secured = !matcher.match(contextPath + "/service/registerClient", uri);
				try {
					securityService.verifyToken(token, secured);
				} catch (AccessDeniedException e){
					sendAccessDeniedError(response);
					return false;
				} catch (Exception e) {
					logger.error("Unable to verify token (remote address: {})", address);
					throw e;
				}
			}
		}
		return true;
	}
	
	private void sendAccessDeniedError(HttpServletResponse response) throws IOException{
		ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
		Error error = new Error();
		error.setCode(errorCode.getStatusCode());
		error.setDescription(bundle.getText(errorCode.getCode()));
		response.setContentType("application/xml");
		PrintWriter writer = response.getWriter();
		writer.write(XMLUtil.toXml(error));
		writer.flush();
		writer.close();
	}
}
