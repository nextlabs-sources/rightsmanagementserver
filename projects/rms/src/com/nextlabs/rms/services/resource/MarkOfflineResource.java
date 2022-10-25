package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.rmc.MarkOfflineRequestDocument;
import com.nextlabs.rms.rmc.MarkOfflineResponseDocument;
import com.nextlabs.rms.rmc.MarkOfflineResponseDocument.MarkOfflineResponse;
import com.nextlabs.rms.services.manager.CachedFileSvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

public class MarkOfflineResource extends AbstractSyncResource {
	static final Logger logger = Logger.getLogger(MarkOfflineResource.class);

	protected Representation handlePost(Representation entity)
			throws IOException, XmlException, UserNotFoundException, RepositoryNotFoundException {
		CachedFileSvcManager theManager = CachedFileSvcManager.getInstance();

		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("MarkOfflineRequest:" + xml);
		}
		MarkOfflineRequestDocument doc = MarkOfflineRequestDocument.Factory.parse(xml);
		MarkOfflineRequestDocument.MarkOfflineRequest request = doc.getMarkOfflineRequest();
		MarkOfflineResponseDocument markOfflineResponse = theManager.markOfflineRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("MarkOfflineResponse:" + markOfflineResponse.toString());
		}
		return getStringRepresentation(markOfflineResponse);
	}

	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		MarkOfflineResponseDocument doc = MarkOfflineResponseDocument.Factory.newInstance();
		MarkOfflineResponse response = doc.addNewMarkOfflineResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
}