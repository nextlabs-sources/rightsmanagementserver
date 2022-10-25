package com.nextlabs.rms.repository.sharepointrest;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SPRestSearchJsonHelper {

	@SerializedName("d")
	public SPResponse spResponse;
	
	public SPResponse getSpResponse() {
		return spResponse;
	}

	public void setSpResponse(SPResponse spResponse) {
		this.spResponse = spResponse;
	}

	public class SPResponse{
		public Query query;

		public Query getQuery() {
			return query;
		}
	
		public void setQuery(Query query) {
			this.query = query;
		}
	}
	
	public class Query{
		public PrimaryQueryResult PrimaryQueryResult;

		public PrimaryQueryResult getPrimaryQueryResult() {
			return PrimaryQueryResult;
		}
	
		public void setPrimaryQueryResult(PrimaryQueryResult primaryQueryResult) {
			this.PrimaryQueryResult = primaryQueryResult;
		}
	}

	public class PrimaryQueryResult{
		public RelevantResults RelevantResults;

		public RelevantResults getRelevantResults() {
			return RelevantResults;
		}
	
		public void setRelevantResults(RelevantResults relevantResults) {
			this.RelevantResults = relevantResults;
		}
	}
	
	public class RelevantResults{
		public Table Table;

		public Table getTable() {
			return Table;
		}
	
		public void setTable(Table table) {
			this.Table = table;
		}
	}	
	
	public class Table{
		public Rows Rows;

		public Rows getRows() {
			return Rows;
		}
	
		public void setRows(Rows rows) {
			this.Rows = rows;
		}
			
	}
	
	public class Rows{
		public List<Results> results;

		public List<Results> getResults() {
			return results;
		}
	
		public void setResults(List<Results> results) {
			this.results = results;
		}
	}
	
	public class Results{
		public Cells Cells;
		public String Key;
		public String Value;
		
		public String getValue() {
			return Value;
		}
	
		public void setValue(String value) {
			Value = value;
		}
	
		public String getKey() {
			return Key;
		}
	
		public void setKey(String key) {
			Key = key;
		}	

		public Cells getCells() {
			return Cells;
		}
	
		public void setCells(Cells cells) {
			this.Cells = cells;
		}
	}
	
	public class Cells{
		public List<Results> results;

		public List<Results> getResults() {
			return results;
		}
	
		public void setResults(List<Results> results) {
			this.results = results;
		}
	}
}
