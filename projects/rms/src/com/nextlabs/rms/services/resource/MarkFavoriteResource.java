package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.rmc.MarkFavoriteRequestDocument;
import com.nextlabs.rms.rmc.MarkFavoriteResponseDocument;
import com.nextlabs.rms.rmc.MarkFavoriteResponseDocument.MarkFavoriteResponse;
import com.nextlabs.rms.services.manager.CachedFileSvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

public class MarkFavoriteResource extends AbstractSyncResource {
	static final Logger logger = Logger.getLogger(MarkFavoriteResource.class);

	protected Representation handlePost(Representation entity)
			throws IOException, XmlException, UserNotFoundException, RepositoryNotFoundException {
		CachedFileSvcManager theManager = CachedFileSvcManager.getInstance();

		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("MarkFavoriteRequest:" + xml);
		}
		MarkFavoriteRequestDocument doc = MarkFavoriteRequestDocument.Factory.parse(xml);
		MarkFavoriteRequestDocument.MarkFavoriteRequest request = doc.getMarkFavoriteRequest();
		MarkFavoriteResponseDocument markFavoriteResponse = theManager.markFavoriteRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("MarkFavoriteResponse:" + markFavoriteResponse.toString());
		}

		return getStringRepresentation(markFavoriteResponse);
	}

	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		MarkFavoriteResponseDocument doc = MarkFavoriteResponseDocument.Factory.newInstance();
		MarkFavoriteResponse response = doc.addNewMarkFavoriteResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
}