package com.nextlabs.rms.repository;

import java.io.Serializable;

public class FileCacheId implements Serializable{

	private static final long serialVersionUID = -6012110325597792380L;

	private String sessionId;
	
	private String userName;
	
	private String docId;
	
	public FileCacheId(String sessionId, String userName, String cacheId) {
		this.sessionId=sessionId;
		this.userName=userName;
		this.docId=cacheId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getUserName() {
		return userName;
	}

	public String getDocId() {
		return docId;
	}

	 public boolean equals(Object obj){  
		  if((obj instanceof FileCacheId)){
			  FileCacheId fileCacheId=(FileCacheId)obj;
			  if((docId.equalsIgnoreCase(fileCacheId.getDocId()))&&
					  (sessionId.equalsIgnoreCase(fileCacheId.getSessionId()))&&
					  (userName.equalsIgnoreCase(fileCacheId.getUserName()))){
				  return true;
			  }
		  }
		  return false;
	 }
	 
	 @Override
	 public int hashCode()
	 {
	     return docId.hashCode()+sessionId.hashCode()+userName.hashCode();
	 }

}
