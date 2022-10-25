package com.test.nextlabs.rms.qa.testAutomation.webpages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;

/*******************************************************
 * @author: Michelle Xu

 * Created Date: 15/09/2015
 * This is UploadFile case with different file formats
  *********************************************************************/

public class UploadFilePage {

	WebDriver driver;
	
	// Constructor the UploadFile Page
	public UploadFilePage(WebDriver driver){
		this.driver = driver;
	}
	
	/*
	*******************
	UploadFile
	*******************
	*/
	
	//private static WebElement uploadDescription;
	@FindBy(xpath = "//label[contains(text(), 'Click the button below to upload a file for viewing in Rights Management.')]")
	private static WebElement uploadDescription;
	
	//private static WebElement note;
	@FindBy(xpath = "//label[contains(text(), 'Note: The file will not be uploaded to any repository. It will only be uploaded to the Rights Management Server temporarily for viewing.')]")
	private static WebElement note;
	
	//private static WebElement uploadFile;
	@FindBy(xpath = "//span[contains(text(), 'Upload & View')]")
	private static WebElement uploadFile;
    
	//private static WebElememnt upload error message
	@FindBy(id="display-error")
	private static WebElement uploadError;
	
	//This method is used to upload files
	public UploadFilePage uploadFile(String fileName)throws Exception
	{	
		//String currentWindow = driver.getWindowHandle();//get handler for current window
		WebElement fileUpload = driver.findElement(By.id("fileupload"));
		fileUpload.sendKeys(fileName);

		uploadFile.click();
		//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
		Thread.sleep(10000);
		RMSCommon.switchTowindow(driver);
		
		driver.manage().window().maximize();
		
		Thread.sleep(2000);
		return PageFactory.initElements(driver, UploadFilePage.class);
	}
	
	public boolean checkFileUpload(String fileName,String errorMes)
	{	
		try {
			//String currentWindow = driver.getWindowHandle();//get handler for current window
			WebElement fileUpload = driver.findElement(By.id("fileupload"));
			fileUpload.sendKeys(fileName);

			uploadFile.click();
			//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
			Thread.sleep(10000);
			RMSCommon.switchTowindow(driver);
			
			String message = getUploadErrorLog();
			BaseTest.logger.info("[Expected error message]: " + errorMes);
			BaseTest.logger.info("[Actual error message]: " + message);
			
			if(message.equals(errorMes))
			{
				BaseTest.logger.info("[Result]: error message is the same, successfully!");
				return true;
			}else {
				BaseTest.logger.info("[Result]: error message is not the same, failed!");
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: no the error message!");
			return false;
		}
	}
	
	public boolean checkErrorMessage(String errorMes,WebDriver drive)
	{
		try {

/*			RMSCommon.switchTowindow(drive);
			RMSCommon.clickCerError(drive);*/
		
			String message = getUploadErrorLog();
			BaseTest.logger.info("[Expected error message]: " + errorMes);
			BaseTest.logger.info("[Actual error message]: " + message);
			
			if(message.equals(errorMes))
			{
				BaseTest.logger.info("[Result]: error message is the same, successfully!");
				System.out.println("[Result]: error message is the same, successfully!");
				return true;
			}else {
				BaseTest.logger.info("[Result]: error message is not the same, failed!");
				System.out.println("[Result]: error message is not the same, failed!");
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: no the error message!" + e.getMessage());
			return false;
		}
	}

	//print opened file in IE
	public boolean iEPrintFile(String printType, WebDriver drive,PictureViewerPage p)
	{
		
		boolean checkResult = false;
		try
		{
			
			boolean checkProcess = RMSCommon.checkApplication("AcroRd32.exe");
			
			if(checkProcess)
			{
				RMSCommon.deleteProcess("AcroRd32.exe");
			}
			
			checkProcess = RMSCommon.checkApplication("AcroRd32.exe");
			BaseTest.logger.info("[Print acrord32 process]: if true, it means has AcroRd32.exe process---" + checkProcess);
			
			
			if(printType.equalsIgnoreCase("Deny_Print"))
			{
				BaseTest.logger.info("[Expected Result]: Deny_print");
				p.clickPrint();
				checkProcess = RMSCommon.checkApplication("AcroRd32.exe");
				if(!checkProcess)
				{
					checkResult = true;
				}
			}
			else
			{
				BaseTest.logger.info("[Expected Result]: Allow_print");
				Thread.sleep(3000);
		       	p.clickPrint();
		       	Thread.sleep(3000);
		       	
				if(!checkProcess)
				{
					BaseTest.logger.info("splwow64.exe is not exist: " + checkProcess);					
					checkProcess = RMSCommon.checkApplication("AcroRd32.exe");
					if(checkProcess)
					{
						BaseTest.logger.info("[Print Window]: Print Window is displayed");
						BaseTest.logger.info("[Actual Result]: Allow_print");
						checkResult = true;
					}
				}
			}
			Runtime.getRuntime().exec("taskkill /F /IM AcroRd32.exe");
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
		}
		 return checkResult;

	}

	//print opened file in chrome
	public boolean chromePrintFile(String printType, WebDriver drive)
	{
		boolean checkResult = false;
		try
		{
	        PictureViewerPage p = PageFactory.initElements(driver, PictureViewerPage.class);
	   		 
	        boolean checkProcess = RMSCommon.checkApplication("splwow64.exe");
			if(checkProcess)
			{
				Runtime.getRuntime().exec("taskkill /F /IM splwow64.exe");
			}
			
			checkProcess = RMSCommon.checkApplication("splwow64.exe");
	        
	        if(printType.equalsIgnoreCase("Deny_Print"))
	        {
	        	BaseTest.logger.info("[Expected Result]: Deny_print");
				boolean checkDisablePrint = p.clickDisablePrint();
				if(checkDisablePrint)
				{
					checkResult = true;
				}
	        }
	        else
	        {  	
		       	BaseTest.logger.info("[Expected Result]: Allow_print");
				if(!checkProcess)
				{
					Thread.sleep(2000);
					BaseTest.logger.info("splwow64.exe is not exist:  " + checkProcess);
					p.clickPrint();
					Thread.sleep(6000);
					
					checkProcess = RMSCommon.checkApplication("splwow64.exe");
					if(checkProcess)
					{
						BaseTest.logger.info("[Actual Result]: Allow_print");
						checkResult = true;
					}
				}
	        }
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ",e);
		}
		return checkResult;
	}
	
	//This method is return error details from page
	public static String getUploadErrorLog (){
		return uploadError.getText();
	}

}
