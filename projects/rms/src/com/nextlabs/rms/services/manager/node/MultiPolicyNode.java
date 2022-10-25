package com.nextlabs.rms.services.manager.node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import noNamespace.PropertyType;

import com.fathzer.soft.javaluator.Operator;
import com.nextlabs.rms.services.manager.PolicyComponentHelper;

public class MultiPolicyNode implements Node {
	private List<Node> components = new LinkedList<Node>();
	private List<Operator> operators = new LinkedList<Operator>();

	public void addNode(Node component, Operator operator) {
		if (!components.isEmpty()) {
			if (operator == null) {
				throw new IllegalArgumentException();
			}
			operators.add(operator);
		}
		components.add(component);
	}

	public void addRoot(Node component) {
		addNode(component, null);
	}

	public List<Node> getComponents() {
		return components;
	}

	public List<Operator> getOperators() {
		return operators;
	}

	public Node negate() {
		MultiPolicyNode result = new MultiPolicyNode();
		for (int i = 0; i < components.size(); i++) {
			Node node = components.get(i);
			Node negateNode = null;
			if (node instanceof MultiPolicyNode) {
				MultiPolicyNode component = (MultiPolicyNode) node;
				if (allowConvertToSingleNode(component)) {
					negateNode = convertToSingleNode(component);
				} else {
					negateNode = node.negate();
				}
			} else {
				negateNode = node.negate();
			}
			if (i == 0) {
				result.addRoot(negateNode);
			} else {
				Operator operator = operators.get(i - 1);
				Operator negateOperator = PolicyComponentHelper.negateLogicalOperator(operator);
				result.addNode(negateNode, negateOperator);
			}
		}
		return result;
	}

	private Node convertToSingleNode(MultiPolicyNode component) {
		List<Node> components = component.getComponents();
		SinglePolicyNode root = (SinglePolicyNode) components.get(0);
		SinglePolicyNode result = new SinglePolicyNode(root.getComponentType());
		for (int i = 0; i < components.size(); i++) {
			SinglePolicyNode node = (SinglePolicyNode) components.get(i);
			List<PropertyType> propertyList = node.getPropertyList();
			if (propertyList.size() > 0) {
				PropertyType propertyType = propertyList.get(0);
				String method = propertyType.getMethod();
				propertyType.setMethod(PolicyComponentHelper.negateRelationalOperator(method));
				result.addProperty(propertyType);
			}
		}
		return result;
	}

	private boolean allowConvertToSingleNode(MultiPolicyNode component) {
		List<Node> components = component.getComponents();
		List<Operator> operators = component.getOperators();
		Set<String> componentTypes = new HashSet<>(components.size());
		for (Node node : components) {
			if (!(node instanceof SinglePolicyNode)) {
				return false;
			}
			SinglePolicyNode singlePolicyNode = (SinglePolicyNode) node;
			if (singlePolicyNode.getPropertyList().size() > 1) {
				return false;
			}
			componentTypes.add(singlePolicyNode.getComponentType());
		}
		if (componentTypes.size() > 1) {
			return false;
		}
		for (Operator operator : operators) {
			if (!PolicyComponentHelper.OR.equals(operator)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Multi {components: " + components + ", operators: " + operators + "}";
	}
}
