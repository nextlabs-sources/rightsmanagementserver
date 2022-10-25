package com.nextlabs.rms.repository.sharepointrest;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SPRestList {
	
	@SerializedName("d")
	public SPResponse spResponse;
	
	public SPResponse getSpResponse() {
		return spResponse;
	}

	public void setSpResponse(SPResponse spResponse) {
		this.spResponse = spResponse;
	}

	public class SPResponse{
		public List<Results> results;

		public List<Results> getResults() {
			return results;
		}
	
		public void setResults(List<Results> results) {
			this.results = results;
		}
	}
	
	public class Results{
		public String Title;
		
		public String LastItemModifiedDate;

		public String getLastItemModifiedDate() {
			return LastItemModifiedDate;
		}

		public void setLastItemModifiedDate(String lastItemModifiedDate) {
			LastItemModifiedDate = lastItemModifiedDate;
		}

		public String getTitle() {
			return Title;
		}
	
		public void setTitle(String title) {
			Title = title;
		}		
	}
}
