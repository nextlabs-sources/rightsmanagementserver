package com.nextlabs.rms.services.resource;


import noNamespace.LoginServiceDocument;

public class LoginException extends Exception {


	private static final long serialVersionUID = -4559245731859564395L;

	private LoginServiceDocument response;
	
	public LoginException(LoginServiceDocument resp){
		response=resp;	
	}

	public LoginServiceDocument getResponse() {
		return response;
	}

}
