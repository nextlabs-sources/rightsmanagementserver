package com.test.nextlabs.rms.qa.testAutomation.common;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.test.nextlabs.rms.qa.testAutomation.webpages.ExcelViewPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.PictureViewerPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.UploadFilePage;

/*******************************************************
 * @author: Michelle Xu

 * Created Date: 28/09/2015
 * This Class is used to store all utility for the testing
  *********************************************************************/

public class RMSUtility {
	public static String userDomain;
	public static String rmsServer;
	public static String sharepointURL;
	public static String rmsURL;
	public static String rmsPort;
	public static String adminUsr;
	public static String adminPsd;
	public static String testingBrowser;
	public static String screenShotFolder;
	public static String platFrom;
	public static List<String> lsUserDomains = new ArrayList<String>();
	Properties smtpServerProps = new Properties();
	
	public static String bName;//browser name
	public static String bVersion;//browser version
	public static String bplatform;//platform
	public static String bFullName;//browser full name
	
	private static final String RMS_BUILD_NUMBER = "RMS_BUILD_NUMBER";
	private static final String RMS_VERSION_NUMBER= "8.3.1";
	
	// public static final String testingBrowser = "Chrome"; //IE, Chrome, Safari
	public enum testBrowser {IE, Safari, FF, Chrome;};
	  
	public static WebDriver settingDriver(){
		WebDriver driver = null;
		switch(testBrowser.valueOf(testingBrowser)) {
		case IE:		 
			 // this line of code is to resolve protected mode issue capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);		 
			//has untrusted SSL certificate error in IE browser, must setted like this
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			//capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			String iefile = "drivers/IEDriverServer.exe";
			System.setProperty("webdriver.ie.driver", iefile);
			driver = new InternetExplorerDriver(capabilities);
			
			driver.manage().window().maximize();
			break;
		/*case Safari:  
			driver = new FirefoxDriver();
			driver.manage().window().maximize();
			break;*/
		case FF:  
			driver = new FirefoxDriver();
			break;
		case Chrome:  
			//String chromefile = "C:\\selenium dll\\chromedriver.exe";
			String chromefile = "drivers/chromedriver.exe";
			System.setProperty("webdriver.chrome.driver", chromefile); 
			ChromeOptions o = new ChromeOptions();
			o.addArguments("--start-maximized");
			o.addArguments("--disable-popup-blocking");
			o.addArguments("--disable-extensions");
			//o.addArguments("--ignore-certificate-errors");
			driver = new ChromeDriver(o);
			break;
		default:
			break;
		}
		return driver;
		}
	  
	  // This method will create a directory for keeping all screen shot for any failed test case
	  // For the test suite
	public static void createScreenShotFolder (){
		  String mainScreenShotFolder = "C:\\automation\\RMS\\Automation_Screenshots";
		  File file= new File(mainScreenShotFolder);
		  boolean exists = file.exists();
		  if (!exists) {
			  // It returns false if File or directory does not exist
			  file.mkdir(); // creating root folder	  
		  }
		  
		  // Creating sub folder for each run
		  mainScreenShotFolder = mainScreenShotFolder + "\\For_The_Run_On_" + getDateTime ();
		  file = new File (mainScreenShotFolder);
		  file.mkdir();
		  screenShotFolder = mainScreenShotFolder + "\\";
	  }


		// This method is for taking a screenshot at current page
	public static void takeScreenshot(WebDriver driver, String screenshotFileName) {

			// verify if the screenshot folder existing, if not we will create folder
			// before adding screenshot
			if (driver instanceof TakesScreenshot) {
				File tempFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
				try {
					FileUtils.copyFile(tempFile, new File(screenShotFolder + screenshotFileName + ".png"));
				} catch (IOException e) {
					e.toString();
				}
			}
		}

		// This method will get current date time and convert to a string
	public static String getDateTime (){
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			Date date = new Date();
			String currentTime = dateFormat.format(date);
			return currentTime;
		}
		
		// This method is for handling each verification error while test case running
		// and will take a screen shot of current testing page
	public static void assertSoft (boolean success, String errMessage, 
				StringBuilder errorList, WebDriver driver, String fileName){
			
			if (!success) {
				takeScreenshot (driver, fileName);
				errorList.append(errMessage + "\n");
				errorList.append(RMSCommon.logs + "\n");
			}
		}
		
	public static void assertSoft (boolean success, WebDriver driver, String fileName){
			if (!success) {
				takeScreenshot (driver, fileName);
			}
		}
		
		// This method will use to take a screen shot of current page when there is any error from error list
	public static void TakeScreenShotPerTestcase (StringBuilder errorList, WebDriver driver, String fileName){
			
		if (errorList.length() > 1)
			//errorTestResult.append(errPerPage + "\n");
			RMSUtility.takeScreenshot (driver, fileName);
		}
		
		// This method is for final assert that test case has any failed verification
		// to mark test case is failed or passed
	public static void assertFinal (StringBuilder errorList) {
			// System.out.println(errorList.toString());
			try {
				Assert.assertTrue((errorList.length () == 1), errorList.toString());
			}catch (Exception e){
				e.toString();
			}
		}	

	public static String[] getEmails(String email)
	{
		String[] emails = null;
		
		if(email!=null)
		{
			emails = email.split(";");
		}
		return emails;
	}

	//check opened file content
	public static boolean checkFileType(String sourceFile,String desFile,String rights, String type,String num,String errorMes, WebDriver driver)
	{
		boolean result =false;
		
		if(rights.equals("Allow_View"))
		{
			switch(RMSCommon.types.valueOf(type))
			{
				case PICTURE:
					result = checkDocFile(rights,sourceFile,desFile,driver);
					break;
				case EXCEL:
					result = CheckExcelFile(rights,sourceFile,desFile,num,driver);
				default:
					break;
			}
		}else if (rights.equals("Deny_View"))
		{
			UploadFilePage upfile = PageFactory.initElements(driver, UploadFilePage.class);
			result = upfile.checkErrorMessage(errorMes,driver);
		}

		return result;
	}
	
	//check opened 2D file content
	public static boolean checkDocFile(String rights, String sourceFile, String desFile,WebDriver driver)
	{
		try
		{
			
		    boolean compareResult = false;
			PictureViewerPage page = PageFactory.initElements(driver, PictureViewerPage.class);
			
			//check redirect to correct page
			boolean isRedirect = RMSCommon.isDOCViewer(driver, rights);
			
			RMSCommon.makeDirs(desFile);
			
			//go to other pages, and get its content
			boolean pageResult = page.checkPageNum(desFile);
			
			//initial sourcefile, and desfile
			File file = new File(sourceFile);
			if(!(file.exists()))
			{
				file.createNewFile();
			}
			BaseTest.logger.info("[result]:open file, switch to file successfully!");
			
			//compare file content with original file
			compareResult = RMSCommon.compareFiles(sourceFile, desFile);
			
			return compareResult && pageResult && isRedirect;
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
			return false;
		}
	}
	
	//chekc opened excel file content
	public static boolean CheckExcelFile(String rights, String sourceFile, String desFile, String num, WebDriver driver)
	{
		try
		{
			ExcelViewPage page = PageFactory.initElements(driver, ExcelViewPage.class);
			
			//check redirect url
			boolean checkExcel = RMSCommon.isExcelViewer(driver, rights);
			if(!checkExcel)
			{
				BaseTest.logger.info("open excel file from repository, but it redirect to wrong url");
			}
			
			//check sheets number
			boolean checkSheets = page.checkSheesNum(Integer.parseInt(num));
			
			//get temp file path
			String fileURL = driver.getCurrentUrl();
			String fileTempPath = fileURL.substring(fileURL.indexOf("temp"), fileURL.length());

			String[] tempFolder = RMSUtility.rmsServer.split("\\.");
			String server = "\\\\"+tempFolder[0];
			
			String[] folders = fileTempPath.split("/");
			for(int i=0;i<folders.length;i++)
			{
				if(folders[i].contains("%20"))
				{
					folders[i] = folders[i].replace("%20", " ");
				}
				server = server + "\\" + folders[i];
			}
			
			Thread.sleep(2000);	
			//System.out.println(server);
			
			//get temp file content
			RMSCommon.getFileContent(server, sourceFile);
			
			Thread.sleep(3000);
			
			//compare temp file
			File source = new File(sourceFile);
			File des = new File(desFile);
			String soureFileMD5 = RMSCommon.getFileMD5(source);
			String desFileMD5 = RMSCommon.getFileMD5(des);
			BaseTest.logger.info("The source file md5 is : " + soureFileMD5);
			BaseTest.logger.info("The des file md5 is : " + desFileMD5);
			
			boolean checkFileContent = soureFileMD5.equals(desFileMD5);
			if(!checkFileContent)
			{
				BaseTest.logger.info("Excel file conten is changed");
			}
			
			return checkFileContent&&checkSheets&&checkExcel;		
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
			return false;
		}
	}
	
	public static String getRMSVersionNumber(){
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ResourceBundle bundle = ResourceBundle.getBundle("com.nextlabs.rms.qa.config.RMSVersion",
				Locale.getDefault(), cl);
		String buildNumber = bundle.getString(RMS_BUILD_NUMBER);
		String versionNumWithBuild = RMS_VERSION_NUMBER+" ( Build : "+buildNumber+" )";
		return versionNumWithBuild;
	}
}
