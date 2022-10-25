package com.nextlabs.kms.model;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class ProviderTypeAttribute implements Serializable {
	private static final long serialVersionUID = -818559188080785652L;
	private final String name;
	private final String value;

	public ProviderTypeAttribute(String name, String value) {
		Assert.hasText(name, "Name should have length; it must not be null or empty");
		Assert.hasText(value, "Value should have length; it must not be null or empty");
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ProviderTypeAttribute) {
			ProviderTypeAttribute oth = (ProviderTypeAttribute) obj;
			return ObjectUtils.nullSafeEquals(getName(), oth.getName())
					&& ObjectUtils.nullSafeEquals(getValue(), oth.getValue());
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
		int hash = ObjectUtils.nullSafeHashCode(getName());
		hash = hash * 13 + ObjectUtils.nullSafeHashCode(getValue());
		return hash;
	}
}
