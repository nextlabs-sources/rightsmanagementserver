package com.nextlabs.rms.command.kms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;

import com.nextlabs.kms.RegisterTenantRequest;
import com.nextlabs.kms.RegisterTenantResponse;
import com.nextlabs.kms.types.Attribute;
import com.nextlabs.kms.types.Provider;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class RegisterTenantService{
	private static Logger logger = Logger.getLogger(RegisterTenantService.class);
	
	public static void registerTenant(String provider, String tenantId, Map<String, String> attributes) throws Exception {
		
		RegisterTenantRequest tenantRequest = new RegisterTenantRequest();
		tenantRequest.setProvider(Provider.fromValue(provider));
		tenantRequest.setTenantId(tenantId);
		List<Attribute> attrList = new ArrayList<>();
		
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
	    Attribute attr = new Attribute();
	    attr.setName(entry.getKey());
	    attr.setValue(entry.getValue());
	    attrList.add(attr);
		}
		tenantRequest.getAttributes().addAll(attrList);
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<RegisterTenantRequest>(MediaType.APPLICATION_XML, tenantRequest);
		RegisterTenantResponse tenantDetailResponse = RestletUtil.sendRequest(KMSWebSvcUrl.REGISTER_TENANT.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(RegisterTenantResponse.class, errorResult));
		if(errorResult.getMessage() == null) {
			logger.info("Created new tenant with ID: " + tenantId);
		}
	}
}
