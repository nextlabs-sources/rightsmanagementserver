package com.nextlabs.rms.command.kms;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;

import com.google.gson.JsonObject;
import com.nextlabs.kms.GetTenantDetailRequest;
import com.nextlabs.kms.GetTenantDetailResponse;
import com.nextlabs.kms.types.Provider;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.command.AbstractCommand;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class GetTenantDetailsCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(GetTenantDetailsCommand.class);
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		GetTenantDetailRequest tenantDetailRequest = new GetTenantDetailRequest();
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		tenantDetailRequest.setTenantId(user.getTenantId());
		tenantDetailRequest.setVersion(1);
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<GetTenantDetailRequest>(MediaType.APPLICATION_XML, tenantDetailRequest);
		GetTenantDetailResponse tenantDetailResponse = RestletUtil.sendRequest(KMSWebSvcUrl.GET_TENANT_DETAIL.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(GetTenantDetailResponse.class, errorResult));
		if(errorResult.getMessage() != null) {
			JsonUtil.writeJsonToResponse(errorResult, response); 
		}
		else {
			Provider p = tenantDetailResponse.getProvider();
			JsonObject json = new JsonObject();
			json.addProperty("tenantId", user.getTenantId());
			json.addProperty("tenantProvider", p.value());
			json.addProperty("result", true);
			logger.info("Retrieved tenant details.");
			JsonUtil.writeJsonToResponse(json, response);
		}
	}
}