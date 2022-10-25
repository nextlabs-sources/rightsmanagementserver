package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

public class ComponentStringSplitter {
	private static final String CLOSE_PARENTHESIS = ")";
	private static final String TRUE_STRING = "TRUE";
	private static final String RESOURCE_STRING = "resource.";
	private static final String APPLICATION_STRING = "application.";
	private static final String HOST_STRING = "host.";
	private static final String USER_STRING = "user.";
	private static final String DOUBLE_QUOTE = "\"";
	private static final String OPEN_PARENTHESIS = "(";
	private int doubleQuoteCount = 0;
	
	public List<String> splitSubjects(String input) {
		List<String> results = new ArrayList<String>();
		String formattedInput = input;
		if (input.startsWith(OPEN_PARENTHESIS)) {
			formattedInput = input.substring(1, input.length()-1);
		}

		StringTokenizer str = new StringTokenizer(formattedInput, " ()", true);
		String currToken = "";
		Stack<String> holder = new Stack<String>();
		Stack<String> backTrack = new Stack<String>();
		boolean isRHS = false;
		while (str.hasMoreTokens()) {
			String token = str.nextToken();
//			System.out.println(token);
			holder.push(token);
			if(token.equals(DOUBLE_QUOTE)){
				doubleQuoteCount++;
			}
			if(PolicyComponentHelperV2.isOperator(token)){
				isRHS = true;
			}
			if ((token.startsWith(USER_STRING)||token.startsWith(DOUBLE_QUOTE+USER_STRING)) && !isRHS && doubleQuoteCount%2==0) {
				if (!currToken.equals(USER_STRING)) {
					if (!currToken.equals("")) {
						extractComponent(holder, backTrack, results);
					}
					currToken = USER_STRING;
				}
			}
			if ((token.startsWith(HOST_STRING)||token.startsWith(DOUBLE_QUOTE+HOST_STRING)) && !isRHS && doubleQuoteCount%2==0) {
				if (!currToken.equals(HOST_STRING)) {
					if (!currToken.equals("")) {
						extractComponent(holder, backTrack, results);
					}
					currToken = HOST_STRING;
				}
			}
			if ((token.startsWith(APPLICATION_STRING)||token.startsWith(DOUBLE_QUOTE+APPLICATION_STRING)) && !isRHS && doubleQuoteCount%2==0) {
				if (!currToken.equals(APPLICATION_STRING)) {
					if (!currToken.equals("")) {
						extractComponent(holder, backTrack, results);
					}
					currToken = APPLICATION_STRING;
				}
			}
			if ((token.startsWith(RESOURCE_STRING)||token.startsWith(DOUBLE_QUOTE+RESOURCE_STRING)) && !isRHS && doubleQuoteCount%2==0) {
				if (!currToken.equals(RESOURCE_STRING)) {
					if (!currToken.equals("")) {
						extractComponent(holder, backTrack, results);
					}
					currToken = RESOURCE_STRING;
				}
			}
			if(!PolicyComponentHelperV2.isOperator(token) && !token.trim().equals("")){
				isRHS = false;
			}
		}
		Stack<String> finalComponent = new Stack<>();
		while (holder.size()>0) {
			String poppedToken = holder.pop(); 
			finalComponent.push(poppedToken);
			if(poppedToken.equals(DOUBLE_QUOTE)){
				doubleQuoteCount--;
			}
		}
		StringBuffer buf = new StringBuffer();
		while (finalComponent.size() > 0) {
			buf.append(finalComponent.pop());
		}
		results.add(buf.toString().trim());
		return results;
	}

	
	public List<String> getParts(String pql){
		List<String> parts = new ArrayList<>();
		if(pql.startsWith(OPEN_PARENTHESIS)){
			pql=pql.substring(1,pql.length()-1);
		}
		StringTokenizer str = new StringTokenizer(pql, " ()",true);
		String tempString = addPartToList(parts,str);
		if(tempString.startsWith(OPEN_PARENTHESIS)){
			tempString = tempString.substring(1, tempString.length()-1);
		}
		tempString = addPartToList(parts, new StringTokenizer(tempString, " ()",true));
		if(!tempString.equals(TRUE_STRING)){
			parts.add(tempString);
		}
		return parts;
	}

	private String addPartToList(List<String> parts, StringTokenizer str) {
		int bracketCount = 0;
		StringBuffer component1 = new StringBuffer();
		while(str.hasMoreTokens()){
			String token = str.nextToken();
			if(token.equals(OPEN_PARENTHESIS)){
				bracketCount++;
			}else if(token.equals(CLOSE_PARENTHESIS)){
				bracketCount--;
			}
			if(token.equals(PolicyComponentHelperV2.AND.getSymbol()) && bracketCount == 0){
				if(!component1.toString().trim().equals(TRUE_STRING)){
					parts.add(component1.toString().trim());
				}
				break;
			}
			component1.append(token);
		}
		StringBuffer tempBuffer = new StringBuffer();
		while(str.hasMoreTokens()){
			tempBuffer.append(str.nextToken());
		}
		return tempBuffer.toString().trim();
	}
	
	private void extractComponent(Stack<String> holder, Stack<String> backtrack, List<String> results) {
		boolean andFound = false; 
		int openBrackets = 0;
		int closedBrackets = 0;
		Stack<String> temporary = new Stack<>();
		String poppedStr = null;
		while (holder.size() > 0) {
			poppedStr = holder.pop();
			if(poppedStr.equals(DOUBLE_QUOTE)){
				doubleQuoteCount--;
			}
			if (!andFound) {
				if (poppedStr.equals(PolicyComponentHelperV2.AND.getSymbol())) {
					andFound = true;
					continue;
				}
				backtrack.push(poppedStr);
			}
			else {
				temporary.push(poppedStr);
				if (poppedStr.equals(OPEN_PARENTHESIS)) {
					openBrackets++;
				} else if (poppedStr.equals(CLOSE_PARENTHESIS)) {
					closedBrackets++;
				}
				if (openBrackets == closedBrackets && openBrackets > 0 && closedBrackets > 0) {
					while(holder.size()>0 && holder.peek().trim().equals("")){
						holder.pop();
					}
					if(holder.size()>0 && holder.peek().equals(PolicyComponentHelperV2.NOT.getSymbol())){
						temporary.push(holder.pop());
					}
					String createdExpression ="";
					StringBuffer buf = new StringBuffer();
					while (temporary.size() > 0) {
						createdExpression = temporary.pop();
						buf.append(createdExpression);
					}
					results.add(buf.toString().trim());
					break;
				}
			}
		}
		while (backtrack.size() > 0){
			String token = backtrack.pop();
			holder.push(token);
			if(token.equals(DOUBLE_QUOTE)){
				doubleQuoteCount++;
			}
		}
		if (temporary.size() != 0) {
			StringBuffer buf = new StringBuffer();
			while (temporary.size() > 0) {
				buf.append(temporary.pop());
			}
			results.add(buf.toString().trim());
		}
	}
}