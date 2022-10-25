package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.services.manager.CommCCManager;

import noNamespace.HeartBeatRequestDocument;
import noNamespace.HeartBeatResponseDocument;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.ByteArrayOutputStream;

public class HeartBeatResource extends ServerResource {

    static final Logger logger = Logger.getLogger(HeartBeatResource.class);

    @Post
    public Representation doPost(Representation entity) throws Exception {
        StringRepresentation response = null;

        try {
        	CommCCManager theManager = CommCCManager.getInstance();
            String xml = entity.getText();
            if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("HeartBeatRequest:"+xml);
            }
            HeartBeatRequestDocument doc = HeartBeatRequestDocument.Factory.parse(xml);
            HeartBeatResponseDocument heartBeatResponse = theManager.sendHeartbeat(doc);
            if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("HeartBeatResponse:"+heartBeatResponse.toString());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            heartBeatResponse.save(baos);
            response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
        } catch (Exception e) {
            logger.error("Error occurred when handling POST request for heartbeat:", e);
        }

        return response;
    }
}