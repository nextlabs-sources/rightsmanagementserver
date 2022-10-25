package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.rmc.RemoveRepositoryRequestDocument;
import com.nextlabs.rms.rmc.RemoveRepositoryResponseDocument;
import com.nextlabs.rms.services.manager.RepositorySvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

public class RemoveRepoResource extends AbstractSyncResource {
	static final Logger logger = Logger.getLogger(RemoveRepoResource.class);

	protected Representation handlePost(Representation entity)
			throws XmlException, IOException, RepositoryNotFoundException, UnauthorizedOperationException {
		RepositorySvcManager theManager = RepositorySvcManager.getInstance();
		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("RemoveRepositoryRequest:" + xml);
		}
		RemoveRepositoryRequestDocument doc = RemoveRepositoryRequestDocument.Factory.parse(xml);
		RemoveRepositoryRequestDocument.RemoveRepositoryRequest request = doc.getRemoveRepositoryRequest();
		RemoveRepositoryResponseDocument removeRepositoryResponse = theManager.removeRepositoryRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("RemoveRepositoryResponse:" + removeRepositoryResponse.toString());
		}
		return getStringRepresentation(removeRepositoryResponse);
	}

	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		RemoveRepositoryResponseDocument doc = RemoveRepositoryResponseDocument.Factory.newInstance();
		RemoveRepositoryResponseDocument.RemoveRepositoryResponse response = doc.addNewRemoveRepositoryResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}
}