package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.Parameters;
import com.nextlabs.rms.eval.UnsupportedComponentException;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.*;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS.APPLICATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.ENVS.ENV;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS.LOCATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS.USER;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.CONDITION;
import noNamespace.PropertyType;

public class PolicyComponentParserV5 implements IPolicyComponentParser{
	private static final String USER_GROUP = "user.GROUP";
	public static final String POLICY_OBJECT_KEY = "POLICY_OBJECT";
	public static final String NODE_TREE_KEY = "NODE_TREE";
	public static final String USER_ATTRIB = "user.";
	public static final String APP_ATTRIB = "application.";
	public static final String LOC_ATTRIB = "host.";
	public static final String ENV_ATTRIB = "ENVIRONMENT.";
	public static final String ENV_TIME_ATTRIB = "CURRENT_TIME.";
	public static final String RES_ATTRIB = "resource.fso.";
	public static final String COMPONENTTYPE_RES = "res";
	public static final String COMPONENTTYPE_USER = "usr";
	public static final String COMPONENTTYPE_APP = "app";
	public static final String COMPONENTTYPE_LOC = "loc";
	public static final String COMPONENTTYPE_ENV = "env";
	private static final String RES_STRING = "resource.";

	private static final Parameters PARAMETERS;
	static {
		// Create the evaluator's parameters
		PARAMETERS = new Parameters();
		// Add the supported operators
		PARAMETERS.add(PolicyComponentHelperV2.AND);
		PARAMETERS.add(PolicyComponentHelperV2.OR);
		PARAMETERS.add(PolicyComponentHelperV2.NOT);
		// Add the parentheses
		PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
	}

	private int runningResId = 0;
	private int runningAppId = 0;
	private int runningLocId = 0;
	private int runningUserId = 0;
	private int runningEnvId = 0;
	private RESOURCES resources;
	private USERS users;
	private APPLICATIONS apps;

	private LOCATIONS locs;
	private ENVS envs;

    private static ExpressionEvaluatorV2 ev;
    public Map<String, String> operandMap;

	public PolicyComponentParserV5(RESOURCES res, USERS users, APPLICATIONS apps, LOCATIONS locs, ENVS envs) {
        operandMap = new HashMap<String, String>();
        ev = new ExpressionEvaluatorV2(PolicyComponentHelperV2.NOT.getSymbol(), PolicyComponentHelperV2.AND.getSymbol(), PolicyComponentHelperV2.OR.getSymbol());

		this.resources = res;
		this.users = users;
		this.apps = apps;
		this.locs = locs;
		this.envs = envs;
	}
	
	public APPLICATIONS getApps() {
		return apps;
	}

	private String getComponentType(String str) {
		if (str == null || str.length() == 0) {
			return "";
		}
		if(str.startsWith(PolicyComponentHelperV2.NOT.getSymbol())){
			str = str.substring(PolicyComponentHelperV2.NOT.getSymbol().length()).trim();
		}
		if (str.startsWith(APP_ATTRIB)) {
			return COMPONENTTYPE_APP;
		} else if (str.startsWith(LOC_ATTRIB)) {
			return COMPONENTTYPE_LOC;
		} else if (str.startsWith(ENV_ATTRIB)) {
			return COMPONENTTYPE_ENV;
		} else if (str.startsWith(ENV_TIME_ATTRIB)) {
			return COMPONENTTYPE_ENV;
		} else if (str.startsWith(RES_ATTRIB)) {
			return COMPONENTTYPE_RES;
		} else if (str.startsWith(USER_ATTRIB)) {
			return COMPONENTTYPE_USER;
		}
		return "";
	}

	public ENVS getEnvs() {
		return envs;
	}

	public LOCATIONS getLocs() {
		return locs;
	}

	public RESOURCES getResources() {
		return resources;
	}

	private int getRunningId(String compType) {
		switch (compType) {
		case COMPONENTTYPE_APP:
			return runningAppId++;
		case COMPONENTTYPE_ENV:
			return runningEnvId++;
		case COMPONENTTYPE_LOC:
			return runningLocId++;
		case COMPONENTTYPE_RES:
			return runningResId++;
		case COMPONENTTYPE_USER:
			return runningUserId++;
		}
		return -1;
	}
	
	public int revertRunningUserId(){
		return --runningUserId;
	}

	public USERS getUsers() {
		return users;
	}

	private void processComponents(POLICY policy, String pqlString) {
		if (pqlString == null || pqlString.length() == 0) {
            return;
        }
        String processedSubStr = replaceLogicalOperators(pqlString);
        List<String> compList = new ArrayList<String>();
        if (processedSubStr.startsWith("BY ")) {
            processedSubStr = processedSubStr.trim().substring(3);
            ComponentStringSplitter splitter = new ComponentStringSplitter();
            compList.addAll(splitter.splitSubjects(processedSubStr));
        } else if (processedSubStr.startsWith("WHERE ")) {
        	processedSubStr = processedSubStr.trim().substring(6);
        	//Do regex magic here and seperate out 3 parts of advanced condition and send each of the parts to componentStringSplitter
            ComponentStringSplitter splitter = new ComponentStringSplitter();
            List<String> parts = splitter.getParts(processedSubStr);
            for(String part:parts){
            	compList.addAll(splitter.splitSubjects(part));
            }
        } else if (processedSubStr.startsWith("FOR ")) {
            processedSubStr = processedSubStr.trim().substring(4);
        	compList.add(processedSubStr);
        }
        if ((processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || processedSubStr
                .equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))) {
            return;
        }
        for (String pqlExp : compList) {
        	String lastEvalOpen = TreeBooleanEvaluatorV2.doIt(pqlExp);
            addConditionAndProperties(policy, lastEvalOpen);
        }
    }

	private void addConditionAndProperties(POLICY policy, String pqlString) {
		// application group can be ignored
        // user group should ignored, but added to the policy in a different area
        // NOT in front of any group should be ignored
        // NOT in front of any other property must be evaluated
        //     this means that NOT a < b becomes a >= b
        //                     NOT a >=b becomes a < b
        // Note: We may want to use trim() here
		String compType = getComponentType(pqlString);
		String[] components = pqlString.trim().split(PolicyComponentHelperV2.OR.getSymbol());

        String stringValue = "";

        switch(compType){
	        case COMPONENTTYPE_APP:
	        	stringValue = handleApplicationComponent(compType, components, stringValue);
	        break;
	        case COMPONENTTYPE_USER:
	        	stringValue = handleUserComponent(policy, compType, components, stringValue);
	        break;
	        case COMPONENTTYPE_LOC:
	        	stringValue = handleLocationComponent(compType, components, stringValue);
	        break;
	        case COMPONENTTYPE_RES:
			stringValue = handleResourceComponent(compType, components, stringValue);
	        break;
	        default:
	        		if(components[0].trim().startsWith(USER_ATTRIB) || components[0].startsWith(PolicyComponentHelperV2.NOT.getSymbol()+USER_ATTRIB)){
	        			compType = COMPONENTTYPE_USER;
	        			stringValue = handleUserComponent(policy,compType , components, stringValue);
	        		}else if(components[0].trim().startsWith(APP_ATTRIB) || components[0].startsWith(PolicyComponentHelperV2.NOT.getSymbol()+APP_ATTRIB)){
	        			compType = COMPONENTTYPE_APP;
	        			stringValue = handleApplicationComponent(compType, components, stringValue);
	        		}else if(components[0].trim().startsWith(LOC_ATTRIB) || components[0].startsWith(PolicyComponentHelperV2.NOT.getSymbol()+LOC_ATTRIB)){
	        			compType = COMPONENTTYPE_LOC;
	        			stringValue = handleLocationComponent(compType, components, stringValue);
	        		}else if(components[0].trim().startsWith(RES_STRING) || components[0].startsWith(PolicyComponentHelperV2.NOT.getSymbol()+RES_STRING)){
	        			compType = COMPONENTTYPE_RES;
	        			stringValue = handleResourceComponent(compType, components, stringValue);
	        		}
	        		else{
	        			stringValue = handleEnvironmentComponent(components, stringValue);
			            compType=COMPONENTTYPE_ENV;
	        		}
	        	}
        
        if(stringValue!=null && stringValue.length()!=0){
	        CONDITION condition = policy.addNewCONDITION();
	        condition.setType(compType);
	        condition.setExclude(false);
	        condition.setStringValue(stringValue);
        }
	}

	private String handleEnvironmentComponent(String[] components, String stringValue) {
		for (int i=0; i < components.length; ++i) {
			String[] properties = components[i].trim().split(PolicyComponentHelperV2.AND.getSymbol());
			int runningId = getRunningId(COMPONENTTYPE_ENV);
		    ENV env = envs.insertNewENV(runningId);
		    env.setId(runningId);
			if (i > 0) {
				stringValue += ",";
			}
			stringValue += runningId;
		    for (int j=0; j < properties.length; ++j) {
		    	if (!properties[j].trim().equals( "TRUE" )) {
		    		PropertyType propertyType = env.addNewPROPERTY();
		    		fillPropertyType(properties[j], propertyType);
		    	}
		    }
		}
		return stringValue;
	}

	private String handleResourceComponent(String compType, String[] components, String stringValue) {
		for (int i=0; i < components.length; ++i) {
			int runningId = getRunningId(compType);
		    RESOURCE res = resources.insertNewRESOURCE(runningId);
		    res.setId(runningId);
			if (i > 0) {
				stringValue += ",";
			}
			stringValue += runningId;
		    String[] properties = components[i].trim().split(PolicyComponentHelperV2.AND.getSymbol());
		    for (int j=0; j < properties.length; ++j) {
		        PropertyType propertyType = res.addNewPROPERTY();
		        fillPropertyType(properties[j], propertyType);
		    }
		}
		return stringValue;
	}

	private String handleLocationComponent(String compType, String[] components, String stringValue) {
		for (int i=0; i < components.length; ++i) {
			int runningId = getRunningId(compType);
		    LOCATION loc = locs.insertNewLOCATION(runningId);
		    loc.setId(runningId);
			if (i > 0) {
				stringValue += ",";
			}
			stringValue += runningId;
		    String[] properties = components[i].trim().split(PolicyComponentHelperV2.AND.getSymbol());
		    for (int j=0; j < properties.length; ++j) {
		        PropertyType propertyType = loc.addNewPROPERTY();
		        fillPropertyType(properties[j], propertyType);
		    }
		}
		return stringValue;
	}

	private String handleUserComponent(POLICY policy, String compType, String[] components, String stringValue) {
		Set<String> userGroupSet = new HashSet<>();
		for (int i=0; i < components.length; ++i) {
			String[] properties = components[i].trim().split(PolicyComponentHelperV2.AND.getSymbol());
			boolean hasProp = false;
			//Doing this in order to avoid creating a user component when there is only user.Group properties
			for(int a=0; a<properties.length;a++){
				if(!properties[a].startsWith(USER_GROUP) && !properties[a].startsWith(PolicyComponentHelperV2.NOT.getSymbol()+USER_GROUP)){
					hasProp = true;
					break;
				}
			}
			if(!hasProp){
				for(int a=0;a<properties.length;a++){
					processUserGroup(policy, properties[a],userGroupSet);
				}
			}else{
				int runningId = getRunningId(compType);
			    USER user = users.insertNewUSER(runningId);
			    user.setId(runningId);
				if (stringValue!=null && stringValue.trim().length()>0) {
					stringValue += ",";
				}
				stringValue += runningId;
			    for (int j=0; j < properties.length; ++j) {
			    	if(properties[j].startsWith(USER_GROUP)|| properties[j].startsWith(PolicyComponentHelperV2.NOT.getSymbol()+USER_GROUP)){
			    		processUserGroup(policy, properties[j],userGroupSet);
			            continue;
			    	}
			        PropertyType propertyType = user.addNewPROPERTY();
			        fillPropertyType(properties[j], propertyType);
			    }
			}
		}
		return stringValue;
	}

	private String handleApplicationComponent(String compType, String[] components, String stringValue) {
		for (int i=0; i < components.length; ++i) {
			int runningId = getRunningId(compType);
		    APPLICATION app = apps.insertNewAPPLICATION(runningId);
		    app.setId(runningId);
			if (i > 0) {
				stringValue += ",";
			}
			stringValue += runningId;
		    String[] properties = components[i].trim().split(PolicyComponentHelperV2.AND.getSymbol());
		    for (int j=0; j < properties.length; ++j) {
		        PropertyType propertyType = app.addNewPROPERTY();
		        fillPropertyType(properties[j], propertyType);
		    }
		}
		return stringValue;
	}

	private void processUserGroup(POLICY policy, String property, Set<String> userGroupSet) {
		//Add the user group to the policy
		String pattern = "^(\".*\"|[^\"]*) (~#) (\".*\"|[^\"]*)$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(property);
		String userGroupName,operator,userGroupVal = null;
		if (m.find()) {
		    userGroupName = PolicyTransformer.escapeUnwantedQuotes(m.group(1).trim());
		    operator = m.group(2).trim();
		    userGroupVal = PolicyTransformer.escapeUnwantedQuotes(m.group(3).trim());
		}
		if(userGroupVal!=null && userGroupVal.length()>0){
			//Eliminate duplicate user groups
			if(!userGroupSet.contains(userGroupVal)){
				String usergroup = policy.getUsergroup();
				if(usergroup!=null && usergroup.length()>0){
					usergroup+=",";
				}if(usergroup == null){
					usergroup="";
				}
				usergroup+=userGroupVal;
				policy.setUsergroup(usergroup);
				userGroupSet.add(userGroupVal);
			}
		}
	}

    private void fillPropertyType(String property, PropertyType propertyType) {
    	String operand1 = "", operand2 = "", operator = "";
        property = property.trim();
        boolean hasNOT = property.substring(0, 2).equals("!!");
        if (hasNOT) {
            property = property.substring(2);
        }

        String pattern = "^(.*) (=|!=|<|>|<=|>=|~#) (\".*\"|[^\"]*)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(property);
        if (m.find()) {
            operand1 = PolicyTransformer.escapeUnwantedQuotes(m.group(1).trim());
            operator = m.group(2).trim();
            operand2 = PolicyTransformer.escapeUnwantedQuotes(m.group(3).trim());
        }
        String compareMethod = PolicyTransformer.getCompareMethod(operator);
        if (hasNOT) {
            compareMethod = PolicyComponentHelperV2.negateRelationalOperator(compareMethod);
        }
        //Remove the prefix
        if(operand1.startsWith(USER_ATTRIB)){
        	operand1 = operand1.substring(USER_ATTRIB.length());
        }else if(operand1.startsWith(APP_ATTRIB)){
        	operand1 = operand1.substring(APP_ATTRIB.length());
        }else if(operand1.startsWith(LOC_ATTRIB)){
        	operand1 = operand1.substring(LOC_ATTRIB.length());
        }else if(operand1.startsWith(RES_ATTRIB)){
        	operand1 = operand1.substring(RES_ATTRIB.length());
        }else if(operand1.startsWith(ENV_ATTRIB)||operand1.startsWith(ENV_TIME_ATTRIB)){
        }else{
        	throw new UnsupportedComponentException("Unsupported Element type");
        }
        operand1 = operand1.replaceAll("\"(.*)\"", "$1");
        propertyType.setName(operand1);
        propertyType.setMethod(compareMethod);
        propertyType.setValue(operand2);

        return;
    }

	@Override
	public void processPolicy(POLICY pol, String resourceStr, String subjectStr, String envStr) {
		processComponents(pol, subjectStr);
		processComponents(pol, resourceStr);
		processComponents(pol, envStr);
	}

	private String replaceLogicalOperators(String inputStr) {
		String replacedStr = inputStr.replaceAll("(?m)\\bOR\\b(?=(?:\"[^\"]*\"|[^\"])*$)", PolicyComponentHelperV2.OR.getSymbol());
		replacedStr = replacedStr.replaceAll("(?m)\\bAND\\b(?=(?:\"[^\"]*\"|[^\"])*$)", PolicyComponentHelperV2.AND.getSymbol());
		replacedStr = replacedStr.replaceAll("(?m)\\bNOT\\b(?=(?:\"[^\"]*\"|[^\"])*$)", PolicyComponentHelperV2.NOT.getSymbol());
		replacedStr = replacedStr.replaceAll("(?m)\\bhas\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "~#");
		return replacedStr;
	}

}
