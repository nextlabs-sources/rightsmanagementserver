package com.nextlabs.rms.eval;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Rights implements Serializable {
	private static final long serialVersionUID = -5538082327571094736L;
	private final Set<String> rights = new HashSet<>();

	public void addRights(String rightsValue) {
		if (rightsValue == null) {
			throw new IllegalArgumentException("Rights is required");
		}
		this.rights.add(rightsValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Rights) {
			Rights oth = (Rights) obj;
			return (getRights() != null ? Arrays.equals(getRights(), oth.getRights()) : oth.getRights() == null);
		}
		return false;
	}

	public String[] getRights() {
		return rights.toArray(new String[rights.size()]);
	}

	@Override
	public int hashCode() {
		int hash = getRights() != null ? Arrays.hashCode(getRights()) : 0;
		return hash;
	}

	public boolean hasRights(String rights) {
		return this.rights.contains(rights);
	}

	@Override
	public String toString() {
		return "Rights{rights: " + getRights() != null ? Arrays.toString(getRights()) : "<empty>" + "}";
	}
}
