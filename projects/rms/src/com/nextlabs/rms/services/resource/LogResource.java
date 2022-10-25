package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.services.manager.CommCCManager;
import noNamespace.LogServiceDocument;
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

public class LogResource extends ServerResource {
    static final Logger logger = Logger.getLogger(RegisterAgentResource.class.getName());

    @Post
    public Representation doPost(Representation entity) throws XmlException, IOException, GeneralSecurityException, SOAPException, ServiceException, ClassNotFoundException {
        CommCCManager theManager = CommCCManager.getInstance();
        String xml = entity.getText();
        if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            logger.debug("Log Request:" + xml);
        }

        LogServiceDocument doc = LogServiceDocument.Factory.parse(xml);
        LogServiceDocument result = theManager.sendLog(doc);
        if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
            logger.debug("Log Response:" + result.toString());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.save(baos);
        StringRepresentation response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);

        return response;
    }
}