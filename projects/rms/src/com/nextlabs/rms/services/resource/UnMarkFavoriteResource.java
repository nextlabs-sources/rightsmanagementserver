package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.rmc.UnmarkFavoriteRequestDocument;
import com.nextlabs.rms.rmc.UnmarkFavoriteResponseDocument;
import com.nextlabs.rms.rmc.UnmarkFavoriteResponseDocument.UnmarkFavoriteResponse;
import com.nextlabs.rms.services.manager.CachedFileSvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

public class UnMarkFavoriteResource extends AbstractSyncResource {
	static final Logger logger = Logger.getLogger(UnMarkFavoriteResource.class);

	protected Representation handlePost(Representation entity) throws IOException, XmlException {
		CachedFileSvcManager theManager = CachedFileSvcManager.getInstance();

		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("UnMarkFavoriteRequest:" + xml);
		}
		UnmarkFavoriteRequestDocument doc = UnmarkFavoriteRequestDocument.Factory.parse(xml);
		UnmarkFavoriteRequestDocument.UnmarkFavoriteRequest request = doc.getUnmarkFavoriteRequest();
		UnmarkFavoriteResponseDocument unmarkFavoriteResponse = theManager.unmarkFavoriteRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("UnMarkFavoriteResponse:" + unmarkFavoriteResponse.toString());
		}
		return getStringRepresentation(unmarkFavoriteResponse);
	}

	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		UnmarkFavoriteResponseDocument doc = UnmarkFavoriteResponseDocument.Factory.newInstance();
		UnmarkFavoriteResponse response = doc.addNewUnmarkFavoriteResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
}