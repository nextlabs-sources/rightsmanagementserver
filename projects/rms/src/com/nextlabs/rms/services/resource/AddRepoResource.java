package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryAlreadyExists;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.rmc.AddRepositoryRequestDocument;
import com.nextlabs.rms.rmc.AddRepositoryResponseDocument;
import com.nextlabs.rms.rmc.AddRepositoryResponseDocument.AddRepositoryResponse;
import com.nextlabs.rms.services.manager.RepositorySvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.IOException;

public class AddRepoResource extends AbstractSyncResource {
    static final Logger logger = Logger.getLogger(AddRepoResource.class);

	protected Representation handlePost(Representation entity) 
			throws IOException, XmlException, UserNotFoundException, RepositoryAlreadyExists, DuplicateRepositoryNameException {
		RepositorySvcManager theManager = RepositorySvcManager.getInstance();
		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("AddRepositoryRequest:" + xml);
		}
		AddRepositoryRequestDocument doc = AddRepositoryRequestDocument.Factory.parse(xml);
		AddRepositoryRequestDocument.AddRepositoryRequest request = doc.getAddRepositoryRequest();
		AddRepositoryResponseDocument addRepositoryResponse = theManager.addRepositoryRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("AddRepositoryResponse:" + addRepositoryResponse.toString());
		}
		return getStringRepresentation(addRepositoryResponse);
	}
    	
	protected StringRepresentation getErrorResponse(int errCode, String errMsg) throws IOException {
		AddRepositoryResponseDocument doc = AddRepositoryResponseDocument.Factory.newInstance();
		AddRepositoryResponse response = doc.addNewAddRepositoryResponse();
		response.setStatus(getStatus(errCode, errMsg));
		return getStringRepresentation(doc);
	}

}