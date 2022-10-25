package com.nextlabs.rms.eval;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPEnforcement;
import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPSDKCallback;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.bluejungle.destiny.agent.pdpapi.PDPException;
import com.bluejungle.destiny.agent.pdpapi.PDPHost;
import com.bluejungle.destiny.agent.pdpapi.PDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.PDPResource;
import com.bluejungle.destiny.agent.pdpapi.PDPSDK;
import com.bluejungle.destiny.agent.pdpapi.PDPTimeout;
import com.bluejungle.destiny.agent.pdpapi.PDPUser;
import com.nextlabs.rms.config.GlobalConfigManager;

public class APIEvalAdapter implements IEvalAdapter {

	private static Logger logger = Logger.getLogger(APIEvalAdapter.class);

	@Override
	public EvalResponse evaluate(EvalRequest req) {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		EvalResponse response = null;
		try {
			long startTime = System.currentTimeMillis();
			response = new EvalResponse();
			List<String> rights = req.getRights();
			if (rights != null && !rights.isEmpty()) {
				int idx = 0;
				IPDPEnforcement[] responses = new IPDPEnforcement[rights.size()];
				for (String action : rights) {
					IPDPResource srcRes = getResourceWithAttribs(req.getSrcResource());
					IPDPResource targetRes = getResourceWithAttribs(req.getTargetResource());
					int totalResource = 1 + (targetRes != null ? 1 : 0);
					IPDPResource[] resourceArr = new IPDPResource[totalResource];
					resourceArr[0] = srcRes;
					if (targetRes != null) {
						resourceArr[1] = targetRes;
					}
					for (IPDPResource resource : resourceArr) {
						resource.setAttribute("ce::nocache", "yes");
					}
					IPDPUser user = getUser(req.getUser());
					IPDPHost host = getHost(req.getHost());
					IPDPNamedAttributes[] environmentAttributes = getEnvironmentAttributes(req);
					IPDPApplication application = getApplication(req.getApplication());
					IPDPEnforcement enf = PDPSDK.PDPQueryDecisionEngine(action, resourceArr, user, application, host,
							req.isPerformObligations(), environmentAttributes, req.getNoiseLevel(), req.getTimeoutInMins(), IPDPSDKCallback.NONE);
					responses[idx++] = enf;
				}
				populateResponses(response, rights, responses, req);
			}
			logger.info("Evaluation Completed");
			long endTime = System.currentTimeMillis();
			logger.info("Time taken for evaluating the request: " + (endTime - startTime) + " ms");
		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument Error occurred while evaluating request: " + e.getMessage(), e);
			response = new EvalResponse();
		} catch (PDPTimeout e) {
			logger.error("Timeout Error occurred while evaluating request: " + e.getMessage(), e);
			response = new EvalResponse();
		} catch (PDPException e) {
			logger.error("Error occurred while evaluating request: " + e.getMessage(), e);
			response = new EvalResponse();
		} catch (Exception e) {
			logger.error("Error occurred while evaluating request: " + e.getMessage(), e);
			response = new EvalResponse();
		} finally {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (!originalClassLoader.equals(contextClassLoader)) {
				Thread.currentThread().setContextClassLoader(originalClassLoader);
			}
		}
		return response;
	}

	private IPDPHost getHost(Host host) {
		Integer ipAddress = host.getIpAddress();
		String hostname = host.getHostname();
		NamedAttributes namedAttributes = host.getAttributes();
		IPDPHost pdpHost = ipAddress != null ? new PDPHost(ipAddress) : new PDPHost(hostname);
		if (!namedAttributes.getAttributes().isEmpty()) {
			Map<String, List<String>> attributes = namedAttributes.getAttributes();
			Set<Entry<String, List<String>>> entrySet = attributes.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					for (String value : values) {
						pdpHost.setAttribute(entry.getKey(), value);
					}
				}
			}
		}
		return pdpHost;
	}

	private IPDPApplication getApplication(Application application) {
		Long pid = application.getPid();
		String name = application.getName();
		NamedAttributes namedAttributes = application.getAttributes();
		IPDPApplication app = pid != null ? new PDPApplication(name, pid) : new PDPApplication(name);
		if (!namedAttributes.getAttributes().isEmpty()) {
			Map<String, List<String>> attributes = namedAttributes.getAttributes();
			Set<Entry<String, List<String>>> entrySet = attributes.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					for (String value : values) {
						app.setAttribute(entry.getKey(), value);
					}
				}
			}

		}
		return app;
	}
	
	private String generateResponseLog(List<String> rights, IPDPEnforcement[] responses, EvalRequest req) {
		StringBuilder log = new StringBuilder();
		User user = req.getUser();
		Resource srcResource = req.getSrcResource();
		log.append("Result of policy evaluation (User SID: ");
		log.append(user.getId()).append(", Resource name: ");
		log.append(srcResource.getResourceName()).append("): \n");
		if (rights != null && !rights.isEmpty()) {
			int idx = 0;
			for (String action : rights) {
				IPDPEnforcement enforcement = responses[idx++];
				String permission = enforcement != null ? enforcement.getResult() : "";
				log.append("\t");
				log.append(action).append(" : ").append(permission);
				log.append("\n");
			}
		}
		return log.toString();
	}

	private void populateResponses(EvalResponse response, List<String> rights, IPDPEnforcement[] responses, EvalRequest req) {
		int idx = 0;
		
		if (rights != null && !rights.isEmpty()) {
			if (logger.isDebugEnabled()) {
				String responseLog = generateResponseLog(rights, responses, req);
				logger.debug(responseLog);
			}
			for (String action : rights) {
				IPDPEnforcement enforcement = responses[idx];
				if (enforcement != null) {
					String permission = enforcement.getResult();
					if (getDecision(permission) == Decision.PERMIT) {
						response.addRights(action);
					}
					String[] obligations = enforcement.getObligations();
					if (obligations != null && obligations.length > 0) {
						Obligation obligation = null;
						Attribute attribute = null;
						boolean hasSingleAttribute = false;
						int totalObligation = 0;
						for (int i = 0; i < obligations.length; i += 2) {
							String key = obligations[i];
							String value = obligations[i + 1];
							if (EvalRequest.OBLIGATION_COUNT.equals(key)) {
								try {
									totalObligation = Integer.parseInt(value);
									logger.debug("Total obligations for " + action + ": " + totalObligation);
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
				idx++;
			}
		}
	}

	private Decision getDecision(String decisionValue) {
		Decision result = Decision.NA;
		boolean defaultAllow = GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.POLICY_EVALUATION_DEFAULT_ALLOW);
		if (decisionValue != null) {
			if (decisionValue.equalsIgnoreCase("allow") || (decisionValue.equalsIgnoreCase("dontcare") && defaultAllow)) {
				result = Decision.PERMIT;
			} else if (decisionValue.equalsIgnoreCase("deny") || (decisionValue.equalsIgnoreCase("dontcare"))) {
				result = Decision.DENY;
			} else {
				result = Decision.INDETERMINATE;
			}
		}
		return result;
	}

	private IPDPNamedAttributes[] getEnvironmentAttributes(EvalRequest req) {
		IPDPNamedAttributes[] environments = null;
		IPDPNamedAttributes[] results = null;
		NamedAttributes[] envAttributes = req.getEnvironmentAttributes();
		String policy = req.getOtfPolicy();
		boolean ignoreBuiltInPolicies = req.isIgnoreBuiltInPolicies();
		final boolean hasPolicy = policy != null && policy.trim().length() > 0;
		final boolean hasEnvAttributes = (envAttributes != null && envAttributes.length > 0);
		IPDPNamedAttributes pdpEnvAttributes = null;
		boolean dontCareAttributePresent = false;
		if (hasEnvAttributes || hasPolicy) {
			int size = (hasEnvAttributes ? envAttributes.length : 0) + (hasPolicy ? 1 : 0);
			environments = new IPDPNamedAttributes[size];
			int idx = 0;
			if (hasEnvAttributes) {
				for (NamedAttributes attribute : envAttributes) {
					String attributeName = attribute.getName();
					environments[idx] = new PDPNamedAttributes(attributeName);
					if (EvalRequest.ENVIRONMENT_ATTRIBUTE_NAME.equals(attributeName)) {
						pdpEnvAttributes = environments[idx];
					}
					Map<String, List<String>> attributes = attribute.getAttributes();
					if (attributes != null && !attributes.isEmpty()) {
						Set<Entry<String, List<String>>> entrySet = attributes.entrySet();
						for (Entry<String, List<String>> entry : entrySet) {
							List<String> values = entry.getValue();
							if (values != null && !values.isEmpty()) {
								if (EvalRequest.ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE.equals(entry.getKey())
										&& EvalRequest.ENVIRONMENT_ATTRIBUTE_NAME.equals(attributeName)) {
									dontCareAttributePresent = true;
								}
								for (String value : values) {
									environments[idx].setAttribute(entry.getKey(), value);
								}
							}
						}
					}
					++idx;
				}
			}
			if (hasPolicy) {
				environments[idx] = new PDPNamedAttributes("policies");
				environments[idx].setAttribute("pql", policy);
				environments[idx].setAttribute("ignoredefault", ignoreBuiltInPolicies ? "yes" : "no");
			}
		}
		if (!dontCareAttributePresent) {
			results = environments != null ? new IPDPNamedAttributes[environments.length + (pdpEnvAttributes != null ? 0 : 1)]
					: new IPDPNamedAttributes[1];
			if (environments != null) {
				System.arraycopy(environments, 0, results, 0, environments.length);
			}
			if (pdpEnvAttributes != null) {
				pdpEnvAttributes.setAttribute(EvalRequest.ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE, "yes");
			} else {
				results[results.length - 1] = new PDPNamedAttributes(EvalRequest.ENVIRONMENT_ATTRIBUTE_NAME);
				results[results.length - 1].setAttribute(EvalRequest.ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE, "yes");
			}
		} else {
			results = environments;
		}
		return results;
	}

	private IPDPResource getResourceWithAttribs(Resource resource) {
		if (resource == null) {
			return null;
		}
		IPDPResource ipdpResource = new PDPResource(resource.getDimensionName(), resource.getResourceName(), resource.getResourceType());
		NamedAttributes attributes = resource.getAttributes();
		Map<String, List<String>> attributesList = attributes.getAttributes();
		if (attributesList != null && !attributesList.isEmpty()) {
			Set<Entry<String, List<String>>> entrySet = attributesList.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					for (String value : values) {
						ipdpResource.setAttribute(entry.getKey(), value);
					}
				}
			}
		}
		return ipdpResource;
	}

	private IPDPUser getUser(User user) {
		IPDPUser pdpUser = new PDPUser(user.getId(), user.getName());
		NamedAttributes userAttribMap = user.getAttributes();
		Map<String, List<String>> attributes = userAttribMap.getAttributes();
		if (attributes != null && !attributes.isEmpty()) {
			Set<Entry<String, List<String>>> entrySet = attributes.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					for (String value : values) {
						pdpUser.setAttribute(entry.getKey(), value);
					}
				}
			}
		}
		return pdpUser;
	}

	@Override
	public String getConnStatus() {
		return null;
	}

	@Override
	public void initializeSDK() {
	}
}
