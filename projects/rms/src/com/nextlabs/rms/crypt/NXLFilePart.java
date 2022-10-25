package com.nextlabs.rms.crypt;

public class NXLFilePart {
	
	private int streamSize;
	
	private String streamName;
	
	private String nxlMajorVersion;
	
	private String nxlMinorVersion;
	
	private String keyRingName;
	
	private byte[] keyID;
	
	private int fileSize;
	
	private String encryptedKey;
	
	private int flags;
	
	private int paddingLength;
	
	private byte[] paddingData;
	
	private byte[] decryptedFileContent;

	public int getStreamSize() {
		return streamSize;
	}

	public void setStreamSize(int streamSize) {
		this.streamSize = streamSize;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

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

	public String getKeyRingName() {
		return keyRingName;
	}

	public void setKeyRingName(String keyRingName) {
		this.keyRingName = keyRingName;
	}

	public byte[] getKeyID() {
		return keyID;
	}

	public void setKeyID(byte[] keyID) {
		this.keyID = keyID;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public String getEncryptedKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(String encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getPaddingLength() {
		return paddingLength;
	}

	public void setPaddingLength(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	public byte[] getPaddingData() {
		return paddingData;
	}

	public void setPaddingData(byte[] paddingData) {
		this.paddingData = paddingData;
	}

	public byte[] getDecryptedFileContent() {
		return decryptedFileContent;
	}

	public void setDecryptedFileContent(byte[] decryptedFileContent) {
		this.decryptedFileContent = decryptedFileContent;
	}

}