package com.nextlabs.rms.repository;

import java.util.List;

public class SearchResultListObject {
	
	private List<SearchResult> results;

	private String error;
	
	public List<SearchResult> getResults() {
		return results;
	}

	public void setResults(List<SearchResult> results) {
		this.results = results;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
}
