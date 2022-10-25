package com.nextlabs.rms.eval;

import java.io.Serializable;

public class User implements Serializable, INamedAttribute {
	private static final long serialVersionUID = -7852064734318809530L;
	private String id;
	private String name;
	private final NamedAttributes attributes = new NamedAttributes("");

	public User(String id) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		this.id = id;
	}

	public User(String id, String name) {
		this(id);
		if (name == null) {
			throw new IllegalArgumentException("ID is null");
		}
		this.name = name;
	}

	public void addAttribute(String key, String value) {
		attributes.addAttribute(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof User) {
			User oth = (User) obj;
			return (getId() != null ? getId().equals(oth.getId()) : oth.getId() == null)
					&& (getName() != null ? getName().equals(oth.getName()) : oth.getName() == null) && (getAttributes().equals(oth.getAttributes()));
		}
		return false;
	}

	public NamedAttributes getAttributes() {
		return attributes;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		int hash = getId() != null ? getId().hashCode() : 0;
		hash = 31 * hash + (getName() != null ? getName().hashCode() : 0);
		hash = 31 * hash + getAttributes().hashCode();
		return hash;
	}
}
