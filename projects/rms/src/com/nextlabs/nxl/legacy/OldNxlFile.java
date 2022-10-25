package com.nextlabs.nxl.legacy;
import com.nextlabs.nxl.interfaces.*;
public class OldNxlFile implements INxlFile {
	
	private OldNXLFileMetaData metaData;

	private NXLFilePart nxlFilePart;
	
	public OldNXLFileMetaData getMetaData(){
		return metaData;			
	}
	
	public void setMetaData(OldNXLFileMetaData metaData){
		this.metaData = metaData;
	}
	
	public NXLFilePart getNxlFilePart() {
		return nxlFilePart;
	}

	public void setNxlFilePart(NXLFilePart nxlFilePart) {
		this.nxlFilePart = nxlFilePart;
	}

}