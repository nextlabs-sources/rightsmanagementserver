package com.nextlabs.nxl.legacy;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.nextlabs.client.keyservice.KeyServiceSDKException;
import com.nextlabs.keymanagement.KeyRetrievalManager;
import com.nextlabs.kms.types.KeyDTO;
import com.nextlabs.nxl.crypt.AESEncryptionUtil;
import com.nextlabs.nxl.interfaces.IDecryptionHandler;
import com.nextlabs.nxl.interfaces.INxlFile;
import com.nextlabs.nxl.interfaces.INxlFileMetaData;
import com.nextlabs.nxl.util.DecryptionUtil;

public class OldNxlDecryptionHandler implements IDecryptionHandler{

	private static Logger logger = Logger.getLogger(OldNxlDecryptionHandler.class);
	//private static Logger logger = Logger.getLogger(UnwrapHelper.class);

	private OldNXLFileMetaData metaData = null;
	
	private RandomAccessFile file = null;
	
	private OldNxlFile unwrappedFile = null;


	public OldNxlDecryptionHandler(RandomAccessFile file){
		this.file=file;
		unwrappedFile = new OldNxlFile();
	}
	public INxlFileMetaData readMeta()
			throws Exception {
		metaData =  new OldNXLFileMetaData();
		long startTime = System.currentTimeMillis();
		////logger.debug("Parsing file - " + inputFile.getAbsolutePath()+"....");
//		String majVer = readUnsignedChar(file, 0, 1);
//		String minVer = readUnsignedChar(file, 1, 1);
//		metaData.setNxlMajorVersion(majVer);
//		metaData.setNxlMinorVersion(minVer);
//		////logger.debug("NXL FileType Major Version(offset 0, 1 byte):::"+ majVer); 
//		//logger.debug("NXL FileType Minor Version(offset 1, 1 byte):::"+ minVer);
//		int headerSize = readInt(file, 4, 4);
//		metaData.setHeaderSize(headerSize);
//		////logger.debug("Header Size(offset 4, 4 bytes):::" + headerSize);
//		int streamCount = readInt(file, 8, 4);
//		metaData.setStreamCount(streamCount);
//		//logger.debug("Stream Count(offset 8, 4 bytes):::" + streamCount);
		String origFileName = DecryptionUtil.readWCharStr(file, 512, 512);
		((OldNXLFileMetaData) metaData).setOrigFileName(origFileName);
		//logger.debug("Original FileName(offset 512, 512 bytes):::"+origFileName);
		unwrappedFile.setMetaData((OldNXLFileMetaData) metaData);
		//logger.debug("Time taken for parsing metadata from NXL file:"+(System.currentTimeMillis()-startTime) + " ms");
		readTags();
		return metaData;
	}


	private void readTags()throws Exception {
		long startTime = System.currentTimeMillis();
		NLTagPart nltagPart = new NLTagPart();
		unwrappedFile.getMetaData().setNlTagPart(nltagPart);
//		int streamSize = readInt(file, 1536, 4);
//		nltagPart.setStreamSize(streamSize);
//		//logger.debug("2nd streamSize(offset 1536, 4 bytes):::"
//				+ streamSize);
//		String streamName = readUnsignedChar(file, 1540, 8);
//		nltagPart.setStreamName(streamName);
//		//logger.debug("2nd streamName(offset 1540, 8 bytes):::"
//				+ streamName);
//		String nltagMajVer = readUnsignedChar(file, 1548, 1);
//		nltagPart.setNltMajorVersion(nltagMajVer);
//		//logger.debug("Major version for Tag format(offset 1548, 1 byte):::"
//						+ nltagMajVer);
//		String nltagMinVer = readUnsignedChar(file, 1549, 1);
//		nltagPart.setNltMinorVersion(nltagMinVer);
//		//logger.debug("Minor version for Tag format(offset 1549, 1 byte):::"
//						+ nltagMinVer);
		int sizeOfTags = DecryptionUtil.readInt(file, 1552, 4);
		nltagPart.setTagsSize(sizeOfTags);
		//logger.debug("Size of tags:" + sizeOfTags);
		Map<String, String> tagMap = DecryptionUtil.readWCharMap(file, 1572, 4060);
		nltagPart.setTags(tagMap);
		/*if(logger.isDebugEnabled()){
			logger.debug("Tags in file:::");
			for (String key : tagMap.keySet()) {
				logger.debug(key + "=" + tagMap.get(key));
			}			
		}*/
		//logger.debug("Time taken for reading Tags:"+(System.currentTimeMillis() - startTime) + " ms");
	}

	private byte[] getDecryptedContent(byte[] decryptedAESKeyArr,
			byte[] fileContentArr, int iterationCount, int blockSize,
			int paddingLength, byte[] paddingData) {
		byte[] encryptedContent = new byte[blockSize];
		// read 512 bytes of data from File Content Array
		System.arraycopy(fileContentArr, iterationCount * 512,
				encryptedContent, 0, blockSize);
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(0, iterationCount * 512);
		byte[] blockArray = bb.array();
		byte[] ivArray = new byte[16];
		System.arraycopy(blockArray, 0, ivArray, 0, 8);
		// Decrypt the contents
		byte[] dePaddedFileArr = decryptContent(decryptedAESKeyArr,
				paddingLength, paddingData, encryptedContent, ivArray);
		return dePaddedFileArr;
	}

	private byte[] decryptContent(byte[] decryptedAESKeyArr,
			int aesPaddingLength, byte[] aesPaddingData, byte[] fileContentArr,
			byte[] ivArray) {
		byte[] paddedFileContent = fileContentArr;
		if (aesPaddingLength > 0) {
			ArrayList<Byte> padBytes = new ArrayList<Byte>();
			for (byte b : aesPaddingData) {
				if (b != 0) {
					padBytes.add(b);
				}
			}
			byte[] paddingData = new byte[padBytes.size()];
			int i = 0;
			for (byte b : padBytes) {
				paddingData[i] = b;
				i++;
			}
			paddedFileContent = new byte[fileContentArr.length
					+ aesPaddingLength];
			System.arraycopy(fileContentArr, 0, paddedFileContent, 0,
					fileContentArr.length);
			System.arraycopy(paddingData, 0, paddedFileContent,
					fileContentArr.length, paddingData.length);
		}
		byte[] decryptedFileArr = AESEncryptionUtil.getProcessedContent(paddedFileContent,
				decryptedAESKeyArr, ivArray, Cipher.DECRYPT_MODE);
		if (aesPaddingLength > 0) {
			byte[] dePaddedFileArr = removePaddingData(decryptedFileArr,
					aesPaddingLength);
			return dePaddedFileArr;
		} else {
			return decryptedFileArr;
		}
	}

	private byte[] removePaddingData(byte[] decryptedFileArr,
			int aesPaddingLength) {
		byte[] dePaddedData = new byte[decryptedFileArr.length
				- aesPaddingLength];
		System.arraycopy(decryptedFileArr, 0, dePaddedData, 0,
				decryptedFileArr.length - aesPaddingLength);
		return dePaddedData;
	}

	private byte[] getKey(RandomAccessFile file, String keyRingName,
			NXLFilePart nxlFilePart) throws Exception {
		long startTime = System.currentTimeMillis();
		byte[] password = new byte[] { 7, -117, 34, -79, -74, 85, -10, -63,
				-99, -120, 103, 15, -48, -46, -8, -88 };
		byte[] keyId=DecryptionUtil.readBytes(file, 1056, 32);
		//logger.debug("Key ID:::" + new String(keyId));
		nxlFilePart.setKeyID(keyId);
		long timeStamp = DecryptionUtil.readInt(file, 1088, 4);
		//logger.debug("TimeStamp of key:::" + timeStamp);
		KeyDTO k;
		try {
				k = KeyRetrievalManager.getInstance().getKey(password, keyRingName, keyId, timeStamp*1000);
		} catch (KeyServiceSDKException e) {
			logger.error("Unable to get key:::" + e.getMessage(), e);
			return null;
		}
//		String pcKey = toHex(k.getEncoded());
//		logger.debug("---------------------------------");
		logger.debug("KeyRetrievalManager invocation completed");
//		logger.debug("Key ID:::" + toHex(k.getKeyId().getId()));
//		logger.debug("Key:::" + pcKey);
		Date dt = new Date();		
		dt.setTime(k.getKeyId().getTimestamp());
		SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
//		logger.debug("Timestamp of key:::" + date_format.format(dt));
//		logger.debug("---------------------------------");
		logger.debug("Time taken for getting key from Policy Controller:"
				+ (System.currentTimeMillis() - startTime) + " ms");
		return k.getKeyValue();
	}
	
	public void close(){
		if(file!=null){
			try {
				file.close();
			} catch (IOException e) {
				//logger.error("Error occurred while closing file");
			}
		}
	}

	@Override
	public INxlFile parseContent() throws Exception {
		try {
			long startTime = System.currentTimeMillis();;
			// NXL Stream Metadata
			NXLFilePart nxlFilePart = new NXLFilePart();
			unwrappedFile.setNxlFilePart(nxlFilePart);
//			int streamSize = readInt(file, 1024, 4);
//			nxlFilePart.setStreamSize(streamSize);
//			logger.debug("streamSize(offset 1024, 4 bytes):::" + streamSize);
//			String streamName = readUnsignedChar(file, 1028, 8);
//			nxlFilePart.setStreamName(streamName);
//			logger.debug("streamName(offset 1028, 8 bytes):::" + streamName);
//			String nxlMajVer = readUnsignedChar(file, 1036, 1);
//			nxlFilePart.setNxlMajorVersion(nxlMajVer);
//			logger.debug("NLE Major Version(offset 1036, 1 byte):::"
//					+ nxlMajVer);
//			String nxlMinVer = readUnsignedChar(file, 1037, 1);
//			nxlFilePart.setNxlMinorVersion(nxlMinVer);
//			logger.debug("NLE Minor Version(offset 1037, 1 byte):::"
//					+ nxlMinVer);
			String keyRingName = DecryptionUtil.readUnsignedChar(file, 1040, 16);
			nxlFilePart.setKeyRingName(keyRingName);
			//logger.debug("keyRingName:::" + keyRingName);
			byte[] pcKey = getKey(file, keyRingName, nxlFilePart);
			int fileLength = DecryptionUtil.readInt(file, 1096, 8);
			nxlFilePart.setFileSize(fileLength);
			//logger.debug("fileLength:::" + fileLength);
			String aesKey = DecryptionUtil.readUnsignedChar(file, 1104, 16);
			nxlFilePart.setEncryptedKey(aesKey);
//			logger.debug("Encrypted Key from NXL File(offset 1104, 16 bytes):::"
//							+ aesKey);
			// Decrypt the NXL file
			byte[] aesKeyArr = DecryptionUtil.readBytes(file, 1104, 16);
			byte[] decryptedAESKeyArr = AESEncryptionUtil.getProcessedContent(aesKeyArr, pcKey,
					new byte[16], Cipher.DECRYPT_MODE);
//			String decryptedAESKey = toHex(decryptedAESKeyArr);
//			logger.debug("---------------------------------");
//			logger.debug("decryptedAESKey:" + decryptedAESKey);
//			logger.debug("---------------------------------");
//			int flags = readInt(file, 1120, 8);
//			nxlFilePart.setFlags(flags);
//			logger.debug("flags(offset 1120, 8 bytes):::" + flags);
			int aesPaddingLength = DecryptionUtil.readInt(file, 1136, 4);
			nxlFilePart.setPaddingLength(aesPaddingLength);
//			logger.debug("Padding Length:::" + aesPaddingLength);
			byte[] aesPaddingData = DecryptionUtil.readBytes(file, 1140, 16);
			nxlFilePart.setPaddingData(aesPaddingData);
//			logger.debug("Padding Data(offset 1140, 16 bytes):::"
//					+ toHex(aesPaddingData));
			byte[] fileContentArr = DecryptionUtil.readBytes(file, 5632, fileLength);
			int iterationCount = 0;
			int blockSize = 512;
			int bytesToRead = fileLength;
			byte[] decryptedFileArr = new byte[fileLength];
			while (bytesToRead >= blockSize) {
				byte[] dePaddedFileArr = getDecryptedContent(decryptedAESKeyArr,
						fileContentArr, iterationCount, blockSize, 0, null);
				System.arraycopy(dePaddedFileArr, 0, decryptedFileArr,
						iterationCount * blockSize, dePaddedFileArr.length);
				iterationCount++;
				bytesToRead -= 512;
			}
			if (bytesToRead > 0) {
				byte[] dePaddedFileArr = getDecryptedContent(decryptedAESKeyArr,
						fileContentArr, iterationCount, bytesToRead,
						aesPaddingLength, aesPaddingData);
				// int i=0;
				System.arraycopy(dePaddedFileArr, 0, decryptedFileArr,
						iterationCount * blockSize, dePaddedFileArr.length);
			}
			nxlFilePart.setDecryptedFileContent(decryptedFileArr);
			/*logger.debug("decryption completed.");
			logger.debug("Time taken for decrypting contents(including key retrieval):"
					+ (System.currentTimeMillis() - startTime) + " ms");*/

			return unwrappedFile;
		} finally{
			if(file!=null){
				file.close();
			}
		}
	}


	

}
