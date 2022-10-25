package com.nextlabs.kms.service.secure;

import java.io.Serializable;

public class Artifact implements Serializable {
	private static final long serialVersionUID = 6953368229735530829L;
	private long timeStamp;
	private Object artifact;

	public Artifact(long timeStamp, Object artifact) {
		this.timeStamp = timeStamp;
		this.artifact = artifact;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Object getKeyManagers() {
		return artifact;
	}

	public void setKeyManagers(Object artifact) {
		this.artifact = artifact;
	}
}
