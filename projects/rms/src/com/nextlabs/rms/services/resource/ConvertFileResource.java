package com.nextlabs.rms.services.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.visualization.FileConversionSvcManager;

import noNamespace.ConvertFileServiceDocument;


public class ConvertFileResource extends ServerResource{
	static final Logger logger = Logger.getLogger(ConvertFileResource.class.getName());
	
	@Post
    public Representation doPost(Representation entity) throws XmlException, IOException, GeneralSecurityException, SOAPException, ServiceException, ClassNotFoundException, RMSException {
		StringRepresentation response = null;
		try {
			FileConversionSvcManager theManager=new FileConversionSvcManager();
			String xml = entity.getText();
			if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
	        	logger.debug("ConvertFileRequest:"+xml);
	        }
	        ConvertFileServiceDocument doc = ConvertFileServiceDocument.Factory.parse(xml);
	        ConvertFileServiceDocument result=theManager.convertFile(doc);
	        if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
	        	logger.debug("ConvertFileResponse:"+result.toString());
	        }
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        result.save(baos);
	        response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
		}catch (Exception e) {
			logger.error("Error occurred when handling POST request for ConvertFile", e);
		}
		return response;
	}
	
}
