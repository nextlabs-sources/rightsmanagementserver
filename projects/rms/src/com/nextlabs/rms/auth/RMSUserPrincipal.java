package com.nextlabs.rms.auth;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RMSUserPrincipal implements Serializable {
	private static final long serialVersionUID = -3163251860102551822L;

	public static final String NORMAL_USER = "NORMAL_USER";
	
	public static final String ADMIN_USER = "ADMIN_USER";
	
	public static final String RMS = "RMS";
	
	public static final String AUTH_LDAP = "LDAP";
	
	public static final String AUTH_OKTA = "OKTA";
	
	public static final String AUTH_SAML = "SAML";
	
	private String userName;
	
	private char[] password;
	
	private Map<String,List<String>> userAttributes;
	
	private String uid;
	
	private String role = NORMAL_USER;
	
	private String domain;
	
	private String loginContext= RMS;
	
	private String authProvider = AUTH_LDAP;

	private boolean isRMSUser = false;
	
	private String principalName;
	
	private String tenantId;
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String sid) {
		this.uid = sid;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Map<String,List<String>> getUserAttributes() {
		return userAttributes;
	}

	public void setUserAttributes(Map<String,List<String>> userAttributes) {
		this.userAttributes = userAttributes;
	}

	public boolean isRMSUser() {
		return isRMSUser;
	}

	public void setRMSUser(boolean isRMSUser) {
		this.isRMSUser = isRMSUser;
	}
	
	public String getLoginContext() {
		return loginContext;
	}

	public void setLoginContext(String loginContext) {
		this.loginContext = loginContext;
	}

	public String getAuthProvider() {
	    return authProvider; 
	}
	
	public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }
	
	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}
	
	public String getTenantId(){
		return tenantId;
	}
	
	public void setTenantId(String tenantId){
		this.tenantId = tenantId;
	}
}