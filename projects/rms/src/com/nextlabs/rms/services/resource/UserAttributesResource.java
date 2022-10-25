package com.nextlabs.rms.services.resource;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.rmc.UserAttributeRequestDocument;
import com.nextlabs.rms.rmc.UserAttributeResponseDocument;
import com.nextlabs.rms.rmc.UserAttributeResponseDocument.UserAttributeResponse;
import com.nextlabs.rms.services.manager.UserAttributeServiceManager;

public class UserAttributesResource extends AbstractSyncResource {
	private static final Logger logger = Logger.getLogger(UserAttributesResource.class);

	protected Representation handlePost(Representation entity) throws IOException, XmlException {
		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("GetUserAttributesRequest:" + xml);
		}
		UserAttributeRequestDocument doc = UserAttributeRequestDocument.Factory.parse(xml);
		UserAttributeRequestDocument.UserAttributeRequest request = doc.getUserAttributeRequest();
		UserAttributeResponseDocument userAttrResponse = UserAttributeServiceManager.getUserAttributes(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("GetUserAttributesResponse:" + userAttrResponse.toString());
		}
		return getStringRepresentation(userAttrResponse);
	}
	
	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		UserAttributeResponseDocument doc = UserAttributeResponseDocument.Factory.newInstance();
		UserAttributeResponse response =  doc.addNewUserAttributeResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
	
}