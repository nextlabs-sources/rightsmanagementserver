package com.nextlabs.nxl.pojos;

import com.nextlabs.nxl.interfaces.INxlFile;

public class NewNxlFile implements INxlFile{

	private NXLHeaders headers;

	private byte[] decryptedBytes;
	
		
	private SectionTable sectionTable;
	
	public NXLHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(NXLHeaders headers) {
		this.headers = headers;
	}

	public byte[] getDecryptedBytes() {
		return decryptedBytes;
	}

	public void setDecryptedBytes(byte[] decryptedBytes) {
		this.decryptedBytes = decryptedBytes;
	}

	public SectionTable getSectionTable() {
		return sectionTable;
	}

	public void setSectionTable(SectionTable sectionTable) {
		this.sectionTable = sectionTable;
	}

}
