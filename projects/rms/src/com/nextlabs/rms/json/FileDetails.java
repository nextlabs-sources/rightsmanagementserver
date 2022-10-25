package com.nextlabs.rms.json;

import java.util.List;
import java.util.Map;

public class FileDetails {
	
	private Map<String,List<String>> tagsMap;
	
	private Map<String,String[]> rightsMap ;
	
	public Map<String, List<String>> getTagsMap() {
		return tagsMap;
	}

	public void setTagsMap(Map<String, List<String>> tagsMap) {
		this.tagsMap = tagsMap;
	}

	public Map<String, String[]> getRightsMap() {
		return rightsMap;
	}

	public void setRightsMap(Map<String, String[]> rightsMap) {
		this.rightsMap = rightsMap;
	}

	

}
