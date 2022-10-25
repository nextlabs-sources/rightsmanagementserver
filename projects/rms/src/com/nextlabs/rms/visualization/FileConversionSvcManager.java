package com.nextlabs.rms.visualization;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.conversion.ConverterFactory;
import com.nextlabs.rms.conversion.IFileConverter;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.nxl.decrypt.CryptManager;
import com.nextlabs.rms.services.application.RMSApplicationErrorCodes;
import com.nextlabs.rms.visualization.PDFVisManager.PDFType;

import noNamespace.ConvertFileRequestType;
import noNamespace.ConvertFileResponseType;
import noNamespace.ConvertFileServiceDocument;
import noNamespace.ConvertFileServiceType;
import noNamespace.ErrorType;

public class FileConversionSvcManager{
	
	private static Logger logger = Logger.getLogger(FileConversionSvcManager.class);
	
	private List<String> HOOPSFileFormats = GlobalConfigManager.getInstance().getSupportedHOOPSFileFormatList();
	
	private static final String FORMAT_HSF = "hsf";
	private static final String FORMAT_SCS = "scs";

	private static final String[] supportedFormats = {FORMAT_HSF, FORMAT_SCS};
	
	public ConvertFileServiceDocument convertFile(ConvertFileServiceDocument convertFileServiceDoc) throws RMSException{
		ConvertFileRequestType request = convertFileServiceDoc.getConvertFileService().getConvertFileRequest();
		ConvertFileServiceType convertService=convertFileServiceDoc.addNewConvertFileService();
		ConvertFileServiceDocument response=ConvertFileServiceDocument.Factory.newInstance();
		ConvertFileResponseType responseType=ConvertFileResponseType.Factory.newInstance();				
		String tempDir = GlobalConfigManager.getInstance().getTempDir()+File.separator+UUID.randomUUID()+System.currentTimeMillis();
		try{				
			String fileName = request.getFileName();
			String binaryFile = request.getBinaryFile();
			String checksum = request.getChecksum();
			String toFormat=FORMAT_HSF;
			if(request.getToFormat()!=null){
				toFormat = request.getToFormat().toString();
			}				
			boolean isNxl = false;			
			if(request.getIsNxl()){
				isNxl = request.getIsNxl(); 
			}
			String fileNameWithoutNXL = EvaluationHandler.getRequestedFileNameWithoutNXL(new File(fileName));
			boolean isInputValid = validateInput(fileName, binaryFile, toFormat, isNxl, fileNameWithoutNXL, checksum);
			if(!isInputValid){
				return getErrorResponse(convertService, response, responseType, RMSApplicationErrorCodes.MALFORMED_INPUT_ERROR, "Incorrect Input");
			}
			String destFileName = replaceFileType(fileNameWithoutNXL, "."+toFormat);	
			String tempInput=null;
			String tempOutput = null;
			boolean isFileNameAsDocId=GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.USE_FILENAME_AS_DOCUMENTID);
			if(isFileNameAsDocId){
				 //for testing purpose
				tempInput=fileNameWithoutNXL;
				tempOutput=destFileName;
			} else{
				tempInput=System.currentTimeMillis() + UUID.randomUUID().toString()+getFileExtension(fileNameWithoutNXL);
				tempOutput=System.currentTimeMillis() + UUID.randomUUID().toString()+"."+toFormat;
			}
			File inputFile = getInputFile(request, tempInput, fileNameWithoutNXL, isNxl, tempDir);
			if(getFileExtension(fileNameWithoutNXL).equalsIgnoreCase(GlobalConfigManager.PDF_FILE_EXTN)){
				PDFType pdfType = PDFVisManager.determinePDFType(Base64.decodeBase64(binaryFile), fileNameWithoutNXL);
				if(pdfType != PDFType.PDF_3D){
					logger.error("Unsupported PDF format");
					return getErrorResponse(convertService, response, responseType, RMSApplicationErrorCodes.UNSUPPORTED_ERROR, "Unsupported PDF format");					
				}
			}
			File destFile = convertFile(inputFile, tempDir, tempOutput);
			responseType.setChecksum(calcChecksum(destFile));
			responseType.setBinaryFile(Base64.encodeBase64String(FileUtils.readFileToByteArray(destFile)));
			responseType.setFileName(destFileName);
			convertService.setConvertFileResponse(responseType);
			response.setConvertFileService(convertService);					
			return response;		
		}catch(Exception e){
			logger.error("Error occured while processing the request: "+e.getMessage());
			return getErrorResponse(convertService, response, responseType, RMSApplicationErrorCodes.GENERAL_ERROR, "Error occurred while processing the file");
		}finally{			
			FileUtils.deleteQuietly(new File(tempDir));
		}
	}

	public static String calcChecksum(File inputFile) throws IOException{
		byte[] inputBytes = FileUtils.readFileToByteArray(inputFile);
		return DigestUtils.md5Hex(inputBytes);
	}

	private File convertFile(File inputFile, String tempDir, String destFileName) throws Exception {
		String destinationPath=tempDir+File.separator+destFileName;			
		File destFile = new File(destinationPath); 			
		IFileConverter fileConverter=ConverterFactory.getInstance().getConverter(ConverterFactory.CONVERTER_TYPE_CAD);
		boolean conversionResult=fileConverter.convertFile(inputFile.getAbsolutePath(),destinationPath);
		if(!conversionResult){
			logger.error("Error occurred while converting the file:" + inputFile.getName() + " to " +destFileName);
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		}
		return destFile;
	}

	private File getInputFile(ConvertFileRequestType request, String fileName, String fileNameWithoutNXL, boolean isNxl, String tempDir) throws RMSException, IOException {
		byte[] decodedBytes=Base64.decodeBase64(request.getBinaryFile());
		String inputFilePath = tempDir + File.separator + fileName;
		File file = new File(inputFilePath);
		FileUtils.writeByteArrayToFile(file, decodedBytes);
		if(!calcChecksum(file).equals(request.getChecksum().toLowerCase())){
			logger.error("Checksum mismatch");
			throw new RMSException("Checksum mismatch");
		}
		if(isNxl){
			byte[] nxlDecodedBytes = decrypt(file, fileNameWithoutNXL);
			inputFilePath = tempDir + File.separator + fileNameWithoutNXL;
			FileUtils.writeByteArrayToFile(file, nxlDecodedBytes);
		}
		return file;
	}

	private ConvertFileServiceDocument getErrorResponse(ConvertFileServiceType convertService,
			ConvertFileServiceDocument response, ConvertFileResponseType responseType, int errCode, String errMsg) {
		ErrorType errorResponse = responseType.addNewError();
		errorResponse.setErrorCode(errCode);
		errorResponse.setErrorMessage(errMsg);
		responseType.setError(errorResponse);
		convertService.setConvertFileResponse(responseType);
		response.setConvertFileService(convertService);					
		return response;
	}
	
	public boolean validateInput(String fileName, String binaryFile, String toFormat, boolean isNxl, String fileNameWithoutNXL, String checksum){
		if(fileName == null || fileName.length()==0 || !fileName.contains(".")){
			logger.error("Incorrect Filename: " + fileName);
			return false;
		}		
		if(binaryFile == null || binaryFile.length()==0){
			logger.error("Incorrect Binary File");
			return false;
		}
		if(!isToFormatSupported(toFormat)){
			logger.error("Incorrect toFormat: "+ toFormat);
			return false;
		}	
		if(isNxl){
			if (!fileName.toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN)){
				logger.error("isNxl set to true but Extension doesn't end with .nxl");
				return false;
			}
		}else{
			if(fileName.toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN)){
				logger.error("isNxl set to false but extension ends with .nxl");
				return false;
			}
		}
		if(!isInputFormatSupported(fileNameWithoutNXL)){
			return false; 
		}
		if(checksum == null || checksum.length()==0){
			logger.error("Checksum is null or empty");
			return false;
		}
		return true;
	}

	private boolean isInputFormatSupported(String fileNameWithoutNXL) {
		String fileExtension = getFileExtension(fileNameWithoutNXL);		
		if(!HOOPSFileFormats.contains(fileExtension.toLowerCase())){
			logger.error("Unsupported CAD format");
			return false;		
		}
		return true;
	}
	
	public byte[] decrypt(File fileToValidate, String fileNameWithoutNXL) throws RMSException{
		CryptManager cryptManager=new CryptManager();		
		byte[] fileContent = cryptManager.getDecryptedBytes(fileToValidate);		
		if(fileContent == null){
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));				
		}
		if(fileContent.length==0){
			logger.error("The file - '"+fileNameWithoutNXL+ "' is empty");
			throw new RMSException(RMSMessageHandler.getClientString("emptyFileErr"));
		}
		return fileContent;
	}
	
	private String replaceFileType(String fileNameWithoutNXL, String toFormat) {
		String fileExtension = getFileExtension(fileNameWithoutNXL);
		String formattedFileName = fileNameWithoutNXL.replaceFirst(fileExtension,toFormat);
		return formattedFileName;
	}

	private String getFileExtension(String fileNameWithoutNXL) {
	int index = fileNameWithoutNXL.lastIndexOf(".");
	String fileExtension=fileNameWithoutNXL.substring(index, fileNameWithoutNXL.length());
	return fileExtension;
	}
	
	private boolean isToFormatSupported(String toFormat){
		if (!Arrays.asList(supportedFormats).contains(toFormat)){
			return false;
		}
		return true;
	}
}