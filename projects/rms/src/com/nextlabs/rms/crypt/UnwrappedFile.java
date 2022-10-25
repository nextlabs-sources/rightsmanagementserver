package com.nextlabs.rms.crypt;

public class UnwrappedFile {
	
	private NXLFileMetaData metaData = new NXLFileMetaData();

	private NXLFilePart nxlFilePart;
	
	public NXLFileMetaData getMetaData(){
		return metaData;			
	}
	
	public void setMetaData(NXLFileMetaData metaData){
		this.metaData = metaData;
	}
	
	public NXLFilePart getNxlFilePart() {
		return nxlFilePart;
	}

	public void setNxlFilePart(NXLFilePart nxlFilePart) {
		this.nxlFilePart = nxlFilePart;
	}

}