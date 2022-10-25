package com.test.nextlabs.rms.qa.testAutomation.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/*******************************************************
 * @author: Michelle Xu
 * Created Date: 18/08/2015
 * This file contains all global constant variables for setting up 
 * the testing environment
 *  *********************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.test.nextlabs.rms.qa.testAutomation.testcases.RepositoryTest;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.UploadFilePage;

import autoitx4java.AutoItX;
import net.lingala.zip4j.core.ZipFile;

public class RMSCommon {
	
	static WebDriver driver;
	public static Robot robot;
	
	public static String logs = "";
	
	public RMSCommon (WebDriver wDriver){
		RMSCommon.driver = wDriver;
	}
	
	// This is method is for Sign in to RMS page
	// return the driver / page objects of Home page
	public static HomePage signInToAdmin (WebDriver driver){
		// Sign in to reporter page
		driver.get(RMSUtility.rmsURL);
		LoginPage logInPage = PageFactory.initElements(driver, LoginPage.class);
		HomePage homePage = logInPage.LoginAdmin();
		return homePage;
	}
	
	//check element lists is present on the web page
	public static boolean isElementPresent(By by) { 
		List<WebElement> elements = driver.findElements(by);
				if (elements.isEmpty()) {
				   return false;
				}
				else
					return true;
	}
	
	// check if one element is present on the web page
	public static boolean isElementPresent(WebElement by) { 
		try{
			by.getText();
		    return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}
	}
	
	// add one sp url, check the url
	public static boolean isTextPresent(WebDriver driver, String text) {
		try{
			driver.findElement(By.tagName("body")).getText().contains(text);
			BaseTest.logger.info("[Result]: add splist successfully!!");
			return true;
		}
		catch (Exception e){
			RepositoryTest.errorMes = e.getMessage();
			BaseTest.logger.info("[Result]: add splist failed!!");
			return false;
		}
	}
	
	//add several sp urls, check these urls
	public static  boolean isSPListPresent(WebDriver driver, String [] sitesNames) {
		boolean result = false;
		try {
			for(int i=0; i<sitesNames.length; i++)
			{
				result = driver.findElement(By.tagName("body")).getText().contains(sitesNames[i]);
				if(!result)
				{
					BaseTest.logger.info("[Result]: add splist failed!!");
					return false;
				}
			}
			BaseTest.logger.info("[Result]: add splist successfully!!");
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			RepositoryTest.errorMes = e.getMessage();
			BaseTest.logger.info("[Result]: add splist failed!!");
			return false;
		}
	}
	
	// get Perceptive Viewer for Office: doc, docx, ppt, pptx, pdf, dwg file
	// For 3D files need to check if "WebGL" in your Chrome web browser is enabled
	public static boolean isDOCViewer(WebDriver driver, String rights) throws Exception{
	     
		try{
			if (rights.equals("Allow_View")){
				System.out.println("CurrentURL:" + driver.getCurrentUrl());
				if(driver.getCurrentUrl().contains("/DocViewer.jsp?documentid"))
				{	
					return true;
				}
				else if (driver.getCurrentUrl().contains("/ShowError.jsp")){
					logs = UploadFilePage.getUploadErrorLog();
					return false;
				}
				else 
				{
					logs = " other error!!";
					return false;
				}
			}
			else{
				if(driver.getCurrentUrl().contains("/ShowError.jsp")
						& UploadFilePage.getUploadErrorLog().contains("You are not authorized to view this document.")){
					return true;
				}
				else {
					logs = "Deny_view faile, maybe faile to deny";
					return false;
				}		
			}
		}
		catch (Exception e)
		{
			logs = "Exception: "+e.getMessage();
			return false;
		}
	}
	
	// get Perceptive Viewer for Excel file
	public static boolean isExcelViewer(WebDriver driver, String rights) throws Exception{
		try{
			System.out.println(driver.getCurrentUrl());
			if(rights.equals("Allow_View"))
			{
				if(driver.getCurrentUrl().contains("/temp/") || driver.getCurrentUrl().endsWith(".html"))
					return true;
				else if(driver.getCurrentUrl().contains("/ShowError.jsp"))
				{
					String log = UploadFilePage.getUploadErrorLog();
					Assert.assertFalse(false, log);
					return false;
				}else {
					return false;
				}
			}else {
				if(driver.getCurrentUrl().contains("/ShowError.jsp")& UploadFilePage.getUploadErrorLog().contains("You are not authorized to view this document."))
				{
					return true;
				}else {
					return false;
				}
			}

		}catch(Exception e)
		{
			throw new Exception(e.getMessage());
		}
	}
	
	// get RHViewer for RH file
	public static boolean isRHViewer(WebDriver driver, String rights){
		try{
			if(rights.equals("Allow_View"))
			{
				if(driver.getCurrentUrl().contains("/SAPViewer/RHViewer.jsp?filePath="))
					return true;
				else if (driver.getCurrentUrl().contains("/ShowError.jsp")){
					
					logs = UploadFilePage.getUploadErrorLog();
					System.out.println(logs);
					return false;
				}
				else 
					return false;
			}else {
				if(driver.getCurrentUrl().contains("/ShowError.jsp")& UploadFilePage.getUploadErrorLog().contains("You are not authorized to view this document."))
				{
					return true;
				}else {
					return false;
				}
			}
			
		}catch(Exception e)
		{
			return false;
		}
	}
	
	// get RHViewer for VDS file
	public static boolean isVDSViewer(WebDriver driver, String rights){
		try{
			if(rights.equals("Allow_View"))
			{
				if(driver.getCurrentUrl().contains("/SAPViewer/VDSViewer.jsp?filePath="))
					return true;
				else if (driver.getCurrentUrl().contains("/ShowError.jsp")){
					logs = UploadFilePage.getUploadErrorLog();
					return false;
				}
				else 
					return false;
			}else {
				if(driver.getCurrentUrl().contains("/ShowError.jsp")& UploadFilePage.getUploadErrorLog().contains("You are not authorized to view this document."))
				{
					return true;
				}else {
					return false;
				}
			}
		}catch(Exception e)
		{
			return false;
		}
	}
	
	// get Hoops Viewer for 3D file
	public static boolean isCADViewer(WebDriver driver, String rights){
		try{
			if(rights.equals("Allow_View"))
			{
				if(driver.getCurrentUrl().contains("/CADViewer.jsp?file=temp"))
					return true;
				else if (driver.getCurrentUrl().contains("/ShowError.jsp")){
					logs = UploadFilePage.getUploadErrorLog();
					return false;
				}
				else 
					return false;
			} {
				if(driver.getCurrentUrl().contains("/ShowError.jsp")& UploadFilePage.getUploadErrorLog().contains("You are not authorized to view this document."))
				{
					return true;
				}else {
					return false;
				}
			}		
		}catch(Exception e)
		{
			return false;
		}
	}

	//get excel file content
	public static void getFileContent(String tempPath, String sourcePath) throws Exception
	{
		
		File inputFile = new File(tempPath);
		File tempFile = new File(sourcePath);
		
		makeDirs(sourcePath);
		
		if(tempFile.exists())
		{
			tempFile.delete();	
		}
		
		tempFile.createNewFile();

		try {	
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
		
			String lineToRemove = "toolbar";
			String currentLine;
			
			while((currentLine = reader.readLine()) != null)
			{
				String trimmedLine = currentLine.trim();
				if(trimmedLine.contains(lineToRemove))	continue;
				writer.write(currentLine + System.getProperty("line.separator"));
			}
			writer.close();
			reader.close();
			Thread.sleep(2000);
		} catch (Exception e) {
			// TODO: handle exception
			logs = e.getMessage();
		}
	}

	//get doc file content
	//get word, pdf, ppt file content
	public static void getFileContent(WebDriver driver, String filePath)
	{
		String responseBody = driver.getPageSource();
		//String fileContent = StringUtils.substringBetween(responseBody, "iVBORw0KGgoAAAANSUhEUg", "style=");
		String fileContent = StringUtils.substringBetween(responseBody, "iVBORw0KGgoAAAANSUhEUg", "\"");
		try {
			BufferedWriter out = null;
	         out = new BufferedWriter(new OutputStreamWriter(   
                     new FileOutputStream(filePath, true)));   
            out.write(fileContent);
            out.flush();
            out.close();
		} catch (Exception e) {
			// TODO: handle exception
			logs = e.getMessage();
		}
	}
	
	//compare files mehod one
	public static String getFileMD5(File file)
	{
		if(!file.isFile())
		{
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in =null;
		byte buffer[] = new byte[8192];
		int len;
		try
		{
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while((len = in.read(buffer)) != -1)
			{
				digest.update(buffer,0,len);	
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString(16);
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}finally
		{
			try
			{
				in.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//get file content

	
	//compare files method two
	public static boolean compareFiles(String filePath1, String filePath2) {
		try {
			BaseTest.logger.info("compare file content starting...");
			BaseTest.logger.info("CompareSourceFile: " + filePath1);
			BaseTest.logger.info("CompareDesFile: " + filePath2);
			File file1 = new File(filePath1);
			File file2 = new File(filePath2);
			
			FileReader fr = new FileReader(file1);
			FileReader fr2 = new FileReader(file2);
			
			BufferedReader br1 = new BufferedReader(fr);
			BufferedReader br2 = new BufferedReader(fr2);
		
			
			String s1 = null;
			String s2 = null;
			int i = 0; //row numbers
			boolean b = true;//check row
			
			while((s1=br1.readLine())!=null)
			{
				i++;
				while ((s2=br2.readLine())!=null) {
					if(!(s1.equals(s2)))
					{
						b = false;
					}				
					break;
				}		
			}
			
			if(file1.length()==0)
			{
				b = false;
			}
			
			br1.close();
			br2.close();
			
			if(b)
			{
				System.out.println("These two files are the same content");
				BaseTest.logger.info("[Result]: These two files are the same content, successfully!!");
				return true;
			}else {
				BaseTest.logger.info("[Result]: These two files are not the same content, failed!!");
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			return false;
		}
	}

	// get error page 
	
	public static boolean isErrorPage(WebDriver driver, String type){
		driver.getCurrentUrl().contains("/ShowError.jsp");
		return true;
	}

	
	
	public static boolean dealPotentialAlert(WebDriver driver,boolean option) {
		//exist or not
		boolean flag = false;
		//get exception
		try {
		    Alert alert = driver.switchTo().alert();
		    //get exception
		    try {
		        //confirm or cancel
		        if (option) {
		            //comfirm
		            alert.accept();
		            System.out.println("Accept the alert: " + alert.getText());
		        } else {
		            //cancel
		            alert.dismiss();
		            System.out.println("Dismiss the alert: " + alert.getText());
		        }
		        	flag = true;
		    } catch (WebDriverException e) {
		        if (e.getMessage().startsWith("Could not find")){
		            System.out.println("There is no111 alert appear!");
		        }else{
		            throw e;
		        }
		    }
		} catch (NoAlertPresentException e) {
		    System.out.println("There is no222 alert appear!");
		}
		return flag;
		}


	
	//check downlod RMC package file
	public static boolean isFileDownload(String directory, String source,String cer) throws Exception
	{
		//using zip4j api to unzip file
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(directory);
			File file = new File(directory);
			File[] filelist = file.listFiles();

			for (int i = 0; i < filelist.length; i++) {
				if(filelist[i].isFile())
				{
					System.out.println("filename-------"+filelist[i].getName());
					if(filelist[i].getName().equals("register.xml"))
					{
						SAXReader reader = new SAXReader();
						File xmlfile = new File(directory+"\\"+"register.xml");
						Document doc = reader.read(xmlfile);
						
						Element root = doc.getRootElement();
						String cers = root.element("Cert").getText();
						System.out.println("cers--"+cers);
						System.out.println("cer--"+cer);
						if(!cers.equals(cer))
						{
							throw new Exception("The download RMC package cer is not right, please check!");
						}
					}
				}
				if(filelist[i].isDirectory())
				{
					System.out.println("directoryname-----------"+filelist[i].getName());
				}
			}
			if(filelist.length!=5)
			{
				throw new Exception("The files in download RMC package  are not right, please check!");
			}else 
			{
				return true;
			}	
	}

	//get file name
	
	
	public  static String getFileName(String filePath) {
		File file = new File(filePath);
		String fileName = file.getName();
		
		String name = fileName.substring(0, fileName.lastIndexOf("."));
		
		return name;
	}
	
	//get file name
	
	
	public  static boolean makeDirs(String filePath2) throws IOException {
		File file = new File(filePath2);
		String foldrName = file.getParent();
		
		System.out.println(foldrName);
		
		if(foldrName==null || foldrName.isEmpty())
		{
			return false;
		}
		
		File folder = new File(foldrName);
		
		return (folder.exists() && folder.isDirectory())? true : folder.mkdirs();
		
	}
	
	//checkResult
	public static boolean checkResult (String types, WebDriver dirver, String rights) throws Exception {
		boolean result = true;
		switch (RMSCommon.types.valueOf(types)) {
		case PICTURE:
			result = isDOCViewer(dirver, rights);
			break;
		case EXCEL:
			result = isExcelViewer(dirver, rights);
			break;
		case RH:
			result = isRHViewer(dirver, rights);
			break;
		case VDS:
			result = isVDSViewer(dirver, rights);
			break;
		case CAD:
			result = isCADViewer(dirver, rights);
			break;
		default:
			throw new Exception("no this file type, please check");
		}
		return result;
	}

	
	// switch to other window and maximaize it 
	public static void switchTowindow(WebDriver driver)
	{ 
	    try {  
	        String currentHandle = driver.getWindowHandle();  
	        Set<String> handles = driver.getWindowHandles();  
	        for (String s : handles) {  
	           if (s.equals(currentHandle))  
	                continue;  
	            else {  
	                driver.switchTo().window(s); 
	                driver.manage().window().maximize();
	            }
	        }  
		    } catch (Exception e) {  
		    	BaseTest.logger.info("[Exception]: ", e);
		    }  
	}

	//type string to authentication dialog
	
	//use robot to type 
	public static void type(CharSequence characters) {
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            type(character);
        }
    }

    
	public static void type(char character) {
        switch (character) {
        case 'a': doType(KeyEvent.VK_A); break;
        case 'b': doType(KeyEvent.VK_B); break;
        case 'c': doType(KeyEvent.VK_C); break;
        case 'd': doType(KeyEvent.VK_D); break;
        case 'e': doType(KeyEvent.VK_E); break;
        case 'f': doType(KeyEvent.VK_F); break;
        case 'g': doType(KeyEvent.VK_G); break;
        case 'h': doType(KeyEvent.VK_H); break;
        case 'i': doType(KeyEvent.VK_I); break;
        case 'j': doType(KeyEvent.VK_J); break;
        case 'k': doType(KeyEvent.VK_K); break;
        case 'l': doType(KeyEvent.VK_L); break;
        case 'm': doType(KeyEvent.VK_M); break;
        case 'n': doType(KeyEvent.VK_N); break;
        case 'o': doType(KeyEvent.VK_O); break;
        case 'p': doType(KeyEvent.VK_P); break;
        case 'q': doType(KeyEvent.VK_Q); break;
        case 'r': doType(KeyEvent.VK_R); break;
        case 's': doType(KeyEvent.VK_S); break;
        case 't': doType(KeyEvent.VK_T); break;
        case 'u': doType(KeyEvent.VK_U); break;
        case 'v': doType(KeyEvent.VK_V); break;
        case 'w': doType(KeyEvent.VK_W); break;
        case 'x': doType(KeyEvent.VK_X); break;
        case 'y': doType(KeyEvent.VK_Y); break;
        case 'z': doType(KeyEvent.VK_Z); break;
        case 'A': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
        case 'B': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
        case 'C': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
        case 'D': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
        case 'E': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
        case 'F': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
        case 'G': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
        case 'H': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
        case 'I': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
        case 'J': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
        case 'K': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
        case 'L': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
        case 'M': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
        case 'N': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
        case 'O': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
        case 'P': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
        case 'Q': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
        case 'R': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
        case 'S': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
        case 'T': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
        case 'U': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
        case 'V': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
        case 'W': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
        case 'X': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
        case 'Y': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
        case 'Z': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
        case '`': doType(KeyEvent.VK_BACK_QUOTE); break;
        case '0': doType(KeyEvent.VK_0); break;
        case '1': doType(KeyEvent.VK_1); break;
        case '2': doType(KeyEvent.VK_2); break;
        case '3': doType(KeyEvent.VK_3); break;
        case '4': doType(KeyEvent.VK_4); break;
        case '5': doType(KeyEvent.VK_5); break;
        case '6': doType(KeyEvent.VK_6); break;
        case '7': doType(KeyEvent.VK_7); break;
        case '8': doType(KeyEvent.VK_8); break;
        case '9': doType(KeyEvent.VK_9); break;
        case '-': doType(KeyEvent.VK_MINUS); break;
        case '=': doType(KeyEvent.VK_EQUALS); break;
        case '~': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
        case '!': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_1); break;
        case '@': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_2); break;
        case '#': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_3); break;
        case '$': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_4); break;
        case '%': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
        case '^': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_6); break;
        case '&': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_7); break;
        case '*': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_8); break;
        case '(': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_9); break;
        case ')': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_0); break;
        case '_': doType(KeyEvent.VK_UNDERSCORE); break;
        case '+': doType(KeyEvent.VK_PLUS); break;
        case '\t': doType(KeyEvent.VK_TAB); break;
        case '\n': doType(KeyEvent.VK_ENTER); break;
        case '[': doType(KeyEvent.VK_OPEN_BRACKET); break;
        case ']': doType(KeyEvent.VK_CLOSE_BRACKET); break;
        case '\\': doType(KeyEvent.VK_BACK_SLASH); break;
        case '{': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
        case '}': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
        case '|': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
        case ';': doType(KeyEvent.VK_SEMICOLON); break;
        case ':': doType(KeyEvent.VK_COLON); break;
        case '\'': doType(KeyEvent.VK_QUOTE); break;
        case '"': doType(KeyEvent.VK_QUOTEDBL); break;
        case ',': doType(KeyEvent.VK_COMMA); break;
        case '<': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
        case '.': doType(KeyEvent.VK_PERIOD); break;
        case '>': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
        case '/': doType(KeyEvent.VK_SLASH); break;
        case '?': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
        case ' ': doType(KeyEvent.VK_SPACE); break;
        default:
            throw new IllegalArgumentException("Cannot type character " + character);
        }
    }

  
	private static void  doType(int... keyCodes) {
        doType(keyCodes, 0, keyCodes.length);
    }

  
	private static void  doType(int[] keyCodes, int offset, int length) {
        if (length == 0) {
            return;
        }
        
        robot.keyPress(keyCodes[offset]);
        doType(keyCodes, offset + 1, length - 1);
        robot.keyRelease(keyCodes[offset]);
    }
	
  //handle sharepoint download dialog
   
	public static void handleDownloadDialog()
    {
		try
		{
			AutoItX x = new AutoItX();
			//get a handle to the in window
			String winHandle = x.winGetHandle("[Class:IEFrame]", "");
			
			//get a handle to the main window
			String winTitle = "[HANDLE:" + winHandle + "]";
			System.out.println("winTitle" + winTitle);
			
			//get a handle to the control (IE9 notification bar)
			String controlHandle = x.controlGetHandle(winTitle, "", "[Class:DirectUIHWND]");
			String conTitle = "[HANDLE:" + controlHandle + "]";
			System.out.println("contitl" + conTitle);
			
			//must have this line in here in order to get a handle to the control
			x.winWaitActive(conTitle, "[Class:DirectUIHWND]", 10);
			
			// Get the x, y coordinates of the control
			int X = x.controlGetPosWidth(winTitle, "","[Class:DirectUIHWND]")-150;
			int Y = x.controlGetPosHeight(winTitle, "","[Class:DirectUIHWND]")-25;
			
			x.controlFocus(winTitle, "Do you want to open or save", "[Class:DirectUIHWND]");
			x.winActivate(winTitle, "Do you want to open or save");
			x.controlFocus(winTitle, "Do you want to open or save", "[Class:DirectUIHWND]");
			x.winActivate(conTitle);
			
			Thread.sleep(2000);
			x.controlClick(winTitle, "", "[Class:DirectUIHWND]", "left", 2, X, Y);
			x.controlSend(winTitle, "", "[Class:DirectUIHWND]","{Enter}");
			
			Thread.sleep(3000);
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
    }
  
    //get VM version
	public static String VMVersion()
	{
		return System.getProperty("os.name");
	}
	
	//check process if exist, it will be used in print action
	public static boolean checkApplication(String exeName)
	{
		Process proc;
		try
		{
			proc = Runtime.getRuntime().exec("tasklist");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc
                    .getInputStream()));
            String info = br.readLine();
            while (info != null) {
                if (info.indexOf(exeName) >= 0) {
                    return true;
                }
                info = br.readLine();
            }
		}catch(Exception e)
		{
			return false;
		}
		 return false;
	}

	public static void deleteProcess(String exeName)
	{
		Process proc;
		try
		{
			proc = Runtime.getRuntime().exec("tasklist");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc
                    .getInputStream()));
            String info = br.readLine();
            while (info != null) {
                if (info.indexOf(exeName) >= 0) {
                	Runtime.getRuntime().exec("taskkill /F /IM splwow64.exe");
                }
                info = br.readLine();
            }
		}catch(Exception e)
		{
			
		}
	}
	
	// for IE,it has "There is a problem with this website's security certificate." page
	public static void clickCerError(WebDriver drive)
	{
   	 	switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
		{
			case IE9:
			case IE10:
			case IE11:
				drive.get("javascript:document.getElementById('overridelink').click();");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
				break;
			case chrome:
				break;
			default:
				break;
		}
	}
	
	public static void dealWithDownload()
	{
		try
		{
			
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			Thread.sleep(2000);
			
			robot.keyPress(KeyEvent.VK_J);
			Thread.sleep(2000);
			robot.keyPress(KeyEvent.VK_SHIFT);
			Thread.sleep(2000);
			
			robot.keyPress(KeyEvent.VK_CONTROL);
			Thread.sleep(2000);
			
			robot.keyPress(KeyEvent.VK_J);
		}catch(Exception e)
		{
			
		}
		
	}
	
    //file covert method
	public static enum types
	{
		PICTURE,
		EXCEL,
		RH,
		VDS,
		CAD
	}
	
	//browser types
	public static enum browserTypes
	{
		IE9,
		IE10,
		IE11,
		chrome
	}
	
	public static enum platForm
	{
		win2012,
		win2008,
		linux
	}
	
}


