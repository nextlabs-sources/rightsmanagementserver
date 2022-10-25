package com.nextlabs.kms.controller.service;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.nextlabs.kms.GetKeyRingWithKeysRequest;
import com.nextlabs.kms.GetKeyRingWithKeysResponse;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.ObjectFactory;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.exception.BadRequestException;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.exception.KeyRingNotFoundException;
import com.nextlabs.kms.exception.TenantNotFoundException;
import com.nextlabs.kms.impl.DTOConverter;
import com.nextlabs.kms.service.IKeyRingManager;
import com.nextlabs.kms.service.TenantService;
import com.nextlabs.kms.service.impl.ProviderService;
import com.nextlabs.kms.types.Error;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;
import com.nextlabs.kms.util.XMLUtil;

public class GetKeyRingWithKeysResource extends ServerResource {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired(required = true)
	private ProviderService providerService;
	@Autowired(required = true)
	private TenantService tenantService;
	@Autowired(required = true)
	private ServiceExceptionHandler handler;
	
	@Post
	public Representation doPost(Representation entity) throws IOException {	
		final ObjectFactory factory = new ObjectFactory();
		GetKeyRingWithKeysResponse keyRingResponse = factory.createGetKeyRingWithKeysResponse();
		GetKeyRingWithKeysRequest keyRingRequest = JAXB.unmarshal(new StringReader(entity.getText()), GetKeyRingWithKeysRequest.class);
		String responseXml = "";	
		try {
			String tenantCode = keyRingRequest.getTenantId();
			String keyRingName = keyRingRequest.getName();
			if(keyRingName == null || keyRingName.length()<=0){
				throw new BadRequestException("Invalid XML format.");
			}		
			Tenant tenant = tenantService.getTenant(tenantCode);
			if (tenant == null) {
				logger.error("No tenant found for tenantID: " + tenantCode);
				throw new TenantNotFoundException(tenantCode);
			}		
			IKeyRingManager keyRingManager = providerService.getKeyRingManager(tenantCode);
			IKeyRing iKeyRing = keyRingManager.getKeyRing(keyRingName);
			if (iKeyRing == null) {
				logger.error("No keyring found for keyRing: " + keyRingName);
				throw new KeyRingNotFoundException(keyRingName);
			}
			KeyRingWithKeysDTO keyRing = DTOConverter.convertToKeyRingWithKeysDTO(iKeyRing);
			keyRingResponse.setKeyRingWithKeys(keyRing);
			responseXml = XMLUtil.toXml(keyRingResponse);
			logger.debug("Served " + this.getClass().getSimpleName() + " for tenant " + tenantCode);
		} catch (KeyManagementException e) {
			Error error = handler.handleException(e);
			responseXml = XMLUtil.toXml(error);
			this.setStatus(new Status(error.getCode(), error.getDescription()));
		}
		Representation representation = new StringRepresentation(responseXml, MediaType.APPLICATION_XML);
		logger.debug("Exiting " + this.getClass().getName());
		return representation;
	}
}
