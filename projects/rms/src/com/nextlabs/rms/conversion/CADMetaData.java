package com.nextlabs.rms.conversion;

public class CADMetaData {
	
	private String displayName;
	
	private boolean isPrintAllowed;
	
	private boolean isPMIAllowed;
	
	public boolean isPMIAllowed() {
		return isPMIAllowed;
	}

	public void setPMIAllowed(boolean isPropertyWindowAllowed) {
		this.isPMIAllowed = isPropertyWindowAllowed;
	}

	public boolean isPrintAllowed() {
		return isPrintAllowed;
	}

	public void setPrintAllowed(boolean isPrintAllowed) {
		this.isPrintAllowed = isPrintAllowed;
	}

	private WaterMark watermark;
	
	private String errMsg;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public WaterMark getWatermark() {
		return watermark;
	}

	public void setWatermark(WaterMark watermark) {
		this.watermark = watermark;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	
}
