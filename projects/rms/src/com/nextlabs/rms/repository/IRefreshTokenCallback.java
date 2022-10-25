package com.nextlabs.rms.repository;

import com.nextlabs.rms.repository.exception.RepositoryException;

public interface IRefreshTokenCallback {
	public ITokenResponse execute() throws RepositoryException;
}
