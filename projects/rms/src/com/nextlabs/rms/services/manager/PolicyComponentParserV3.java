package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

public class PolicyComponentParserV3 extends AbstractEvaluator<String> {
	
	public static final String USER_ATTRIB = "user.";
	public static final String APP_ATTRIB = "application.";
	public static final String LOC_ATTRIB = "host.";
	public static final String ENV_ATTRIB = "ENVIRONMENT.";
	public static final String ENV_TIME_ATTRIB = "CURRENT_TIME.";
	public static final String RES_ATTRIB = "resource.fso.";
	public static final String OPERATOR_AND = "&&";
	public static final String OPERATOR_OR = "||";
	public static final String OPERATOR_NOT = "!!";
	public static final String OPERATOR_HAS = "~#";
	public static final String COMPONENTTYPE_RES = "res";
	public static final String COMPONENTTYPE_USER = "usr";
	public static final String COMPONENTTYPE_APP = "app";
	public static final String COMPONENTTYPE_LOC = "loc";
	public static final String COMPONENTTYPE_ENV = "env";
	
	private int runningResId = 0;
	private int runningAppId = 0;
	private int runningLocId = 0;
	private int runningUserId = 0;
	private int runningEnvId = 0;
	
	private HashMap<String, PolicyComponent> componentMap = new HashMap<String, PolicyComponent>();
	private HashMap<String, PolicyComponent> addedComponentMap = new HashMap<String, PolicyComponent>();
	private String lastParsedExpression="";

	private RESOURCES resources;
	private USERS users;
	private APPLICATIONS apps;
	private LOCATIONS locs;
	private ENVS envs;

	final static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
	final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);
	final static Operator NOT = new Operator("!!", 1, Operator.Associativity.LEFT, 3);

	private static final Parameters PARAMETERS;

	static {
		// Create the evaluator's parameters
		PARAMETERS = new Parameters();
		// Add the supported operators
		PARAMETERS.add(AND);
		PARAMETERS.add(OR);
		PARAMETERS.add(NOT);
		// Add the parentheses
		PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
	}

	public PolicyComponentParserV3(RESOURCES res, USERS users, APPLICATIONS apps, LOCATIONS locs, ENVS envs) {
		super(PARAMETERS);
		this.resources = res;
		this.users = users;
		this.apps = apps;
		this.locs = locs;
		this.envs = envs;
	}

	@Override
	protected String toValue(String literal, Object evaluationContext) {
		return literal;
	}
	
	@Override
	protected String evaluate(Operator operator, Iterator<String> operands,
			Object evaluationContext) {
//		List<Component> tree = (List<Component>) evaluationContext;
		HashMap<String, Object> contextMap = (HashMap<String, Object>) evaluationContext;
		POLICY pol = (POLICY)contextMap.get("POLICY_OBJECT");
		String operand1 = operands.next().trim();
		String operand2 = null;
		if (operands.hasNext()) {
			operand2 = operands.next().trim();
		}
		String equation = "";
		if (operand2 != null) {
			equation = "(" + operand1 + " " + operator.getSymbol() + " " + operand2 + ")";
		} else {
			equation = operator.getSymbol() + " " + operand1;
			//Unary operator. handle NOT and return.
			if(componentMap.size()==0 && (lastParsedExpression == null || lastParsedExpression.length()==0)){
				addSingleCondition(pol, operand1);
			}else{
				PolicyComponent comp = getLastComponent();
				if(comp!=null){
					addANDConditionToPolicy(pol, comp);
					componentMap.remove(operand1);				
					addedComponentMap.put(operand1, comp);
				}
				if(addedComponentMap.get(operand1)==null){
					//component has not been added to the policy yet. Add it and negate it.
					PolicyComponent component = componentMap.get(operand1);
					if(component==null){
						//component hasn't been added to componentMap either. Add it.
						addSingleCondition(pol, operand1);
					}
				}
			}
			negateConditionInPolicy(pol, operand1);
			lastParsedExpression = equation;
			return equation;			
		}
//		System.out.println(equation);
		String compToHandle = null; 
		if(operand1.startsWith("(") && operand2.startsWith("(")){
			//both operands seem to be expressions. Handle both.	
			PolicyComponent comp1 = componentMap.get(operand1);
			PolicyComponent comp2 = componentMap.get(operand2);
			if(comp1!=null){
				addANDConditionToPolicy(pol, comp1);
				componentMap.remove(operand1);				
				addedComponentMap.put(operand1, comp1);
			}
			if(comp2!=null){
				if(operator.getSymbol().equals(OPERATOR_AND)){
					addANDConditionToPolicy(pol, comp2);
					componentMap.remove(operand2);
					addedComponentMap.put(operand1, comp2);
				}else{
					addORConditionToPolicy(pol, comp2);
					componentMap.remove(operand2);
					addedComponentMap.put(operand2, comp2);
				}				
			}
		}else{
			PolicyComponent lastPolicyComp = getLastComponent();
			compToHandle = getComponentToHandle(operand1, operand2, lastParsedExpression);	
			boolean isNewExpression = false;
			if(compToHandle==null){//handle both operands
				if(componentMap.size()>0){
					//some expression has already been processed. Looks like this is a new expression
					lastPolicyComp = null;
					isNewExpression = true;
				}
				String op1Type = getComponentType(operand1);
				String op2Type = getComponentType(operand2);
				PolicyComponent comp1 = null;
				if(lastPolicyComp == null){//No component open right now. create one.
					if(isHasCondition(operand1)){
						addUserGroupToPolicy(pol, operand1);
					}else{
						if(op1Type.length()>0){
							comp1 = createNewComponent(op1Type);						
							addNewProperty(comp1, operand1);							
						}
					}
				}
				handleSingleComponent(operator, pol, operand2, op1Type, op2Type, comp1, equation, isNewExpression);
			}else{//only one operand to be handled
				if(lastPolicyComp == null){//previous expression has already been added to the Policy. This is a new single attrib component.
					handleSingleAttribComp(operator, pol, compToHandle, getComponentType(compToHandle), equation);
				}else{
					handleSingleComponent(operator, pol, compToHandle, lastPolicyComp.getComponentType(), getComponentType(compToHandle), lastPolicyComp, equation, isNewExpression);			
					
				}
				
			}
		}
		lastParsedExpression = equation;
		return equation;
	}
	
	private void negateConditionInPolicy(POLICY pol, String operand) {
		PolicyComponent comp = addedComponentMap.get(operand);
		CONDITION[] condArr = pol.getCONDITIONArray();
		for (CONDITION condition : condArr) {
			if(condition.getType().equals(comp.getComponentType()) && isMatchingId(condition.getStringValue(), comp.getId()+"")){
				condition.setExclude(true);
			}
		}
	}
	
	private boolean isMatchingId(String condStr, String id){
		if(condStr.equals(id)){
			return true;
		}
		if(condStr.indexOf(',')>0){
			String[] ids = condStr.split(",");
			for (String idStr : ids) {
				if(idStr.equals(id)){
					return true;
				}
			}
		}
		return false;
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
	
	private void handleSingleAttribComp(Operator operator, POLICY pol,
			String compToHandle, String componentType, String equation) {
		if(componentType.length()==0){
			return;
		}
		if(isHasCondition(compToHandle)){
			addUserGroupToPolicy(pol, compToHandle);
		}else{
			PolicyComponent comp = createNewComponent(componentType);
			addNewProperty(comp, compToHandle);
			//Add both Components to the Policy
			if(operator.getSymbol().equals(OPERATOR_OR)){
				addORConditionToPolicy(pol, comp);
			}else{
				addANDConditionToPolicy(pol, comp);
			}
			addedComponentMap.put(equation, comp);
		}
		componentMap.remove(getLastExpression());	
	}

	private String getLastExpression(){
		if(componentMap.size()==0){
			return null;			
		}
		String lastKey = null;		
		for (Iterator<String> iter = componentMap.keySet().iterator(); iter.hasNext();){
			lastKey = iter.next();
		}
		return lastKey;
	}

	private PolicyComponent getLastComponent() {
		String lastExpression = getLastExpression();
		if(lastExpression==null){
			return null;
		}
		return componentMap.get(lastExpression);
	}

	private void handleSingleComponent(Operator operator, POLICY pol,
			String operand, String op1Type, String op2Type,
			PolicyComponent comp1, String expression, boolean isNewExpression) {
		if(op1Type.equals(op2Type) && operator.getSymbol().equalsIgnoreCase(OPERATOR_AND)){			
			//both are of same type and the Operator is 'AND'. Add it to the same component.
			if(comp1 == null){//comp1 will be null if operand1 was something like "user.GROUP has 0";
				comp1 = createNewComponent(op2Type);
			}
			if(isHasCondition(operand)){
				addUserGroupToPolicy(pol, operand);
			}else{
				addNewProperty(comp1, operand);
			}
			if(!isNewExpression){
				componentMap.remove(getLastExpression());				
			}
			componentMap.put(expression, comp1);
		}else{
			//Either the Type of Component is different or the Operator is 'OR'. Create a new component.
			PolicyComponent comp2 = null;
			if(isHasCondition(operand)){
				addUserGroupToPolicy(pol, operand);
			}else{
				if(op2Type.length()>0){
					comp2 = createNewComponent(op2Type);
					addNewProperty(comp2, operand);					
				}
			}
			//Add both Components to the Policy
			if(operator.getSymbol().equals(OPERATOR_OR)){
				if(comp1!=null){//comp1 will be null if operand1 was something like "user.GROUP has 0";
					addORConditionToPolicy(pol, comp1);
					PolicyComponent comp = componentMap.get(lastParsedExpression);
					if(comp!=null && comp.getComponentType().equals(comp1.getComponentType()) && comp.getId()==comp1.getId()){
						addedComponentMap.put(lastParsedExpression, comp1);
					}else{
						addedComponentMap.put(expression, comp1);						
					}
						
				}
				if(comp2!=null){
					addORConditionToPolicy(pol, comp2);
					addedComponentMap.put(operand, comp2);
				}
			}else{
				if(comp1!=null){//comp1 will be null if operand1 was something like "user.GROUP has 0";
					addANDConditionToPolicy(pol, comp1);	
					addedComponentMap.put(lastParsedExpression, comp1);
				}
				if(comp2!=null){
					addANDConditionToPolicy(pol, comp2);
					addedComponentMap.put(operand, comp2);
				}
			}
			componentMap.remove(getLastExpression());
		}
	}

	private void addORConditionToPolicy(POLICY pol, PolicyComponent comp) {
		if(comp == null){
			return;
		}
		CONDITION[] condArr = pol.getCONDITIONArray();
		for (CONDITION condition : condArr) {
			if(condition.getType().equals(comp.getComponentType()) && !condition.getExclude()){
				String newStr = condition.getStringValue() + "," + comp.getId();
				condition.setStringValue(newStr);
				addComponentToGroup(comp);
				return;
			}			
		}
		addANDConditionToPolicy(pol, comp);
	}

	private void addANDConditionToPolicy(POLICY pol, PolicyComponent comp) {
		if(comp==null){
			return;
		}
		CONDITION condition = pol.addNewCONDITION();
		condition.setExclude(false);
		condition.setType(comp.getComponentType());
		condition.setStringValue(comp.getId()+"");
		addComponentToGroup(comp);
	}

	private void addComponentToGroup(PolicyComponent comp) {
		switch (comp.getComponentType()) {
		case COMPONENTTYPE_USER:
			USER user = users.addNewUSER();
			user.setId(comp.getId());
			user.setPROPERTYArray(comp.getPropertyList().toArray(new PropertyType[comp.getPropertyList().size()]));
			break;
		case COMPONENTTYPE_APP:
			APPLICATION app = apps.addNewAPPLICATION();
			app.setId(comp.getId());
			app.setPROPERTYArray(comp.getPropertyList().toArray(new PropertyType[comp.getPropertyList().size()]));
			break;
		case COMPONENTTYPE_LOC:
			LOCATION loc = locs.addNewLOCATION();
			loc.setId(comp.getId());
			loc.setPROPERTYArray(comp.getPropertyList().toArray(new PropertyType[comp.getPropertyList().size()]));
			break;
		case COMPONENTTYPE_RES:
			RESOURCE res = resources.addNewRESOURCE();
			res.setId(comp.getId());
			res.setPROPERTYArray(comp.getPropertyList().toArray(new PropertyType[comp.getPropertyList().size()]));
			break;
		case COMPONENTTYPE_ENV:
			ENV env = envs.addNewENV();
			env.setId(comp.getId());
			env.setPROPERTYArray(comp.getPropertyList().toArray(new PropertyType[comp.getPropertyList().size()]));
			break;
		}
	}

	private String getComponentToHandle(String operand1, String operand2,
			String lastParsedExpression) {
		if(lastParsedExpression==null || lastParsedExpression.length()==0){
			return null;
		}else if(lastParsedExpression.equals(operand1)){
			return operand2;
		}else if(lastParsedExpression.equals(operand2)){
			return operand1;
		}			
		return null;		
	}

	private void addNewProperty(PolicyComponent comp1, String operand1) {
		if(!(comp1.getComponentType().equals(COMPONENTTYPE_ENV) && (operand1.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || operand1.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE)))){//This can happen for ENV components
			PropertyType prop = PropertyType.Factory.newInstance();
			updateProperties(operand1, prop);
			comp1.addProperty(prop);			
		}
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
		}else if(operand.startsWith(ENV_ATTRIB) || operand.startsWith(ENV_TIME_ATTRIB)){
			updatePropertyAttribs(splitComponent, prop, null);
		}
	}
	
	private void updatePropertyAttribs(Component splitComponent, PropertyType prop, String prefix) {
		String LHS = splitComponent.getOperand1();
		String RHS = splitComponent.getOperand2();
		String operator = splitComponent.getOperator();
		String attribName = LHS;
		if(prefix != null && prefix.length()<LHS.length()){
			attribName = LHS.substring(prefix.length());
		}
		prop.setName(PolicyTransformer.escapeUnwantedQuotes(attribName));
		prop.setValue(PolicyTransformer.escapeUnwantedQuotes(RHS));
		prop.setMethod(PolicyTransformer.getCompareMethod(operator));
	}
	
	private String getComponentType(String str){
		if(str==null||str.length()==0){
			return "";
		}
		if(str.startsWith(APP_ATTRIB)){
			return COMPONENTTYPE_APP;
		}else if(str.startsWith(LOC_ATTRIB)){
			return COMPONENTTYPE_LOC;
		}else if(str.startsWith(ENV_ATTRIB)){
			return COMPONENTTYPE_ENV;
		}else if(str.startsWith(ENV_TIME_ATTRIB)){
			return COMPONENTTYPE_ENV;
		}else if(str.startsWith(RES_ATTRIB)){
			return COMPONENTTYPE_RES;
		}else if(str.startsWith(USER_ATTRIB)){
			return COMPONENTTYPE_USER;
		}
		return "";
	}

	private PolicyComponent createNewComponent(String compType) {
		int id = getRunningId(compType);
		PolicyComponent comp = new PolicyComponent(id, compType);
		return comp;
	}

	private int getRunningId(String compType) {
		switch(compType){
			case COMPONENTTYPE_APP: return runningAppId++;
			case COMPONENTTYPE_ENV: return runningEnvId++;
			case COMPONENTTYPE_LOC: return runningLocId++;
			case COMPONENTTYPE_RES: return runningResId++;
			case COMPONENTTYPE_USER: return runningUserId++;
		}
		return -1;
	}
	
	private Component getLHSandRHS(String expression) {
		ExpressionEvaluator eval = new ExpressionEvaluator();
		List<Component> sequence = new ArrayList<Component>();
		eval.evaluate(expression, sequence);
		return sequence.get(0);		
	}

	private static String replaceLogicalOperators(String inputStr) {
		String replacedStr = inputStr.replaceAll("(?m)\\bOR\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "||");
		replacedStr = replacedStr.replaceAll("(?m)\\bAND\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "&&");
		replacedStr = replacedStr.replaceAll("(?m)\\bNOT\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "!!");
		replacedStr = replacedStr.replaceAll("(?m)\\bhas\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "~#");
		return replacedStr;
	}

	public RESOURCES getResources() {
		return resources;
	}

	public USERS getUsers() {
		return users;
	}

	public APPLICATIONS getApps() {
		return apps;
	}

	public LOCATIONS getLocs() {
		return locs;
	}

	public ENVS getEnvs() {
		return envs;
	}
	
	private boolean isHasCondition(String condition) {
		if(!(condition.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || condition.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))){//This can happen for ENV components
			Component comp = getLHSandRHS(condition);
			if(comp.getOperator().equals(OPERATOR_HAS) && getComponentType(comp.getOperand1()).equals(COMPONENTTYPE_USER)){
				return true;
			}			
		}
		return false;
	}

	private class PolicyComponent {
		
		private String componentType;
		
		private int id;
		
		private List<PropertyType> propertyList = new ArrayList<PropertyType>();
		
		public PolicyComponent(int id, String componentType){
			this.id = id;
			this.componentType = componentType;
		}
		
		public String getComponentType() {
			return componentType;
		}

		public List<PropertyType> getPropertyList() {
			return propertyList;
		}

		public void setPropertyList(List<PropertyType> propertyList) {
			this.propertyList = propertyList;
		}
		
		public void addProperty(PropertyType property){
			this.propertyList.add(property);
		}

		public int getId() {
			return id;
		}

	}
	
	public void processPolicy(POLICY pol, String resourceStr, String subjectStr, String envStr) {
		processComponents(pol, subjectStr);
		processComponents(pol, resourceStr);
		processComponents(pol, envStr);
	}
	
	private void processComponents(POLICY pol, String str) {
		addedComponentMap.clear();
		componentMap.clear();
		lastParsedExpression="";
		if(str==null || str.length()==0){
			return;
		}
		HashMap<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("POLICY_OBJECT", pol);
		String processedSubStr = "";
		if(str.startsWith("BY ")){
			processedSubStr = str.trim().substring(3);
		}else if(str.startsWith("WHERE ")){
			processedSubStr = str.trim().substring(6);
		}else if(str.startsWith("FOR ")){
			processedSubStr = str.trim().substring(4);
		}
		if((processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))){
			return;
		}
		processedSubStr = replaceLogicalOperators(processedSubStr);
		String res = evaluate(processedSubStr, contextMap);
		if((processedSubStr.equals(res) || processedSubStr.equals("("+res+")")) && addedComponentMap.size()==0){
			if(componentMap.size()==0){
				//There are no logical operators in the equation. This must be a single attribute condition
				if(processedSubStr.startsWith("(")){
					processedSubStr = processedSubStr.substring(1, processedSubStr.length()-1);
				}
				if(isHasCondition(processedSubStr)){
					addUserGroupToPolicy(pol, processedSubStr);
				}else{
					addSingleCondition(pol, processedSubStr);				
				}
			}				
		}
		if(componentMap.size()>0){
			PolicyComponent comp = getLastComponent();
			addANDConditionToPolicy(pol, comp);
		}
	}

	private void addSingleCondition(POLICY pol, String processedSubStr) {
		PolicyComponent comp = createNewComponent(getComponentType(processedSubStr));
		addNewProperty(comp, processedSubStr);
		addANDConditionToPolicy(pol, comp);
		addedComponentMap.put(processedSubStr, comp);
	}
/*
	public static void main(String[] args) {
		POLICY pol = POLICY.Factory.newInstance();
		RESOURCES res = RESOURCES.Factory.newInstance();
		USERS users = USERS.Factory.newInstance();
		APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
		LOCATIONS locs = LOCATIONS.Factory.newInstance();
		ENVS envs = ENVS.Factory.newInstance();
//		String str = "BY (user.name1 = val1 AND user.Group has 0 AND (user.locationcode = \"SG\" AND user.user_location_code = \"IN\") OR (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\") OR (user.GROUP has 2 AND user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\"))";
//		String str = "BY (user.name1 = val1 OR application.name = \"Microsoft WORD\" OR application.type = executable AND (user.locationcode = \"SG\" AND user.user_location_code = \"IN\") OR host.inet_address = \"10.63.1.144\" OR (user.name2 = sudhi AND user.name3 = pranava))";
//		String str = "BY (user.u1 = v1 AND host.name = prince OR host.ip = 11111 OR user.Group has 0 AND (application.name = Microsoft AND application.url = \"http://localhost:8443/RMS\"))";
//		String str = "BY user.Group has 0 AND user.name = hashVal";
//		String str = "BY NOT user.name = sudhi";
//		String str = "FOR ((resource.portal.url = \"sharepoint://labs-sp02.labs01.nextlabs.com/sites/acme/**\" AND resource.fso.content = \"*KEY:CR101>=1;*\"))";
//		String str = "FOR (NOT (resource.fso.filename = \"**C:\\ClassifiedData\\*\") AND resource.fso.\"export security\" = \"ITAR\")";
//		String str = "BY ((user.mail_domain = \"qapf1.qalab01.nextlabs.com\" AND (application.path = \"**\\excel.exe\" OR application.path = \"**\\WINWORD.EXE\" OR application.path = \"**\\AcroRd32.exe\")))";
//		String str = "FOR (resource.fso.sensitivity = \"Non Business\" AND NOT (resource.fso.program = \"PR-01\"))";
//		String str = "BY ((user.organization = \"bolts\" AND user.GROUP has 15) OR (user.ocompany = \"Bolts\" AND user.ocitizenship = \"US\"))";
//		String str = "FOR (resource.fso.content = \"*KEY:ITAR >= 1;*\")";
//		String str = "FOR ((resource.fso.name1 = val1) AND NOT((resource.fso.name2 = val2 AND resource.fso.name3 = val3)))";
//		String str = "FOR (NOT(resource.fso.name1 = val1 AND NOT ((resource.fso.name2 = val2)) AND resource.fso.name3 = val3))";
//		String str = "WHERE TRUE AND TRUE AND TRUE";
//		String str = "BY host.GROUP has 4";
//		String str = "BY (user.GROUP has 18 AND host.GROUP has 4 AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))";
//		String str = "FOR (NOT (resource.fso.filename = \"**C:\\ClassifiedData\\*\") AND resource.fso.export_security = \"ITAR\")";
//		String str = "BY (user.mail_domain = \"qapf1.qalab01.nextlabs.com\" AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))";
//		String str = "WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND CURRENT_TIME.identity >= \"Jun 26, 2015 11:33:00 AM\" AND CURRENT_TIME.identity <= \"Jun 30, 2015 11:33:55 AM\") AND TRUE))";
//		String str = "FOR (resource.fso.name1 = val1 OR ((resource.fso.name2 = val2) AND NOT((resource.fso.name3 = val3 AND resource.fso.name3 = val3))))";
//		String str = "FOR (resource.fso.\"export security\" = \"EAR\" OR resource.fso.name = \"**sap://labs-sap01/dm1" AND NOT (resource.fso.nam1 = val1))";
		String str = "FOR resource.fso.bafa = \"BAFA-03\"";
//		String str = "WHERE (TRUE AND ((CURRENT_TIME.weekday = \"sunday\" OR CURRENT_TIME.weekday = \"tuesday\" OR CURRENT_TIME.weekday = \"thursday\" OR CURRENT_TIME.weekday = \"saturday\") AND TRUE))";
//		String str = "BY (user.organization = \"bolts\" AND user.GROUP has 15) OR (user.ocompany = \"Bolts\" AND user.ocitizenship = \"US\")";
//		String str = "BY user.name = hashVal AND user.Group has 0 AND user.designation = Manager";
//		String str = "FOR resource.fso.nam1 = val1 OR (resource.fso.ear = \"EAR-01\" AND resource.fso.ear = \"EAR-02\" AND resource.fso.ear = \"EAR-03\" AND resource.fso.ear = \"EAR-04\" AND resource.fso.bafa = \"BAFA-01\") OR (resource.fso.program = \"PR-02\" AND resource.fso.program = \"PR-09\")";
//		String str = "WHERE (TRUE AND (ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND TRUE))";
//		String str = "WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT < 180000 AND ENVIRONMENT.REMOTE_ACCESS = 1 AND CURRENT_TIME.identity >= \"Jun 24, 2015 11:14:00 AM\" AND CURRENT_TIME.identity <= \"Jul 10, 2015 11:14:28 AM\" AND CURRENT_TIME.time >= \"11:14:00 AM\" AND CURRENT_TIME.time <= \"5:14:39 PM\" AND CURRENT_TIME.day_in_month = 1 AND CURRENT_TIME.weekday = \"Sunday\") AND resource.fso.abc = def))";
		PolicyComponentParserV3 evaluator = new PolicyComponentParserV3(res, users, apps, locs, envs);
//		contextMap.put("RESOURCES_OBJECT", res);
//		contextMap.put("USERS_OBJECT", users);
//		contextMap.put("APPS_OBJECT", apps);
//		contextMap.put("LOCATIONS_OBJECT", locs);
//		contextMap.put("LOCATIONS_OBJECT", locs);
//		evaluator.processComponents(pol, str);
//		String resourceStr = "FOR (resource.fso.sensitivity = \"Non Business\" AND NOT ((resource.fso.program = \"PR-01\" OR resource.fso.bafa = \"BAFA-01\" OR resource.fso.ear = \"EAR-01\")))";
//		String resourceStr = "FOR resource.fso.course = \"RMS\"";
		String resourceStr = "FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\" OR resource.fso.bafa = \"BAFA-02\") AND NOT (resource.fso.program = \"PR-01\") AND NOT (resource.fso.ear = \"EAR-01\"))";
		String subjectStr = "BY ((host.GROUP has 8 AND host.inet_address = \"10.23.56.55\") AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))";
//		String subjectStr = "BY ((user.GROUP has 18 AND user.mail = \"grover.cleveland@qapf1.qalab01.nextlabs.com\") AND (application.path = \"**\\excel.exe\" OR application.path = \"**\\WINWORD.EXE\" OR application.path = \"**\\AcroRd32.exe\"))";
//		String subjectStr = "BY (user.GROUP has 26 AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))";
//		String envStr = "((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND ENVIRONMENT.REMOTE_ACCESS = 0 AND CURRENT_TIME.identity >= \"Jun 12, 2015 5:14:00 PM\" AND CURRENT_TIME.identity <= \"Jun 13, 2015 3:14:22 PM\" AND CURRENT_TIME.time >= \"5:14:00 PM\" AND (CURRENT_TIME.weekday = \"sunday\" OR CURRENT_TIME.weekday = \"monday\" OR CURRENT_TIME.weekday = \"tuesday\" OR CURRENT_TIME.weekday = \"wednesday\" OR CURRENT_TIME.weekday = \"thursday\") AND CURRENT_TIME.time <= \"11:14:38 PM\"))";
		String envStr = "WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 180 AND CURRENT_TIME.identity >= \"Jun 21, 2015 5:27:30 PM\") AND TRUE))";
//		String envStr = "";
//		evaluator.processComponents(pol, resourceStr);
		evaluator.processPolicy(pol, resourceStr, subjectStr, envStr);
//		evaluator.getLHSandRHS("resource.fso.\"export security\" = \"EAR\"");
		System.out.println("\nPolicy::"+pol.toString());
		System.out.println("Users::"+users.toString());
		System.out.println("Apps::"+apps.toString());
		System.out.println("Locs::"+locs.toString());
		System.out.println("Resources::"+res.toString());
		System.out.println("Envs::"+envs.toString());
	}
*/

}
