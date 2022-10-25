package com.nextlabs.rms.services.manager.node;

import java.util.ArrayList;
import java.util.List;

import noNamespace.PropertyType;

public class PolicyComponent {
	private final String componentType;
	private final int id;
	private final List<PropertyType> propertyList = new ArrayList<PropertyType>();

	public PolicyComponent(int id, String componentType) {
		this.id = id;
		this.componentType = componentType;
	}

	public void addProperties(List<PropertyType> properties) {
		if (properties != null) {
			getPropertyList().addAll(properties);
		}
	}

	public void addProperty(PropertyType property) {
		getPropertyList().add(property);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof PolicyComponent) {
			PolicyComponent oth = (PolicyComponent) obj;
			return oth.getId() == getId()
					&& (getComponentType() != null ? getComponentType().equals(oth.getComponentType()) : oth.getComponentType() == null);
		}
		return super.equals(obj);
	}

	public String getComponentType() {
		return componentType;
	}

	public int getId() {
		return id;
	}

	public List<PropertyType> getPropertyList() {
		return propertyList;
	}

	@Override
	public int hashCode() {
		int hash = getId();
		hash = hash * 31 + (getComponentType() != null ? getComponentType().hashCode() : 0);
		return super.hashCode();
	}
}
