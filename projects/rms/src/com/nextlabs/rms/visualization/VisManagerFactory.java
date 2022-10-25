package com.nextlabs.rms.visualization;

import java.util.List;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;

public class VisManagerFactory {
	
	private static VisManagerFactory instance=new VisManagerFactory();
	
	private static final String XLSX_FILE_EXTN = ".xlsx";
	
	private static final String XLS_FILE_EXTN = ".xls";
	
	public static final String SCS_FILE_EXTN = ".scs";
	
	public static final String HSF_FILE_EXTN = ".hsf";
	
	private List<String> HOOPSFileFormats = GlobalConfigManager.getInstance().getSupportedHOOPSFileFormatList();
		
	private VisManagerFactory(){	
	}
	
	public static VisManagerFactory getInstance(){
		return instance;
	}
	
	public IVisManager getVisManager(String fileNameWithoutNXL) throws RMSException{
		boolean isUseImgForExcel=GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.USE_IMG_FOR_EXCEL);
		int index = fileNameWithoutNXL.toLowerCase().lastIndexOf(".");
		String fileExtension="";
		if(index!=-1){
			fileExtension=fileNameWithoutNXL.substring(index, fileNameWithoutNXL.length()).toLowerCase();
		}
		if((fileExtension.equalsIgnoreCase(XLSX_FILE_EXTN) || fileExtension.equalsIgnoreCase(XLS_FILE_EXTN))&&!isUseImgForExcel){
			return new ExcelVisManager();
		} else if (fileExtension.equalsIgnoreCase(GlobalConfigManager.PDF_FILE_EXTN)) {
			return new PDFVisManager();
		} else if (fileExtension.equalsIgnoreCase(GlobalConfigManager.DWG_FILE_EXTN)) {
			return new GenericVisManager();
		} else if (HOOPSFileFormats.contains(fileExtension)) {
			return new CADVisManager();
		} else if (fileExtension.equalsIgnoreCase(GlobalConfigManager.VDS_FILE_EXTN)) {
			return new SAPVdsVisManager();
		} else if (fileExtension.equalsIgnoreCase(GlobalConfigManager.RH_FILE_EXTN)) {
			return new SAPRHVisManager();
		} else {
			List<String> supportedFileFormats = GlobalConfigManager.getInstance().getSupportedFileFormat();
			if (supportedFileFormats.contains(fileExtension)) {
				return new GenericVisManager();
			} else {
				throw new RMSException(RMSMessageHandler.getClientString("unsupportedFormatErr"));
			}
		}
	}
}
