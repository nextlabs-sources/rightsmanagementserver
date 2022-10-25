package com.nextlabs.kms.controller.service;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import com.nextlabs.kms.ObjectFactory;
import com.nextlabs.kms.RegisterClientResponse;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.service.SecurityService;
import com.nextlabs.kms.util.XMLUtil;
import com.nextlabs.nxl.sharedutil.EncryptionUtil;

public class RegisterClientResource extends ServerResource{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired(required = true)
	private SecurityService securityService;
	
	@Autowired(required=true)
	@Qualifier(value="kms.installdir")
	private Resource installDirResource;
	
	@Autowired(required=true)
	@Qualifier(value="kms.encryptionutil")
	private EncryptionUtil encryptionUtil;
	
	private static final String CERT_STORE_PASS = "123next!";
	
	@Post
	public Representation doPost(Representation entity) throws KeyManagementException {
		final ObjectFactory factory = new ObjectFactory();
		RegisterClientResponse clientResponse = factory.createRegisterClientResponse();
		String responseXml = "";
		String base64Cert = securityService.getKeyStoreSecureCertificate();
		clientResponse.setCertificate(base64Cert);
		clientResponse.setCertStorePass(encryptionUtil.encrypt(CERT_STORE_PASS));
		responseXml = XMLUtil.toXml(clientResponse);
		Representation representation = new StringRepresentation(responseXml, MediaType.APPLICATION_XML);
		logger.debug("Exiting " + this.getClass().getName());
		return representation;
	}
}