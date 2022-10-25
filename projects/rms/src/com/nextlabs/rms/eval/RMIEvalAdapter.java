package com.nextlabs.rms.eval;

import java.rmi.ConnectException;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.nextlabs.destiny.sdk.CEApplication;
import com.nextlabs.destiny.sdk.CEAttributes;
import com.nextlabs.destiny.sdk.CEAttributes.CEAttribute;
import com.nextlabs.destiny.sdk.CEEnforcement;
import com.nextlabs.destiny.sdk.CEEnforcement.CEResponse;
import com.nextlabs.destiny.sdk.CENamedAttributes;
import com.nextlabs.destiny.sdk.CEResource;
import com.nextlabs.destiny.sdk.CESdk;
import com.nextlabs.destiny.sdk.CESdkException;
import com.nextlabs.destiny.sdk.CEUser;
import com.nextlabs.destiny.sdk.CERequest;
import com.nextlabs.rms.config.GlobalConfigManager;

public class RMIEvalAdapter implements IEvalAdapter {

	private CESdk sdk = null;
	
	private int retryCount=0;
	
	private static Logger logger = Logger.getLogger(RMIEvalAdapter.class);
	
	private Object lockObj = new Object();
	
	private String sdkConnStatus="";
	
	private int rmiPortNum;
	
	private String pcHostName;
	
	public RMIEvalAdapter(String hostName, int portNum){
		rmiPortNum = portNum;
		pcHostName = hostName;
		initializeSDK();
	}

	public void initializeSDK() {
		synchronized (lockObj) {
			try {
				sdk = new CESdk(pcHostName, rmiPortNum);
				sdkConnStatus="";
			} catch (Exception e) {
				logger.error("Error occurred while initializing SDK",e);
				if(e.getCause() instanceof UnknownHostException){
					sdkConnStatus=IEvalAdapter.CONN_ERR_UNKNOWN_HOST;
				}
				if(e.getCause() instanceof ConnectException){
					sdkConnStatus=IEvalAdapter.CONN_ERR_CONNECT_EXCEPTION;
				}
			}
			
		}
	}
	
	public String getConnStatus() {
		return sdkConnStatus;
	}
	
	@Override
	public EvalResponse evaluate(EvalRequest req) {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		long startTime = System.currentTimeMillis();
		EvalResponse response = null;
		try{
			if(sdk == null){
				//Try initializing again..
				initializeSDK();
			}
			CEApplication app = getApplication(req.getApplication());
			CEAttributes appAttribs = getAttribs(req.getApplication());
			CEUser user = getUser(req.getUser());
			//TODO: Check what should be passed as recipients
			String[] recipients = null;
			int ip=0;
			Host host = req.getHost();
			if (host != null) {
				Integer ipAddress = host.getIpAddress();
				ip = ipAddress != null ? ipAddress : 0;
			}
			if (ip == 0) {
				ip = CESdk.ipAddressToInteger("127.0.0.1");
			}
			CEResource sourceResource = getResource(req.getSrcResource());
			CEAttributes sourceAttribs = getAttribs(req.getSrcResource());
			CEResource targetResource = getResource(req.getTargetResource());
			CEAttributes targetAttribs = getAttribs(req.getTargetResource());
			sourceAttribs.add("ce::nocache", "yes");
			if (targetAttribs != null) {
				targetAttribs.add("ce::nocache", "yes");
			}
			CEAttributes userAttribs = getAttribs(req.getUser());
			CENamedAttributes[] envAttr = getEnvironments(req);
			List<CERequest> requestList = createRequests(req, envAttr, user, userAttribs, sourceResource, sourceAttribs, targetResource, targetAttribs, app, appAttribs, recipients);
			List<CEEnforcement> ceEnforcementresult = null;
			ceEnforcementresult = sdk.checkResources(requestList, req.getOtfPolicy(), req.isIgnoreBuiltInPolicies(), ip, 10000);
			response = new EvalResponse();
			buildRights(response, requestList, ceEnforcementresult);
			buildObligations(requestList, response, ceEnforcementresult);
			logger.debug("Time taken for Policy Evaluation:" + (System.currentTimeMillis() - startTime) + " ms");
		}catch(CESdkException e){			
			logger.error("Error occurred while evaluating the request",e);
			if(retryCount==0){
				sdk = null;
				retryCount=1;
				response=evaluate(req);
			}
			if(response==null){
				response = new EvalResponse();
			}else{
				retryCount=0;
			}
			return response;
		} finally {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (!originalClassLoader.equals(contextClassLoader)) {
				Thread.currentThread().setContextClassLoader(originalClassLoader);
			}
		}
		return response;
	}

	private CENamedAttributes[] getEnvironments(EvalRequest request) {
		CENamedAttributes[] result = null;
		CENamedAttributes[] environments = null;
		NamedAttributes[] environmentAttributes = request.getEnvironmentAttributes();
		boolean dontCareAttributePresent = false;
		CENamedAttributes envAttributes = null;
		if (environmentAttributes != null && environmentAttributes.length > 0) {
			environments = new CENamedAttributes[environmentAttributes.length];
			int idx = 0;
			for (NamedAttributes attributes : environmentAttributes) {
				String attributeName = attributes.getName();
				environments[idx] = new CENamedAttributes(attributeName);
				if (EvalRequest.ENVIRONMENT_ATTRIBUTE_NAME.equals(attributeName)) {
					envAttributes = environments[idx];
				}
				Map<String, List<String>> attributesValues = attributes.getAttributes();
				if (attributesValues != null && !attributesValues.isEmpty()) {
					Set<Entry<String, List<String>>> entrySet = attributesValues.entrySet();
					for (Entry<String, List<String>> entry : entrySet) {
						List<String> values = entry.getValue();
						String key = entry.getKey();
						if (values != null && !values.isEmpty()) {
							if (EvalRequest.ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE.equals(key)
									&& EvalRequest.ENVIRONMENT_ATTRIBUTE_NAME.equals(attributeName)) {
								dontCareAttributePresent = true;
							}
							for (String value : values) {
								environments[idx].add(key, value);
							}
						}
					}
				}
				++idx;
			}
		}
		if (!dontCareAttributePresent) {
			result = environments != null ? new CENamedAttributes[environments.length + (envAttributes != null ? 0 : 1)]
					: new CENamedAttributes[1];
			if (environments != null) {
				System.arraycopy(environments, 0, result, 0, environments.length);
			}
			if (envAttributes != null) {
				envAttributes.add(EvalRequest.ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE, "yes");
			} else {
				result[result.length - 1] = new CENamedAttributes(EvalRequest.ENVIRONMENT_ATTRIBUTE_NAME);
				result[result.length - 1].add(EvalRequest.ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE, "yes");
			}
		} else {
			result = environments;
		}
		return result;
	}

	private List<CERequest> createRequests(EvalRequest req, CENamedAttributes[] envAttr, CEUser user, CEAttributes userAttribs,
			CEResource source, CEAttributes srcAttribs, CEResource targetResource, CEAttributes targetAttribs, CEApplication app,
			CEAttributes appAttribs, String[] recipients) {
		List<CERequest> requests = new ArrayList<>();
		List<String> rights = req.getRights();
		if (rights != null && !rights.isEmpty()) {
			for (String action : rights) {
				CERequest ceRequest = new CERequest(action, source, srcAttribs, targetResource, targetAttribs, user, userAttribs, app, appAttribs,
						recipients, envAttr, req.isPerformObligations(), req.getNoiseLevel());
				requests.add(ceRequest);
			}
		}
		return requests;
	}

	private void buildRights(EvalResponse response, List<CERequest> requestList, List<CEEnforcement> enforcementResult) {
		for (int i = 0; i < requestList.size(); i++) {
			CERequest request = requestList.get(i);
			CEEnforcement ceEnforcement = enforcementResult.get(i);
			String action = request.getAction();
			Decision decision = getDecision(ceEnforcement.getResponse());
			logger.debug("Result of policy evaluation for rights " + action + " is " + decision);
			if (decision == Decision.PERMIT) {
				response.addRights(action);
			}
		}
	}

	private Decision getDecision(CEResponse response) {
		Decision result = Decision.NA;
		boolean defaultAllow = GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.POLICY_EVALUATION_DEFAULT_ALLOW);
		if (response == CEResponse.ALLOW || (response == CEResponse.DONTCARE && defaultAllow)) {
			result = Decision.PERMIT;
		} else if (response == CEResponse.DENY || response == CEResponse.DONTCARE) {
			result = Decision.DENY;
		} else {
			result = Decision.INDETERMINATE;
		}
		return result;
	}

	private CEAttributes getAttribs(INamedAttribute iNamedAttribute) {
		if (iNamedAttribute != null) {
			CEAttributes srcAttribs = new CEAttributes();
			if (iNamedAttribute != null && iNamedAttribute.getAttributes() != null) {
				NamedAttributes namedAttributes = iNamedAttribute.getAttributes();
				Map<String, List<String>> attributes = namedAttributes.getAttributes();
				for (Entry<String, List<String>> entry : attributes.entrySet()) {
					List<String> values = entry.getValue();
					if (values != null && !values.isEmpty()) {
						logger.debug("Attribute: " + entry.getKey() + ", value: " + Arrays.toString(values.toArray(new String[values.size()])));
						for (String value : values) {
							srcAttribs.add(entry.getKey(), value);
						}
					}
				}
			}
			return srcAttribs;
		}
		return null;
	}

	private void buildObligations(List<CERequest> requestList, EvalResponse response, List<CEEnforcement> ceEnforcementResult) {
		if (ceEnforcementResult != null && !ceEnforcementResult.isEmpty()) {
			for (int j = 0; j < ceEnforcementResult.size(); j++) {
				CEEnforcement ceEnforcement = ceEnforcementResult.get(j);				
				if (ceEnforcement != null) {
					CEAttributes obligations = ceEnforcement.getObligations();					
					CERequest request = requestList.get(j);						
					String action = request.getAction();
					List<CEAttribute> attributes = obligations != null ? obligations.getAttributes() : null;
					if (attributes != null && !attributes.isEmpty()) {
						int totalObligation = 0;
						Obligation obligation = null;
						Attribute attribute = null;
						boolean hasSingleAttribute = false;
						for (int i = 0; i < attributes.size(); i++) {
							CEAttribute ceAttribute = attributes.get(i);
							String key = ceAttribute.getKey();
							String value = ceAttribute.getValue();
							if (EvalRequest.OBLIGATION_COUNT.equals(key)) {
								try {
									totalObligation = Integer.parseInt(value);
									logger.debug("Total obligations: " + totalObligation);
								} catch (Exception e) {
									logger.error("Unable to parse total obligation: " + value);
								}
							} else if (key.startsWith(EvalRequest.OBLIGATION_NAME + ":")) {
								String[] keys = key.split("\\:");
								Integer id = Integer.parseInt(keys[1]);
								attribute = null;
								obligation = new Obligation();
								obligation.setId(id);
								obligation.setName(value);				
								obligation.setRight(action);
								response.addObligation(obligation);
							} else if (key.startsWith(EvalRequest.OBLIGATION_POLICY + ":")) {
								String[] keys = key.split("\\:");
								Integer id = Integer.parseInt(keys[1]);
								if (obligation != null && id.equals(obligation.getId())) {
									obligation.setPolicyName(value);
								}
							} else if (key.startsWith(EvalRequest.OBLIGATION_VALUE + ":")) {
								String[] keys = key.split("\\:");
								if (keys.length == 3) {
									Integer obligationId = Integer.parseInt(keys[1]);
									Integer attributeId = Integer.parseInt(keys[2]);
									if (obligation != null && obligationId.equals(obligation.getId())) {
										if (attributeId % 2 != 0) {
											if (hasSingleAttribute) {
												if (attribute != null) {
													attribute.setValue(value);
													obligation.addAttribute(attribute);
													attribute = null;
												}
											} else {
												attribute = new Attribute();
												attribute.setName(value);
											}
										} else {
											if (attribute != null) {
												attribute.setValue(value);
												obligation.addAttribute(attribute);
												attribute = null;
											}
										}
									}
								} else if (keys.length == 2) {
									Integer obligationId = Integer.parseInt(keys[1]);
									if (obligation != null && obligationId.equals(obligation.getId())) {
										attribute = new Attribute();
										attribute.setName("message");
										hasSingleAttribute = true;
									}
								}
							} else if (key.startsWith(EvalRequest.OBLIGATION_VALUE_COUNT + ":")) {
								obligation = null;
								hasSingleAttribute = false;
							}
						}
					}
				}
			}
		}
	}

	private CEApplication getApplication(Application application) {		
		// Currently CESDK consumes path as a custom attribute.
		CEApplication app = new CEApplication(application.getName(), application.getPath());
		return app;
	}

	private CEUser getUser(User user) {
		String userName = user.getName();
		CEUser ceUser = new CEUser(userName != null ? userName : user.getId(), user.getId());
		return ceUser;
	}

	private CEResource getResource(Resource resource) {
		if (resource != null) {
			String resId = resource.getResourceName();
			String resType = resource.getResourceType();
			CEResource ceRes = new CEResource(resId, resType);
			return ceRes;
		}
		return null;
	}

	protected void finalize(){
		try {
			logger.info("About to close SDK");
//			sdk.Close(handle, 100);
		} catch (Exception e) {
			logger.error("Error occurred while closing the SDK",e);
		}
	}

}
