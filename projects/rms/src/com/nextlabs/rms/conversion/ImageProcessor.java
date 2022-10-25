package com.nextlabs.rms.conversion;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.json.PrintFileUrl;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.util.WatermarkUtil;

public class ImageProcessor {

	private static Logger logger = Logger.getLogger(ImageProcessor.class);

	private final String LICENSE_KEY = "RPMUH GC75V 59RF6 25V7T GGW29 XRV4J";

	private Object docFilters;

	private static ImageProcessor instance = new ImageProcessor();

	private String extractorOpenOptions;//="LIMITS_PAGE_COUNT=2500;GRAPHIC_DPI=120;ISYS_MAX_DOCHANDLES=1000";

	private String canvasOptions;//="WATERMARK=$USERNAME;GRAPHIC_DPI=120;ISYS_MAX_DOCHANDLES=1000";

	private URLClassLoader urlClassLoader = null;
	
	private ImageProcessor(){
		initialize();
		createExtractorOptions();
	}

	public static ImageProcessor getInstance(){
		return instance;
	}

	private void initialize() {
		Class<?> docFiltersClass;
		
		try {
			ClassLoader currentThreadClassLoader
			= Thread.currentThread().getContextClassLoader();
			String perceptiveJarPath = GlobalConfigManager.getInstance().getDocConverterDir()+File.separator+GlobalConfigManager.ISYS11DF_JAR;
			String memoryStreamJarPath = GlobalConfigManager.getInstance().getDocConverterDir()+File.separator+GlobalConfigManager.MEMORY_STREAM_JAR;
			urlClassLoader = new URLClassLoader(new URL[]{new File(perceptiveJarPath).toURI().toURL(), new File(memoryStreamJarPath).toURI().toURL()},
						currentThreadClassLoader);
			docFiltersClass = Class.forName ("com.perceptive.documentfilters.DocumentFilters", true, urlClassLoader);
			docFilters =  docFiltersClass.newInstance();
			Method method = docFiltersClass.getDeclaredMethod("Initialize", new Class[]{String.class,String.class});
			method.invoke(docFilters,LICENSE_KEY, ".");
			logger.info("Doc viewer jar loaded successfully");
		} catch (Exception e) {
			logger.error("Error occurred while loading DocViewer jars and initializing DocumentFilters",e);
		} 	
	}

	private void createExtractorOptions() {
		int imageDPI = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.CONVERTER_IMAGE_DPI);
		if(imageDPI==-1){
			imageDPI=120;
		}
		int pageCount = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.CONVERTER_PAGE_LIMIT);
		if(pageCount==-1){
			pageCount=2500;
		}
		StringBuffer extractorBuff = new StringBuffer();
		extractorBuff.append("LIMITS_PAGE_COUNT=");
		extractorBuff.append(pageCount);
		extractorBuff.append(";GRAPHIC_DPI=");
		extractorBuff.append(imageDPI);
		extractorBuff.append(";ISYS_MAX_DOCHANDLES=1000");
		extractorBuff.append(";IMAGE_PROCESSOR=GDI");
		extractorOpenOptions = extractorBuff.toString();
		StringBuffer canvasBuff = new StringBuffer();
		canvasBuff.append("GRAPHIC_DPI=");
		canvasBuff.append(imageDPI);
		canvasBuff.append(";ISYS_MAX_DOCHANDLES=1000;");
		canvasOptions = canvasBuff.toString();
	}

	
	public int getNumPages(byte[] inputFileByteArr, String fileName) throws RMSException{
		Class<?> docFiltersClass;
		Object docExtractor = null;
		Class<?> extractorClass = null;
		int numPages = -1;
		try {
			docFiltersClass = Class.forName ("com.perceptive.documentfilters.DocumentFilters", true,urlClassLoader);
			Method getExtractor = docFiltersClass.getDeclaredMethod("GetExtractor", byte[].class);
			extractorClass = Class.forName ("com.perceptive.documentfilters.Extractor", true,urlClassLoader);
			docExtractor =  getExtractor.invoke(docFilters, inputFileByteArr);
			Method openDocExtractor = extractorClass.getDeclaredMethod("Open",int.class,String.class);
			Integer IGR_BODY_AND_META = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_BODY_AND_META").get(Integer.class);
			Integer IGR_FORMAT_IMAGE = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_FORMAT_IMAGE").get(Integer.class);
			openDocExtractor.invoke(docExtractor,IGR_BODY_AND_META | IGR_FORMAT_IMAGE, extractorOpenOptions);
			Method getPageCount = extractorClass.getDeclaredMethod("GetPageCount");
			numPages = (int) getPageCount.invoke(docExtractor);
			logger.debug("Number of Pages in file:"+numPages);
			if(numPages <= 0){
				throw new RMSException("Number of pages is less than one. (" + numPages + ").");
			}
		} catch (Exception err) {
			try{
				Class<?> IGRExceptionClass = Class.forName("com.perceptive.documentfilters.IGRException",true,urlClassLoader);
				if(IGRExceptionClass.isInstance(err)){
					logger.error("Error occurred while getting number of pages in file.", err);
					Method getErrorCode = IGRExceptionClass.getDeclaredMethod("getErrorCode");
					int errorCode = (int) getErrorCode.invoke(err);
					if(errorCode == 4 || errorCode == 17) { //IGR_E_NOT_READABLE, IGR_E_FILE_CORRUPT
						throw new RMSException(RMSMessageHandler.getClientString("corruptedFileErr"));		
					} else {
						handleReadMetaDataException(fileName);
					}
				}else{
					logger.error("Error occurred while getting number of pages in file.", err);
					handleReadMetaDataException(fileName);
				}
			}catch(ClassNotFoundException | NoSuchMethodException | SecurityException| IllegalAccessException |IllegalArgumentException| InvocationTargetException e){
				logger.error("Error occurred while processing an exception", err);
				handleReadMetaDataException(fileName);
			}
		} finally {
			try {
				if (docExtractor != null) {
					Method close =  extractorClass.getDeclaredMethod("Close");
					close.invoke(docExtractor);
				}
			} catch (Exception e) {
				logger.error("Error occured while closing docExtractor",e);
			} 
		}
		return numPages;
	}	
	private void handleReadMetaDataException(String fileName) throws RMSException{
		List<String> fileFormats = GlobalConfigManager.getInstance().getSupportedFileFormat();
		String currentFileExt = "." + FilenameUtils.getExtension(fileName).toLowerCase();
		if (fileFormats.contains(currentFileExt)) {
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		} else {
			throw new RMSException(RMSMessageHandler.getClientString("unsupportedFormatErr"));
		}
	}
	
	
	public boolean convertFileToHTML(byte[] inputFileByteArr, String folderPath, String fileName){
		boolean result = false;
		Class<?> docFiltersClass;
		Object docExtractor = null;
		Class<?> extractorClass = null;
		Class<?> subFileClass =null;
		try {
			docFiltersClass = Class.forName ("com.perceptive.documentfilters.DocumentFilters", true,urlClassLoader);
			Method getExtractor = docFiltersClass.getDeclaredMethod("GetExtractor", byte[].class);
			extractorClass = Class.forName ("com.perceptive.documentfilters.Extractor", true,urlClassLoader);
			docExtractor =  getExtractor.invoke(docFilters, inputFileByteArr);
			Method openDocExtractor = extractorClass.getDeclaredMethod("Open",int.class,String.class);
			Integer IGR_DEVICE_HTML = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_DEVICE_HTML").get(Integer.class);
			Integer IGR_DEVICE_XML = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_DEVICE_XML").get(Integer.class);
			Integer IGR_BODY_AND_META = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_BODY_AND_META").get(Integer.class);
			Integer IGR_FORMAT_HTML = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_FORMAT_HTML").get(Integer.class);
			openDocExtractor.invoke(docExtractor,IGR_DEVICE_HTML |IGR_DEVICE_XML  |IGR_BODY_AND_META |IGR_FORMAT_HTML,"IMAGES=Yes;"+extractorOpenOptions);
			Method getFirstImage = extractorClass.getDeclaredMethod("GetFirstImage"); 
			Object file = getFirstImage.invoke(docExtractor);
			subFileClass = Class.forName("com.perceptive.documentfilters.SubFile",true,urlClassLoader);
			Method copyTo = subFileClass.getDeclaredMethod("CopyTo",String.class);
			Method getId = subFileClass.getDeclaredMethod("getID");
			Method Close = extractorClass.getDeclaredMethod("Close");
			while (file != null) {
				try {
					copyTo.invoke(file, folderPath + File.separator + getId.invoke(file));
				} finally {
					Close.invoke(file);
				}
				Method getNextImage = extractorClass.getMethod("GetNextImage");
				file = getNextImage.invoke(docExtractor);
			}
			Method SaveTo = extractorClass.getDeclaredMethod("SaveTo",String.class);
			SaveTo.invoke(docExtractor, folderPath+File.separator+fileName);
			result = true;
		} catch (Exception err) {
			logger.error("Error processing file:"+folderPath+File.separator+fileName, err);
		} finally {
			try {
				if (docExtractor != null) {
					Method Close = extractorClass.getDeclaredMethod("Close");
					Close.invoke(docExtractor);
				}
			} catch (Exception e) {
				logger.error("Couldn't close doc extractor",e);
			}
		}
		return result;
	}	

	public PrintFileUrl convertFileToPDF(byte[] inputFileByteArr, String folderPath, String fileName, String domainName, String userName, WaterMark waterMarkObj) throws RMSException{
		PrintFileUrl printFileUrl = null ;
		String result = "";
		Class<?> docFiltersClass;
		Object docExtractor = null;
		Class<?> extractorClass = null;
		String filepdf = "";
		FileOutputStream fos = null;
		Class<?> IGRSteamClass = null;
		Class<?> MemoryStreamClass = null;
		Object stream = null;
		try {
			IGRSteamClass = Class.forName("com.perceptive.documentfilters.IGRStream", true, urlClassLoader);
			MemoryStreamClass = Class.forName("com.nextlabs.rms.conversion.MemoryStream", true, urlClassLoader);
			stream = MemoryStreamClass.newInstance();
			docFiltersClass = Class.forName ("com.perceptive.documentfilters.DocumentFilters", true,urlClassLoader);
			Method getExtractor = docFiltersClass.getDeclaredMethod("GetExtractor", byte[].class);
			extractorClass = Class.forName ("com.perceptive.documentfilters.Extractor", true,urlClassLoader);
			docExtractor =  getExtractor.invoke(docFilters, inputFileByteArr);
			Method openDocExtractor = extractorClass.getDeclaredMethod("Open",int.class,String.class);
			Integer IGR_BODY_AND_META = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_BODY_AND_META").get(Integer.class);
			Integer IGR_FORMAT_IMAGE = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_FORMAT_IMAGE").get(Integer.class);
			openDocExtractor.invoke(docExtractor,IGR_BODY_AND_META | IGR_FORMAT_IMAGE, extractorOpenOptions);
			String canvasOpenOpt = canvasOptions;
			if(waterMarkObj!=null && waterMarkObj.getWaterMarkStr()!= null && waterMarkObj.getWaterMarkStr().length()>0){
				waterMarkObj.setWaterMarkStr(WatermarkUtil.updateWaterMark(waterMarkObj.getWaterMarkStr(),domainName,userName,waterMarkObj));
			}
			Method MakeOutputCanvas = docFiltersClass.getDeclaredMethod("MakeOutputCanvas",IGRSteamClass,int.class,String.class);
			Integer IGR_DEVICE_IMAGE_PDF = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_DEVICE_IMAGE_PDF").get(Integer.class);
			Object canvas = MakeOutputCanvas.invoke(docFilters,stream, IGR_DEVICE_IMAGE_PDF, canvasOpenOpt);
			Class<?> canvasClass = Class.forName ("com.perceptive.documentfilters.Canvas", true,urlClassLoader);
			try {
				Method getPageCount = extractorClass.getDeclaredMethod("GetPageCount");
				Method GetPage = extractorClass.getDeclaredMethod("GetPage",int.class);
				Class<?> pageClass = Class.forName("com.perceptive.documentfilters.Page", true,urlClassLoader);
				Method RenderPage = canvasClass.getDeclaredMethod("RenderPage",pageClass);
				Method PageClose = pageClass.getDeclaredMethod("Close");
				int numPages = (int) getPageCount.invoke(docExtractor);
				for (int pageIndex = 0; pageIndex < numPages; pageIndex++) {
					Object page = GetPage.invoke(docExtractor, pageIndex);
					try {
						RenderPage.invoke(canvas, page);
						addWaterMark(waterMarkObj , canvas, page); 
					}
					finally {
						if (page != null) {
							PageClose.invoke(page);
						}
					}
				}
			}catch(Exception e){
				logger.error("Error occured while converting file to PDF",e);
			}
				finally {
				Method CanvasClose = canvasClass.getDeclaredMethod("Close");
				if (canvas != null) {
					CanvasClose.invoke(canvas);
				}
			}
			String tempname = folderPath.replace(GlobalConfigManager.getInstance().getWebDir(), "");
			String outFile="";
			if (fileName.indexOf(".") > 0){
				filepdf = fileName.substring(0, fileName.lastIndexOf("."))+ ".pdf";	
				outFile = URLEncoder.encode(filepdf,"UTF-8");
				outFile=outFile.replaceAll("[+]", "%20");
			}
			result = tempname + File.separator + outFile;
			File tempWebDir = new File(folderPath);
			if(!tempWebDir.exists()){
				tempWebDir.mkdirs();
			}
			fos = new FileOutputStream(new File(folderPath, filepdf));
			Method writeTo = MemoryStreamClass.getDeclaredMethod("writeTo", OutputStream.class);
			writeTo.invoke(stream, fos);
		} catch (Exception err) {
			logger.error("Error processing file " + folderPath + File.separator + fileName + ": " + err.getMessage(), err);
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		} finally {
			if (stream != null) {
				Method delete;
				try {
					delete = IGRSteamClass.getDeclaredMethod("delete");
					delete.invoke(stream);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logger.error("Error occured while deleting IGRStream ",e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("Error occurred while closing stream: " + e.getMessage());
				}
			}
			try {
				if (docExtractor != null) {
					Method CloseExtractor =  extractorClass.getDeclaredMethod("Close");
					CloseExtractor.invoke(docExtractor);
				}
			} catch (Exception e) {
				logger.error("Error occurred while closing Extractor: " + e.getMessage());
			}
		}
		try {
			PDFCustomizer.addAutoPrintOption(new File(folderPath + File.separator + filepdf));
			printFileUrl = new PrintFileUrl(result.replace("\\", "/"), null);
		} catch (Exception e) {
			logger.error("Error occurred while processing file " + fileName + ": " + e.getMessage(), e);
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		} finally {

		}
		return printFileUrl;
	}

	public DocumentContent getDocContent(byte[] inputFileByteArr, int[] pageNumArr, String domainName, String userName, WaterMark waterMarkObj){
		long startTime=System.currentTimeMillis();
		DocumentContent content = new DocumentContent();
		Class<?> docFiltersClass;
		Object docExtractor = null;
		Class<?> extractorClass = null;
		try {
			docFiltersClass = Class.forName ("com.perceptive.documentfilters.DocumentFilters", true,urlClassLoader);
			Method getExtractor = docFiltersClass.getDeclaredMethod("GetExtractor", byte[].class);
			extractorClass = Class.forName ("com.perceptive.documentfilters.Extractor", true,urlClassLoader);
			docExtractor =  getExtractor.invoke(docFilters, inputFileByteArr);
			Method openDocExtractor = extractorClass.getDeclaredMethod("Open",int.class,String.class);
			Integer IGR_BODY_AND_META = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_BODY_AND_META").get(Integer.class);
			Integer IGR_FORMAT_IMAGE = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_FORMAT_IMAGE").get(Integer.class);
			openDocExtractor.invoke(docExtractor,IGR_BODY_AND_META | IGR_FORMAT_IMAGE, extractorOpenOptions);
			
			if(pageNumArr.length==0){
				content.setErrMsg("Error occurred while getting contents of document");
			}else{
				String[] pages = new String[pageNumArr.length];
				for (int i=0;i<pageNumArr.length;i++) {
					String pageContent = getPage(docExtractor, pageNumArr[i], domainName, userName, waterMarkObj);
					pages[i] = pageContent;
				}
				content.setPageNum(pageNumArr);
				content.setPageContent(pages);
			}
		}catch(RMSException e){
			logger.error("Error occurred while getting contents of document",e);
			content.setErrMsg(e.getMessage());			
		}catch(Exception e){
			logger.error("Error occurred while getting contents of document", e);
			content.setErrMsg(RMSMessageHandler.getClientString("genericViewerErr"));
		}
		finally {
			try {
				logger.debug("Time taken to convert bytes to Image "+ (System.currentTimeMillis()-startTime));
				if (docExtractor != null) {
					Method CloseExtractor =  extractorClass.getDeclaredMethod("Close");
					CloseExtractor.invoke(docExtractor);
				}
			} catch (Exception e) {
				logger.error("Error occurred while closing Extractor", e);
			}
		}
		return content;
	}

	private String getPage(Object docExtractor, int pageNum, String domainName, String userName, WaterMark waterMarkObj) 
			throws  RMSException, IOException{
		String pageContent = null;
		try{
			Class<?> IGRSteamClass = Class.forName("com.perceptive.documentfilters.IGRStream", true, urlClassLoader);
			Class<?> MemoryStreamClass = Class.forName("com.nextlabs.rms.conversion.MemoryStream", true, urlClassLoader);
			Object stream = MemoryStreamClass.newInstance();
			String canvasOpenOpt = canvasOptions;
			if(waterMarkObj!=null && waterMarkObj.getWaterMarkStr()!= null && waterMarkObj.getWaterMarkStr().length()>0){
				waterMarkObj.setWaterMarkStr(WatermarkUtil.updateWaterMark(waterMarkObj.getWaterMarkStr(),domainName,userName,waterMarkObj)); 
			}		
			Class<?> docFiltersClass = Class.forName ("com.perceptive.documentfilters.DocumentFilters", true,urlClassLoader);
			Class<?> extractorClass = Class.forName ("com.perceptive.documentfilters.Extractor", true,urlClassLoader);
			Class<?> pageClass = Class.forName("com.perceptive.documentfilters.Page", true,urlClassLoader);
			Class<?> canvasClass = Class.forName ("com.perceptive.documentfilters.Canvas", true,urlClassLoader);
			Method MakeOutputCanvas = docFiltersClass.getDeclaredMethod("MakeOutputCanvas",IGRSteamClass,int.class,String.class);
			Integer IGR_DEVICE_IMAGE_PNG = (Integer) Class.forName("com.perceptive.documentfilters.isys_docfiltersConstants",true,urlClassLoader).getDeclaredField("IGR_DEVICE_IMAGE_PNG").get(Integer.class);
			Object canvas = MakeOutputCanvas.invoke(docFilters,stream, IGR_DEVICE_IMAGE_PNG, canvasOpenOpt);
	
			ByteArrayOutputStream bos = null;
			try {
				Method getPageCount = extractorClass.getDeclaredMethod("GetPageCount");
				int numPages = (int) getPageCount.invoke(docExtractor);
				if(pageNum<=0||pageNum>numPages){
					logger.error("Requested page number:"+pageNum+" exceeds number of pages in document:"+numPages);
					throw new RMSException(RMSMessageHandler.getClientString("invalidPageNumberErr"));
				}
				Method GetPage = extractorClass.getDeclaredMethod("GetPage",int.class);
				Object page = GetPage.invoke(docExtractor, pageNum-1);
				try {
					Method RenderPage = canvasClass.getDeclaredMethod("RenderPage",pageClass);
					RenderPage.invoke(canvas, page);
					addWaterMark(waterMarkObj, canvas, page);
				} finally {
					if (page != null) {
						Method PageClose = pageClass.getDeclaredMethod("Close");
						PageClose.invoke(page);
					}
				}
			} finally {
				if (canvas != null) {
					Method canvasClose = canvasClass.getDeclaredMethod("Close");
					canvasClose.invoke(canvas);
				}
			}
			try{
				bos = new ByteArrayOutputStream();			
				Method writeTo = MemoryStreamClass.getDeclaredMethod("writeTo", OutputStream.class);
				writeTo.invoke(stream, bos);
				byte[] fileBytes = bos.toByteArray();
				pageContent = Base64.encodeBase64String(fileBytes);		
			}catch(Exception e){
				logger.error("Error occured while writing byte stream to IGRStream",e);
			}
				finally{
				try {
					if (bos != null) {
						bos.close();
					}
				} catch (IOException e) {
					logger.error("Error occurred while closing stream", e);
				}
				if (stream != null) {
					Method delete = IGRSteamClass.getDeclaredMethod("delete");
					delete.invoke(stream);
				}
			}
		}catch(Exception e){
			logger.error("Error occured while fetching the page ",e);
		}
		return pageContent;
	}
	/*
	 * Add watermark to the page 
	 */
	private void addWaterMark(WaterMark waterMarkObj, Object canvas,
			Object page) throws Exception {
		Class<?> pageClass = Class.forName("com.perceptive.documentfilters.Page", true,urlClassLoader);
		Class<?> canvasClass = Class.forName ("com.perceptive.documentfilters.Canvas", true,urlClassLoader);
		if(waterMarkObj == null)
			return;
		int waterMarkFontSize=waterMarkObj.getWaterMarkFontSize();
		Method getPageWidth = pageClass.getDeclaredMethod("getWidth");
		int width=(int) getPageWidth.invoke(page);
		Method getPageHeight = pageClass.getDeclaredMethod("getHeight");
		int height = (int) getPageHeight.invoke(page);
		int opacity=waterMarkObj.getWaterMarkTransparency();
		if(opacity>100){
			opacity=100;
		}
		String waterMarkFontName=waterMarkObj.getWaterMarkFontName();
		String []fonts=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		int iterator;
		for (iterator = 0; iterator < fonts.length; iterator++) {
			if(fonts[iterator].equals(waterMarkFontName))
				break;
		}
		if(iterator==fonts.length) {
			logger.warn("Specified watermark: " + waterMarkFontName +  " not found on the system. Using Default.");
			waterMarkFontName = WaterMark.WATERMARK_FONT_NAME_FALLBACK;
		}
		
		Method SetCanvasOpacity = canvasClass.getDeclaredMethod("SetOpacity",int.class);
		Method SetCanvasFont = canvasClass.getDeclaredMethod("SetFont",String.class,int.class,int.class);
		Method SetCanvasBrush = canvasClass.getDeclaredMethod("SetBrush",int.class,int.class);
		Method TextWidth = canvasClass.getDeclaredMethod("TextWidth",String.class);
		Method CanvasRotation = canvasClass.getDeclaredMethod("Rotation",int.class);
		Method TextOut = canvasClass.getDeclaredMethod("TextOut",int.class,int.class,String.class);
		Method TextHeight = canvasClass.getDeclaredMethod("TextHeight",String.class);
		SetCanvasOpacity.invoke(canvas, (int) (opacity*(2.5)));
		SetCanvasFont.invoke(canvas, waterMarkFontName, waterMarkFontSize, 0);
		SetCanvasBrush.invoke(canvas, (Integer.valueOf(waterMarkObj.getWaterMarkFontColor().replaceFirst("#",""), 16)), 0);

		String watermark_string = waterMarkObj.getWaterMarkStr();
		int totalSize=(int) TextWidth.invoke(canvas,watermark_string);
		
		while(totalSize>width){
			waterMarkFontSize = waterMarkFontSize-1;
			SetCanvasFont.invoke(canvas, waterMarkFontName, waterMarkFontSize, 0);
			totalSize = (int) TextWidth.invoke(canvas, watermark_string);
		};
		SetCanvasFont.invoke(canvas, waterMarkFontName, waterMarkFontSize, 0);
		String [] lines=watermark_string.split(waterMarkObj.getWaterMarkSplit());	
		
		int heightCount=0;
		int maxWidth=0;
		int heightOffset=0;
		int widthOffset=0;
		String orientation = waterMarkObj.getWaterMarkRotation();
		String density = waterMarkObj.getWaterMarkDensity();
		if(density.equals(WaterMark.WATERMARK_DENSITY_NORMAL_VALUE)){
			heightOffset=200;
			widthOffset=100;
		}
		else if (density.equals(WaterMark.WATERMARK_DENSITY_DENSE_VALUE)) {
			heightOffset=100;
			widthOffset=100;
		}
		else {
			heightOffset=200;
			widthOffset=100;
		}
		heightOffset = heightOffset*waterMarkFontSize/36;
		widthOffset = widthOffset*waterMarkFontSize/36;

		for(String str:lines){
			maxWidth=Math.max(maxWidth,(int) TextWidth.invoke(canvas,str));
		}			

		if(orientation.equals(WaterMark.WATERMARK_ROTATION_CLOCKWISE_VALUE)) {
			int offset = (int)(Math.sin(Math.PI/4)*maxWidth);
			int lineOffset = (int)(heightOffset*1.414);
			CanvasRotation.invoke(canvas, 45);
			int lineCount=0;
			if(density.equals(WaterMark.WATERMARK_DENSITY_DENSE_VALUE))
				offset+=heightOffset-100;
			for(int i=-offset-1000;i<height+2000;i=i+heightCount+heightOffset){
				for(int j=(offset)-1000;j<width+2000;j=j+maxWidth+widthOffset){
					heightCount=0;
					for(String str:lines){
						int whiteSpace = (int)(maxWidth-(int) TextWidth.invoke(canvas,str))/2;
						TextOut.invoke(canvas, j-lineCount*lineOffset+whiteSpace,i+heightCount, str);
						heightCount=heightCount+(int)TextHeight.invoke(canvas, str);
					}			
				}
				lineCount++;
			}
		}
		else if (orientation.equals(WaterMark.WATERMARK_ROTATION_ANTICLOCKWISE_VALUE)){
			int offset = (int)(Math.sin(Math.PI/4)*maxWidth);
			int lineOffset = (int)(heightOffset*1.414);
			CanvasRotation.invoke(canvas, -45);
			int lineCount = 0;
			if(density.equals(WaterMark.WATERMARK_DENSITY_DENSE_VALUE))
				offset-=heightOffset;
			for(int i=offset-1000;i<height+1000;i=i+heightCount+heightOffset){
				for(int j=-offset-1000;j<width+1000;j=j+maxWidth+widthOffset){
					heightCount=0;				
					for(String str:lines){
						int whiteSpace = (int)(maxWidth-(int) TextWidth.invoke(canvas,str))/2;
						TextOut.invoke(canvas,j-lineCount*lineOffset+whiteSpace,i+heightCount, str);	
						heightCount=heightCount+(int)TextHeight.invoke(canvas, str);
					}
				}
				lineCount++;
			}
		}
		else {
			int offset = (int)(Math.sin(Math.PI/4)*maxWidth);
			int lineOffset = (int)(heightOffset*1.414);
			CanvasRotation.invoke(canvas, -45);
			int lineCount = 0;
			if(density.equals(WaterMark.WATERMARK_DENSITY_DENSE_VALUE))
				offset-=heightOffset;
			for(int i=offset-1000;i<height+1000;i=i+heightCount+heightOffset){
				for(int j=-offset-1000;j<width+1000;j=j+maxWidth+widthOffset){
					heightCount=0;				
					for(String str:lines){
						int whiteSpace = (int)(maxWidth-(int) TextWidth.invoke(canvas,str))/2;
						TextOut.invoke(canvas,j-lineCount*lineOffset+whiteSpace,i+heightCount, str);	
						heightCount=heightCount+(int)TextHeight.invoke(canvas, str);
					}
				}
				lineCount++;
			}
		}		
	}
}
