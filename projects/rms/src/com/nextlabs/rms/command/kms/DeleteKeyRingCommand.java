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
import com.nextlabs.kms.DeleteKeyRingRequest;
import com.nextlabs.kms.DeleteKeyRingResponse;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.command.AbstractCommand;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class DeleteKeyRingCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(DeleteKeyRingCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		String keyRingName = request.getParameter("keyring_name");
		DeleteKeyRingRequest keyRingRequest = new DeleteKeyRingRequest();
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		keyRingRequest.setTenantId(user.getTenantId());
		keyRingRequest.setVersion(1);
		keyRingRequest.setName(keyRingName);
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<DeleteKeyRingRequest>(MediaType.APPLICATION_XML, keyRingRequest);
		DeleteKeyRingResponse keyRingResponse = RestletUtil.sendRequest(KMSWebSvcUrl.DELETE_KEYRING.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(DeleteKeyRingResponse.class, errorResult));		
		if(errorResult.getMessage() != null) {
			JsonUtil.writeJsonToResponse(errorResult, response); 
		}
		else {
			logger.info("Deleted Existing KeyRing: " + keyRingName);
			JsonObject json = new JsonObject();
			json.addProperty("result", true);
			JsonUtil.writeJsonToResponse(json, response);
		}
	}
}
