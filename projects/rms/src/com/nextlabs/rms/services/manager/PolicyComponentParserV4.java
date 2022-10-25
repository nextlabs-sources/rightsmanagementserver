package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.nextlabs.rms.eval.UnsupportedComponentException;
import com.nextlabs.rms.services.manager.node.MultiPolicyNode;
import com.nextlabs.rms.services.manager.node.NegatePolicyNode;
import com.nextlabs.rms.services.manager.node.Node;
import com.nextlabs.rms.services.manager.node.PolicyComponent;
import com.nextlabs.rms.services.manager.node.PolicyNode;
import com.nextlabs.rms.services.manager.node.SinglePolicyNode;
import com.nextlabs.rms.services.manager.node.UserGroupNode;

public class PolicyComponentParserV4 extends AbstractEvaluator<String> implements IPolicyComponentParser {
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
	public static final Node EMPTY = new Node() {
		
		@Override
		public Node negate() {
			return this;
		}
	};

	private static final Parameters PARAMETERS;
	static {
		// Create the evaluator's parameters
		PARAMETERS = new Parameters();
		// Add the supported operators
		PARAMETERS.add(PolicyComponentHelper.AND);
		PARAMETERS.add(PolicyComponentHelper.OR);
		PARAMETERS.add(PolicyComponentHelper.NOT);
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

	public PolicyComponentParserV4(RESOURCES res, USERS users, APPLICATIONS apps, LOCATIONS locs, ENVS envs) {
		super(PARAMETERS);
		this.resources = res;
		this.users = users;
		this.apps = apps;
		this.locs = locs;
		this.envs = envs;
	}

	private void addANDConditionToPolicy(POLICY pol, PolicyComponent comp) {
		if (comp == null) {
			return;
		}
		CONDITION condition = pol.addNewCONDITION();
		condition.setExclude(false);
		condition.setType(comp.getComponentType());
		condition.setStringValue(comp.getId() + "");
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

	private void addNewProperty(SinglePolicyNode comp1, String operand1) {
		if (!(comp1.getComponentType().equals(COMPONENTTYPE_ENV) && (operand1.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || operand1
				.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE)))) {
			// This can happen for ENV components
			PropertyType prop = PropertyType.Factory.newInstance();
			updateProperties(operand1, prop);
			comp1.addProperty(prop);
		}
	}

	private void addORConditionToPolicy(POLICY pol, PolicyComponent comp, PolicyComponent previousComponent) {
		if (comp == null) {
			return;
		}
		CONDITION[] condArr = pol.getCONDITIONArray();
		for (CONDITION condition : condArr) {
			if (previousComponent != null && isMatchingId(condition.getStringValue(), String.valueOf(previousComponent.getId()))) {
				if (condition.getType().equals(comp.getComponentType()) && !condition.getExclude()) {
					String newStr = condition.getStringValue() + "," + comp.getId();
					condition.setStringValue(newStr);
					addComponentToGroup(comp);
					return;
				}
			}
		}
		addANDConditionToPolicy(pol, comp);
	}

	private void addUserGroupToPolicy(POLICY pol, UserGroupNode node) {
		String userGroup = pol.getUsergroup();
		if (userGroup == null || userGroup.length() == 0) {
			pol.setUsergroup(node.getValue());
		} else {
			pol.setUsergroup(userGroup + "," + node.getValue());
		}
	}

	private boolean allowToAppend(Operator operator, String componentType, MultiPolicyNode multiComponent) {
		List<Node> components = multiComponent.getComponents();
		List<Operator> operators = multiComponent.getOperators();
		for (int i = 0; i < components.size(); i++) {
			Node node = components.get(i);
			if (!(node instanceof SinglePolicyNode)) {
				return false;
			}
			SinglePolicyNode component = (SinglePolicyNode) node;
			if (!component.getComponentType().equals(componentType)) {
				return false;
			}
			if (i > 0) {
				Operator currentOperator = operators.get(i - 1);
				if (!currentOperator.equals(operator)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean allowToMerge(Operator operator, MultiPolicyNode component1, MultiPolicyNode component2) {
		boolean allowToAppend = hasSameOperator(component1, operator);
		if (allowToAppend) {
			allowToAppend = hasSameOperator(component2, operator);
			if (allowToAppend) {
				List<Node> components1 = component1.getComponents();
				List<Node> components2 = component2.getComponents();
				for (int i = 0; i < components1.size(); i++) {
					Node node1 = components1.get(i);
					if (!(node1 instanceof SinglePolicyNode)) {
						return false;
					}
					SinglePolicyNode singleNode1 = (SinglePolicyNode) node1;
					for (int j = 0; j < components2.size(); j++) {
						Node node2 = components2.get(j);
						if (!(node2 instanceof SinglePolicyNode)) {
							return false;
						}
						SinglePolicyNode singleNode2 = (SinglePolicyNode) node2;
						if (!singleNode1.getComponentType().equals(singleNode2.getComponentType())) {
							return false;
						}
					}
				}
			}
		}
		return allowToAppend;
	}

	private PolicyComponent createNewComponent(String compType) {
		int id = getRunningId(compType);
		PolicyComponent comp = new PolicyComponent(id, compType);
		return comp;
	}

	private MultiPolicyNode createNewMultiPolicyComponent(Node operand1, Node operand2, Operator operator) {
		MultiPolicyNode node = new MultiPolicyNode();
		node.addRoot(operand1);
		node.addNode(operand2, operator);
		return node;
	}

	private SinglePolicyNode createNewSingleNode(String componentType) {
		SinglePolicyNode node = new SinglePolicyNode(componentType);
		return node;
	}

	private UserGroupNode createNewUserGroupNode(String expression) {
		Component component = getLHSandRHS(expression);
		String userGroupValue = component.getOperand2();
		UserGroupNode node = new UserGroupNode(userGroupValue);
		return node;
	}

	private PolicyComponent createPolicyComponent(SinglePolicyNode node) {
		PolicyComponent policyComponent = createNewComponent(node.getComponentType());
		policyComponent.addProperties(node.getPropertyList());
		node.setPolicyComponent(policyComponent);
		return policyComponent;
	}

	private Node determineMergedNode(Node node1, Node node2, String operand1, String operand2, Operator operator) {
		Node result = null;
		if (node1 instanceof SinglePolicyNode) {
			SinglePolicyNode component1 = (SinglePolicyNode) node1;
			if (node2 instanceof SinglePolicyNode) {
				SinglePolicyNode component2 = (SinglePolicyNode) node2;
				if (component1.getComponentType().equals(component2.getComponentType())) {
					if (PolicyComponentHelper.AND.equals(operator)) {
						SinglePolicyNode mergedNode = component1.copy();
						mergedNode.addProperties(component2.getPropertyList());
						result = mergedNode;
					} else {
						// OR condition
						result = createNewMultiPolicyComponent(component1, component2, operator);
					}
				} else {
					// difference type of node
					result = createNewMultiPolicyComponent(component1, component2, operator);
				}
			} else if (node2 instanceof MultiPolicyNode) {
				MultiPolicyNode component2 = (MultiPolicyNode) node2;
				// check whether we can merge node1 to multi list
				if (allowToAppend(operator, component1.getComponentType(), component2)) {
					component2.addNode(component1, operator);
					result = component2;
				} else {
					result = createNewMultiPolicyComponent(node1, node2, operator);
				}
			} else if (node2 != null) {
				result = createNewMultiPolicyComponent(node1, node2, operator);
			} else {
				result = node1;
			}
		} else if (node1 instanceof MultiPolicyNode) {
			MultiPolicyNode component1 = (MultiPolicyNode) node1;
			if (node2 instanceof SinglePolicyNode) {
				SinglePolicyNode component2 = (SinglePolicyNode) node2;
				if (allowToAppend(operator, component2.getComponentType(), component1)) {
					component1.addNode(component2, operator);
					result = component1;
				} else {
					result = createNewMultiPolicyComponent(node1, node2, operator);
				}
			} else if (node2 instanceof MultiPolicyNode) {
				MultiPolicyNode component2 = (MultiPolicyNode) node2;
				if (allowToMerge(operator, component1, component2)) {
					List<Node> components2 = component2.getComponents();
					for (Node node : components2) {
						component1.addNode(node, operator);
					}
					result = component1;
				} else {
					result = createNewMultiPolicyComponent(node1, node2, operator);
				}
			} else if (node2 != null) {
				result = createNewMultiPolicyComponent(node1, node2, operator);
			} else {
				result = node1;
			}
		} else if (node1 instanceof NegatePolicyNode) {
			if (node2 != null) {
				result = createNewMultiPolicyComponent(node1, node2, operator);
			} else {
				result = node1;
			}
		} else if (node1 instanceof UserGroupNode) {
			if (node2 != null) {
				result = createNewMultiPolicyComponent(node1, node2, operator);
			} else {
				result = node1;
			}
		} else {
			result = node2;
		}
		result = result == null ? EMPTY : result;
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String evaluate(Operator operator, Iterator<String> operands, Object evaluationContext) {
		Map<String, Object> contextMap = (Map<String, Object>) evaluationContext;
		List<PolicyNode> nodes = (List<PolicyNode>) contextMap.get(NODE_TREE_KEY);
		String operand1 = operands.next().trim();
		String operand2 = null;
		if (operands.hasNext()) {
			operand2 = operands.next().trim();
		}
		String equation = "";
		if (operand2 != null) {
			equation = BracketPair.PARENTHESES.getOpen() + operand1 + " " + operator.getSymbol() + " " + operand2
					+ BracketPair.PARENTHESES.getClose();
		} else {
			equation = operator.getSymbol() + " " + operand1;
		}
		PolicyNode node = new PolicyNode(operand1, operand2, operator, equation);
		nodes.add(node);
		return equation;
	}

	public APPLICATIONS getApps() {
		return apps;
	}

	private String getComponentType(String str) {
		if (str != null) {
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
			} else if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
				return "";
			}
		}
		throw new UnsupportedComponentException("Unsupported component type for element " + str);
	}

	public ENVS getEnvs() {
		return envs;
	}

	private Component getLHSandRHS(String expression) {
		ExpressionEvaluator eval = new ExpressionEvaluator();
		List<Component> sequence = new ArrayList<Component>();
		eval.evaluate(expression, sequence);
		return sequence.get(0);
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

	public USERS getUsers() {
		return users;
	}

	private void handleComponentList(PolicyNode policyNode, Map<String, Node> policyComponents) {
		String operand1 = policyNode.getOperand1();
		String operand2 = policyNode.getOperand2();
		String equation = policyNode.getEquation();
		Operator operator = policyNode.getOperator();
		if (operand1 != null && operand2 != null) {
			Node node1 = policyComponents.get(operand1);
			Node node2 = policyComponents.get(operand2);
			if (node1 == null && node2 == null) {
				boolean hasConditionOperand1 = isHasCondition(operand1);
				boolean hasConditionOperand2 = isHasCondition(operand2);
				Node newNode = null;
				Node component1 = null;
				Node component2 = null;
				if (hasConditionOperand1) {
					component1 = createNewUserGroupNode(operand1);
				} else {
					String op1ComponentType = getComponentType(operand1);
					if (op1ComponentType.length() > 0) {
						component1 = createNewSingleNode(op1ComponentType);
						addNewProperty((SinglePolicyNode) component1, operand1);
					}
				}

				if (hasConditionOperand2) {
					component2 = createNewUserGroupNode(operand2);
				} else {
					String op2ComponentType = getComponentType(operand2);
					if (op2ComponentType.length() > 0) {
						component2 = createNewSingleNode(op2ComponentType);
						addNewProperty((SinglePolicyNode) component2, operand2);
					}
				}
				newNode = determineMergedNode(component1, component2, operand1, operand2, operator);
				policyComponents.put(equation, newNode);
			} else if (node1 != null && node2 != null) {
				Node newNode = determineMergedNode(node1, node2, operand1, operand2, operator);
				policyComponents.remove(operand1);
				policyComponents.remove(operand2);
				policyComponents.put(equation, newNode);
			} else {
				Node newNode = null;
				Node component1 = node1;
				Node component2 = node2;
				if (node1 == null) {
					boolean hasCondition = isHasCondition(operand1);
					if (hasCondition) {
						component1 = createNewUserGroupNode(operand1);
					} else {
						String op1ComponentType = getComponentType(operand1);
						if (op1ComponentType.length() > 0) {
							component1 = createNewSingleNode(op1ComponentType);
							addNewProperty((SinglePolicyNode) component1, operand1);
						}
					}
				} else {
					// op2List empty
					boolean hasCondition = isHasCondition(operand2);
					if (hasCondition) {
						component2 = createNewUserGroupNode(operand2);
					} else {
						String op2ComponentType = getComponentType(operand2);
						if (op2ComponentType.length() > 0) {
							component2 = createNewSingleNode(op2ComponentType);
							addNewProperty((SinglePolicyNode) component2, operand2);
						}
					}
				}
				newNode = determineMergedNode(component1, component2, operand1, operand2, operator);
				policyComponents.remove(operand1);
				policyComponents.remove(operand2);
				policyComponents.put(equation, newNode);
			}
		} else {
			Node node1 = policyComponents.get(operand1);
			Node newNode = node1;
			if (node1 == null) {
				String op1ComponentType = getComponentType(operand1);
				if (op1ComponentType.length() > 0) {
					SinglePolicyNode component1 = createNewSingleNode(op1ComponentType);
					addNewProperty(component1, operand1);
					newNode = component1.negate();
				}
			} else if (node1 instanceof SinglePolicyNode) {
				SinglePolicyNode component1 = (SinglePolicyNode) node1;
				newNode = component1.negate();
			} else if (node1 instanceof MultiPolicyNode) {
				MultiPolicyNode component1 = (MultiPolicyNode) node1;
				newNode = component1.negate();
			} else {
				NegatePolicyNode negatePolicyComponent = new NegatePolicyNode(node1);
				newNode = negatePolicyComponent;
			}
			policyComponents.remove(operand1);
			policyComponents.put(equation, newNode);
		}
	}

	private boolean hasSameOperator(MultiPolicyNode component, Operator operator) {
		List<Operator> operators = component.getOperators();
		if (operators.isEmpty()) {
			return false;
		}
		for (Operator op : operators) {
			if (!op.equals(operator)) {
				return false;
			}
		}
		return true;
	}

	private boolean isHasCondition(String condition) {
		if (!(condition.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || condition.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))) {
			// This can happen for ENV components
			Component comp = getLHSandRHS(condition);
			if (ExpressionEvaluator.HAS.getSymbol().equals(comp.getOperator()) && getComponentType(comp.getOperand1()).equals(COMPONENTTYPE_USER)) {
				return true;
			}
		}
		return false;
	}

	private boolean isMatchingId(String condStr, String id) {
		if (condStr.equals(id)) {
			return true;
		}
		if (condStr.indexOf(',') > 0) {
			String[] ids = condStr.split(",");
			for (String idStr : ids) {
				if (idStr.equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

	private void negateConditionInPolicy(POLICY pol, Node reply) {
		CONDITION[] condArr = pol.getCONDITIONArray();
		for (CONDITION condition : condArr) {
			if (reply instanceof SinglePolicyNode) {
				SinglePolicyNode component = (SinglePolicyNode) reply;
				PolicyComponent policyComponent = component.getPolicyComponent();
				if (policyComponent != null) {
					if (condition.getType().equals(policyComponent.getComponentType())
							&& isMatchingId(condition.getStringValue(), String.valueOf(policyComponent.getId()))) {
						condition.setExclude(true);
					}
				}
			} else if (reply instanceof MultiPolicyNode) {
				MultiPolicyNode multiComponents = (MultiPolicyNode) reply;
				List<Node> components = multiComponents.getComponents();
				for (Node node : components) {
					if (node instanceof SinglePolicyNode || node instanceof MultiPolicyNode) {
						negateConditionInPolicy(pol, node);
					}
				}
			}
		}
	}

	private void processComponents(POLICY policy, String str) {
		if (str == null || str.length() == 0) {
			return;
		}
		Map<String, Object> contextMap = new HashMap<String, Object>();
		List<PolicyNode> nodes = new ArrayList<PolicyNode>();

		contextMap.put(POLICY_OBJECT_KEY, policy);
		contextMap.put(NODE_TREE_KEY, nodes);
		String processedSubStr = "";
		if (str.startsWith("BY ")) {
			processedSubStr = str.trim().substring(3);
		} else if (str.startsWith("WHERE ")) {
			processedSubStr = str.trim().substring(6);
		} else if (str.startsWith("FOR ")) {
			processedSubStr = str.trim().substring(4);
		}
		if ((processedSubStr.equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE) || processedSubStr
				.equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE))) {
			return;
		}
		processedSubStr = replaceLogicalOperators(processedSubStr);
		String evaluate = evaluate(processedSubStr, contextMap);
		int totalNodes = nodes.size();
		Map<String, Node> policyComponents = new HashMap<String, Node>();
		if (totalNodes > 0) {
			for (int i = 0; i < totalNodes; i++) {
				PolicyNode policyNode = nodes.get(i);
				handleComponentList(policyNode, policyComponents);
			}
		} else {
			// single condition only i.e:
			// FOR resource.fso.ext = pdf
			// BY user.GROUP has 0
			if (evaluate.length() > 0) {
				Node component = null;
				if (isHasCondition(evaluate)) {
					component = createNewUserGroupNode(evaluate);
				} else {
					String op1ComponentType = getComponentType(evaluate);
					if (op1ComponentType.length() > 0) {
						component = createNewSingleNode(op1ComponentType);
						addNewProperty((SinglePolicyNode) component, evaluate);
					}
				}
				policyComponents.put(evaluate, component);
			}
		}
		if (policyComponents.size() > 0) {
			write(policy, null, policyComponents.values().iterator().next(), null);
		}
	}

	@Override
	public void processPolicy(POLICY pol, String resourceStr, String subjectStr, String envStr) {
		processComponents(pol, subjectStr);
		processComponents(pol, resourceStr);
		processComponents(pol, envStr);
	}

	private String replaceLogicalOperators(String inputStr) {
		String replacedStr = inputStr.replaceAll("(?m)\\bOR\\b(?=(?:\"[^\"]*\"|[^\"])*$)", PolicyComponentHelper.OR.getSymbol());
		replacedStr = replacedStr.replaceAll("(?m)\\bAND\\b(?=(?:\"[^\"]*\"|[^\"])*$)", PolicyComponentHelper.AND.getSymbol());
		replacedStr = replacedStr.replaceAll("(?m)\\bNOT\\b(?=(?:\"[^\"]*\"|[^\"])*$)", PolicyComponentHelper.NOT.getSymbol());
		replacedStr = replacedStr.replaceAll("(?m)\\bhas\\b(?=(?:\"[^\"]*\"|[^\"])*$)", "~#");
		// replace call_function to TRUE, because we are not going to send call_function to RMC
		String regex = "(?m)\\bcall_function\\s*\\([\\'\"\" ,.a-zA-Z0-9]+\\)\\s*(=|!=|>|>=|<=|<|~#)\\s*[\"\"\\w]+";
		Pattern p = Pattern.compile(regex);
		int idx = 0;
		final int maxLevel = 10;
		while (idx < maxLevel) {
			Matcher matcher = p.matcher(replacedStr);
			if (!matcher.find()) {
				break;
			} else {
				replacedStr = replacedStr.replaceAll(regex, "TRUE");
				idx++;
			}
		}
		return replacedStr;
	}

	@Override
	protected String toValue(String literal, Object evaluationContext) {
		return literal;
	}

	private void updateProperties(String operand, PropertyType prop) {
		Component splitComponent = getLHSandRHS(operand);
		if (operand.startsWith(USER_ATTRIB)) {
			updatePropertyAttribs(splitComponent, prop, USER_ATTRIB);
		} else if (operand.startsWith(LOC_ATTRIB)) {
			updatePropertyAttribs(splitComponent, prop, LOC_ATTRIB);
		} else if (operand.startsWith(APP_ATTRIB)) {
			updatePropertyAttribs(splitComponent, prop, APP_ATTRIB);
		} else if (operand.startsWith(RES_ATTRIB)) {
			updatePropertyAttribs(splitComponent, prop, RES_ATTRIB);
		} else if (operand.startsWith(ENV_ATTRIB) || operand.startsWith(ENV_TIME_ATTRIB)) {
			updatePropertyAttribs(splitComponent, prop, null);
		}
	}

	private void updatePropertyAttribs(Component splitComponent, PropertyType prop, String prefix) {
		String LHS = splitComponent.getOperand1();
		String RHS = splitComponent.getOperand2();
		String operator = splitComponent.getOperator();
		String attribName = LHS;
		if (prefix != null && prefix.length() < LHS.length()) {
			attribName = LHS.substring(prefix.length());
		}
		prop.setName(PolicyTransformer.escapeUnwantedQuotes(attribName));
		prop.setValue(PolicyTransformer.escapeUnwantedQuotes(RHS));
		prop.setMethod(PolicyTransformer.getCompareMethod(operator));
	}

	private Node write(POLICY policy, Node previousNode, Node currentNode, Operator operator) {
		if (currentNode == null) {
			return null;
		}
		if (currentNode instanceof SinglePolicyNode) {
			SinglePolicyNode node = (SinglePolicyNode) currentNode;
			if (previousNode instanceof SinglePolicyNode && operator != null) {
				SinglePolicyNode prev = (SinglePolicyNode) previousNode;
				if (PolicyComponentHelper.OR.equals(operator)) {
					PolicyComponent policyComponent = node.getPolicyComponent();
					if (policyComponent == null) {
						policyComponent = createPolicyComponent(node);
					}
					PolicyComponent previousPolicyComponent = prev.getPolicyComponent();
					addORConditionToPolicy(policy, policyComponent, previousPolicyComponent);
				} else {
					PolicyComponent policyComponent = createPolicyComponent(node);
					addANDConditionToPolicy(policy, policyComponent);
				}
			} else {
				PolicyComponent policyComponent = createPolicyComponent(node);
				addANDConditionToPolicy(policy, policyComponent);
			}
			return currentNode;
		} else if (currentNode instanceof MultiPolicyNode) {
			MultiPolicyNode multiComponent = (MultiPolicyNode) currentNode;
			List<Node> components = multiComponent.getComponents();
			List<Operator> operators = multiComponent.getOperators();
			previousNode = null;
			for (int i = 0; i < components.size(); i++) {
				Node node = components.get(i);
				if (i == 0) {
					write(policy, previousNode, node, null);
				} else {
					Operator currentOperator = operators.get(i - 1);
					write(policy, previousNode, node, currentOperator);
				}
				previousNode = node;
			}
			return multiComponent;
		} else if (currentNode instanceof UserGroupNode) {
			UserGroupNode userGroupNode = (UserGroupNode) currentNode;
			addUserGroupToPolicy(policy, userGroupNode);
		} else if (currentNode instanceof NegatePolicyNode) {
			NegatePolicyNode component = (NegatePolicyNode) currentNode;
			Node node = component.getNode();
			Node reply = write(policy, null, node, null);
			negateConditionInPolicy(policy, reply);
			return null;
		}
		return null;
	}
}
