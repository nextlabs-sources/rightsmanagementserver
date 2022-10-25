package com.nextlabs.rms.services.manager.node;

import java.util.ArrayList;
import java.util.List;

import noNamespace.PropertyType;

import com.nextlabs.rms.services.manager.PolicyComponentHelper;

public class SinglePolicyNode implements Node {
	private final String componentType;
	private final List<PropertyType> propertyList = new ArrayList<PropertyType>();
	private PolicyComponent policyComponent;

	public SinglePolicyNode(String componentType) {
		this.componentType = componentType;
	}

	public void addProperties(List<PropertyType> propertyTypes) {
		if (propertyTypes != null && !propertyTypes.isEmpty()) {
			getPropertyList().addAll(propertyTypes);
		}
	}

	public void addProperty(PropertyType property) {
		this.propertyList.add(property);
	}

	public SinglePolicyNode copy() {
		SinglePolicyNode node = new SinglePolicyNode(getComponentType());
		node.setPolicyComponent(getPolicyComponent());
		node.addProperties(getPropertyList());
		return node;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof SinglePolicyNode) {
			SinglePolicyNode oth = (SinglePolicyNode) obj;
			return getComponentType() != null ? getComponentType().equals(oth.getComponentType()) : oth.getComponentType() == null
					&& (getPropertyList() != null ? getPropertyList().equals(oth.getPropertyList()) : oth.getPropertyList() == null);
		}
		return false;
	}

	public String getComponentType() {
		return componentType;
	}

	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public List<PropertyType> getPropertyList() {
		return propertyList;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash = hash * 31 + (getComponentType() != null ? getComponentType().hashCode() : 0);
		return super.hashCode();
	}

	public Node negate() {
		Node newNode = null;
		if (getPropertyList().size() <= 1) {
			SinglePolicyNode node = new SinglePolicyNode(getComponentType());
			List<PropertyType> list = new ArrayList<>(getPropertyList());
			for (PropertyType type : list) {
				String operator = type.getMethod();
				type.setMethod(PolicyComponentHelper.negateRelationalOperator(operator));
			}
			node.addProperties(list);
			newNode = node;
		} else {
			MultiPolicyNode multiNode = new MultiPolicyNode();
			List<PropertyType> list = new ArrayList<>(getPropertyList());
			int idx = 0;
			for (PropertyType type : list) {
				String operator = type.getMethod();
				type.setMethod(PolicyComponentHelper.negateRelationalOperator(operator));
				SinglePolicyNode node = new SinglePolicyNode(getComponentType());
				node.addProperty(type);
				if (idx == 0) {
					multiNode.addRoot(node);
				} else {
					multiNode.addNode(node, PolicyComponentHelper.OR);
				}
				++idx;
			}
			newNode = multiNode;
		}
		return newNode;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	@Override
	public String toString() {
		return "Single {componentType: " + componentType + ", propertyList: " + propertyList + "}";
	}
}