package com.nextlabs.rms.conversion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import static java.nio.charset.StandardCharsets.*;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.util.WatermarkUtil;

public class HTMLFileGenerator {
	
	private static String PRINT_IDENTIFIER="<!--ISYSINDEXINGOFF-->";
	
	private static String OTHER_BUTTONS;

	static {
		OTHER_BUTTONS = "<button id=\"download-btn\" type=\"button\" onclick=\"downloadFile()\" class=\"btn btn-default spaced\" "
				+ "data-toggle=\"tooltip\" data-placement=\"bottom\" title=\"Download Protected File\"><span class=\"glyphicon glyphicon-download\"></span></button>";
		OTHER_BUTTONS += "<button id=\"rights-btn\" type=\"button\" onclick=\"showRights()\" class=\"btn btn-default spaced\" "
				+ "tabindex=\"0\" data-trigger=\"focus\" data-toggle=\"popover\" data-placement=\"bottom\" title=\"Rights\" data-container=\"body\"><span class=\"glyphicon glyphicon-list-alt\"></span></button>";
		OTHER_BUTTONS += "<button id=\"info-btn\" type=\"button\" onclick=\"showInfo()\" class=\"btn btn-default spaced\" "
				+ "tabindex=\"0\" data-trigger=\"focus\" data-toggle=\"popover\" data-placement=\"bottom\" title=\"Info\" data-container=\"body\"><span class=\"glyphicon glyphicon-info-sign\"></span></button>";
	}
	
	public static  void handleExcelFile(String sessionId, String docId, String htmlFileName,
		byte[] fileContent, RMSUserPrincipal user, boolean isPrintAllowed, WaterMark waterMarkObj) throws IOException {
		
		String webDir = GlobalConfigManager.getInstance().getWebDir();
		File tempWebDir = new File(webDir, GlobalConfigManager.TEMPDIR_NAME+File.separator+sessionId+File.separator+docId);
		if(!tempWebDir.exists()){
			tempWebDir.mkdirs();
		}
		String outputFilePath = tempWebDir.getAbsolutePath()+File.separator+htmlFileName;
		//Save the the HTML file
		ImageProcessor.getInstance().convertFileToHTML(fileContent, tempWebDir.getAbsolutePath(), htmlFileName);
		String content = getHTMLFileContent(outputFilePath);
		// HTML content to include watermark 
		if (waterMarkObj != null) {
			content = addWaterMark(user, content,waterMarkObj);
		}
		content = addExcelScript(docId,content);	

		content = addToolbar(content, FilenameUtils.getBaseName(htmlFileName));
		content = addError(content,docId);


		File file = new File(outputFilePath); 
		FileUtils.writeStringToFile(file, content, "UTF-8");
	}

	private static String addWaterMark(RMSUserPrincipal user, String content,WaterMark waterMarkObj) {
		String waterMarkStr = "";
		StringBuilder watermarkDiv = new StringBuilder();
		
		String scriptAdd="<head><script src=\"../../../ui/lib/3rdParty/fontChecker.js\"></script><script  src=\"../../../ui/lib/rms/watermarkSpread.js\"></script>";
		if(waterMarkObj.getWaterMarkStr()!= null && waterMarkObj.getWaterMarkStr().length()>0){
			if(user.getUserName()!=null){
				waterMarkStr = waterMarkObj.getWaterMarkStr().replace(GlobalConfigManager.WATERMARK_USERNAME, user.getUserName());
			}
			waterMarkStr=WatermarkUtil.updateWaterMark(waterMarkStr, user.getDomain(), user.getUserName(), waterMarkObj);
			waterMarkStr=waterMarkStr.replaceAll(waterMarkObj.getWaterMarkSplit(), "<br/>");
			waterMarkStr=waterMarkStr.replaceAll("\\\\", "\\\\\\\\");
			waterMarkStr=waterMarkStr.replaceAll("\\$", "\\\\\\$");			
			String rotation = waterMarkObj.getWaterMarkRotation();
			String density = waterMarkObj.getWaterMarkDensity();
			watermarkDiv.append("<body><div class=\"watermark\" style=\"font-size:").append(waterMarkObj.getWaterMarkFontSize()).append("px;color:")
			 .append(waterMarkObj.getWaterMarkFontColor()).append(";opacity:").append(waterMarkObj.getWaterMarkTransparency()/100.0)
			 .append(";\"><p><span style=\"display:inline-block; text-align:center;\">").append(waterMarkStr).append("</span><p></div>")
			 .append("<input type=\"hidden\" id=\"rotation\" value='").append(rotation).append("'/>")
			 .append("<input type=\"hidden\" id=\"waterMarkDensity\" value='").append(density).append("'/>")
			 .append("<input type=\"hidden\" id=\"fontName\" value=\"").append(waterMarkObj.getWaterMarkFontName()).append("\"/>");
			
		}
			content=content.replaceFirst("<body>", watermarkDiv.toString());
			//HTML content to include scripts and css
			content=content.replaceFirst("<head>",scriptAdd);
		return content;
	}
	
	private static String addExcelScript(String docId, String content) {
		StringBuilder titleAndScript = new StringBuilder();
		String documentIdDiv = "";

				
		titleAndScript.append("<head><title>NextLabs Rights Management</title><link rel=\"shortcut icon\" href=\"../../../ui/css/img/favicon.ico\" />")
		.append("<meta name=\"format-detection\" content=\"date=no\"><meta name=\"format-detection\" content=\"address=no\"><meta name=\"format-detection\" content=\"telephone=no\">")
		.append("<link rel=\"stylesheet\" href=\"../../../ui/css/bootstrap.min.css\"/><link rel=\"stylesheet\" href=\"../../../ui/css/style.css\"><link rel=\"stylesheet\" href=\"../../../ui/css/viewer.css\"><link rel=\"stylesheet\" href=\"../../../ui/css/font/fira.css\">")
		.append("<script src=\"../../../ui/lib/3rdParty/shortcut.js\"></script><script src=\"../../../ui/lib/3rdParty/dateformat.js\"></script><script src=\"../../../ui/lib/3rdParty/jquery-1.10.2.min.js\"></script>")
		.append("<script src=\"../../../ui/lib/3rdParty/bootstrap.min.js\"></script><script src=\"../../../ui/lib/rms/rmsUtil.js\"></script><script src=\"../../../ui/lib/rms/protect.js\"></script>")
		.append("<script src=\"../../../ui/lib/3rdParty/jquery.blockUI.js\"></script><script src=\"../../../ui/app/viewers/Viewer.js\"></script><script src=\"../../../ui/app/viewers/ExcelViewer.js\"></script>");
		
		content=content.replaceFirst("<title></title>", "");
		content=content.replaceFirst("<head>",titleAndScript.toString());
		return content;
	}
	
	private static String addToolbar(String content, String docName){
		StringBuilder headerAndToolbarHtml = new StringBuilder();
		headerAndToolbarHtml.append("<body><div class=\"pageWrapper excelViewer\" id=\"pageWrapper\"><div class=\"cc-header\"><div class=\"cc-header-logo\"></div><button id=\"rms-help-button\" title=\"Help\" onclick=\"showHelp('/RMS/help/Document_Viewer.htm')\" class=\"toolbar-button btn btn-default desktop-only spaced\"></button></div>")
		.append("<div class =\"toolbarContainerPlaceholder\"></div><div class =\"toolbarContainer fade-div\"><div id=\"titleContainer\" class=\"titleContainer\"><h5 class=\"titleText\"><b><span title=\"").append(docName).append("\" id=\"title\">").append(docName).append("</span></b></h5></div>")
		.append("<div id=\"toolBar\"><div class=\"toolbar-tools\">")
		.append("<button id=\"rms-print-button\" title=\"Print\" onclick=\"printFile()\" class=\"toolbar-button btn btn-default desktop-only spaced print-enabled\"></button>")
		.append("<button id=\"rms-download-button\" title=\"Download Protected File\" type=\"button\" onclick=\"downloadFile()\" class=\"toolbar-button download btn btn-default spaced\"></button>")
		.append("<button id=\"rms-info-button\" type=\"button\" onclick=\"showInfo()\" class=\"toolbar-button info btn btn-default spaced\" title=\"Info\"></button>") 	
		.append("</div></div></div>")
		.append("<div id =\"viewer-rms-info\" class=\"modal-dialog modal-content\">")
		.append("<div class=\"rms-info-filedetails\">").append("<div><a id=\"fileInfoCloseBtn\" class=\"rms-info-closeLink\">x</a><div class=\"rms-info-fileName\"><span id=\"fileTitle\" style=\"font-family: fira sans;\"></span></div><br><br>")
		.append("<table class=\"rms-info-fileProperties\"><tr><td width=\"30%\"><span id=\"fileTypeSpan\">File Type</span></td><td width=\"30%\"><span id=\"fileSizeSpan\">Size</span></td><td width=\"40%\"><span id=\"fileDateSpan\">Last Modified On</span></td></tr>")	
		.append("<tr><td><b><span id=\"fileTypeValue\">-</span></b></td><td><b><span id=\"fileSizeValue\">-</span></b></td><td><b><span id=\"fileDateValue\">-</span></b></td></tr></table></div></div>")
		.append("<div class=\"rms-info-repoDetails\"><h4><span id=\"fileRepoNameValue\">N/A</span></h4><h5><span id=\"fileRepoPathValue\">N/A</span></h5></div>")
		.append("<div id=\"rms-info-tags\" class=\"rms-info-tags\" style=\"background-color:white\">")
		.append("<br><br><center><label><span id=\"fileTagsSpan\" style=\"color:black;\">File Classification</span></label><br><div id=\"fileTagsValue\"></div><br></center></div>")
		.append("<div class=\"rms-info-rights\" style=\"background-color:white\">")
		.append("<br><br><center><label><span id=\"fileRightsSpan\">File Rights</span></label><br><div id=\"fileRightsValue\"></div><br></center></div><div><br><br><br></div></div>")
		.append("<div id=\"rms-viewer-content\">");
		content=content.replaceFirst("<body>", headerAndToolbarHtml.toString());
		content=content.replaceFirst("</body>","</div></div></body>");
		return content;
	}
	
	private static String addError(String content, String docId){
		return content.replaceFirst("<body>","<body><div id=\"loading\"><img id=\"loading-image\" src=../../../ui/css/img/loading_48.gif alt=\"Loading...\" /></div><div id=\"documentId\" value=\""+docId+"\"style=\"display: none;\"></div><div id=\"documentlength\" value=\""+content.length()+"\"style=\"display: none;\"></div>");
	}

	private static  String addPrintScript(String content,boolean isPrintAllowed) {
		String printScript;
		String closeDivScript;
		boolean printIdentifier = content.contains(PRINT_IDENTIFIER);
		String printFunction = (printIdentifier ? "printFile()" : "window.print()");
		if(isPrintAllowed){
			printScript="<div id=\"toolbar\"> <ul class = \"ISYS_TOC\"><li><button id=\"printbtn\" type=\"button\" onclick=\"" + printFunction + "\" class=\"btn btn-default spaced\" data-toggle=\"tooltip\" data-placement=\"bottom\" title=\"Print\"><span class=\"glyphicon glyphicon-print\"></span></button>__OTHER_BUTTONS__</li>";
		}
		else{
			printScript="<div id=\"toolbar\"> <ul class = \"ISYS_TOC\"><li><button id=\"printbtn\" type=\"button\" class=\"btn btn-default spaced\" data-toggle=\"tooltip\" data-placement=\"bottom\" title=\"Print disabled\" style=\"cursor:default\" disabled><span class=\"glyphicon glyphicon-print text-muted\"></span></button>__OTHER_BUTTONS__</li>";
		}
		
		printScript = printScript.replace("__OTHER_BUTTONS__", OTHER_BUTTONS);
		if (printIdentifier) {
			closeDivScript = "</div><!--ISYSINDEXINGON-->";
			content = content.replaceFirst("<ul class = \"ISYS_TOC\">", printScript);
			content = content.replaceFirst("<!--ISYSINDEXINGON-->", closeDivScript);
		} else {
			printScript = "<body>" + printScript + "</ul></div>";
			content = content.replaceFirst("<body>", printScript);
		}
		return content;
	}

	private static String getHTMLFileContent(String outputFilePath)
			throws IOException {
		Path path = Paths.get(outputFilePath);
		byte[] data = Files.readAllBytes(path); 
		String content = new String(data, UTF_8);
		content = content.replace("" + (char) 0xE, "");
		content = content.replace("" + (char) 0xF, "");
		content = content.replace("" + (char) 0x10, "");
		return content;
	}
}
