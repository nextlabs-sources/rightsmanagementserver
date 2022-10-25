/**
 * 
 */
package com.nextlabs.rms.exception;

import com.nextlabs.rms.dto.repository.RepositoryDTO;

/**
 * @author nnallagatla
 *
 */
public class DuplicateRepositoryNameException extends Exception {

	private RepositoryDTO repoDTO;
	
	private static final long serialVersionUID = 3620980537687315841L;
	
	/**
	 * 
	 */
	public DuplicateRepositoryNameException(RepositoryDTO dto) {
		setRepoDTO(dto);
	}

	/**
	 * @param message
	 */
	public DuplicateRepositoryNameException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DuplicateRepositoryNameException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateRepositoryNameException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DuplicateRepositoryNameException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @return the repoDTO
	 */
	public RepositoryDTO getRepoDTO() {
		return repoDTO;
	}

	/**
	 * @param repoDTO the repoDTO to set
	 */
	private void setRepoDTO(RepositoryDTO repoDTO) {
		this.repoDTO = repoDTO;
	}

}
