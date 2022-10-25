package com.nextlabs.rms.auth;

public class RMSLoginException extends Exception {

	private static final long serialVersionUID = -730947774244770454L;
	
	public RMSLoginException(String errMsg){
		super(errMsg);
	}

}
