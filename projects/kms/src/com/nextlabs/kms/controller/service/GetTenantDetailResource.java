package com.nextlabs.kms.controller.service;

import java.io.IOException;
import java.io.StringReader;
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
import com.nextlabs.kms.GetTenantDetailRequest;
import com.nextlabs.kms.GetTenantDetailResponse;
import com.nextlabs.kms.ObjectFactory;
import com.nextlabs.kms.entity.Provider;
import com.nextlabs.kms.entity.ProviderAttribute;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.exception.TenantNotFoundException;
import com.nextlabs.kms.model.ProviderTypeConverter;
import com.nextlabs.kms.service.TenantService;
import com.nextlabs.kms.types.Attribute;
import com.nextlabs.kms.types.Error;
import com.nextlabs.kms.util.XMLUtil;

public class GetTenantDetailResource extends ServerResource {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired(required = true)
	private TenantService tenantService;
	@Autowired(required = true)
	private ServiceExceptionHandler handler;
	
	@Post
	public Representation doPost(Representation entity) throws IOException {	
		final ObjectFactory factory = new ObjectFactory();
		GetTenantDetailResponse tenantResponse = factory.createGetTenantDetailResponse();
		GetTenantDetailRequest tenantRequest = JAXB.unmarshal(new StringReader(entity.getText()), GetTenantDetailRequest.class);
		String responseXml = "";	
		try {
			String tenantCode = tenantRequest.getTenantId();
			Tenant tenant = tenantService.getTenantDetail(tenantCode);
			if (tenant == null) {
				logger.error("No tenant found for tenantID: " + tenantCode);
				throw new TenantNotFoundException(tenantCode);
			}		
			Provider provider = tenant.getProvider();
			ProviderType providerType = provider.getProviderType();
			List<ProviderAttribute> attributes = provider.getAttributes();
			com.nextlabs.kms.types.Provider managementProvider = ProviderTypeConverter.toProvider(providerType);
			tenantResponse.setProvider(managementProvider);
			if (attributes!=null && !CollectionUtils.isEmpty(attributes)) {
				for (ProviderAttribute attr : attributes) {
					Attribute attribute = new Attribute();
					attribute.setName(attr.getName());
					attribute.setValue(attr.getValue());
					tenantResponse.getAttributes().add(attribute);
				}
			}
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
}
