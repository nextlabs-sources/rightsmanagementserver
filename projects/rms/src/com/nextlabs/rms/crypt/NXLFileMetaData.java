package com.nextlabs.rms.crypt;

public class NXLFileMetaData {
	
	private String nxlMajorVersion;
	
	private String nxlMinorVersion;
	
	private int headerSize;
	
	private int streamCount;
	
	private String origFileName;

	private NLTagPart nlTagPart;

	public String getNxlMajorVersion() {
		return nxlMajorVersion;
	}

	public void setNxlMajorVersion(String nxlMajorVersion) {
		this.nxlMajorVersion = nxlMajorVersion;
	}

	public String getNxlMinorVersion() {
		return nxlMinorVersion;
	}

	public void setNxlMinorVersion(String nxlMinorVersion) {
		this.nxlMinorVersion = nxlMinorVersion;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}

	public int getStreamCount() {
		return streamCount;
	}

	public void setStreamCount(int streamCount) {
		this.streamCount = streamCount;
	}

	public String getOrigFileName() {
		return origFileName;
	}

	public void setOrigFileName(String origFileName) {
		this.origFileName = origFileName;
	}

	public NLTagPart getNlTagPart() {
		return nlTagPart;
	}

	public void setNlTagPart(NLTagPart nlTagPart) {
		this.nlTagPart = nlTagPart;
	}

}