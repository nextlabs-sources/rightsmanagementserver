package com.nextlabs.kms.controller.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.nextlabs.kms.entity.enums.ErrorCode;
import com.nextlabs.kms.exception.*;
import com.nextlabs.kms.service.MessageBundleService;
import com.nextlabs.kms.types.Error;

public class ServiceExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ServiceExceptionHandler.class);
	@Autowired
	private MessageBundleService bundle;
	
	public com.nextlabs.kms.types.Error handleException(final Exception ex) {
		ErrorCode errorCode = ErrorCode.UNKNOWN;
		if (ex instanceof BadRequestException){
			errorCode = ErrorCode.BAD_REQUEST;
		} else if (ex instanceof AccessDeniedException){
			errorCode = ErrorCode.ACCESS_DENIED;
		} else if (ex instanceof KeyNotFoundException){
			errorCode = ErrorCode.KEY_NOT_FOUND;
		} else if (ex instanceof KeyAlreadyExistsException){
			errorCode = ErrorCode.KEY_ALREADY_EXISTS;
		} else if (ex instanceof KeyRingAlreadyExistsException){
			errorCode = ErrorCode.KEY_RING_ALREADY_EXISTS;
		} else if (ex instanceof KeyRingNotFoundException){
			errorCode = ErrorCode.KEY_RING_NOT_FOUND;
		} else if (ex instanceof KeyRingDisabledException){
			errorCode = ErrorCode.KEY_RING_DISABLED;
		} else if (ex instanceof TenantNotFoundException){
			errorCode = ErrorCode.TENANT_NOT_FOUND;
		} else if (ex instanceof TenantAlreadyExistsException){
			errorCode = ErrorCode.TENANT_ALREADY_EXISTS;
		} else if (ex instanceof KeyAlgorithmUnsupportedException){
			errorCode = ErrorCode.KEY_ALGORITHM_NOT_SUPPORTED;
		}
		logger.error(ex.getMessage(), ex);
		return prepareError(errorCode);
	}

	private Error prepareError(ErrorCode errorCode) {
		Error error = new Error();
		error.setCode(errorCode.getStatusCode());
		error.setDescription(bundle.getText(errorCode.getCode()));
		return error;
	}
}
