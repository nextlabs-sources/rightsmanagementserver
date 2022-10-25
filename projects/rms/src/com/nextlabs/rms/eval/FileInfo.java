package com.nextlabs.rms.eval;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class FileInfo implements Serializable{

	private static final long serialVersionUID = -2886586223246820093L;
	
	private Map<String, List<String>> tagMap;

	public Map<String, List<String>> getTagMap() {
		return tagMap;
	}

	public void setTagMap(Map<String, List<String>> tagMap) {
		this.tagMap = tagMap;
	}

}
