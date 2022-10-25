package com.nextlabs.rms.repository.exception;

public class InvalidTokenException extends UnauthorizedRepositoryException {
	
	private static final long serialVersionUID = 4113405042093743090L;
	
	private String repoName = null;

	public InvalidTokenException(String msg) {
		super(msg);
	}

	public InvalidTokenException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public InvalidTokenException(String msg, Throwable e, String repoName){
		super(msg, e);
		this.repoName = repoName;
	}
	
	public String getRepoName(){
		return this.repoName;
	}
	
}
