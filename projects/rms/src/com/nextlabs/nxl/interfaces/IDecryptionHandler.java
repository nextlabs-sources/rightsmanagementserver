package com.nextlabs.nxl.interfaces;

public interface IDecryptionHandler {
	public INxlFile parseContent() throws Exception;
	public INxlFileMetaData readMeta() throws Exception;
	public void close();
}
