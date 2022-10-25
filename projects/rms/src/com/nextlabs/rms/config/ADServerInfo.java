package com.nextlabs.rms.config;

public class ADServerInfo {

	private String rmsAdmin;
	
	// TC AD query
	private String queryDN;
	
	// TC AD query
	private String queryDNPassword;
	
	private String ldapHost;
	
	private String searchBase;
	
	private String userSearchQuery;
	
	private String domain;
	
	private String trustStore;
	
	private String trustStorePassword;
	
	private boolean ldapSSL;
	
	private String rmsGroup;
	
	private String ldapType;
	
	private String uniqueId;
	
	private boolean securityPrincipalUseUserID;
	
	public String getLdapHost() {
		return ldapHost;
	}
	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}
	public String getSearchBase() {
		return searchBase;
	}
	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}
	public String getUserSearchQuery() {
		return userSearchQuery;
	}
	public void setUserSearchQuery(String userSearchQuery) {
		this.userSearchQuery = userSearchQuery;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getRmsAdmin() {
		return rmsAdmin;
	}
	public void setRmsAdmin(String rmsAdmin) {
		this.rmsAdmin = rmsAdmin;
	}
	public String getRmsGroup() {
		return rmsGroup;
	}
	public void setRmsGroup(String rmsGroup) {
		this.rmsGroup = rmsGroup;
	}
	
	// TC AD query
	public String getQueryDN() {
		return queryDN;
	}
	
	// TC AD query
	public void setQueryDN(String queryDN) {
		this.queryDN = queryDN;
	}
	
	// TC AD query
	public String getQueryDNPassword() {
		return queryDNPassword;
	}
	
	// TC AD query
	public void setQueryDNPassword(String queryDNPassword) {
		this.queryDNPassword = queryDNPassword;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public boolean isSecurityPrincipalUseUserID() {
		return securityPrincipalUseUserID;
	}
	public void setSecurityPrincipalUseUserID(boolean securityPrincipalUseUserID) {
		this.securityPrincipalUseUserID = securityPrincipalUseUserID;
	}
	public String getLdapType() {
		return ldapType;
	}
	public void setLdapType(String ldapType) {
		this.ldapType = ldapType;
	}
	public String getTrustStore() {
		return trustStore;
	}
	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}
	public String getTrustStorePassword() {
		return trustStorePassword;
	}
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
	public boolean isLdapSSL() {
		return ldapSSL;
	}
	public void setLdapSSL(boolean ldapSSL) {
		this.ldapSSL = ldapSSL;
	}
	
	

}
