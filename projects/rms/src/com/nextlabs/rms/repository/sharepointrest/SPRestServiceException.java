package com.nextlabs.rms.repository.sharepointrest;

public class SPRestServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int statusCode;
	private final SPRestErrorResponse error;
	private String msg;
	public SPRestServiceException(int statusCode, String msg, SPRestErrorResponse error){
		super(msg);
		this.error = error;
		this.statusCode = statusCode;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public SPRestErrorResponse getError() {
		return error;
	}
	
}
