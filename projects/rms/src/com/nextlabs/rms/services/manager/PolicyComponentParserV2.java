package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS.APPLICATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.ENVS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.ENVS.ENV;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS.LOCATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS.USER;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.CONDITION;
import noNamespace.PropertyType;

public class PolicyComponentParserV2 {
	
	public static final String RESOURCE_FSO = "resource.fso.";
	public static final String USER_ATTRIB = "user.";
	public static final String APP_ATTRIB = "application.";
	public static final String LOC_ATTRIB = "host.";
	public static final String ENV_ATTRIB = "env.";
	public static final String RES_ATTRIB = "resource.fso.";
	public static final String OPERATOR_AND = "&&";
	public static final String OPERATOR_OR = "||";
	public static final String OPERATOR_NOT = "!!";
	public static final String OPERATOR_HAS = "~#";
	public static final String CONDITIONTYPE_RES = "res";
	public static final String CONDITIONTYPE_USER = "usr";
	public static final String CONDITIONTYPE_APP = "app";
	public static final String CONDITIONTYPE_LOC = "loc";
	public static final String CONDITIONTYPE_ENV = "env";
	
	private int runningUserId = 0;
	private int runningLocId = 0;
	private int runningAppId = 0;
	private int runningResId = 0;
	private int runningEnvId = 0;
	private HashMap<String, String> expressionMap = new HashMap<String, String>();

	/*
	public static void main(String[] args) {
//		ConfigManager.getInstance().loadConfigFromDB();
//		String testSubject="BY (user.GROUP has 0 AND (application.size = 0 AND application.name != \"**\\\\rms1.exe\" AND application.fakeproperty1 = \"**\\\\rms\"))";//
		// OR (application.GROUP has 1 AND
		// application.\"dummy property\" = \"Dummy Property 1\")) AND (application.GROUP has 2 AND application.fakeproperty3 = \"**\\notepad\" AND application.dummyproperty1 = \"dummy\" AND application.dummyproperty2 = \"dumm2\") AND NOT ((application.GROUP has 3 AND application.fakeproperty4 = \"4\"))))";
		// String
		// testSubject="BY (user.GROUP has 0 AND ((application.prop = \"prop\" AND application.pro = \"pro\") AND application.blah1 = \"blah1\" AND (application.prop1 = \"prop1\" AND application.prop2 = \"prop2\") AND ((application.prop3 = \"2.45AM\" AND application.prop4 = \"2.45 AM\") AND application.blah = \"blah\"))";
//		 String testSubject="BY ((user.locationcode = \"SG\" AND user.user_location_code = \"IN\") AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\") AND (user.GROUP has 2 AND user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\"))";
//		 String testRes="FOR resource.fso.name = \"**.pdf.nxl\"";
//		String testRes = "FOR resource.fso.nam1 = val1 AND NOT (resource.fso.ear = \"EAR-01\" AND resource.fso.ear = \"EAR-02\" AND resource.fso.ear = \"EAR-03\" AND resource.fso.ear = \"EAR-04\" AND resource.fso.bafa = \"BAFA-01\") OR (resource.fso.program = \"PR-02\" AND resource.fso.program = \"PR-09\")";
//		String testRes = "FOR NOT (resource.fso.ear = EAR01 AND resource.fso.ear2 = EAR02) OR resource.fso.ear3 = EAR03";
//		String testRes = "FOR (resource.fso.program = \"PR-01\" AND resource.fso.bafa = \"BAFA-01\" AND resource.fso.name1 = val1 AND resource.fso.name2 = val2)";
//		 String testSubject="BY (user.GROUP has 0 AND ((application.path = \"**\\acrord32.exe\" AND application.publisher = \"adobe systems, incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\EXCEL.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"SAP SE\")))";
//		 String testSubject="BY (host.GROUP = 0 OR user.name = pranava AND user.GROUP has 0 AND ((application.path = \"**\\acrord32.exe\" AND application.publisher = \"adobe systems, incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\EXCEL.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"SAP SE\")))";
//		 String testSubject="BY ((application.path = \"**\\acrord32.exe\" AND application.publisher = \"adobe systems, incorporated\" AND application.name = AdobeAcrobat) OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWRD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\EXCEL.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"SAP SE\"))";
//		String testSubject="BY (user.u1 = v1 AND user.GROUP has 0 OR ((application.n1 = V1 AND application.n2 > V2 AND application.n3 = V3 AND application.\"name with space\" != \"value with space\") OR (user.name = nameVal AND user.\"loc with space\" <= \"San Mateo\")) OR (host.h1 = hh AND host.h2 = \"with space\"))";//AND (host.h1 = hh AND host.h2 = \"with space\")(user.u1 = v1 OR
//		String testSubject = "BY ((user.locationcode = \"SG\" AND user.user_location_code = \"IN\") AND (user.GROUP has 2 AND user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\") AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\"))";
//		String testSubject = "BY (user.u1 = v1 AND host.name = prince OR host.ip = 11111 OR (application.name = Microsoft AND application.url = \"http://localhost:8443/RMS\"))" ;
//		String testSubject = "BY user.Group has 0 OR (host.name = prince AND host.param = value) AND NOT (application.name = ACROBAT AND application.path = \\*Drive\\*)";
//		String testSubject = "BY user.Group has 0";
//		String testSubject = "BY application.designation = Manager OR host.name = Acrobat OR application.param1 = val1";
//		String testSubject = "BY user.Group has 0 OR host.name = prince AND application.name = ACROBAT OR application.path = \\*Drive\\*";
//		String testSubject = "BY user.Group has 0";
//		String testRes="FOR (resource.fso.name1 = val1 OR (resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"sudhi\") AND (resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") OR resource.fso.param1 = val1)";
//		String testRes = "FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 OR resource.fso.name3 = val3 OR resource.fso.name4 = val4";
//		String testRes = "FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 AND resource.fso.name3 = val3 OR resource.fso.name4 = val4";
//		 String
//		 testRes="FOR ((resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") AND NOT ((resource.fso.name = \"**\" OR resource.fso.name = \"**\")) )";
//		String testSubject = "BY (user.u1 = v1 AND ((application.n1 = V1 AND application.n2 > V2 AND application.n3 = V3 AND application.\"name with space\" != \"value with space\") OR (user.name = nameVal AND user.\"loc with space\" <= \"San Mateo\")) OR (host.h1 = hh AND host.h2 = \"with space\"))";
		
//		String testRes = "FOR ((resource.fso.comp3 = \"property1\" OR resource.fso.comp1 = \"property2\") AND (resource.fso.comp5 = \"property5\" AND resource.fso.comp4 = \"property4\"))";		
		String testRes = "FOR TRUE";
		String testSubject = "BY TRUE";
//		String testEnv = "WHERE TRUE"; 
		String testEnv = "WHERE (TRUE AND (ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND TRUE))"; 
//		String testEnv = "WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 180000 AND ENVIRONMENT.REMOTE_ACCESS = 1 AND CURRENT_TIME.identity >= \"Jun 24, 2015 11:14:00 AM\" AND CURRENT_TIME.identity <= \"Jul 10, 2015 11:14:28 AM\" AND CURRENT_TIME.time >= \"11:14:00 AM\" AND CURRENT_TIME.time <= \"5:14:39 PM\" AND CURRENT_TIME.day_in_month = 1 AND CURRENT_TIME.weekday = \"Sunday\") AND resource.fso.abc = def))"; 
				
		// String testSubject="BY user.GROUP has 3";
//		String testSubject = "BY user.groups = \"Support Analyst\"";
		// String testSubject = "BY NOT (application.path = \"**\\excel.exe\")";
		POLICY pol = POLICY.Factory.newInstance();
		RESOURCES res = RESOURCES.Factory.newInstance();
		USERS users = USERS.Factory.newInstance();
		APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
		LOCATIONS locs = LOCATIONS.Factory.newInstance();
		ENVS envs = ENVS.Factory.newInstance();
		PolicyComponentParserV2 parser = new PolicyComponentParserV2();
		parser.processPolicy(pol, testRes, testSubject, testEnv, res, users, apps, locs, envs);
		// parser.processedPolicy(POLICY.Factory.newInstance(),testRes,testSubject,RESOURCES.Factory.newInstance(),USERS.Factory.newInstance(),APPLICATIONS.Factory.newInstance(),LOCATIONS.Factory.newInstance());
		System.out.println("------------------>");
		System.out.println("POLICY:::" + pol.toString());
		System.out.println("RESOURCES:::" + res.toString());
		System.out.println("USERS:::" + users.toString());
		System.out.println("APPS:::" + apps.toString());
		System.out.println("LOCS:::" + locs.toString());
		System.out.println("ENVS:::" + envs.toString());
	}
*/
	public void processPolicy(POLICY pol, String resourceStr, String subjectStr, String envStr,
			RESOURCES res, USERS users, APPLICATIONS apps, LOCATIONS locs, ENVS envs) {
		expressionMap.clear();
		parseSubjects(pol, subjectStr, users, apps, locs);
		parseResources(pol, resourceStr, res);
		parseEnv(pol, envStr, envs);
	}
	
	private String replaceLogicalOperators(String inputStr){
		String replacedStr = inputStr.replaceAll("(?m)\\bOR\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "||");
		replacedStr =  replacedStr.replaceAll("(?m)\\bAND\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "&&");
		replacedStr =  replacedStr.replaceAll("(?m)\\bNOT\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "!!");
		replacedStr = replacedStr.replaceAll("(?m)\\bhas\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "~#");
		return replacedStr;
	}

	private void parseEnv(POLICY pol, String envStr, ENVS envs) {
		if(envStr == null || envStr.length()==0){
			return;
		}
//		subjectStr.replaceAll(" ABD ", replacement)
		String processedSubStr = "";
		if(envStr.startsWith("WHERE ")){
			processedSubStr = envStr.trim().substring(6);
		}
		if((processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))){
			return;
		}
		processedSubStr = replaceLogicalOperators(processedSubStr);
		TreeBooleanEvaluator evaluator = new TreeBooleanEvaluator();
		List<Component> sequence = new ArrayList<Component>();
		evaluator.evaluate(processedSubStr, sequence);
		ENV env = envs.addNewENV();
		env.setId(runningEnvId++);
		String lastParsedCondition = "";
		String lastParsedExpression = "";
		for (Component component : sequence) {
			System.out.println(component.getEquation());
			String condition1 = component.getOperand1();
			String condition2 = component.getOperand2();
			if(component.getOperator().equals(OPERATOR_AND)){
				String conditionToHandle = getComponentToHandle(pol, lastParsedCondition, component);
				if(conditionToHandle==null){//add both conditions
					addEnvProperty(condition1, env);
					addEnvProperty(condition2, env);
				}else{
					addEnvProperty(conditionToHandle, env);
				}
				lastParsedCondition =  component.getEquation();
			}
		}
		if(env.getPROPERTYArray()!=null && env.getPROPERTYArray().length>0){
			CONDITION condition = pol.addNewCONDITION();
			condition.setType(CONDITIONTYPE_ENV);
			condition.setExclude(false);
			condition.setStringValue(env.getId()+"");
		}
	}

	private void addEnvProperty(String condition, ENV env) {
		if(!(condition.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || condition.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))){
			PropertyType prop = env.addNewPROPERTY();
			Component splitComponent = getLHSandRHS(condition);
			updatePropertyAttribs(splitComponent, prop, null);
		}
	}

	private void parseResources(POLICY pol, String resourceStr, RESOURCES resources) {
		if(resourceStr == null || resourceStr.length()==0){
			return;
		}
//		subjectStr.replaceAll(" ABD ", replacement)
		String processedSubStr = "";
		if(resourceStr.startsWith("FOR ")){
			processedSubStr = resourceStr.trim().substring(4);
		}
		if((processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))){
			return;
		}
		processedSubStr = replaceLogicalOperators(processedSubStr);
		TreeBooleanEvaluator evaluator = new TreeBooleanEvaluator();
		List<Component> sequence = new ArrayList<Component>();
		evaluator.evaluate(processedSubStr, sequence);
		RESOURCE res = null;
		String lastParsedCondition = "";
		String lastParsedExpression = "";
		int counter = 0;
		if(sequence.size()==0){
			//This doesn't have AND or OR. Just one condition
			res = resources.addNewRESOURCE();
			expressionMap.put(processedSubStr, CONDITIONTYPE_RES+"::"+runningResId);
			res.setId(runningResId++);
			PropertyType prop;
			addNewResProperty(res, processedSubStr);
			addConditionToPolicy(pol, null, processedSubStr);
			return;
		}
		for (Component component : sequence) {
			System.out.println(component.getEquation());
			if(component.getOperand2()==null){
				//unary operator(NOT).
				if(component.getOperand1().equalsIgnoreCase(lastParsedExpression)){
					negateConditionInPolicy(lastParsedExpression, pol);
					lastParsedExpression = component.getEquation();
				}else if(component.getOperand1().equalsIgnoreCase(lastParsedCondition)){
					addConditionToPolicy(pol, component, lastParsedCondition);
					negateConditionInPolicy(lastParsedCondition, pol);
					lastParsedExpression = component.getEquation();
				}else{
					res = resources.addNewRESOURCE();
					expressionMap.put(component.getOperand1(), CONDITIONTYPE_RES+"::"+runningResId);
					res.setId(runningResId++);
					addNewResProperty(res, component.getOperand1());
					addConditionToPolicy(pol, component, component.getOperand1());
					negateConditionInPolicy(component.getOperand1(), pol);
					lastParsedExpression = component.getEquation();
				}
				continue;
			}
			String condition1 = component.getOperand1();
			String condition2 = component.getOperand2();
			if(condition1.startsWith("(") && condition2.startsWith("(")){
				String componentToHandle = ""; 
				componentToHandle = getComponentToHandle(pol, lastParsedExpression, component);
				addConditionToPolicy(pol, component, componentToHandle);
				//We are comparing two components here. Update the condition.
				lastParsedExpression = component.getEquation();
			}else if (component.getOperator().equals(OPERATOR_OR)){
				String componentToHandle = ""; 
				componentToHandle = getComponentToHandle(pol, lastParsedExpression, component);
				if(componentToHandle == null){
					//add both components to condition
					String conditionToHandle = getComponentToHandle(pol, lastParsedCondition, component);
					if(conditionToHandle==null){
						//add both conditions to resource
						res = resources.addNewRESOURCE();
						expressionMap.put(condition1, CONDITIONTYPE_RES+"::"+runningResId);
						res.setId(runningResId++);
						addNewResProperty(res, condition1);
						res = resources.addNewRESOURCE();
						expressionMap.put(condition2, CONDITIONTYPE_RES+"::"+runningResId);
						res.setId(runningResId++);
						addNewResProperty(res, condition2);
//						addConditionToPolicy(pol, component, condition2);
					}else{
						res = resources.addNewRESOURCE();
						expressionMap.put(conditionToHandle, CONDITIONTYPE_RES+"::"+runningResId);
						res.setId(runningResId++);
						addNewResProperty(res, condition1);
					}
					addConditionToPolicy(pol, component, condition1);
					addConditionToPolicy(pol, component, condition2);
				}else{
					res = resources.addNewRESOURCE();
					expressionMap.put(componentToHandle, CONDITIONTYPE_RES+"::"+runningResId);
					res.setId(runningResId++);
					addNewResProperty(res, componentToHandle);
					addConditionToPolicy(pol, component, componentToHandle);					
				}
				//We are comparing two components here. Update the condition.
				lastParsedExpression = component.getEquation();
			}else{//Add this to the current Resource Component
				PropertyType prop = null;
				String conditionToHandle = null;
				String handledCondition = null;
				if(condition1.equalsIgnoreCase(lastParsedCondition) || condition1.equalsIgnoreCase(lastParsedExpression)){
					conditionToHandle = condition2;
					handledCondition = condition1;					
				}else if(condition2.equalsIgnoreCase(lastParsedCondition) || condition2.equalsIgnoreCase(lastParsedExpression)){
					conditionToHandle = condition1;
					handledCondition = condition2;
				}
				if(conditionToHandle==null){//Both are new conditions. Add both.
					res = resources.addNewRESOURCE();
					expressionMap.put(component.getEquation(), CONDITIONTYPE_RES+"::"+runningResId);
					res.setId(runningResId++);
					addNewResProperty(res, condition1);
					addNewResProperty(res, condition2);
					lastParsedCondition = component.getEquation();
					if(counter == sequence.size()-1){
						addConditionToPolicy(pol, component, component.getEquation());
						lastParsedCondition = component.getEquation();
					}
				}else{
					if(handledCondition.equalsIgnoreCase(lastParsedExpression)){
					//condition2 has already been added to Policy. Just add the new condition to policy.
						if(conditionToHandle.trim().startsWith(OPERATOR_NOT)){//This is a NOT condition. This should have already been added to the policy
							break;
						}
						res = resources.addNewRESOURCE();
						expressionMap.put(conditionToHandle, CONDITIONTYPE_RES+"::"+runningResId);
						res.setId(runningResId++);
						addNewResProperty(res, conditionToHandle);						
						addConditionToPolicy(pol, component, conditionToHandle);
						lastParsedCondition = conditionToHandle;
					}else{
						addNewResProperty(res, conditionToHandle);
						updateExpressionMap(lastParsedCondition, component);
						if(counter == sequence.size()-1){
							addConditionToPolicy(pol, component, component.getEquation());
							lastParsedCondition = component.getEquation();
						}
					}
					lastParsedCondition = component.getEquation();
				}
				
			}
			counter++;
		}
	}

	private void negateConditionInPolicy(String lastParsedExpression, POLICY pol) {
		String op = expressionMap.get(lastParsedExpression);
		String type = op.substring(0, 3);
		String id = op.substring(5);
		CONDITION[] condArr = pol.getCONDITIONArray();
		for (CONDITION condition : condArr) {
			if(condition.getType().equalsIgnoreCase(type) && condition.getStringValue().equalsIgnoreCase(id)){
				condition.setExclude(true);
			}
		}
	}

	private void addNewResProperty(RESOURCE res, String condition) {
		PropertyType prop = res.addNewPROPERTY();
		updateProperties(condition, prop);
	}

	private void parseSubjects(POLICY pol, String subjectStr, USERS users,
			APPLICATIONS apps, LOCATIONS locs) {
		if(subjectStr == null || subjectStr.length()==0){
			return;
		}
//		subjectStr.replaceAll(" ABD ", replacement)
		String processedSubStr = "";
		if(subjectStr.startsWith("BY ")){
			processedSubStr = subjectStr.trim().substring(3);
		}
		if((processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))){
			return;
		}
		processedSubStr = replaceLogicalOperators(processedSubStr);
		TreeBooleanEvaluator evaluator = new TreeBooleanEvaluator();
		List<Component> sequence = new ArrayList<Component>();
		evaluator.evaluate(processedSubStr, sequence);
		USER user = null;
		LOCATION loc = null;
		APPLICATION app = null;
		String lastParsedCondition = "";
		String lastParsedExpression = "";
		int counter = 0;
		if(sequence.size()==0){
			//This doesn't have AND or OR. Just one condition
			if(isHasCondition(processedSubStr)){
				addUserGroupToPolicy(pol, processedSubStr);
			}else{
				PropertyType prop = createNewCompAndProperty(users, apps, locs, processedSubStr, null);
				updateProperties(processedSubStr, prop);
//				String componentToHandle = getComponentToHandle(pol, lastParsedExpression, comp);
				addConditionToPolicy(pol, null, processedSubStr);							
			}
		}
		for (Component comp : sequence) {
			System.out.println(comp.getEquation());
			if(comp.getOperand2()==null){
				//unary operator(NOT). Handle accordingly.
			}else{
				String condition1 = comp.getOperand1();
				String condition2 = comp.getOperand2();
				if(counter == 0){//first time entry into loop
					PropertyType prop = null;
//					handleBothConditions(condition1, condition2, users, apps, locs, user, loc, app, comp);
					//handle both conditions
					if(comp.getOperator().equalsIgnoreCase(OPERATOR_AND) && isSameComponents(condition1, condition2)){
						if(condition1.startsWith(USER_ATTRIB)){
							user = users.addNewUSER();
							expressionMap.put(comp.getEquation(), CONDITIONTYPE_USER+"::"+runningUserId);
							user.setId(runningUserId++);
							createNewUserProperty(pol, user, condition1);
							createNewUserProperty(pol, user, condition2);						
						}else if(condition1.startsWith(LOC_ATTRIB)){
							loc = locs.addNewLOCATION();
							expressionMap.put(comp.getEquation(), CONDITIONTYPE_LOC+"::"+runningLocId);
							loc.setId(runningLocId++);
							createNewLocProperty(loc, condition1);
							createNewLocProperty(loc, condition2);						
						}else if(condition1.startsWith(APP_ATTRIB)){
							app = apps.addNewAPPLICATION();
							expressionMap.put(comp.getEquation(), CONDITIONTYPE_APP+"::"+runningAppId);
							app.setId(runningAppId++);
							createNewAppProperty(app, condition1);
							createNewAppProperty(app, condition2);						
						}
						if(counter+1 == sequence.size()){
							addConditionToPolicy(pol, comp, comp.getEquation());
						}
					}
					else{//Different Components
						prop = createNewCompAndProperty(users, apps, locs, condition1, comp);
						updateProperties(condition1, prop);
						prop = createNewCompAndProperty(users, apps, locs, condition2, comp);
						updateProperties(condition2, prop);
						//2 different single attrib components in the first pass itself. Add both to CONDITION.
						addConditionToPolicy(pol, comp, null);
						lastParsedExpression = comp.getEquation();
					}
					lastParsedCondition = comp.getEquation();
				}else{//Not the first time
					PropertyType prop = null;
					String conditionToHandle = condition1;
					if(condition1.equalsIgnoreCase(lastParsedCondition)){
						conditionToHandle = condition2;
					}else if(condition2.equalsIgnoreCase(lastParsedCondition)){
						conditionToHandle = condition1;
					}else{//This looks like a new component. Parse both the conditions
						//handle both conditions
						if(comp.getOperator().equalsIgnoreCase(OPERATOR_OR)){
							prop = createNewCompAndProperty(users, apps, locs, condition1, comp);
							updateProperties(condition1, prop);
							prop = createNewCompAndProperty(users, apps, locs, condition2, comp);
							updateProperties(condition2, prop);
						}else if(comp.getOperator().equalsIgnoreCase(OPERATOR_AND) && isSameComponents(condition1, condition2)){
							if(condition1.startsWith(USER_ATTRIB)){
								user = users.addNewUSER();
								expressionMap.put(comp.getEquation(), CONDITIONTYPE_USER+"::"+runningUserId);
								user.setId(runningUserId++);								
								createNewUserProperty(pol, user, condition1);
								createNewUserProperty(pol, user, condition2);						
							}else if(condition1.startsWith(LOC_ATTRIB)){
								loc = locs.addNewLOCATION();
								expressionMap.put(comp.getEquation(), CONDITIONTYPE_LOC+"::"+runningLocId);
								loc.setId(runningLocId++);
								createNewLocProperty(loc, condition1);
								createNewLocProperty(loc, condition2);						
							}else if(condition1.startsWith(APP_ATTRIB)){
								app = apps.addNewAPPLICATION();
								expressionMap.put(comp.getEquation(), CONDITIONTYPE_APP+"::"+runningAppId);
								app.setId(runningAppId++);
								createNewAppProperty(app, condition1);
								createNewAppProperty(app, condition2);						
							}
							if(counter == sequence.size()-1){
								addConditionToPolicy(pol, comp, comp.getEquation());
							}
						}
						lastParsedCondition = comp.getEquation();
						//handle both conditions
						continue;
					}
					if(comp.getOperand1().startsWith("(") && comp.getOperand2().startsWith("(")){
						String componentToHandle = ""; 
						componentToHandle = getComponentToHandle(pol, lastParsedExpression, comp);
						addConditionToPolicy(pol, comp, componentToHandle);
						user = null;
						app = null;
						loc = null;
						//We are comparing two components here. Update the condition.
						lastParsedExpression = comp.getEquation();
					}else if(comp.getOperator().equalsIgnoreCase(OPERATOR_OR) || 
							(comp.getOperator().equalsIgnoreCase(OPERATOR_AND)
							&& conditionToHandle.startsWith("("))){
						if(isHasCondition(conditionToHandle)){
							addUserGroupToPolicy(pol, conditionToHandle);
						}else{
							prop = createNewCompAndProperty(users, apps, locs, conditionToHandle, comp);
							updateProperties(conditionToHandle, prop);
							String componentToHandle = getComponentToHandle(pol, lastParsedExpression, comp);
							addConditionToPolicy(pol, comp, componentToHandle);
							user = null;
							app = null;
							loc = null;
						}
						lastParsedExpression = comp.getEquation();
						lastParsedCondition = comp.getEquation();
					} else {//This is not a new component. Update the existing component itself.
						boolean isDiffComponent = false;
//						createNewCompAndProperty(users, apps, locs, conditionToHandle, comp);
						if(conditionToHandle.startsWith(USER_ATTRIB)){
							if(user == null){
								isDiffComponent = true;
								if(!isHasCondition(conditionToHandle)){
									user = users.addNewUSER();
									expressionMap.put(conditionToHandle, CONDITIONTYPE_USER+"::"+runningUserId);									
								}									
							}
							if(!isHasCondition(conditionToHandle)){
								createNewUserProperty(pol, user, conditionToHandle);								
							}
						}else if(conditionToHandle.startsWith(LOC_ATTRIB)){
							if(loc == null){
								isDiffComponent = true;
								loc = locs.addNewLOCATION();
								expressionMap.put(conditionToHandle, CONDITIONTYPE_LOC+"::"+runningLocId);
							}
							createNewLocProperty(loc, conditionToHandle);
						}else if(conditionToHandle.startsWith(APP_ATTRIB)){
							if(app == null){
								isDiffComponent = true;
								app = apps.addNewAPPLICATION();
								expressionMap.put(conditionToHandle, CONDITIONTYPE_APP+"::"+runningAppId);
							}
							createNewAppProperty(app, conditionToHandle);
						}
						if(isDiffComponent){
							if(isHasCondition(conditionToHandle)){
								addUserGroupToPolicy(pol, conditionToHandle);
							}else{
								String componentToHandle = getComponentToHandle(pol, lastParsedExpression, comp);
								addConditionToPolicy(pol, comp, componentToHandle);	
							}
							if(counter == sequence.size()-1 && lastParsedExpression.length()==0){								
								addConditionToPolicy(pol, comp, lastParsedCondition);
							}
							lastParsedExpression = comp.getEquation();							
						}else{
							updateExpressionMap(lastParsedCondition, comp);
							if(counter == sequence.size()-1){
								addConditionToPolicy(pol, comp, comp.getEquation());
							}
						}
					}
					lastParsedCondition = comp.getEquation();
				}
//				updateProperties(condition1, prop);
//				updateProperties(condition2, prop);
			}		
			counter++;
		}
	}

	private void createNewAppProperty(APPLICATION app, String condition) {
		PropertyType prop;
		prop = app.addNewPROPERTY();
		updateProperties(condition, prop);
	}

	private void createNewLocProperty(LOCATION loc, String condition) {
		PropertyType prop;
		prop = loc.addNewPROPERTY();
		updateProperties(condition, prop);
	}

	private void createNewUserProperty(POLICY pol, USER user, String condition) {
		if(!isHasCondition(condition)){
			PropertyType prop = user.addNewPROPERTY();
			updateProperties(condition, prop);			
		}else{
			addUserGroupToPolicy(pol, condition);
		}
	}

	private void addUserGroupToPolicy(POLICY pol, String condition) {
		Component comp = getLHSandRHS(condition);
		String userGroup  = pol.getUsergroup();
		if(userGroup==null || userGroup.length()==0){
			pol.setUsergroup(comp.getOperand2());
		}else{
			pol.setUsergroup(userGroup+","+comp.getOperand2());
		}
	}

	private boolean isHasCondition(String condition) {
		Component comp = getLHSandRHS(condition);
		if(comp.getOperator().equals(OPERATOR_HAS)){
			return true;
		}
		return false;
	}

	private String getComponentToHandle(POLICY pol, String lastParsedExpression, Component comp) {
		if(comp.getOperand1().equals(lastParsedExpression)){
			return comp.getOperand2();
		}else if(comp.getOperand2().equals(lastParsedExpression)){
			return comp.getOperand1();
		}else{//both components need to be added to CONDITION
			return null;
		}
	}

	private void addConditionToPolicy(POLICY pol, Component comp, String componentToHandle) {
		if(componentToHandle==null){
			String op1 = expressionMap.get(comp.getOperand1());
			String type1 = op1.substring(0, 3);
			String id1 = op1.substring(5);
			String op2 = expressionMap.get(comp.getOperand2());
			String type2 = op2.substring(0, 3);
			String id2 = op2.substring(5);
			if(comp.getOperator().equalsIgnoreCase(OPERATOR_AND)){
				addNewCondition(pol, type1, id1);
				addNewCondition(pol, type2, id2);
			}else if(comp.getOperator().equalsIgnoreCase(OPERATOR_OR)){
				addORCondition(pol, type1, id1);
				addORCondition(pol, type2, id2);
			}
		}else{
			String op1 = expressionMap.get(componentToHandle);
			String type1 = op1.substring(0, 3);
			String id1 = op1.substring(5);
			if(comp==null){
				addNewCondition(pol, type1, id1);								
			}else if(comp.getOperator().equalsIgnoreCase(OPERATOR_AND)){
				addNewCondition(pol, type1, id1);				
			}else if(comp.getOperator().equalsIgnoreCase(OPERATOR_OR)){
				addORCondition(pol, type1, id1);
			}else if(comp.getOperator().equalsIgnoreCase(OPERATOR_NOT)){
				addNewCondition(pol, type1, id1);
			}
		}
	}

	private void addORCondition(POLICY pol, String type, String id) {
		CONDITION[] conditionArr = pol.getCONDITIONArray();
		if(conditionArr.length==0){
			addNewCondition(pol, type, id);		
			return;
		}
		boolean found = false;
		for (CONDITION condition : conditionArr) {
			if(condition.getType().equalsIgnoreCase(type)){
				String condVal = condition.getStringValue();
				condVal += ","+id;
				condition.setStringValue(condVal);
				found = true;
				break;
			}
		}
		if(!found){
			addNewCondition(pol, type, id);			
		}			
//		pol.setCONDITIONArray(conditionArr);		
	}

	private void addNewCondition(POLICY pol, String type, String id) {
		CONDITION condition1 = pol.addNewCONDITION();
		condition1.setExclude(false);
		condition1.setType(type);
		condition1.setStringValue(id);
	}

	private void updateExpressionMap(String lastParsedCondition, Component comp) {
		String runningId = expressionMap.get(lastParsedCondition);
		expressionMap.remove(lastParsedCondition);
		expressionMap.put(comp.getEquation(), runningId);		
	}



	private boolean isSameComponents(String condition1, String condition2) {
		if((condition1.startsWith(USER_ATTRIB) && condition2.startsWith(USER_ATTRIB))
				|| (condition1.startsWith(LOC_ATTRIB) && condition2.startsWith(LOC_ATTRIB))
				||(condition1.startsWith(APP_ATTRIB) && condition2.startsWith(APP_ATTRIB))){
			return true;
		}
		return false;
	}
	
	
	private String getComponentType(String str){
		if(str==null||str.length()==0){
			return "";
		}
		if(str.startsWith(APP_ATTRIB)){
			return CONDITIONTYPE_APP;
		}else if(str.startsWith(LOC_ATTRIB)){
			return CONDITIONTYPE_LOC;
		}else if(str.startsWith(ENV_ATTRIB)){
			return CONDITIONTYPE_ENV;
		}else if(str.startsWith(RES_ATTRIB)){
			return CONDITIONTYPE_RES;
		}else if(str.startsWith(USER_ATTRIB)){
			return CONDITIONTYPE_USER;
		}
		return "";
	}

	private PropertyType createNewCompAndProperty(USERS users, APPLICATIONS apps,
			LOCATIONS locs, String condition, Component comp) {
		PropertyType prop = null;
		String conditionComponentType = getComponentType(condition);
		boolean addCondition = false;
		if(comp!=null){
			if(!getComponentType(comp.getOperand1()).equals(getComponentType(comp.getOperand2()))
					|| (comp.getOperator().equalsIgnoreCase(OPERATOR_OR))){
				addCondition = true;
			}			
		}
		if(conditionComponentType.equals(CONDITIONTYPE_USER)){
			USER user = users.addNewUSER();
			if(addCondition){
				expressionMap.put(condition, CONDITIONTYPE_USER+"::"+runningUserId);
			}else{
				if(comp==null){
					expressionMap.put(condition, CONDITIONTYPE_USER+"::"+runningUserId);
				}else{
					expressionMap.put(comp.getEquation(), CONDITIONTYPE_USER+"::"+runningUserId);									
				}
			}
			user.setId(runningUserId++);
			prop = user.addNewPROPERTY();
		}else if(conditionComponentType.equals(CONDITIONTYPE_LOC)){
			LOCATION loc = locs.addNewLOCATION();
			if(addCondition){
				expressionMap.put(condition, CONDITIONTYPE_LOC+"::"+runningLocId);
			}else{
				if(comp==null){
					expressionMap.put(condition, CONDITIONTYPE_LOC+"::"+runningUserId);
				}else{
					expressionMap.put(comp.getEquation(), CONDITIONTYPE_LOC+"::"+runningLocId);
				}
			}
			loc.setId(runningLocId++);
			prop = loc.addNewPROPERTY();
		}else if(conditionComponentType.equals(CONDITIONTYPE_APP)){
			APPLICATION app = apps.addNewAPPLICATION();
			if(addCondition){
				expressionMap.put(condition, CONDITIONTYPE_APP+"::"+runningAppId);
			}else{
				if(comp==null){
					expressionMap.put(condition, CONDITIONTYPE_APP+"::"+runningAppId);
				}else{
					expressionMap.put(comp.getEquation(), CONDITIONTYPE_APP+"::"+runningAppId);
				}
			}
			app.setId(runningAppId++);
			prop = app.addNewPROPERTY();
		}
		return prop;
	}

	private void updateProperties(String operand, PropertyType prop) {
		Component splitComponent = getLHSandRHS(operand);
		if(operand.startsWith(USER_ATTRIB)){
			updatePropertyAttribs(splitComponent, prop, USER_ATTRIB);
		}else if(operand.startsWith(LOC_ATTRIB)){
			updatePropertyAttribs(splitComponent, prop, LOC_ATTRIB);
		}else if(operand.startsWith(APP_ATTRIB)){
			updatePropertyAttribs(splitComponent, prop, APP_ATTRIB);
		}else if(operand.startsWith(RES_ATTRIB)){
			updatePropertyAttribs(splitComponent, prop, RES_ATTRIB);
		}
	}

	private void updatePropertyAttribs(Component splitComponent, PropertyType prop, String prefix) {
		String LHS = splitComponent.getOperand1();
		String RHS = splitComponent.getOperand2();
		String operator = splitComponent.getOperator();
		String attribName = LHS;
		if(prefix != null){
			attribName = LHS.substring(prefix.length());
		}
		prop.setName(PolicyTransformer.escapeUnwantedQuotes(attribName));
		prop.setValue(PolicyTransformer.escapeUnwantedQuotes(RHS));
		prop.setMethod(PolicyTransformer.getCompareMethod(operator));
	}

	private Component getLHSandRHS(String expression) {
		ExpressionEvaluator eval = new ExpressionEvaluator();
		List<Component> sequence = new ArrayList<Component>();
		eval.evaluate(expression, sequence);
		return sequence.get(0);		
	}

}
