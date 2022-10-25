package com.nextlabs.rms.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvalResponse {
	private final List<Obligation> obligations = new ArrayList<>();
	private final Rights rights = new Rights();
	private Map<String, List<String>> tagMap =new HashMap<>(); 

	public void addObligation(Obligation obligation) {
		obligations.add(obligation);
	}

	public void addRights(String rightsValue) {
		this.rights.addRights(rightsValue);
	}

	public Obligation getObligation(String name) {
		for (Obligation obligation : obligations) {
			if (obligation.getName().equals(name)) {
				return obligation;
			}
		}
		return null;
	}

	public List<Obligation> getObligations() {
		return obligations;
	}
	
	public Rights getRights() {
		return rights;
	}
	public void setTagMap(Map<String, List<String>> tagMap) {
		this.tagMap = tagMap;
	}

	public Map<String, List<String>> getTagMap() {
		return tagMap;
	}
}
