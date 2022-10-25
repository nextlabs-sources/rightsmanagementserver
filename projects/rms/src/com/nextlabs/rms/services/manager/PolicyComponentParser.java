package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.ConfigManager;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS.APPLICATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS.LOCATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS.USER;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.CONDITION;
import noNamespace.PropertyType;


public class PolicyComponentParser {

	private static Set<String> notNeededWords = new HashSet<String>(); 
	private static Set operations = new HashSet<String>();
	private static String RESOURCE="RESOURCE";
	private static String SUBJECT="SUBJECT";
	public static HashSet<String> validOperations;
	private static Logger logger= Logger.getLogger(PolicyComponentParser.class);
	static{
		notNeededWords.add("FOR");
		notNeededWords.add("BY");
		operations.add("AND");
		operations.add("OR");
		operations.add("AND NOT");
		validOperations=new HashSet<>();
		validOperations.addAll(Arrays.asList(new String[]{"=",">=","<=",">","<","!="}));
	}

	public static void main(String[] args) {
		//		evaluate(null,Arrays.asList(new Integer[]{2,2,1,1}),Arrays.asList(new String[]{"AND","OR","AND"}));
		PolicyComponentParser parser=new PolicyComponentParser();
		/*String[] policyComponentString=new String[]{
				" FOR ((resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") AND resource.fso.dummy property1 = \"propertyBlah\" AND NOT (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\"))))",
//				"FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp2 = \"property1\" AND pranava = \"pranava\" AND NOT ((resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")))",
//				"FOR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")",
//				"FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp2 = \"property1\" AND NOT ((resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")))",
//				"FOR (((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") OR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")) AND NOT (resource.fso.comp2 = \"property1\"))",
				"FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\" AND NOT (((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND (resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\"))))",
//				"FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND resource.fso.\"comp2property1\" = \"\" AND NOT ((resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")))",
//				"FOR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")",
//				"FOR (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")) AND (resource.fso.pranava=\"Pranava\" OR resource.fso.Pranava1=\"Pranava1\"))",
//				"FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND resource.fso.\"comp2property1\" = \"\" AND NOT ((resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")))",
//				"FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND (resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\"))",
				"FOR (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")) AND NOT (resource.fso.\"comp2property1\" = \"\"))"
		};
//		String testString="FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp2 = \"property1\" AND NOT ((resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")))";
//		String testString="((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\"))";
//		String testString="FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND resource.fso.\"comp2property1\" = \"\" AND NOT ((resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")))";
//		String testString="FOR (((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") OR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")) AND NOT (resource.fso.comp2 = \"property1\"))";
		String testString="FOR (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")) AND (resource.fso.pranava=\"Pranava\" OR resource.fso.Pranava1=\"Pranava1\"))";
//		String testString="(((a1 = \"a1\" AND a2 = \"a2\") OR (a3 = \"a3\" AND a4  = \"a4\")) OR (a5 = \"a5\" AND a6 = \"a6\"))";
//		parser.parsePolicy("FOR (((r1 = \"r1\" AND r2 = \"r2\") OR (r3 = \"r3\" AND r4 = \"r4\")) AND (r5 = \"r5\" AND r6 = \"r6\") OR ((r7 = \"r7\" & r8 = \"r8\") AND NOT (r9 = \"r9\")))");
//		parser.parsePolicy(testString);
		for(int i=0;i<policyComponentString.length-1;i++){
			System.out.println("Original String: ");
			System.out.println(policyComponentString[i]);
			System.out.println("Processed list");
			parser.parseResources(policyComponentString[i]);
			System.out.println("----------------------------\n");
		}*/
//		String testSubject="BY (user.GROUP has 0 AND (application.GROUP has 0 AND application.name != \"**\\\\rms1.exe\" AND application.fakeproperty1 = \"**\\\\rms\"))";// OR (application.GROUP has 1 AND application.\"dummy property\" = \"Dummy Property 1\")) AND (application.GROUP has 2 AND application.fakeproperty3 = \"**\\notepad\" AND application.dummyproperty1 = \"dummy\" AND application.dummyproperty2 = \"dumm2\") AND NOT ((application.GROUP has 3 AND application.fakeproperty4 = \"4\"))))";
//		String testSubject="BY (user.GROUP has 0 AND ((application.prop = \"prop\" AND application.pro = \"pro\") AND application.blah1 = \"blah1\" AND (application.prop1 = \"prop1\" AND application.prop2 = \"prop2\") AND ((application.prop3 = \"2.45AM\" AND application.prop4 = \"2.45 AM\") AND application.blah = \"blah\"))";
		//String testSubject="BY ((user.locationcode = \"SG\" AND user.user_location_code = \"IN\") AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\") AND (user.GROUP has 2 AND user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\"))";
//		String testRes="FOR resource.fso.name = \"**.jt\"";
		String testSubject="BY (user.GROUP has 0 AND ((application.path = \"**\\acrord32.exe\" AND application.publisher = \"adobe systems, incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\EXCEL.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"SAP SE\")))";
//		String testRes="FOR ((resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") OR (resource.fso.name = \"**\" AND resource.fso.name = \"pranava\"))";
//		String testRes="FOR ((resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") AND NOT ((resource.fso.name = \"**\" OR resource.fso.name = \"**\")) )";
		String testRes = "FOR TRUE";
//		String testSubject="BY user.GROUP has 3";
//		String testSubject = "BY user.groups = \"Support Analyst\"";
//		String testSubject = "BY NOT (application.path = \"**\\excel.exe\")";
		POLICY pol = POLICY.Factory.newInstance();
		RESOURCES res = RESOURCES.Factory.newInstance();
		USERS users =USERS.Factory.newInstance();
		APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
		LOCATIONS locs = LOCATIONS.Factory.newInstance();
		parser.processedPolicy(pol,testRes,testSubject,res,users,apps,locs);
		//parser.processedPolicy(POLICY.Factory.newInstance(),testRes,testSubject,RESOURCES.Factory.newInstance(),USERS.Factory.newInstance(),APPLICATIONS.Factory.newInstance(),LOCATIONS.Factory.newInstance());
	}
	
	public void processedPolicy(POLICY originalPolicy,String resources, String subject, RESOURCES res, USERS users, APPLICATIONS apps, LOCATIONS locs){
		
		CONDITION[] resConditions = null;
		CONDITION[] subjConditions = null;
		CONDITION[] combinedArray = null;
		POLICY resPolicy = null;
		POLICY subPolicy = null;
		List<POLICY> subjectPolicyList = parseSubjects(subject,users,apps,locs,originalPolicy);
		if(subjectPolicyList!=null && subjectPolicyList.size()>0){
			subPolicy=subjectPolicyList.get(0);
			subjConditions= subPolicy.getCONDITIONArray();
		}
		List<POLICY> resPolicyList = parseResources(resources,res);
		if(resPolicyList!=null && resPolicyList.size()>0 ){
			resPolicy=resPolicyList.get(0);
			resConditions = resPolicy.getCONDITIONArray();
		}
		
		if(resConditions!=null && subjConditions!=null){
			combinedArray= new CONDITION[resConditions.length+subjConditions.length];
			System.arraycopy(resConditions, 0, combinedArray, 0, resConditions.length);
			System.arraycopy(subjConditions, 0, combinedArray, resConditions.length, subjConditions.length);
		}else if(resConditions==null){
			combinedArray=subjConditions;
		}else if(subjConditions==null){
			combinedArray=resConditions;
		}
		originalPolicy.setCONDITIONArray(combinedArray);
	}
	
	//TEST nested AND NOT
	public List<POLICY> parseResources(String resourceComponentString, RESOURCES resources) {
		if(resourceComponentString.equals(PolicyTransformer.PREDICATE_TRUE) || resourceComponentString.equals(PolicyTransformer.PREDICATE_FALSE)){
			 ArrayList<POLICY> empty=new ArrayList<>();
			 empty.add(POLICY.Factory.newInstance());
			 return empty;
		}
		StringTokenizer policyTokenizer= new StringTokenizer(resourceComponentString);
		Stack<String> bracketStack= new Stack<>();
		List<String> list=new ArrayList<String>();
		List<String> operationsList=new ArrayList<String>();
		List<Integer> indexList= new ArrayList<Integer>();
		if(resourceComponentString.indexOf("(")<=0){
			//No '('. This should be a single component. e.g., FOR resource.fso.name = "**.jt.nxl" 
			String subStr = resourceComponentString.trim().substring(resourceComponentString.indexOf(" "));
			if(!subStr.trim().equals("TRUE") && !subStr.trim().equals("FALSE")){
				list.add(subStr);
				indexList.add(new Integer(1));				
			}
//			extractSingleComponent(subjectComponentString, list, operationsList, indexList);
		}else if((getCharCount(resourceComponentString,'(')==1) && resourceComponentString.indexOf(" AND ")<0 && resourceComponentString.indexOf(" OR ")>0){
			resourceComponentString = resourceComponentString.substring(resourceComponentString.indexOf("(")+1,resourceComponentString.indexOf(")")); 
			String[] tokens = resourceComponentString.split(" OR ");
			for (int i = 0; i < tokens.length; i++) {
				list.add(tokens[i].trim());
				indexList.add(2);
				operationsList.add("OR");
			}
			operationsList.remove(operationsList.size()-1);
		}
		else{
			extractComponents(policyTokenizer,
					bracketStack, list, operationsList, indexList);
		}
//		int resId=resources.getRESOURCEArray().length;
		List<noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE> currPolRes = new ArrayList<noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE>(); 
		createResources(list, resources, currPolRes);
		boolean badPolicy=isSupportedPolicy(operationsList,indexList);
		List<POLICY> policies = new ArrayList<>();
		if(!badPolicy){
			for (RESOURCE res : currPolRes) {
				POLICY pol= POLICY.Factory.newInstance();
				CONDITION addNewCONDITION = pol.addNewCONDITION();
				addNewCONDITION.setType("res");
				addNewCONDITION.setExclude(false);
				addNewCONDITION.newCursor().setTextValue(res.getId()+"");
				policies.add(pol);
			}
			ArrayList<Integer> priorityQueue = new ArrayList<>();
			HashSet<Integer> uniqueSet= new HashSet<>();
			for(int b=0;b<indexList.size();b++){
				if(!uniqueSet.contains(indexList.get(b))){
					priorityQueue.add(indexList.get(b));
					uniqueSet.add(indexList.get(b));
				}
			}
			Collections.sort(priorityQueue,Collections.reverseOrder());
//			for(Integer inte:priorityQueue){
////				System.out.println(inte);
//			}
			alternateCondition(indexList, operationsList,policies,priorityQueue);
//			System.out.println("");
		}
		return policies;
	}

	public List<POLICY> parseSubjects(String subjectComponentString, USERS usersComponent, APPLICATIONS appsComponent, LOCATIONS locsComponent, POLICY originalPolicy) {
		if(subjectComponentString.equals(PolicyTransformer.PREDICATE_TRUE) || subjectComponentString.equals(PolicyTransformer.PREDICATE_FALSE)){
			 ArrayList<POLICY> empty=new ArrayList<>();
			 empty.add(POLICY.Factory.newInstance());
			 return empty;
		}
		StringTokenizer policyTokenizer= new StringTokenizer(subjectComponentString);
		Stack<String> bracketStack= new Stack<>();
		List<String> list=new ArrayList<String>();
		List<String> operationsList=new ArrayList<String>();
		List<Integer> indexList= new ArrayList<Integer>();	
		if(subjectComponentString.indexOf("(")<=0){
			//No '('. This should be a single component. e.g., FOR resource.fso.name = "**.jt.nxl" 
			String subStr = subjectComponentString.trim().substring(subjectComponentString.indexOf(" "));
			list.add(subStr);
			indexList.add(new Integer(1));
//			extractSingleComponent(subjectComponentString, list, operationsList, indexList);
		}else if((getCharCount(subjectComponentString,'(')==1) && subjectComponentString.indexOf(" AND ")<0 && subjectComponentString.indexOf(" OR ")>0){
			subjectComponentString = subjectComponentString.substring(subjectComponentString.indexOf("(")+1,subjectComponentString.indexOf(")")); 
			String[] tokens = subjectComponentString.split(" OR ");
			for (int i = 0; i < tokens.length; i++) {
				list.add(tokens[i].trim());
				indexList.add(2);
				operationsList.add("OR");
			}
			operationsList.remove(operationsList.size()-1);
		}
		else{
			extractComponents(policyTokenizer,
					bracketStack, list, operationsList, indexList);
		}
//		int appId = usersComponent.getUSERArray().length;
//		int userId = appsComponent.getAPPLICATIONArray().length;
//		int locationId = locsComponent.getLOCATIONArray().length;
		List<USER> currPolUsers = new ArrayList<USER>();
		List<APPLICATION> currPolApps = new ArrayList<APPLICATION>();
		List<LOCATION> currPolLocs = new ArrayList<LOCATION>();
		createUsers(list,usersComponent,appsComponent,locsComponent,originalPolicy, currPolUsers, currPolApps, currPolLocs, operationsList, indexList);
		boolean badPolicy=isSupportedPolicy(operationsList,indexList);
		List<POLICY> userPolicies = new ArrayList<>(); 
		if(!badPolicy){
			for (USER user: currPolUsers) {
				POLICY pol= POLICY.Factory.newInstance();
				CONDITION addNewCONDITION = pol.addNewCONDITION();
				addNewCONDITION.setExclude(false);
				addNewCONDITION.setType("usr");
				addNewCONDITION.newCursor().setTextValue(user.getId()+"");
				userPolicies.add(pol);
			}
			for(APPLICATION app: currPolApps){
				POLICY pol= POLICY.Factory.newInstance();
				CONDITION addNewCONDITION = pol.addNewCONDITION();
				addNewCONDITION.setExclude(false);
				addNewCONDITION.setType("app");
				addNewCONDITION.newCursor().setTextValue(app.getId()+"");
				userPolicies.add(pol);
			}
			for(LOCATION loc: currPolLocs){
				POLICY pol= POLICY.Factory.newInstance();
				CONDITION addNewCONDITION = pol.addNewCONDITION();
				addNewCONDITION.setExclude(false);
				addNewCONDITION.setType("loc");
				addNewCONDITION.newCursor().setTextValue(loc.getId()+"");
				userPolicies.add(pol);
			}
			ArrayList<Integer> priorityQueue = new ArrayList<>();
			HashSet<Integer> uniqueSet= new HashSet<>();
			for(int b=0;b<indexList.size();b++){
				if(!uniqueSet.contains(indexList.get(b))){
					priorityQueue.add(indexList.get(b));
					uniqueSet.add(indexList.get(b));
				}
			}
			Collections.sort(priorityQueue,Collections.reverseOrder());
//			for(Integer inte:priorityQueue){
//				System.out.println(inte);
//			}
			alternateCondition(indexList, operationsList,userPolicies,priorityQueue);
//			System.out.println(appsComponent.toString());
		}
		return userPolicies;
	}

//	private void extractSingleComponent(String componentString,
//			List<String> list, List<String> operationsList,
//			List<Integer> indexList) {
//		String str = componentString.trim().substring(componentString.indexOf(" "));
////		StringTokenizer tokenizer = new StringTokenizer(str, delim)
//		int delimPosn = -1;
//		String delimiter = "";
//		for (Iterator iterator = validOperations.iterator(); iterator.hasNext();) {
//			delimiter = (String) iterator.next();
//			delimPosn = str.indexOf(delimiter);
//			if(delimPosn>=0){
//				break;				
//			}
//		}
//		String name = str.substring(0, delimPosn-1);
//		String val = str.substring(delimPosn+delimiter.length());
//		
//		
//	}

	private int getCharCount(String subjectComponentString, char c) {
		int count = 0;
		for(char ch : subjectComponentString.toCharArray() ){
			if(ch==c){
				count++;
			}
		}
		return count;
	}

	private USERS createUsers(List<String> list, USERS users, APPLICATIONS applications, LOCATIONS locations, POLICY originalPolicy, List<USER> currPolUsers, List<APPLICATION> currPolApps, List<LOCATION> currPolLocs, List<String> operationsList, List<Integer> indexList) {
		int appId = applications.getAPPLICATIONArray().length;;
		int userId = users.getUSERArray().length;
		int locationId = locations.getLOCATIONArray().length;
		for(int i=0; i<list.size(); i++){
			String curSubject=list.get(i);
			if(curSubject.trim().startsWith("user")){
				USER user=null;
				String[] subjectCreator=curSubject.split("AND");
				if(subjectCreator.length==0){
					if(curSubject.indexOf(" has ")>0){
						String userGroup = curSubject.trim().substring(curSubject.indexOf(" has ")+" has ".length()); 
						originalPolicy.setUsergroup(userGroup.trim());
					}
				}
				for(String token: subjectCreator){
					String curProperty=token;
					StringTokenizer strTokenizer=new StringTokenizer(curProperty);
					String name = strTokenizer.nextToken().trim();
					String operationString=strTokenizer.nextToken();
					while(!validOperations.contains(operationString.trim()) && !operationString.trim().equals("has")){
						name+=" "+operationString;
						operationString=strTokenizer.nextToken().trim();
					}
					if(name.trim().equals("user.GROUP")){
						//What to do here if there are multiple user groups
						if(curProperty.indexOf(" has ")>=0){
							String userGroup = curProperty.substring(curProperty.indexOf(" has ")+" has ".length()); 
							originalPolicy.setUsergroup(userGroup.trim());
							if(subjectCreator.length==1){
								if(operationsList.size()>0){
									operationsList.remove(i);
								}
								indexList.remove(i);
							}
						}
					}else{
						if(user==null){
							user= users.addNewUSER();
							user.setId(userId++);
							currPolUsers.add(user);
						}
						operationString=PolicyTransformer.getCompareMethod(operationString);
						String value = strTokenizer.nextToken().trim();
						while(strTokenizer.hasMoreTokens()){
							value+=" "+strTokenizer.nextToken();
						}
						if(value.length()>=2){
							value=value.substring(1,value.length()-1);
						}
						PropertyType newProperty = user.addNewPROPERTY();
						newProperty.setType("string");
						newProperty.setMethod(operationString);
						newProperty.setValue(PolicyTransformer.escapeUnwantedQuotes(value));
						if(name.startsWith("user.")){
							name=name.trim().substring(name.indexOf("user.")+"user.".length());
						}
						newProperty.setName(name);
					}
				}
				user=null;
			}
			else if(curSubject.startsWith("application")){
				APPLICATION application=null;
				String[] subjectCreator=curSubject.split("AND");
				for(String token: subjectCreator){
					String curProperty=token;
					StringTokenizer strTokenizer=new StringTokenizer(curProperty);
					String name = strTokenizer.nextToken().trim();
					String operationString=strTokenizer.nextToken();
					while(!validOperations.contains(operationString.trim()) && !operationString.trim().equals("has")){
						name+=" "+operationString;
						operationString=strTokenizer.nextToken().trim();
					}
					//					if(!name.trim().equals("application.GROUP")){
					if(application==null){
						application= applications.addNewAPPLICATION();
						application.setId(appId++);
						currPolApps.add(application);
					}
					operationString=PolicyTransformer.getCompareMethod(operationString);
					String value = strTokenizer.nextToken().trim();
					while(strTokenizer.hasMoreTokens()){
						value+=" "+strTokenizer.nextToken();
					}
					if(value.length()>=2){
						value=value.substring(1,value.length()-1);
					}
					PropertyType newProperty = application.addNewPROPERTY();
					newProperty.setType("string");
					newProperty.setMethod(operationString);
					newProperty.setValue(PolicyTransformer.escapeUnwantedQuotes(value));
					if(name.startsWith("application.")){
						name=name.trim().substring(name.indexOf("application.")+"application.".length());
					}
					newProperty.setName(name);
					//					}
				}
				application=null;
			}
			else if(curSubject.startsWith("host")){
				LOCATION location=null;
				String[] subjectCreator=curSubject.split("AND");
				for(String token: subjectCreator){
					String curProperty=token;
					StringTokenizer strTokenizer=new StringTokenizer(curProperty);
					String name = strTokenizer.nextToken().trim();
					String operationString=strTokenizer.nextToken();
					while(!validOperations.contains(operationString.trim()) && !operationString.trim().equals("has")){
						name+=" "+operationString;
						operationString=strTokenizer.nextToken().trim();
					}
					//					if(!name.trim().equals("host.GROUP")){
					if(location==null){
						location= locations.addNewLOCATION();
						location.setId(locationId++);
						currPolLocs.add(location);
					}
					operationString=PolicyTransformer.getCompareMethod(operationString);
					String value = strTokenizer.nextToken().trim();
					while(strTokenizer.hasMoreTokens()){
						value+=" "+strTokenizer.nextToken();
					}
					if(value.length()>=2){
						value=value.substring(1,value.length()-1);
					}
					PropertyType newProperty = location.addNewPROPERTY();
					newProperty.setType("string");
					newProperty.setMethod(operationString);
					newProperty.setValue(PolicyTransformer.escapeUnwantedQuotes(value));
					if(name.startsWith("host.")){
						name=name.trim().substring(name.indexOf("host.")+"host.".length());
					}
					if(name.trim().equals(PolicyTransformer.DATATYPE_INET_ADDRESS)){
						newProperty.setName(PolicyTransformer.PROPERTYNAME_IPV4);
					}else{
						newProperty.setName(name);
					}
					//					}
				}
				location=null;
			}
		}
		return users;
	}

	private void extractComponents(StringTokenizer policyTokenizer,
			Stack<String> bracketStack, List<String> list,
			List<String> operationsList, List<Integer> indexList) {
		String component="";
		while(policyTokenizer.hasMoreTokens()){
			String nextToken=policyTokenizer.nextToken().trim();
			//Handlin (
			if(nextToken.startsWith("(")){
				int newBrackets=0;
				while(nextToken.startsWith("(")){
					bracketStack.push("(");
					newBrackets++;
					nextToken=nextToken.substring(1);
				}
				//If doesn't component starts with a keyword like FOR or BY
				if(!notNeededWords.contains(component.trim())){
					if(operations.contains(component.trim())){
						operationsList.add(component.trim()+" ");
					}else{
						//This will happen only for single components
						if(component.trim().endsWith("AND")){
							operationsList.add("AND");
							component=component.trim();
							component=component.substring(0, component.length()-3);
						}
						else if(component.trim().endsWith("AND NOT")){
							operationsList.add("AND NOT");
							component=component.trim().substring(0, component.trim().lastIndexOf("AND NOT"));
						}
						/*if(component.trim().endsWith("OR")){
							operationsList.add("OR");
							component=component.trim();
							component=component.substring(0, component.length()-2);
						}*/
						if(component.trim().length()>0){
							if(indexList.size()>0){
								indexList.add(indexList.get(indexList.size()-1));
							}else{
								indexList.add(bracketStack.size()-newBrackets+1);
							}
							list.add(component.trim()+" ");
						}
					}
				}
				component=nextToken+" ";
			}
			else if(nextToken.endsWith(")")){
				int numRemoved = 0;
				while(nextToken.endsWith(")")){
					numRemoved++;
					bracketStack.pop();
					nextToken=nextToken.substring(0,nextToken.length()-1);
				}
				component+=nextToken;
				if(component.trim().length()>0){
					//If the last component had only a single component
					if(component.split("\\s+").length==3){
						numRemoved++;
					}
					indexList.add(bracketStack.size()+numRemoved);
					numRemoved=0;
					list.add(component+" ");
				}
				component="";
			}
			else{
				component+=nextToken+" ";
			}
			if(component.trim().startsWith("AND")){
				operationsList.add(component);
				component="";
			}
			if(component.trim().startsWith("NOT")){
				operationsList.add(nextToken);
				component="";
			}
			if(component.trim().startsWith("OR")){
				operationsList.add(nextToken);
				component="";
			}
		}

		/*for(int i=0;i<list.size();i++){
			System.out.println(list.get(i));
		}*/
		sanitizeOperation(operationsList,indexList);
		/*System.out.println("Printing operations list");

		for(int i=0;i<operationsList.size();i++){
			System.out.println(operationsList.get(i));
		}

		for(int i=0;i<indexList.size();i++){
			System.out.println(indexList.get(i));
		}*/
	}

	//All the weird conditions to make RMC work
	private boolean isSupportedPolicy(List<String> operationsList, List<Integer> indexList) {
		return false;
//		for(int i=0;i<indexList.size()-1;i++){
//			int curBrackets=indexList.get(i);
//			int nextBrackets= indexList.get(i+1);
//			if(curBrackets!=nextBrackets && operationsList.get(i-1).trim().equals("OR")){
//				System.out.println("Complicated policy to handle. Nested OR detected");
//				return true;
//			}
//		}
//		return false;
	}

	private void createResources(List<String> list, RESOURCES resources, List<noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE> currPolRes) {
		int resLength=resources.getRESOURCEArray().length;
		for(int i=0; i<list.size(); i++){
			RESOURCE res= resources.addNewRESOURCE();
			res.setId(i+resLength);
			currPolRes.add(res);
			String curRes=list.get(i);
			String[] resourceCreator=curRes.split("AND");
			for(String token : resourceCreator){
				String curProperty = token;
				StringTokenizer strTokenizer=new StringTokenizer(curProperty);
				String name = strTokenizer.nextToken().trim();
				String operationString=strTokenizer.nextToken();
				while(!validOperations.contains(operationString.trim())){
					name+=" "+operationString;
					operationString=strTokenizer.nextToken().trim();
				}
				operationString=PolicyTransformer.getCompareMethod(operationString);
				String value = strTokenizer.nextToken().trim();
				while(strTokenizer.hasMoreTokens()){
					value+=" "+strTokenizer.nextToken();
				}
				value=value.substring(1,value.length()-1);
				PropertyType newProperty = res.addNewPROPERTY();
				newProperty.setType("string");
				newProperty.setMethod(operationString);
				newProperty.setValue(PolicyTransformer.escapeUnwantedQuotes(value));
				if(name.startsWith("resource.fso")){
					name=name.trim().substring(name.indexOf("resource.fso.")+"resource.fso.".length());
				}
				newProperty.setName(name);
			}
		}
	}

	private void sanitizeOperation(List<String> operList, List<Integer> indexList) {
		for(int i=0;i<operList.size();i++){
			if(operList.get(i).trim().equals("NOT") && i>0){
				operList.set(i-1,operList.get(i-1).trim()+" "+operList.get(i).trim());
				operList.remove(i);
			}
		}
	}


	private void alternateCondition(List<Integer> indexList, List<String> operationsList, List<POLICY> policy, ArrayList<Integer> priorityQueue){
		if(operationsList.size()==0){
			return;
		}
		//int maxIndex=priorityQueue
		//		int prevBrackets=indexList.get(0);
		int maxBrackets=priorityQueue.get(0);
		boolean merged=false;
		for(int a=0; a<indexList.size()-1;a++){
			int curBrackets=indexList.get(a);
			int nextBracket=indexList.get(a+1);
			
			if(curBrackets==maxBrackets){
				CONDITION[] nextCondition = policy.get(a+1).getCONDITIONArray();
				CONDITION[] curConditions = policy.get(a).getCONDITIONArray();
				// Now we can proceed for AND and OR conditions
				if(curBrackets==nextBracket){
					//AND OPERATION
					if(operationsList.get(a).trim().equals("AND")){
						CONDITION[] newArray= new CONDITION[curConditions.length+nextCondition.length];
						System.arraycopy(nextCondition, 0, newArray, 0, nextCondition.length);
						System.arraycopy(curConditions, 0, newArray, nextCondition.length, curConditions.length);
						policy.get(a+1).setCONDITIONArray(newArray);
						policy.remove(a);
						operationsList.remove(a);
						indexList.remove(a);
						//indexList.set(a-1, indexList.get(a-1)-1);
						merged=true;
						a--;
						continue;
					}
					//OR OPERATION
					else if(operationsList.get(a).trim().equals("OR")){				
						nextCondition[0].newCursor().setTextValue(nextCondition[0].newCursor().getTextValue()+","+curConditions[0].newCursor().getTextValue());
						operationsList.remove(a);
						//indexList.set(a-1, indexList.get(a)-1);
						indexList.remove(a);
						policy.remove(a);
						merged=true;
						a--;
						continue;
					}
				}
				//AND NOT
				if(operationsList.get(a-1).trim().equals("AND NOT")){
					if(merged){
						if(a>0){
							indexList.set(a, indexList.get(a)-1);
						}else if(a==0){
							indexList.set(0, indexList.get(0)-1);
						}
						merged=false;
					}
					for(CONDITION c:policy.get(a).getCONDITIONArray()){
						c.setExclude(true);
					}
					operationsList.set(a-1, "AND");
					int curIndex = indexList.get(a);
					curIndex--;
					indexList.set(a, curIndex);
					if(priorityQueue.size()>1){
						if(!(priorityQueue.get(1)==curIndex)){
							priorityQueue.add(1,curIndex);
						}
					}
				}
				else{
					//prevBrackets=curBrackets;
					if(merged){
						if(a>0){
							indexList.set(a-1, indexList.get(a-1)-1);
						}else if(a==0){
							indexList.set(0, indexList.get(0)-1);
						}
						merged=false;
					}
				}

			}
		}
		if(merged){
			indexList.set(indexList.size()-1, indexList.get(indexList.size()-1)-1);
			//maxBrackets--;
		}
		int lastIndex=indexList.size()-1;
		if(maxBrackets==indexList.get(lastIndex) && operationsList.get(lastIndex-1).trim().equals("AND NOT")){
			for(CONDITION c:policy.get(lastIndex).getCONDITIONArray()){
				c.setExclude(true);
			}
			operationsList.set(lastIndex-1, "AND");
			int curIndex = indexList.get(lastIndex);
			curIndex--;
			indexList.set(lastIndex, curIndex);
			if(priorityQueue.size()>1){
				if(!(priorityQueue.get(1)==curIndex)){
					priorityQueue.add(1,curIndex);
				}
			}
		}
		int newMax=Collections.max(indexList);
		if(newMax!=priorityQueue.get(0)){
			priorityQueue.remove(0);
		}
		if(!priorityQueue.contains(newMax)){
			priorityQueue.add(0,newMax);
		}
		alternateCondition(indexList, operationsList, policy,priorityQueue);

	}
}

