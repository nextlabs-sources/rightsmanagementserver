package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.rmc.UnmarkOfflineRequestDocument;
import com.nextlabs.rms.rmc.UnmarkOfflineResponseDocument;
import com.nextlabs.rms.rmc.UnmarkOfflineResponseDocument.UnmarkOfflineResponse;
import com.nextlabs.rms.services.manager.CachedFileSvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import java.io.IOException;

public class UnMarkOfflineResource extends AbstractSyncResource {
	static final Logger logger = Logger.getLogger(UnMarkOfflineResource.class);

	protected Representation handlePost(Representation entity) throws IOException, XmlException {
		CachedFileSvcManager theManager = CachedFileSvcManager.getInstance();
		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("UnMarkOfflineRequest:" + xml);
		}
		UnmarkOfflineRequestDocument doc = UnmarkOfflineRequestDocument.Factory.parse(xml);
		UnmarkOfflineRequestDocument.UnmarkOfflineRequest request = doc.getUnmarkOfflineRequest();
		UnmarkOfflineResponseDocument unmarkOfflineResponse = theManager.unmarkOfflineRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("UnMarkOfflineResponse:" + unmarkOfflineResponse.toString());
		}
		return getStringRepresentation(unmarkOfflineResponse);
	}

	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		UnmarkOfflineResponseDocument doc = UnmarkOfflineResponseDocument.Factory.newInstance();
		UnmarkOfflineResponse response = doc.addNewUnmarkOfflineResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
}