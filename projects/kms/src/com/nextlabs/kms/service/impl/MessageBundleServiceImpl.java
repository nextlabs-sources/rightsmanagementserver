package com.nextlabs.kms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.nextlabs.kms.service.MessageBundleService;

@Service
public class MessageBundleServiceImpl implements MessageBundleService {
	@Autowired(required = true)
	@Qualifier(value="kms.msg.resource")
	private MessageSource messageSource;

	@Override
	public String getText(String code) {
		String msg = messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
		return msg;
	}

	@Override
	public String getText(String code, String defaultMessage) {
		String msg = messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
		return msg;
	}

	@Override
	public String getText(String code, Object[] args) {
		String msg = messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
		return msg;
	}

	@Override
	public String getText(String code, Object[] args, String defaultMessage) {
		String msg = messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
		return msg;
	}
}
