package com.nextlabs.rms.conversion;

public class ConverterFactory {
	
	private static ConverterFactory instance=new ConverterFactory();
	public static final String CONVERTER_TYPE_IMG = "CONVERTER_TYPE_IMG";
	public static final String CONVERTER_TYPE_CAD = "CONVERTER_TYPE_CAD";
	public static final String CONVERTER_TYPE_XLS = "CONVERTER_TYPE_XLS";
	private ConverterFactory(){	
	}
	
	public static ConverterFactory getInstance(){
		return instance;
	}
	
	public IFileConverter getConverter(String type){
		if(type.equalsIgnoreCase(CONVERTER_TYPE_XLS)){
			return new ExcelConverter();
		}
		else if(type.equalsIgnoreCase(CONVERTER_TYPE_CAD)){
			return new CADConverter();
		}
		else{
			return new ImageConverter();
		}
	
	}
}