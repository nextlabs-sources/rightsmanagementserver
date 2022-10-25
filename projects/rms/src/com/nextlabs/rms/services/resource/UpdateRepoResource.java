package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.exception.DuplicateRepositoryNameException;
import com.nextlabs.rms.exception.RepositoryNotFoundException;
import com.nextlabs.rms.exception.UnauthorizedOperationException;
import com.nextlabs.rms.exception.UserNotFoundException;
import com.nextlabs.rms.rmc.UpdateRepositoryRequestDocument;
import com.nextlabs.rms.rmc.UpdateRepositoryResponseDocument;
import com.nextlabs.rms.services.manager.RepositorySvcManager;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.representation.Representation;

import java.io.IOException;

public class UpdateRepoResource extends AbstractSyncResource {
    static final Logger logger = Logger.getLogger(UpdateRepoResource.class);
    
	protected Representation handlePost(Representation entity) 
			throws IOException, RepositoryNotFoundException, UserNotFoundException, XmlException, UnauthorizedOperationException, DuplicateRepositoryNameException {
		RepositorySvcManager theManager = RepositorySvcManager.getInstance();

		String xml = entity.getText();
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("UpdateRepositoryRequest:" + xml);
		}
		UpdateRepositoryRequestDocument doc = UpdateRepositoryRequestDocument.Factory.parse(xml);
		UpdateRepositoryRequestDocument.UpdateRepositoryRequest request = doc.getUpdateRepositoryRequest();
		UpdateRepositoryResponseDocument updateRepositoryResponse = theManager.updateRepositoryRequest(request);
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)) {
			logger.debug("UpdateRepositoryResponse:" + updateRepositoryResponse.toString());
		}
		
		return getStringRepresentation(updateRepositoryResponse);
	}

    @Override
    protected Representation getErrorResponse(int errCode, String errMsg) throws IOException {
        UpdateRepositoryResponseDocument doc = UpdateRepositoryResponseDocument.Factory.newInstance();
        UpdateRepositoryResponseDocument.UpdateRepositoryResponse response = doc.addNewUpdateRepositoryResponse();
        response.setStatus(getStatus(errCode, errMsg));
        return getStringRepresentation(doc);
    }
}