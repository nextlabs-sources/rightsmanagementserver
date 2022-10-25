/**
 * 
 */
package com.nextlabs.rms.exception;

/**
 * @author nnallagatla
 *
 */
public class UserNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2278993119841935524L;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	long userId;
	
	/**
	 * 
	 */
	public UserNotFoundException() {
		
	}

	public UserNotFoundException(long userId) {
		this.userId = userId;
	}
	
	/**
	 * @param message
	 */
	public UserNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UserNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UserNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
