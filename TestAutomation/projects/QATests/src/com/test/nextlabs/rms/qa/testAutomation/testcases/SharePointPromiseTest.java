package com.test.nextlabs.rms.qa.testAutomation.testcases;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSUtility;
import com.test.nextlabs.rms.qa.testAutomation.webpages.SharepointOnPromise;
import com.test.nextlabs.rms.qa.testAutomation.webpages.UploadFilePage;

public class SharePointPromiseTest extends BaseTest{
	
	@BeforeMethod
	public void setUp(ITestContext result) throws InterruptedException {
		
		logger.info("[test cases name]>>>>>>>" + result.getName() + " is running");
		
		accessToSharepoint();
		
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		//get browser name
		RMSUtility.bName = cap.getBrowserName().toLowerCase();
		logger.info("[BrowserName]: " + RMSUtility.bName);
		if(RMSUtility.bName.equals("internet explorer"))
		{
			RMSUtility.bName = "IE";
		}
		
		//get browser version
		RMSUtility.bVersion = cap.getVersion().toLowerCase();
		logger.info("[BrowserVersion]: " + RMSUtility.bVersion);
		
		//get platform
		RMSUtility.bplatform = cap.getPlatform().toString();
		logger.info("[RunningPlatform]: " + RMSUtility.bplatform);
		
		RMSUtility.bFullName = RMSUtility.bName + RMSUtility.bVersion;
		
		if(RMSUtility.bName.equals("chrome"))
		{
			RMSUtility.bFullName = RMSUtility.bName;
		}
		
		Thread.sleep(3000);
	}
	
	@Parameters({"FilePath","UserName","Password","CompareSourceFile","CompareDesFile","Rights","sheetNum"})
	@Test
	public void openExcelFromSP(String filePath, String account, String password, String sourceFile,String desFile,String rights,String num)
	{
		try
		{	
			Thread.sleep(2000);
			SharepointOnPromise sp = PageFactory.initElements(driver, SharepointOnPromise.class);
			boolean result = false;
			
			
			switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
			{
				case IE9:
				case IE10:
					//result = sp.accessFolderAndViewFile(filePath,account,password,sourceFile,desFile,rights,type,errorMes);
					//break;
				case IE11:
					sp.accessFileByIE11(filePath,account,password);	
					break;
				case chrome:
					sp.accessFileByChrome(filePath,account,password);
					break;
				default:
					break;
			}
			
			Thread.sleep(2000);
			result = RMSUtility.CheckExcelFile(rights, sourceFile, desFile, num, driver);
			Assert.assertTrue(result, "open file from sharepoint failed");
			
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			logger.info("[Exception]: ", e);
		}
		
	}
	
	@Parameters({"Rights","FilePath","UserName","Password","CompareSourceFileInWin2012","CompareSourceFileInWin2008","CompareSourceFileInLinux","CompareDesFile"})
	@Test
	public void openDocFromSP(String rights,String filePath, String account, String password, String filePathForWin2012,String filePathForWin2008,String filePathForLinux,String desFile)
	{
		try
		{	
			Thread.sleep(2000);
			SharepointOnPromise sp = PageFactory.initElements(driver, SharepointOnPromise.class);
			boolean result = false;
			
			String fileSourcePath = null;
			
			switch(RMSCommon.platForm.valueOf(RMSUtility.platFrom))
			{
					case win2012:
						fileSourcePath = filePathForWin2012;
						break;
					case win2008:
						fileSourcePath = filePathForWin2008;
						break;
					case linux:
						fileSourcePath = filePathForLinux;
						break;
			}
			
			switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
			{
				case IE9:
				case IE10:
					//result = sp.accessFolderAndViewFile(filePath,account,password,sourceFile,desFile,rights,type,errorMes);
					//break;
				case IE11:
					sp.accessFileByIE11(filePath,account,password);	
					break;
				case chrome:
					sp.accessFileByChrome(filePath,account,password);
					break;
				default:
					break;
			}
			
			Thread.sleep(2000);
			
			result = RMSUtility.checkDocFile(rights, fileSourcePath, desFile, driver);
			Assert.assertTrue(result, "open doc file from sharepoint failed");
			
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			logger.info("[Exception]: ", e);
		}
		
	}
	
	@Parameters({"FilePath","UserName","Password","ErrorMes"})
	@Test
	public void openNotSupportFromSP(String filePath, String account, String password, String errorMes)
	{
		try
		{	
			Thread.sleep(2000);
			SharepointOnPromise sp = PageFactory.initElements(driver, SharepointOnPromise.class);
			boolean result = false;
			
			switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
			{
				case IE9:
				case IE10:
					//result = sp.accessFolderAndViewFile(filePath,account,password,sourceFile,desFile,rights,type,errorMes);
					//break;
				case IE11:
					sp.accessFileByIE11(filePath,account,password);	
					break;
				case chrome:
					sp.accessFileByChrome(filePath,account,password);
					break;
				default:
					break;
			}
			
			Thread.sleep(2000);
			RMSCommon.switchTowindow(driver);
			
			UploadFilePage upfile = PageFactory.initElements(driver, UploadFilePage.class);
			RMSCommon.switchTowindow(driver);
			result = upfile.checkErrorMessage(errorMes,driver);
			Assert.assertTrue(result, "open file from sharepoint failed"+errorMes);
			
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			logger.info("[Exception]: ", e);
		}
	}
}
