package com.nextlabs.rms.repository.exception;

public class UnauthorizedRepositoryException extends RepositoryException {
	
	private static final long serialVersionUID = -1195820078603842972L;

	private String repoName = null;
	
	public UnauthorizedRepositoryException(String msg, Throwable e) {
		super(msg, e);
	}

	public UnauthorizedRepositoryException(String msg) {
		super(msg);
	}
	
	public UnauthorizedRepositoryException(String msg, Throwable e, String repoName){
		super(msg, e);
		this.repoName = repoName;
	}
	
	public String getRepoName(){
		return this.repoName;
	}
	
}
