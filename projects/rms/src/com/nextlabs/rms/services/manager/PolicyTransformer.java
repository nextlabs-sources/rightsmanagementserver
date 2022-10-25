package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import noNamespace.POLICYBUNDLETYPE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS.APPLICATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.ENVS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS.LOCATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS.USER;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.OBLIGATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.OBLIGATION.PARAM;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.RIGHTSET;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.USERGROUPMAP;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.USERGROUPMAP.USERMAP;

import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle.ISubjectKeyMapping;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDTarget;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.LogObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.eval.UnsupportedComponentException;

public class PolicyTransformer {


	private final Map<String, Location> locationByName = new HashMap<String, Location>();
	
	private IDPolicy[] policies = null;

	private final String noRights = "0x0000000000000000";

	public static final String PROPERTYNAME_IPV4 = "ipv4";

	public static final String PREDICATE_FALSE = "FALSE";

	public static final String PREDICATE_TRUE = "TRUE";

	public static final String DATATYPE_GROUP = "GROUP";

	public static final String DATATYPE_STRING = "string";
	
	public static final String DATATYPE_USR = "usr";

	public static final String DATATYPE_LOC = "loc";

	public static final String DATATYPE_APP = "app";
	
	public static final String DATATYPE_INET_ADDRESS = "inet_address";
	
	public static final String DATATYPE_NOT = "NOT";

	public static final String DATATYPE_RES = "res";

	public static final String DATATYPE_TAG = "tag";

	public static final String DATATYPE_PATH = "path";
	
	public static final String builtin = "builtin";
	
	public static final String DATATYPE_MESSAGE = "message";
	
	public static final String DATATYPE_EMAIL_ADDRESSES = "emailAddresses";
	
	public static final String DATATYPE_BODY = "body";
	
	public static final String DATATYPE_DENY = "deny";
	
	public static final String DATATYPE_POLICY_BUNDLE = "PolicyBundle";
	
	private static Logger logger= Logger.getLogger(PolicyTransformer.class);
	
	public POLICYBUNDLETYPE tranformPolicies(IDeploymentBundleV2 bundle2, com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligationsData ccData) throws Exception {
		if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
			logger.debug("Policy Bundle From CC:"+bundle2.toString());
		}
		if(bundle2==null){
			throw new RMSException("The policy bundle passed to policy transformer is null");
		}
	
		int userId=0;
		int resourceId=0;
		int applicationId=0;
		int locId=0;
		HashMap<String,Integer> resourceCache=new HashMap<String, Integer>();
		final SortedMap<Long, IDPolicy> policyById = new TreeMap<Long, IDPolicy>();
		try {
			// Parse the PQL from the bundle to collect all policies and
			// locations
			// (there will be nothing else in the bundle - server side makes
			// sure of that).
			DomainObjectBuilder.processInternalPQL(bundle2.getDeploymentEntities(),
					new DefaultPQLVisitor() {
				public void visitPolicy(DomainObjectDescriptor descr,
						IDPolicy policy) {
					policyById.put(descr.getId(), policy);
				}

				public void visitLocation(
						DomainObjectDescriptor descriptor,
						Location location) {
					locationByName.put(descriptor.getName(), location);
				}

			});
		} catch (PQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.policies = policyById.values().toArray(
				new IDPolicy[policyById.size()]);
		Map<String,String> customObligationMap=new HashMap<>();
	
		//CustomObligation mapping
		List<com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligation> ccCustomObligations = ccData.getCustomObligation();
        if(ccCustomObligations!=null){
			for (com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligation ccCustomObligation : ccCustomObligations) {
	        	customObligationMap.put(ccCustomObligation.getDisplayName(), ccCustomObligation.getInvocationString());
	        }
        }
		
		POLICYBUNDLETYPE policyBundleType = POLICYBUNDLETYPE.Factory.newInstance();
		POLICYBUNDLE bundle = policyBundleType.addNewPOLICYBUNDLE();
		bundle.setTimestamp(bundle2.getTimestamp());
		bundle.setVersion("1.0");
		POLICYSET set = bundle.addNewPOLICYSET();
		COMPONENTS comp = bundle.addNewCOMPONENTS();
		RESOURCES resources = comp.addNewRESOURCES();
		APPLICATIONS applications= comp.addNewAPPLICATIONS();
		LOCATIONS locations=comp.addNewLOCATIONS();
		USERS users=comp.addNewUSERS();
		ENVS envs = comp.addNewENVS(); 
		//No choice
		StringTokenizer tokenizer = new StringTokenizer(bundle2.getDeploymentEntities(),"\n");
		String[] resourcesArray = new String[policies.length];
		String[] subjectArray = new String[policies.length];
		String[] envArray = new String[policies.length];
		int subjectCount=0;
		int resourceCount=0;
		while(tokenizer.hasMoreTokens() && subjectCount<=policies.length && resourceCount<=policies.length){
			String token=tokenizer.nextToken().trim();
			if(subjectCount<policies.length){
				if(token.startsWith("BY") && !token.startsWith("BY DEFAULT")){
					subjectArray[subjectCount]=token;
					subjectCount++;
				}else if(token.startsWith("FOR")){
					resourcesArray[resourceCount]=token;
					resourceCount++;
				}
			}
			if(token.startsWith("WHERE")){
				envArray[subjectCount-1]=token;
			}
		}
		subjectCount=-1;
		String regex = "(?m)\\bcall_function\\b(?=\\s*\\(.*\\))";
		Pattern pattern = Pattern.compile(regex);
//		PolicyComponentParserV4 parser= new PolicyComponentParserV4(resources, users, applications, locations, envs);
		IPolicyComponentParser parser = null; 
		if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.USE_OLD_POLICY_PARSER)){
			logger.debug("Using Policy parser version 4");
			parser = new PolicyComponentParserV4(resources, users, applications, locations, envs);
		}else{
			logger.debug("Using Policy parser version 5");
			parser= new PolicyComponentParserV5(resources, users, applications, locations, envs);
		}
		for (IDPolicy policy : policies) {
			subjectCount++;
			if(policy.getMainEffect().toString().equalsIgnoreCase(DATATYPE_DENY)){
    			continue;
	    	}
            ActionPredicateVisitor actionVisitor=new ActionPredicateVisitor();
            policy.getTarget().getActionPred().accept(actionVisitor, IPredicateVisitor.PREORDER);
            String rights=getRights(actionVisitor);
            if(rights==null){
            	continue;
            }
			String env = envArray[subjectCount];
			if (env != null && env.trim().length() > 0) {
				Matcher matcher = pattern.matcher(env);
				if (matcher.find()) {
					continue;
				}
			}
			POLICY pol = set.addNewPOLICY();
			pol.setName(policy.getName());
			pol.setId(policy.getId());
			IDTarget evaluationTarget = policy.getEvaluationTarget();
			try{
				parser.processPolicy(pol, resourcesArray[subjectCount], subjectArray[subjectCount], envArray[subjectCount]);
			}catch(Exception e){
				if (e instanceof UnsupportedComponentException) {
					logger.error("Error occurred while processing policy (ID: " + policy.getId() + ", Policy Name: "
							+ policy.getName() + "). Ignoring the policy. Error: " + e.getMessage());
				} else {
					logger.error("Error occured while processing policy id "+policy.getId()+".Policy Name: "+policy.getName()+" . Ignoring conditions and proceeding. Exception message is : "+e.getMessage());
				}
				int totalPolicy = set.sizeOfPOLICYArray();
				set.removePOLICY(totalPolicy - 1);
				continue;
			}
			//Add the correct rights
			pol.setRights(rights);
			
			//Add obligations based on obligation type
			IObligation[] allowObligation=policy.getObligationArray(EffectType.ALLOW);
			for(IObligation obligation: allowObligation){
				OBLIGATION obl = pol.addNewOBLIGATION();
				if(obligation instanceof CustomObligation){
					CustomObligation customObl = (CustomObligation) obligation;
					List<String> list=(List<String>)customObl.getCustomObligationArgs();
					String oblName=customObligationMap.get(customObl.getCustomObligationName());
					if(oblName==null || oblName.length()==0){
						oblName=customObl.getCustomObligationName();
					}
					obl.setName(oblName);
					for(int i=0;i<list.size();){
						PARAM parameter = obl.addNewPARAM();
						parameter.setName(list.get(i++));
						parameter.setValue(list.get(i++));
					}
				}
				else if(obligation instanceof LogObligation){
					obl.setName(obligation.getType());
				}
				else if(obligation instanceof NotifyObligation){
					NotifyObligation notifyObl= (NotifyObligation) obligation;
					obl.setName(notifyObl.getType());
					if(notifyObl.getBody()!=null){
						PARAM body = obl.addNewPARAM();
						body.setName(DATATYPE_BODY);
						body.setValue(notifyObl.getBody());
					}
					if(notifyObl.getEmailAddresses()!=null){
						PARAM emailAddresses = obl.addNewPARAM();
						emailAddresses.setName(DATATYPE_EMAIL_ADDRESSES);
						emailAddresses.setValue(notifyObl.getEmailAddresses());
					}
					
				}
				else if(obligation instanceof DisplayObligation){
					DisplayObligation displayObl = (DisplayObligation) obligation;
					PARAM message = obl.addNewPARAM();
					message.setName(DATATYPE_MESSAGE);
					message.setValue(displayObl.getMessage());
				}
			}
		}
		//Handling unhandled/ignored components
		List<RESOURCE> validResList=new ArrayList<>();
		for( RESOURCE resource : resources.getRESOURCEArray()){
			if(resource.getPROPERTYArray()!=null && resource.getPROPERTYArray().length>0){
				validResList.add(resource);
			}
		}
		RESOURCE[] validResArray = new RESOURCE[validResList.size()];
		resources.setRESOURCEArray(validResList.toArray(validResArray));
		
		List<USER> validUserList=new ArrayList<>();
		for( USER user : users.getUSERArray()){
			if(user.getPROPERTYArray()!=null && user.getPROPERTYArray().length>0){
				validUserList.add(user);
			}
		}
		USER[] validUserArray= new USER[validUserList.size()];
		users.setUSERArray(validUserList.toArray(validUserArray));
		
		List<APPLICATION> validAppList=new ArrayList<>();
		for(APPLICATION application : applications.getAPPLICATIONArray()){
			if(application.getPROPERTYArray()!=null && application.getPROPERTYArray().length>0){
				validAppList.add(application);
			}
		}
		APPLICATION[] validAppArray= new APPLICATION[validAppList.size()];
		applications.setAPPLICATIONArray(validAppList.toArray(validAppArray));
		
		List<LOCATION> validLocList=new ArrayList<>();
		for(LOCATION location : locations.getLOCATIONArray()){
			if(location.getPROPERTYArray()!=null && location.getPROPERTYArray().length>0){
				validLocList.add(location);
			}
		}
		LOCATION[] validLocArray = new LOCATION[validLocList.size()];
		locations.setLOCATIONArray(validLocList.toArray(validLocArray));
		
		USERGROUPMAP userGroupMap=bundle.addNewUSERGROUPMAP();
		addUserGroupMapping(userGroupMap,bundle2.getSubjectKeyMappings(),bundle2.getSubjectToGroup());
		RIGHTSET rightset=bundle.addNewRIGHTSET();
//		System.out.println(nxpackage.toString());
		return policyBundleType;
	}

	private String getRights(ActionPredicateVisitor actionVisitor) {
		Set<String> rightSet = actionVisitor.getRightSet();
		if(rightSet.isEmpty()){
			return null;
		}
		Iterator<String> it= rightSet.iterator();
		StringBuilder rights=new StringBuilder();
		while(it.hasNext()){
			rights.append(it.next()+",");
		}
		return rights.toString().substring(0,rights.length()-1);
	}

	private void addUserGroupMapping(USERGROUPMAP userGroupMap, Collection<ISubjectKeyMapping> subjKepMapping, Map<Long,BitSet> subjToGroup) {
		Iterator it=subjKepMapping.iterator();
		Map<String,UserMap> userSet=new HashMap<String,UserMap>();
		while(it.hasNext()){
			ISubjectKeyMapping mapping=(ISubjectKeyMapping)it.next();
			String uid=mapping.getUid();
			if(subjToGroup.containsKey(mapping.getId())){
				Set<String> groupsSet=new HashSet<String>(Arrays.asList(subjToGroup.get(mapping.getId()).toString().replace("{","").replace("}", "").replace(" ", "").split(",")));
				if(userSet.containsKey(uid)){
					if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
						logger.debug("User with uid :"+uid+ "already present.");
					}
					userSet.get(uid).getGroups().addAll(groupsSet);
				}
				else{
					PolicyTransformer.UserMap userMap = new PolicyTransformer.UserMap();
					userMap.setId(uid);
					userMap.setContext(mapping.getId()+"");
					userMap.setGroups(groupsSet);
					userSet.put(uid, userMap);
				}
			}
		}
		USERMAP usermap;
		for(UserMap userMap:userSet.values()){
			usermap=userGroupMap.addNewUSERMAP();
			usermap.setId(userMap.getid());
			usermap.setContext(userMap.getContext());
			String groups=userMap.getGroups().toString().replaceAll("\\[|\\]", "");
			if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_WEBSVC_DEBUG_LOGS)){
				logger.debug("User with uid :"+userMap.getid()+ "has groups: "+groups);
			}
			usermap.newCursor().setTextValue(groups);
		}
	}

	public static String getCompareMethod(String compareVariable){
		if(compareVariable.equalsIgnoreCase("=")){
			return "EQ";
		}
		else if(compareVariable.equalsIgnoreCase("!=")){
			return "NE";

		}
		else if(compareVariable.equalsIgnoreCase("<")){
			return "LT";

		}
		else if(compareVariable.equalsIgnoreCase(">")){
			return "GT";

		}
		else if(compareVariable.equalsIgnoreCase("<=")){
			return "LE";

		}
		else if(compareVariable.equalsIgnoreCase(">=")){
			return "GE";

		}
		else if(compareVariable.equalsIgnoreCase("~#")){
			return "has";

		}
		return null;
	}
	
	public static String escapeUnwantedQuotes(String unescapedString){
		if(unescapedString.startsWith("\"") && unescapedString.endsWith("\"")){
			unescapedString=unescapedString.substring(1, unescapedString.length()-1);
		}
		String escapedString=unescapedString.replaceAll("\\\\\\\\", Matcher.quoteReplacement("\\") );
		return escapedString;
	}
	
	private static class UserMap {

		  private String id;

			private String context;

			private Set<String> groups;

			public String getid() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getContext() {
				return context;
			}

			public void setContext(String context) {
				this.context = context;
			}

			public Set<String> getGroups() {
				return groups;
			}

			public void setGroups(Set<String> groups) {
				this.groups = groups;
			}
		}
}
