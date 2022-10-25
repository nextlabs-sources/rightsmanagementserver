package com.nextlabs.rms.eval;

import java.io.Serializable;

public class Attribute implements Serializable {
	private static final long serialVersionUID = -6058680956050593584L;
	private String name;
	private String value;

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Attribute) {
			Attribute oth = (Attribute) obj;
			return (name != null ? name.equals(oth.getName()) : oth.getName() == null)
					&& (value != null ? value.equals(oth.getValue()) : oth.getValue() == null);
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int hash = name != null ? name.hashCode() : 0;
		hash = 31 * hash + (value != null ? value.hashCode() : 0);
		return hash;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Attribute{name: " + name + ", value: " + value + "}";
	}
}
