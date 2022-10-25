package com.nextlabs.rms.wrapper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.bind.DatatypeConverter;

public class HTMLWrapperUtil {

	public static final String FIELD_NAME="fileContent";
	
	public static final String ORIGFILENAME = "origFileName";
	
//	static final String[] ALLOWED_EXTN = { ".doc",".docx",".xls",".xlsx",".ppt",".pptx",".dwg",".jpg",".gif",".jpeg" };
	
	public static String getHTMLWithFile(File inputFile, String protocol,
			String hostName, String portNum, String contextName) throws IOException {		
		String origFileName = inputFile.getName();
		/*if(!HTMLWrapperUtil.checkAllowedFormat(origFileName))
			return null;*/
		byte[] byteArr = new byte[(int) inputFile.length()];
		InputStream ios = null;
		try {
			ios = new FileInputStream(inputFile);
			if (ios.read(byteArr) == -1) {
				throw new IOException(
						"EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		byte[] encoded = getEncodedBytes(byteArr);
		
		String htmlContent = generateHTML(encoded, protocol, hostName, portNum, contextName,
				origFileName);
		return htmlContent;
	}
	
	public static String generateHTML(byte[] encoded, String protocol, String hostName, String portNum, String contextName, String origFileName)
	{
		String url = null;
		if(portNum!=null&&portNum.length()>0){
			url = protocol + "://" + hostName + ":" + portNum + "/" + contextName + "/RMSViewer/UploadFile";			
		}else{
			url = protocol + "://" + hostName + "/" + contextName + "/RMSViewer/UploadFile";
		}
		String htmlContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head>" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>NextLabs Rights Management</title>"+
				"<style type=\"text/css\">"+
				"table.curvedEdges { border:10px solid #808080;-webkit-border-radius:13px;-moz-border-radius:13px;-ms-border-radius:13px;-o-border-radius:13px;border-radius:13px; background-color:#808080;margin:0 auto;padding:0;}"+
				"table.curvedEdgesLight { border:10px solid #F8F8F8;-webkit-border-radius:13px;-moz-border-radius:13px;-ms-border-radius:13px;-o-border-radius:13px;border-radius:13px; background-color:#F8F8F8}"+
				"input[type=submit] {padding:5px 15px; background:#3366FF; border:0 none;cursor:pointer;-webkit-border-radius: 5px;border-radius: 5px; color:#FFFFFF}"+
				"input[type=submit]:hover {background:#0000FF;}"+
				"</style></head>" +
				"<body><table class=\"curvedEdges\" align=\"center\"><tr><td><table class=\"curvedEdgesLight\" align=\"center\"><tr><td>"+
				"<h1 align=\"Center\">NextLabs Rights Management</h1><h3 align=\"Center\">Rights Protected Document</h3><hr>"+
				"</td></tr><tr><td>You are attempting to view a NextLabs Rights Protected File. Click 'View File' to access the file.<br>&nbsp;</td></tr><tr>"+
				"<td align=\"center\">"+
				"</font><form method=\"post\" action=\"" + url + "\" " +
				"onSubmit=\" \">$$ENCODED_CONTENT$$" +
				"<input type=\"submit\" align=\"center\" value=\"View File\">"+
				"</form></td></tr></table></td></tr></table></body></html>";
		String FIELD_NAME_VALUE="<input name=\""+FIELD_NAME;
		String TYPE_HIDDEN="\" type=\"hidden\" value=\"";
		String ANGULAR_BRACKETS="\">\n";
		// loop on content and dump as input values
		int offset = 0;
		int id = 0;
		int len = encoded.length;
		StringBuilder htmlBuilder = new StringBuilder();
		while (offset <= (len - 1024)) {
			String portion = new String(encoded, offset, 1024);
			offset += 1024;
			htmlBuilder.append(FIELD_NAME_VALUE + id++
					+ TYPE_HIDDEN + portion +ANGULAR_BRACKETS );
		}
		// Handle any remaining portion
		int remainder = len - offset;
		if ((remainder > 0) && (remainder <= 1024)) {
			String portion = new String(encoded, offset, remainder);
			htmlBuilder.append(FIELD_NAME_VALUE + id++
					+ TYPE_HIDDEN + portion +ANGULAR_BRACKETS );
		}
		//Add the name of the file to HTML
		htmlBuilder.append("<input name=\""+ ORIGFILENAME + TYPE_HIDDEN + origFileName +ANGULAR_BRACKETS);		
		htmlContent = htmlContent.replace("$$ENCODED_CONTENT$$", htmlBuilder.toString());
		return htmlContent;
	}
	
	public static byte[] getEncodedBytes(byte[] byteArr)
	{
		String encodedString=DatatypeConverter.printBase64Binary(byteArr);
		return encodedString.getBytes();	
	}
	/*
	public static boolean checkAllowedFormat(String name)
	{
		if(name==null){
			return false;			
		}
		String[] allowedExtnArr = getAllowedList();
		if(allowedExtnArr.length==0){
			allowedExtnArr = HTMLWrapperUtil.ALLOWED_EXTN;
		}
		for(String extn:allowedExtnArr){
			if(name.endsWith(extn.trim())){
				return true;				
			}
		}
		return false;
	}
	
	public  static String[] getAllowedList() {
		String allowed=ConfigManager.getInstance().getStringProperty(ConfigManager.ALLOWED_FILE_EXTN);
		String[] allowedExtnArr = allowed.split(",");
		return allowedExtnArr;
	}
	*/

	public static String generateHTMLUnorderedList(String message, String[] unorderedList) {
		StringBuilder builder = new StringBuilder();
		builder.append(message);
		if (unorderedList != null && unorderedList.length > 0) {
			builder.append("<ul>");
			for (String value : unorderedList) {
				builder.append("<li>").append(value).append("</li>");
			}
			builder.append("</ul>");
		}
		return builder.toString();
	}

	public static String generateHTMLUnorderedList(String message, Collection<String> unorderedList) {
		String[] list = null;
		if (unorderedList != null) {
			list = unorderedList.toArray(new String[unorderedList.size()]);
		}
		return generateHTMLUnorderedList(message, list);
	}
}
