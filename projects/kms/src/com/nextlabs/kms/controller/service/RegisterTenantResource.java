package com.nextlabs.kms.controller.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.util.CollectionUtils;
import com.nextlabs.kms.ObjectFactory;
import com.nextlabs.kms.RegisterTenantRequest;
import com.nextlabs.kms.RegisterTenantResponse;
import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.exception.BadRequestException;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.model.ProviderTypeAttribute;
import com.nextlabs.kms.model.ProviderTypeConverter;
import com.nextlabs.kms.service.TenantService;
import com.nextlabs.kms.service.impl.KeyRingServiceImpl;
import com.nextlabs.kms.types.Attribute;
import com.nextlabs.kms.types.Error;
import com.nextlabs.kms.types.Provider;
import com.nextlabs.kms.util.XMLUtil;

public class RegisterTenantResource extends ServerResource{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired(required = true)
	private TenantService tenantService;
	@Autowired(required = true)
	private ServiceExceptionHandler handler;

	@Post
	public Representation doPost(Representation entity) throws IOException {	
		final ObjectFactory factory = new ObjectFactory();
		RegisterTenantResponse tenantResponse = factory.createRegisterTenantResponse();
		RegisterTenantRequest tenantRequest = JAXB.unmarshal(new StringReader(entity.getText()), RegisterTenantRequest.class);	
		String responseXml = "";
		try {
			String tenantCode = tenantRequest.getTenantId();
			Provider provider = tenantRequest.getProvider();
			List<Attribute> attributes = tenantRequest.getAttributes();
			if(provider==null || attributes==null){
				throw new BadRequestException("Invalid XML format.");
			}
			
			List<ProviderTypeAttribute> attributeList = new ArrayList<>();
			List<String> attributeNameList = new ArrayList<>();

			if (!CollectionUtils.isEmpty(attributes)) {
				for (Attribute attribute : attributes) {
					String name = attribute.getName();
					String value = attribute.getValue();
					if(name==null || value==null || name.length()<=0 || value.length()<=0){
						throw new BadRequestException("Invalid XML.");
					}
					
					attributeList.add(new ProviderTypeAttribute(name, value));
					attributeNameList.add(name);
				}
			}
			
			if(!validateAttributes(provider, attributeNameList)){
				throw new BadRequestException("Invalid XML. Missing Attributes.");
			}
			
			ProviderType providerType = ProviderTypeConverter.toProviderType(provider);
			tenantService.createTenant(tenantCode, providerType, attributeList);
			responseXml = XMLUtil.toXml(tenantResponse);
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
	
	private boolean validateAttributes(Provider provider, List<String> attributeNameList){
		List<String> mandatoryList = new ArrayList<>();
		switch(provider) {
			case DEFAULT:
				mandatoryList.add(KeyRingServiceImpl.STOREPASS_KEY);
		}
		return Arrays.asList(attributeNameList).containsAll(Arrays.asList(mandatoryList));
	}
}
