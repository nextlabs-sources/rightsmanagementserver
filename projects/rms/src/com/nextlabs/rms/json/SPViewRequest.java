package com.nextlabs.rms.json;

import java.util.List;
import java.util.Map;

public class SPViewRequest {

	private String token;
	
	private String siteName;
	
	private String path;
	
	private String uid;
	
	private String userName;
	
	private Map<String,List<String>> userAttributes;

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Map<String,List<String>> getUserAttributes() {
		return userAttributes;
	}

	public void setUserAttributes(Map<String,List<String>> userAttributes) {
		this.userAttributes = userAttributes;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}
