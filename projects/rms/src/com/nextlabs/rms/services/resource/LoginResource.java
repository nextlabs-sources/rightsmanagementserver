package com.nextlabs.rms.services.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.nextlabs.rms.auth.LoginServiceManager;
import com.nextlabs.rms.config.GlobalConfigManager;

import noNamespace.ConvertFileServiceDocument;
import noNamespace.LoginServiceDocument;

public class LoginResource extends ServerResource {
	static final Logger logger = Logger.getLogger(LoginResource.class.getName());

	@Post
	public Representation doPost(Representation entity) throws IOException, XmlException{
		StringRepresentation response = null;
		try{
			LoginServiceManager theManager=new LoginServiceManager();
			String xml = entity.getText();
			if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
				String debugXml=xml.replaceAll("(?<=<Password>).*?(?=</Password>)", "****");
				logger.debug("LoginRequest:"+debugXml);
			}
			LoginServiceDocument doc=LoginServiceDocument.Factory.parse(xml);
			LoginServiceDocument result;
			try{
				result=theManager.authenticate(doc);
			}catch(LoginException e){
				result=e.getResponse();
				getResponse().setStatus(new Status(result.getLoginService().getLoginResponse().getError().getErrorCode()));
			}
			if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
				logger.debug("LoginResponse:"+result.toString());
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			result.save(baos);
			response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
		}catch(Exception e){
			logger.error("Error occurred when handling POST request for Login", e);
		}
		return response;
	}

}
