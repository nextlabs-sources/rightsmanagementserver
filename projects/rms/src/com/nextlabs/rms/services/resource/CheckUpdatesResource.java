package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.services.manager.CommCCManager;

import noNamespace.CheckUpdatesDocument;
import noNamespace.CheckUpdatesType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CheckUpdatesResource extends ServerResource {

	private static final Logger logger = Logger.getLogger(CheckUpdatesResource.class);
	
    @Post
    public Representation doPost(Representation entity) throws XmlException, IOException {            	   	
    	StringRepresentation response = null;
    	try {
			CommCCManager theManager = CommCCManager.getInstance();
			String xml = entity.getText();
            if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("CheckUpdatesRequest:"+xml);
            }
			CheckUpdatesDocument doc = CheckUpdatesDocument.Factory.parse(xml);
			CheckUpdatesType request = doc.getCheckUpdates();
			CheckUpdatesDocument checkUpdatesResponse = theManager.checkUpdates(request);
            if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("CheckUpdatesResponse:"+checkUpdatesResponse.toString());
            }
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			checkUpdatesResponse.save(baos);
			response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error occurred when handling POST request for CheckUpdates", e);
		}
        return response;
    }
}