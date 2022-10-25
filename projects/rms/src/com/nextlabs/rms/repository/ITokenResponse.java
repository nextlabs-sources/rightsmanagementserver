package com.nextlabs.rms.repository;

public interface ITokenResponse {
	public String getAccessToken();

	public String getRefreshToken();

	public Long getExpiresInSeconds();
}
