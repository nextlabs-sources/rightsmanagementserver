package com.nextlabs.rms.repository.sharepointrest;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SPRestFilesJsonHelper {
	
	@SerializedName("d")
	public SPResponse spResponse;
	
	public SPResponse getSpResponse() {
		return spResponse;
	}

	public void setSpResponse(SPResponse spResponse) {
		this.spResponse = spResponse;
	}

	public class SPResponse{
		public Files Files;
		public Folders Folders;
		public Files getFiles() {
			return Files;
		}
		public void setFiles(Files files) {
			this.Files = files;
		}
		public Folders getFolders() {
			return Folders;
		}
		public void setFolders(Folders folders) {
			this.Folders = folders;
		}	
	}
	
	public class Files{
		public List<Results> results;

		public List<Results> getResults() {
			return results;
		}
	
		public void setResults(List<Results> results) {
			this.results = results;
		}
	}
	
	public class Folders{
		public List<Results> results;

		public List<Results> getResults() {
			return results;
		}
	
		public void setResults(List<Results> results) {
			this.results = results;
		}
	}
	
	public class Results{
		public String Name;
		public String ServerRelativeUrl;
		public String TimeLastModified;
		public String Length;
		
		public String getLength() {
			return Length;
		}
		public void setLength(String length) {
			Length = length;
		}
		public String getTimeLastModified() {
			return TimeLastModified;
		}
		public void setTimeLastModified(String timeLastModified) {
			TimeLastModified = timeLastModified;
		}
		public String getName() {
			return Name;
		}
		public void setName(String name) {
			Name = name;
		}
		public String getServerRelativeUrl() {
			return ServerRelativeUrl;
		}
		public void setServerRelativeUrl(String serverRelativeUrl) {
			ServerRelativeUrl = serverRelativeUrl;
		}
	}
}
