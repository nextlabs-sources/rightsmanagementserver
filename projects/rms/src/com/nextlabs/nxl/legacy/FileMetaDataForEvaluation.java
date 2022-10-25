package com.nextlabs.nxl.legacy;

import java.util.List;
import java.util.Map;

public class FileMetaDataForEvaluation {
	
	public Map<String,List<String>> attr;
	
	public Map<String, List<String>> getAttr() {
		return attr;
	}

	public void setAttr(Map<String, List<String>> attr) {
		this.attr = attr;
	}

	public Map<String, List<String>> getRights() {
		return rights;
	}

	public void setRights(Map<String, List<String>> rights) {
		this.rights = rights;
	}

	public Map<String, List<String>> getTags() {
		return tags;
	}

	public void setTags(Map<String, List<String>> tags) {
		this.tags = tags;
	}

	public Map<String,List<String>> rights;
	
	public Map<String,List<String>> tags;

}
