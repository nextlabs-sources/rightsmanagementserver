package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.rmc.GetRepositoryDetailsRequestDocument;
import com.nextlabs.rms.rmc.GetRepositoryDetailsResponseDocument;
import com.nextlabs.rms.services.manager.RepositorySvcManager;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

public class GetRepositoryDetailsResource extends AbstractSyncResource {
	static final Logger logger = Logger.getLogger(GetRepositoryDetailsResource.class);

	protected Representation handlePost(Representation entity) throws IOException, XmlException {
		RepositorySvcManager theManager = RepositorySvcManager.getInstance();

		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("GetRepositoryDetailsRequest:" + xml);
		}
		GetRepositoryDetailsRequestDocument doc = GetRepositoryDetailsRequestDocument.Factory.parse(xml);
		GetRepositoryDetailsRequestDocument.GetRepositoryDetailsRequest request = doc.getGetRepositoryDetailsRequest();
		GetRepositoryDetailsResponseDocument getRepositoryDetailsResponse = theManager.getRepositoryDetailsRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("GetRepositoryDetailsResponse:" + getRepositoryDetailsResponse.toString());
		}
		return getStringRepresentation(getRepositoryDetailsResponse);
	}

	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		GetRepositoryDetailsResponseDocument doc = GetRepositoryDetailsResponseDocument.Factory.newInstance();
		GetRepositoryDetailsResponseDocument.GetRepositoryDetailsResponse response = doc.getGetRepositoryDetailsResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
}