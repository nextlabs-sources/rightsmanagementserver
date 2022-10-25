package com.nextlabs.rms.command.kms;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;

import com.google.gson.JsonObject;
import com.nextlabs.kms.GenerateKeyRequest;
import com.nextlabs.kms.GenerateKeyResponse;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.command.AbstractCommand;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class GenerateKeyCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(GenerateKeyCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		String keyRingName = request.getParameter("keyring_name");
		GenerateKeyRequest keyRingRequest = new GenerateKeyRequest();
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		keyRingRequest.setTenantId(user.getTenantId());
		keyRingRequest.setVersion(1);
		keyRingRequest.setKeyRingName(keyRingName);
		
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<GenerateKeyRequest>(MediaType.APPLICATION_XML, keyRingRequest);
		GenerateKeyResponse keyRingResponse = RestletUtil.sendRequest(KMSWebSvcUrl.GENERATE_KEY.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(GenerateKeyResponse.class, errorResult));		
		if(errorResult.getMessage() != null) {
			JsonUtil.writeJsonToResponse(errorResult, response); 
		}
		else {
			String keyId = Hex.encodeHexString(keyRingResponse.getKeyId().getHash()).concat(String.valueOf(keyRingResponse.getKeyId().getTimestamp()));
			JsonObject json = new JsonObject();
			logger.info("Generated a new key in KeyRing: " + keyRingName);
			json.addProperty("keyId", keyId);
			json.addProperty("result", true);
			JsonUtil.writeJsonToResponse(json, response);
		}
	}
}
