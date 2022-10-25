package com.nextlabs.rms.conversion;

public class DocumentContent {

	private int[] pageNum;

	private String[] pageContent;
	
	private String errMsg;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public int[] getPageNum() {
		return pageNum;
	}

	public void setPageNum(int[] pageNum) {
		this.pageNum = pageNum;
	}

	public String[] getPageContent() {
		return pageContent;
	}

	public void setPageContent(String[] pageContent) {
		this.pageContent = pageContent;
	}
	
}