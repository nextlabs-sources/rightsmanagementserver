/**
 * 
 */
package com.nextlabs.rms.exception;

/**
 * @author nnallagatla
 *
 */
public class ServiceProviderAlreadyExists extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4569692199400047149L;

	/**
	 * 
	 */
	public ServiceProviderAlreadyExists() {
	}

	/**
	 * @param message
	 */
	public ServiceProviderAlreadyExists(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ServiceProviderAlreadyExists(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceProviderAlreadyExists(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ServiceProviderAlreadyExists(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
