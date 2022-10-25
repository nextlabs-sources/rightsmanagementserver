/**
 * 
 */
package com.nextlabs.rms.exception;

/**
 * @author nnallagatla
 *
 */
public class ServiceProviderDuplicateAppNameException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3620980532720315841L;

	/**
	 * 
	 */
	public ServiceProviderDuplicateAppNameException() {
	}

	/**
	 * @param message
	 */
	public ServiceProviderDuplicateAppNameException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ServiceProviderDuplicateAppNameException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceProviderDuplicateAppNameException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ServiceProviderDuplicateAppNameException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
