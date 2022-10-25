package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.services.manager.CommCCManager;
import noNamespace.AcknowledgeHeartBeatRequestDocument;
import noNamespace.AcknowledgeHeartBeatResponseDocument;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.ByteArrayOutputStream;

public class AckHeartBeatResource extends ServerResource {

    static final Logger logger = Logger.getLogger(AckHeartBeatResource.class);

    @Post
    public Representation doPost(Representation entity) throws Exception {
        StringRepresentation response = null;

        try {
            CommCCManager theManager = CommCCManager.getInstance();
            String xml = entity.getText();
            if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("AckHeartBeatRequest:"+xml.toString());
            }
            AcknowledgeHeartBeatRequestDocument doc = AcknowledgeHeartBeatRequestDocument.Factory.parse(xml);
            AcknowledgeHeartBeatResponseDocument ackHeartBeatResponse = theManager.ackHeartbeat(doc);
            if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("AckHeartBeatResponse:" + ackHeartBeatResponse.toString());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ackHeartBeatResponse.save(baos);
            response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
        } catch (Exception e) {
            logger.error("Error occurred when handling POST request for ack heartbeat:", e);
        }

        return response;
    }
}