package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.services.manager.CommCCManager;

import noNamespace.RegisterAgentRequest;
import noNamespace.RegisterAgentRequestDocument;
import noNamespace.RegisterAgentResponseDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class RegisterAgentResource extends ServerResource {
    static final Logger logger = Logger.getLogger(RegisterAgentResource.class);

    @Post
    public Representation doPost(Representation entity) throws XmlException, IOException, GeneralSecurityException, SOAPException, ServiceException {
        CommCCManager theManager = CommCCManager.getInstance();

        StringRepresentation response = null;

        try {
            String xml = entity.getText();
            if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("RegisterAgentRequest:"+xml);
            }
            RegisterAgentRequestDocument doc = RegisterAgentRequestDocument.Factory.parse(xml);
            RegisterAgentRequest request = doc.getRegisterAgentRequest();
            RegisterAgentResponseDocument registerAgentResponse = theManager.registerAgent(request);
            if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            	logger.debug("RegisterAgentResponse:"+registerAgentResponse.toString());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            registerAgentResponse.save(baos);
            response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
        } catch (Exception e) {
            logger.error("Error occurred when handling POST request for register agent:\n", e);
        }

        return response;
    }
}