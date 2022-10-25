package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.nextlabs.destiny.sdk.CESdk;
import com.nextlabs.destiny.sdk.CESdkException;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.Application;
import com.nextlabs.rms.eval.Attribute;
import com.nextlabs.rms.eval.EvalRequest;
import com.nextlabs.rms.eval.EvalResponse;
import com.nextlabs.rms.eval.EvaluationAdapterFactory;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.Host;
import com.nextlabs.rms.eval.IEvalAdapter;
import com.nextlabs.rms.eval.NamedAttributes;
import com.nextlabs.rms.eval.Obligation;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.eval.Resource;
import com.nextlabs.rms.eval.Rights;
import com.nextlabs.rms.eval.User;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.policy.eval.ApplicationType;
import com.nextlabs.rms.policy.eval.AttributeType;
import com.nextlabs.rms.policy.eval.BooleanRestriction;
import com.nextlabs.rms.policy.eval.BooleanRestriction.Enum;
import com.nextlabs.rms.policy.eval.EnvironmentType;
import com.nextlabs.rms.policy.eval.EnvironmentsType;
import com.nextlabs.rms.policy.eval.ErrorType;
import com.nextlabs.rms.policy.eval.HostType;
import com.nextlabs.rms.policy.eval.OTFPolicyType;
import com.nextlabs.rms.policy.eval.ObligationType;
import com.nextlabs.rms.policy.eval.ObligationsType;
import com.nextlabs.rms.policy.eval.PolicyQueriesDocument;
import com.nextlabs.rms.policy.eval.PolicyQueriesType;
import com.nextlabs.rms.policy.eval.PolicyQueryType;
import com.nextlabs.rms.policy.eval.RequestType;
import com.nextlabs.rms.policy.eval.ResourceType;
import com.nextlabs.rms.policy.eval.ResourcesType;
import com.nextlabs.rms.policy.eval.ResponseType;
import com.nextlabs.rms.policy.eval.RightsType;
import com.nextlabs.rms.policy.eval.SubjectType;
import com.nextlabs.rms.policy.eval.UserType;
import com.nextlabs.rms.services.application.RMSApplicationErrorCodes;

public class PolicyEvaluationSvcManager {
	private static final Logger logger = Logger.getLogger(PolicyEvaluationSvcManager.class);
		
	public PolicyQueriesDocument evaluate(PolicyQueriesDocument requestDoc) {
		IEvalAdapter adapter = EvaluationAdapterFactory.getInstance().getAdapter(false);
		PolicyQueriesDocument responseDoc = PolicyQueriesDocument.Factory.newInstance();
		PolicyQueriesType responsePolicyQueries = responseDoc.addNewPolicyQueries();
		PolicyQueryType[] policyQueryArray = requestDoc.getPolicyQueries().getPolicyQueryArray();
		for(PolicyQueryType policyQuery: policyQueryArray){
			PolicyQueryType newPolicyQueryResponse = responsePolicyQueries.addNewPolicyQuery(); 
			ResponseType newResponse = newPolicyQueryResponse.addNewResponse();
			try{
				//Create an evalRequest object
				EvalRequest evalReq = new EvalRequest();
				RequestType request = policyQuery.getRequest();
				SubjectType subject = request.getSubject();
				ResourcesType resources = request.getResources();
				EnvironmentsType environments = request.getEnvironments();
				Enum performObligations = request.getPerformObligations();
				RightsType rights = request.getRights();
				OTFPolicyType otfPolicyType = request.getOTFPolicy();
				String otfPolicy = null;
				boolean ignoreBuiltInPolicies = false;
				if (otfPolicyType != null) {
					ignoreBuiltInPolicies = otfPolicyType.getIgnoreBuiltInPolicies();
					otfPolicy = otfPolicyType.getPql();
				}
				try {
					addSubjectsToEvalRequest(evalReq,subject);
					addResourcesToEvalRequest(evalReq,resources);
					addEnvironmentsToEvalRequest(evalReq,environments);
				} catch (RMSException e) {
					logger.error("Exception occured while processing EvaluatePolicies input XML request",e);
					ErrorType newError = newResponse.addNewError();
					newError.setErrorCode(RMSApplicationErrorCodes.MALFORMED_INPUT_ERROR);
					newError.setErrorMessage(e.getMessage());
					return responseDoc;
				}
				addActionsToEvalRequest(evalReq, rights);
				evalReq.setOtfPolicy(otfPolicy);
				evalReq.setIgnoreBuiltInPolicies(ignoreBuiltInPolicies);
				if (performObligations != null) {
					evalReq.setPerformObligations(performObligations == BooleanRestriction.TRUE);
				}
				//Call the API to get a response
				EvalResponse response = adapter.evaluate(evalReq);

				//Populate XML response
				populateResponseFields(response,newResponse);
			}
			catch(Exception e){
				logger.error("Exception occured while processing the request: ", e);
				newResponse = newPolicyQueryResponse.getResponse();
				if(newResponse==null){
					newResponse = newPolicyQueryResponse.addNewResponse();
				}
				ErrorType newError = newResponse.addNewError();
				newError.setErrorCode(RMSApplicationErrorCodes.GENERAL_ERROR);
				newError.setErrorMessage("A general error occured");
				return responseDoc;
			}
		}
		return responseDoc;
	}

	private void addActionsToEvalRequest(EvalRequest evalReq, RightsType rights) {
		List<String> rightsList = evalReq.getRights();
		String defaultRights = null;
		if(rights == null || rights.getRightArray()==null || rights.getRightArray().length==0){
			defaultRights = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.SUPPORTED_POLICY_EVAL_WEBSVC_RIGHTS);
			if(defaultRights == null || defaultRights.length() == 0){
				defaultRights = GlobalConfigManager.DEFAULT_POLICY_EVAL_WEBSVC_RIGHTS;
			}
			StringTokenizer rightsTokenizer = new StringTokenizer(defaultRights,",");
			while(rightsTokenizer.hasMoreTokens()){
				rightsList.add(rightsTokenizer.nextToken().trim());
			}
		}else{
			for(String right:rights.getRightArray()){
				rightsList.add(right);
			}
		}
	}

	private void populateResponseFields(EvalResponse response, ResponseType newResponse) {
		//Convert Response to XML
		List<Obligation> obligations = response.getObligations();
		RightsType rightsType = newResponse.addNewRights();
		Rights rights = response.getRights();
		String[] permittedRights = rights.getRights();
		if (permittedRights != null && permittedRights.length > 0) {
			for (String right : permittedRights) {
				rightsType.addRight(right);
			}
		}
		ObligationsType newObligations = newResponse.addNewObligations();
		newResponse.setRights(rightsType);
		//Add obligations to response
		for(Obligation obg: obligations){
			ObligationType newObligation = newObligations.addNewObligation();
			newObligation.setName(obg.getName());
			newObligation.setRight(obg.getRight());
			Map<String, List<String>> obgAtrributes = groupObligationAtrributesByName(obg.getAttributes());
			Iterator<Entry<String, List<String>>> it = obgAtrributes.entrySet().iterator();
			while(it.hasNext()){
				Entry<String,List<String>> entry = (Entry<String, List<String>>) it.next();
				String attrName = entry.getKey();
				List<String> attrValList = entry.getValue();
				AttributeType newAttribute = newObligation.addNewAttribute();
				newAttribute.setName(attrName);
				for(String val: attrValList){
					newAttribute.addAttributeValue(val);
				}
			}
		}
	}

	private Map<String, List<String>> groupObligationAtrributesByName(List<Attribute> attributes) {
		Map<String, List<String>> map = new java.util.HashMap<String, List<String>>();
		for(Attribute attr:attributes){
			List<String> list = map.get(attr.getName());
			if (list == null) {
				list = new ArrayList<>();
				map.put(attr.getName(), list);
			}
			list.add(attr.getValue());
		}
		return map;
	}

	private void addEnvironmentsToEvalRequest(EvalRequest evalReq, EnvironmentsType environments) throws RMSException {
		EnvironmentType[] environementArray = environments.getEnvironmentArray();
		List<NamedAttributes> envAttrList = new ArrayList<>();
		for(EnvironmentType env: environementArray){
			String name = env.getName();
			if(name == null){
				throw new RMSException(RMSMessageHandler.getClientString("envInlineAttribNotFoundForEval"));
			}
			NamedAttributes envAttr = new NamedAttributes(name); 
			List<Entry<String, String>> attributesEntryArray = getAttributesEntryArray(env.getAttributeArray());
			for(Entry<String,String> entry: attributesEntryArray){
				envAttr.addAttribute(entry.getKey(), entry.getValue());
			}
			envAttrList.add(envAttr);
		}
		NamedAttributes[] envAttrArr = new NamedAttributes[envAttrList.size()];
		envAttrList.toArray(envAttrArr);
		evalReq.setEnvironmentAttributes(envAttrArr);
	}

	private void addResourcesToEvalRequest(EvalRequest evalReq, ResourcesType resources) throws RMSException {
		if(resources == null){
			throw new RMSException(RMSMessageHandler.getClientString("resReqdForEval"));
		}
		ResourceType[] resourceArray = resources.getResourceArray();
		List<Resource> resList = new ArrayList<>();
		for(ResourceType res: resourceArray){
			String dim  = res.getDimension();
			String name = res.getName();
			String type = res.getType();
			if(dim == null || name == null || type == null){
				throw new RMSException(RMSMessageHandler.getClientString("resInlineAttribNotFoundForEval"));
			}
			Resource resource = new Resource(dim, name, type);
			AttributeType[] attributeArray = res.getAttributeArray();
			List<Entry<String, String>> entryArray = getAttributesEntryArray(attributeArray);
			for(Entry<String,String> namedAttr: entryArray){
				resource.addAttribute(namedAttr.getKey(),namedAttr.getValue());
			}
			resList.add(resource);
		}
		if(resList.size()==0){
			throw new RMSException(RMSMessageHandler.getClientString("resReqdForEval"));
		}
		evalReq.setSrcResource((Resource)resList.get(0));
		if(resList.size()==2){
			Resource targetResource = resList.get(1);
			evalReq.setTargetResource(targetResource);
		}
	}

	private void addSubjectsToEvalRequest(EvalRequest evalReq, SubjectType subject) throws RMSException {
		if(subject==null){
			throw new RMSException(RMSMessageHandler.getClientString("subjReqdForEval"));
		}
		UserType user = subject.getUser();
		HostType host = subject.getHost();
		ApplicationType application = subject.getApplication();
		User pdpUser = extractUserFromRequest(user);
		Application app = extractApplicationFromRequest(application);
		Host pdpHost = extractHostFromRequest(host);
		evalReq.setUser(pdpUser);
		evalReq.setApplication(app);
		evalReq.setHost(pdpHost);
	}

	private Host extractHostFromRequest(HostType hostType) throws RMSException {
		if(hostType==null){
			throw new RMSException(RMSMessageHandler.getClientString("hostReqdForEval"));
		}
		String dottedIP = hostType.getIp();
		if(dottedIP == null){
			throw new RMSException(RMSMessageHandler.getClientString("hostInlineAttribNotFoundForEval"));
		}
		int ipAddress = 0;
		try {
			ipAddress = CESdk.ipAddressToInteger(dottedIP);
		} catch (CESdkException e) {
			logger.error("Unable to parse IP address: " + dottedIP);
		}
		if(ipAddress==0){
			try {
				ipAddress = CESdk.ipAddressToInteger(EvaluationHandler.LOCALHOST_IP);
			} catch (CESdkException e) {
				logger.error("Unable to parse IP address: " + dottedIP);
			}
		}
		AttributeType[] attributeArray = hostType.getAttributeArray();
		Host host = new Host(ipAddress);
		List<Entry<String, String>> entryArray = getAttributesEntryArray(attributeArray);
		for(Entry<String,String> namedAttr: entryArray){
			host.addAttribute(namedAttr.getKey(),namedAttr.getValue());
		}
		return host;
	}

	private Application extractApplicationFromRequest(ApplicationType application) throws RMSException {
		if(application==null){
				throw new RMSException(RMSMessageHandler.getClientString("appReqdForEval"));
		}
		String name = application.getName();
		String pid = application.getPid();
		String path = application.getPath() != null && application.getPath().trim().length() > 0 ? application.getPath() : null;
		if(name == null || pid == null){
			throw new RMSException(RMSMessageHandler.getClientString("appInlineAttribNotFoundForEval"));
		}
		AttributeType[] attributeArray = application.getAttributeArray();
		Application app = null;
		Long pidLong = null;
		if (pid != null && pid.length() != 0) {
			try {
				pidLong = Long.parseLong(pid);
			} catch (Exception e) {
				logger.debug("Invalid PID value. PID Can't be converted to a long value : " + pid);
			}
		}
		app = new Application(name, pidLong, path);
		List<Entry<String, String>> entryArray = getAttributesEntryArray(attributeArray);
		for (Entry<String, String> namedAttr : entryArray) {
			app.addAttribute(namedAttr.getKey(), namedAttr.getValue());
		}
		return app;
	}

	private User extractUserFromRequest(UserType user) throws RMSException {
		if(user==null){
			throw new RMSException(RMSMessageHandler.getClientString("userReqdForEval"));
		}
		String id = user.getId();
		String name = user.getName();
		if(id == null || name == null){
			throw new RMSException(RMSMessageHandler.getClientString("userInlineAttribNotFoundForEval"));
		}
		AttributeType[] attributeArray = user.getAttributeArray();
		User userObject = new User(id, name);
		List<Entry<String, String>> entryArray = getAttributesEntryArray(attributeArray);
		for(Entry<String,String> namedAttr: entryArray){
			userObject.addAttribute(namedAttr.getKey(),namedAttr.getValue());
		}
		return userObject;
	}

	private List<Entry<String,String>> getAttributesEntryArray(AttributeType[] attributeArray) throws RMSException {
		List<Entry<String,String>> attrList = new ArrayList<>();
		for(AttributeType attr: attributeArray){
			String attrName = attr.getName();
			if(attrName == null){
				throw new RMSException(RMSMessageHandler.getClientString("attrNameNotFoundForEval"));
			}
			for(String val:attr.getAttributeValueArray()){
				java.util.Map.Entry<String,String> pair=new java.util.AbstractMap.SimpleEntry<>(attrName,val);
				attrList.add(pair);
			}
		}
		return attrList;
	}

}
