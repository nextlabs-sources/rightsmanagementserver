package com.nextlabs.rms.services.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.policy.eval.PolicyQueriesDocument;
import com.nextlabs.rms.services.manager.PolicyEvaluationSvcManager;

public class EvaluatePoliciesResource extends ServerResource{
	static final Logger logger = Logger.getLogger(EvaluatePoliciesResource.class.getName());
	@Post
    public Representation doPost(Representation entity) throws XmlException, IOException, GeneralSecurityException, SOAPException, ServiceException, ClassNotFoundException, RMSException {
		StringRepresentation response = null;
		try {
			PolicyEvaluationSvcManager policyEvaluationManager=new PolicyEvaluationSvcManager();
			String xml = entity.getText();
			if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
	        	logger.debug("EvaluatePoliciesRequest:"+xml);
	        }
			XmlOptions xmlParseOpts = new XmlOptions();
			xmlParseOpts.setCharacterEncoding("UTF-8");
	        PolicyQueriesDocument doc = PolicyQueriesDocument.Factory.parse(xml, xmlParseOpts);
	        PolicyQueriesDocument result=policyEvaluationManager.evaluate(doc);
	        if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
	        	logger.debug("EvaluatePoliciesResponse:"+result.toString());
	        }
	        Map<String, String> nsmap=new HashMap<String, String>();
	    		nsmap.put("https://www.nextlabs.com/rms/policy/eval","");

	    		XmlOptions xop=new XmlOptions();
	    		xop.setLoadSubstituteNamespaces(nsmap);
	    		xop.setUseDefaultNamespace();
	    		xop.setCharacterEncoding("UTF-8");
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	        result.save(baos);
	        result.save(baos, xop);
	        response = new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
		}catch (Exception e) {
			logger.error("Error occurred when handling POST request for EvaluatePolicies", e);
		}
		return response;
	}
}
