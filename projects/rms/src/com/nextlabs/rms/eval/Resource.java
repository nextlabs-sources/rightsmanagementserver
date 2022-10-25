package com.nextlabs.rms.eval;

import java.io.Serializable;

public class Resource implements Serializable, INamedAttribute {
	private static final long serialVersionUID = 502236718517555448L;
	private final String dimensionName;
	private final String resourceName;
	private final String resourceType;
	private final NamedAttributes attributes;

	public Resource(String dimensionName, String resourceName, String resourceType) {
		if (dimensionName == null) {
			throw new IllegalArgumentException("Dimension name is null");
		}
		if (resourceName == null) {
			throw new IllegalArgumentException("Resource name is null");
		}
		if (resourceType == null) {
			throw new IllegalArgumentException("Resource type is null");
		}
		this.resourceName = resourceName;
		this.resourceType = resourceType;
		this.dimensionName = dimensionName;
		this.attributes = new NamedAttributes("resource");
	}

	public void addAttribute(String key, String value) {
		this.attributes.addAttribute(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Resource) {
			Resource oth = (Resource) obj;
			return getResourceName().equals(oth.getResourceName()) && getResourceType().equals(oth.getResourceType())
					&& getAttributes().equals(oth.getAttributes());
		}
		return false;
	}

	public NamedAttributes getAttributes() {
		return attributes;
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getResourceType() {
		return resourceType;
	}
	
	@Override
	public int hashCode() {
		int hash = getResourceName().hashCode();
		hash = 31 * hash + getResourceType().hashCode();
		hash = 31 * hash + getAttributes().hashCode();
		return hash;
	}
}
