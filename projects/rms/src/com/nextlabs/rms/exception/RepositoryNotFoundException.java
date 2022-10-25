/**
 * 
 */
package com.nextlabs.rms.exception;

/**
 * @author nnallagatla
 *
 */
public class RepositoryNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1027109578392165137L;

	/**
	 * 
	 */

	public long getRepoId() {
		return repoId;
	}


	public void setRepoId(long repoId) {
		this.repoId = repoId;
	}

	long repoId;
	
	/**
	 * 
	 */
	public RepositoryNotFoundException() {
	}

	
	public RepositoryNotFoundException(long repoId) {
		this.repoId = repoId;
	}
	
	/**
	 * @param message
	 */
	public RepositoryNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RepositoryNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RepositoryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public RepositoryNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
