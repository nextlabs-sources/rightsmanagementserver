/**
 * 
 */
package com.nextlabs.rms.exception;

/**
 * @author nnallagatla
 *
 */
public class RepositoryAuthorizationNotFound extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4802893642983253038L;

	/**
	 * 
	 */
	public RepositoryAuthorizationNotFound() {
	}

	/**
	 * @param message
	 */
	public RepositoryAuthorizationNotFound(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RepositoryAuthorizationNotFound(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RepositoryAuthorizationNotFound(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public RepositoryAuthorizationNotFound(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
