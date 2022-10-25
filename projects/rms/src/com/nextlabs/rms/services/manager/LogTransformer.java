package com.nextlabs.rms.services.manager;

import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.nextlabs.destiny.bindings.log.v5.DABSLogServiceWSConverter;
import com.nextlabs.domain.log.PolicyActivityInfoV5;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import noNamespace.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.*;

public class LogTransformer {
    private static final String ACTION_OPEN = "OPEN";
	private static Log logger = LogFactory.getLog(LogTransformer.class.toString());

	/*public static void main(String[] args) throws XmlException, IOException, RMSException {
		LogServiceDocument doc = LogServiceDocument.Factory.parse(new File("C:/temp/test.xml"));
		transform(doc);
	}*/
	
	public static String transform(LogServiceDocument logDocument) throws IOException, RMSException {
		LogServiceType logService = logDocument.getLogService();
		LogRequestType logRequest = logService.getLogRequest();
        LogsType logs = logRequest.getLogs();
        LogType[] logArray = logs.getLogArray();
        List<BaseLogEntry> logEntryList = new ArrayList<BaseLogEntry>(); 
        
        for (LogType logType: logArray) { 
        	if (logType != null) {
        		parseLogType(logEntryList, logType);
        	}
        }
        String requestOut = DABSLogServiceWSConverter.encodeLogEntries(logEntryList);
        return requestOut;
	}

	private static void parseLogType(List<BaseLogEntry> logEntryList, LogType logType) throws RMSException {
		long uid = logType.getUid();
		logger.debug("UID is: " + uid);
		GregorianCalendar gc = new GregorianCalendar();
        try {
            gc.setTime(logType.getTimestamp().getTime());
        } catch(Exception e) {
            throw new RMSException("Log had an invalid timestamp." + e.toString());
        }
        long timestamp = gc.getTimeInMillis();
   
		Map<String, DynamicAttributes> daMap = new HashMap<String, DynamicAttributes>();

		String rights = logType.getRights();
		if (logType.getType() == LogTypeEnum.EVALUATION && rights == null) {
			throw new RMSException("Rights are mandatory for evaluation logs.");
		}

		String operation = logType.getOperation();
		if(operation == null){
			operation = ACTION_OPEN;
		}
		if (logType.getType() == LogTypeEnum.OPERATION && operation == null) {
			throw new RMSException("Operation is mandatory for operation logs.");
		}

		EnvironmentType environment = logType.getEnvironment();
		int secondsSinceLastHeartbeat = 0;//Unused
		if (logType.getType() == LogTypeEnum.EVALUATION && environment == null) {
			throw new RMSException("Environment is mandatory for evaluation logs.");
		}
		if (environment != null) {
			secondsSinceLastHeartbeat = environment.getSecondsSinceLastHeartbeat();
		}

		UserType user = logType.getUser();
        if (user == null) {
            throw new RMSException("User is mandatory for all logs.");
        }
        String userContext = user.getContext();
        long userId = 0L;
        try {
            userId = Long.parseLong(userContext);
        } catch(NumberFormatException e) {
            throw new RMSException("Invalid user id when logging (value not a long)." + e.toString());
        }
        String userName = user.getName();
		UserAttributes attributes = user.getAttributes();
		Item[] attributeArray = attributes.getAttributeArray();
		DynamicAttributes userDA = new DynamicAttributes();
		for (Item attribute : attributeArray) {
			String attrName = attribute.getName();
			String attrValue = attribute.getValue();
			userDA.add(attrName, attrValue);
		}
		if (userDA.size() > 0) {
			daMap.put("SU", userDA);
		}

		HostType host = logType.getHost();
		String hostIp = host.getIpv4();
		if(hostIp==null){
			hostIp="";
		}
		String hostName = host.getName();

		DynamicAttributes resourceDA = new DynamicAttributes();
		ApplicationType application = logType.getApplication();
		String image = "";
		String publisher = "";
		if (logType.getType() == LogTypeEnum.EVALUATION && application == null) {
			throw new RMSException("Application is mandatory for evaluation logs.");
		}
		if (application != null) {
			image = application.getImage();
			publisher = application.getPublisher();
			if(publisher!=null){
				String publisherKey = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RM_APPLICATION_PUBLISHER);
				if(publisherKey == null || publisherKey.length() == 0){
					publisherKey=GlobalConfigManager.RM_APPLICATION_PUBLISHER;
				}
					resourceDA.add(publisherKey, publisher);
			}
		}
		
		ResourceType resource = logType.getResource();
		String resPath = resource.getPath();
		TagsType tags = resource.getTags();
		Item[] tagArray = tags.getTagArray();
		for (Item tag : tagArray) {
			String tagName = tag.getName();
			String tagValue = tag.getValue();
			resourceDA.add(tagName, tagValue);
		}
		daMap.put("RF", resourceDA);

		PolicyType[] policyArray = logType.getHitPolicies().getPolicyArray();
		String policyIds = "";
		String policyNames = "";
        int firstPolicyId = -1;
		for (int i=0; i < policyArray.length; ++i) {
			PolicyType policyType = policyArray[i];
			int policyId = policyType.getId();
			String policyName = policyType.getName();
            if (policyName == null) {
                policyName = "";
            }
			if (i > 0) {
				policyIds += ", ";
				policyNames +=", ";
			} else {
                firstPolicyId = policyId;
            }
			policyIds += policyId;
			policyNames+=policyName;
		}
		String policyIdKey = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RM_EVALUATED_POLICY_IDS);
		String policyNameKey = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RM_EVALUATED_POLICY_NAMES);
		
		if(policyIdKey == null || policyIdKey.length() == 0) {
			policyIdKey = GlobalConfigManager.RM_EVALUATED_POLICY_IDS;
		}
		if(policyNameKey == null || policyNameKey.length() == 0) {
			policyNameKey = GlobalConfigManager.RM_EVALUATED_POLICY_NAMES;
		}
		if (policyIds.length() > 0) {
			resourceDA.add(policyIdKey, policyIds);
		}
		if (policyNames.length() > 0){
			resourceDA.add(GlobalConfigManager.RM_EVALUATED_POLICY_NAMES, policyNames);
		}
		String rightsKey = GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RM_RIGHTS_GRANTED_LOGGING_KEY);
		if(rightsKey == null || rightsKey.length() == 0) {
			rightsKey = GlobalConfigManager.RM_RIGHTS_GRANTED_LOGGING_KEY;
		}
		if (rights != null) {
			resourceDA.add(rightsKey, rights);
		}
        try {
            addToLogEntryList(logEntryList, uid, timestamp, operation, resPath, userId, userName, hostIp, hostName, image,
                    publisher, daMap, firstPolicyId);
        } catch(Exception e) {
            throw new RMSException("General error when writing RMC log: " + e.toString());
        }
   	}

	private static void addToLogEntryList(List<BaseLogEntry> logEntryList, long uid, Long timestamp, String operation,
			String resPath, long userId, String userName, String hostIp, String hostName, String image,
			String publisher, Map<String, DynamicAttributes> daMap, int policyId) throws Exception {
		FromResourceInformation fromResource = new FromResourceInformation();
		fromResource.setName(resPath);
		
		// placeholder values for from resource are mandatory (and for ALL fields actually)
		fromResource.setCreatedDate(0L);
		fromResource.setModifiedDate(0L);
		fromResource.setSize(0L);
		fromResource.setOwnerId("");
		
		ToResourceInformation toResource = new ToResourceInformation();
		toResource.setName("");
		
		PolicyActivityInfoV5 activityInfo = new PolicyActivityInfoV5(fromResource,
																		toResource,
																		userName,
																		userId,
																		hostName,
																		hostIp,
																		0,
																		image,
																		0,
																		operation,
																		PolicyDecisionEnumType.POLICY_DECISION_ALLOW,
																		uid,
																		timestamp,
																		3,
																		daMap,
																		null
																		);

		BaseLogEntry logEntry = new PolicyActivityLogEntryV5(activityInfo,policyId,uid);
		
		logEntry.setTimestamp(timestamp);
		logEntryList.add(logEntry);
	}
}
