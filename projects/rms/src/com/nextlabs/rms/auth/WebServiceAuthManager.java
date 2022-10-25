package com.nextlabs.rms.auth;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class WebServiceAuthManager {

	private static Logger logger = Logger
			.getLogger(WebServiceAuthManager.class);

	public static boolean authenticateWebSvc(HttpServletRequest paramHttpServletRequest) {
		if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.DISABLE_WEBSVC_AUTH)){
			return true;
		}
		boolean isUnSecure = false;
		String uri = paramHttpServletRequest.getRequestURI();
		if (uri.equals("/RMS/service/RegisterAgent")){
			isUnSecure = true;			
		}
		Enumeration localEnumeration = paramHttpServletRequest.getHeaderNames();
		while (localEnumeration.hasMoreElements()) {
			String headerName = (String) localEnumeration.nextElement();
			// logger.debug("HeaderName:"+headerName);
			if (headerName.equalsIgnoreCase("X-NXL-S-CERT")) {
				try {
					String certInRequest = paramHttpServletRequest.getHeader(headerName);
//					logger.debug("Cert in Header:" + certInRequest);
					String localCert = null;
					if (isUnSecure) {
						localCert = SSLSocketFactoryGenerator.getKeyStoreUnSecureCertificate();
					} else {
						localCert = SSLSocketFactoryGenerator.getKeyStoreSecureCertificate();
					}
					if(certInRequest.equalsIgnoreCase(localCert)){
//						logger.debug("Certificate validated successfully");
						return true;
					}
				} catch (Exception e) {
					logger.error("Error occurred while validating client certificate", e);
					return false;
				}
			}
		}
		return false;
	}

}
