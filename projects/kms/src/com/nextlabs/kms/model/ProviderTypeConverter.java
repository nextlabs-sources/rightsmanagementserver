package com.nextlabs.kms.model;

import org.springframework.util.ObjectUtils;

import com.nextlabs.kms.entity.enums.ProviderType;
import com.nextlabs.kms.types.Provider;

public final class ProviderTypeConverter {
	public static Provider toProvider(ProviderType providerType) {
		Provider[] values = Provider.values();
		String name = providerType.name();
		for (Provider type : values) {
			if (ObjectUtils.nullSafeEquals(name, type.name())) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown provider: " + providerType);
	}

	public static ProviderType toProviderType(Provider provider) {
		ProviderType[] values = ProviderType.values();
		String name = provider.name();
		for (ProviderType type : values) {
			if (ObjectUtils.nullSafeEquals(name, type.name())) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown provider type: " + name);
	}
}
