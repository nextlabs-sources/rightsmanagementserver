package com.nextlabs.rms.crypt;

import java.util.Map;

public class NLTagPart {
	
	private int streamSize;
	
	private String streamName;
	
	private String nltMajorVersion;
	
	private String nltMinorVersion;
	
	private int tagsSize;
	
	private Map<String, String> tags;

	public int getStreamSize() {
		return streamSize;
	}

	public void setStreamSize(int streamSize) {
		this.streamSize = streamSize;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public String getNltMajorVersion() {
		return nltMajorVersion;
	}

	public void setNltMajorVersion(String nltMajorVersion) {
		this.nltMajorVersion = nltMajorVersion;
	}

	public String getNltMinorVersion() {
		return nltMinorVersion;
	}

	public void setNltMinorVersion(String nltMinorVersion) {
		this.nltMinorVersion = nltMinorVersion;
	}

	public int getTagsSize() {
		return tagsSize;
	}

	public void setTagsSize(int tagsSize) {
		this.tagsSize = tagsSize;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

}