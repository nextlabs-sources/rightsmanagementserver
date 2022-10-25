package com.nextlabs.rms.command.kms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;

import com.google.gson.JsonObject;
import com.nextlabs.kms.GetAllKeyRingsWithKeysRequest;
import com.nextlabs.kms.GetAllKeyRingsWithKeysResponse;
import com.nextlabs.kms.types.KeyDTO;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.command.AbstractCommand;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class GetKeyRingsWithKeysCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(GetKeyRingsWithKeysCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		GetAllKeyRingsWithKeysRequest keyRingRequest= new GetAllKeyRingsWithKeysRequest();
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		keyRingRequest.setTenantId(user.getTenantId());
		keyRingRequest.setVersion(1);
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<GetAllKeyRingsWithKeysRequest>(MediaType.APPLICATION_XML, keyRingRequest);
		GetAllKeyRingsWithKeysResponse keyRingResponse = RestletUtil.sendRequest(KMSWebSvcUrl.GET_ALL_KEYRINGS_WITH_KEYS.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(GetAllKeyRingsWithKeysResponse.class, errorResult));		
		if(errorResult.getMessage() != null) {
			JsonUtil.writeJsonToResponse(errorResult, response); 
		}
		else {
			List<KeyRingWithKeysDTO> keyRingsWithKeys = keyRingResponse.getKeyRingWithKeys().getKeyRings();
			logger.debug("Retrieved KeyRings");
			List<JsonObject> jsonList = new ArrayList<>();
			for(KeyRingWithKeysDTO keyRingWithKeys: keyRingsWithKeys){
				List<KeyDTO> keys = keyRingWithKeys.getKeys();
				String keyRingName = keyRingWithKeys.getName();
				long lastModifiedDate = keyRingWithKeys.getLastModifiedDate();
				long createdDate = keyRingWithKeys.getCreatedDate();
				JsonObject keyRingJson = new JsonObject();
				keyRingJson.addProperty("name", keyRingName);
				keyRingJson.addProperty("cDate", createdDate);
				keyRingJson.addProperty("mDate", lastModifiedDate);
				keyRingJson.addProperty("size", keys.size());
				List<JsonObject> keysJson = new ArrayList<>();
				for(KeyDTO key: keys) {
					JsonObject keyJson = new JsonObject();
					keyJson.addProperty("id", key.getKeyId().getId());
					keyJson.addProperty("cdate", key.getKeyId().getTimestamp());
					keyJson.addProperty("algorithm", key.getKeyAlgorithm());
					keyJson.addProperty("length", key.getKeyLength());
					keysJson.add(keyJson);
				}
				keyRingJson.add("keys", JsonUtil.getJsonArray(keysJson));
				jsonList.add(keyRingJson);
			}
			
			JsonObject json = new JsonObject();
			json.add("keyRings", JsonUtil.getJsonArray(jsonList));
			json.addProperty("result", true);
			JsonUtil.writeJsonToResponse(json, response);
		}
	}
}
