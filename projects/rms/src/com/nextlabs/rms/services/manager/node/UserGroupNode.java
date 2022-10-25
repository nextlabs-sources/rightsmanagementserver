package com.nextlabs.rms.services.manager.node;

public class UserGroupNode implements Node {
	private final String value;

	public UserGroupNode(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof UserGroupNode) {
			UserGroupNode oth = (UserGroupNode) obj;
			return getValue() != null ? getValue().equals(oth.getValue()) : oth.getValue() == null;
		}
		return false;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int hash = getValue() != null ? getValue().hashCode() : 0;
		return hash;
	}

	@Override
	public String toString() {
		return "UG {value: " + getValue() + "}";
	}

	@Override
	public Node negate() {
		return this;
	}
}
