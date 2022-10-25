package com.nextlabs.kms.service;

public interface MessageBundleService {
	String getText(String code);

	String getText(String code, String defaultMessage);

	String getText(String code, Object[] args);

	String getText(String code, Object[] args, String defaultMessage);
}
