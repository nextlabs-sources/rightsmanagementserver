/**
 * 
 */
package com.nextlabs.rms.dto.repository;

/**
 * @author nnallagatla
 *
 */
public class AuthorizedRepoUserDTO {
    public AuthorizedRepoUserDTO() {
    }
    
    private long repoId;
    
    private String refreshToken;
    
    private String accountId;
    
    private String accountName;
    
    private String userId;
    
    private String tenantId;
    
    public long getRepoId() {
        return repoId;
    }

    public void setRepoId(long repoId) {
        this.repoId = repoId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String userAccountId) {
        this.accountId = userAccountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String userAccountName) {
        this.accountName = userAccountName;
    }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

}