package com.test.nextlabs.rms.qa.testAutomation.webpages;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;

import autoitx4java.AutoItX;

public class SharepointOnPromise {
	
	public WebDriver driver;
	public SharepointOnPromise(){}
	
	public SharepointOnPromise( WebDriver driver)
	{
		this.driver = driver;
	}
	
	// access sharepoint folder
	private void accessFile(String filePath,String userName, String password)
	{
		try
		{
			
			String[] folders = filePath.split("/");
			int fileLength = folders.length;
			
			//access to folder
			driver.findElement(By.xpath("//ul[@class='root ms-core-listMenu-root static']/li/a/span/span[text()='"+ folders[0] +"']")).click();
			Thread.sleep(2000);
			if(fileLength>2)
			{
				for(int i=1; i<fileLength-1; i++)
				{
					Thread.sleep(2000);
					driver.findElement(By.xpath("//table/tbody/tr/td[3]/div/a[text()='"+ folders[i] +"']")).click();
					BaseTest.logger.info("click folder " + folders[i]);
					Thread.sleep(2000);
					driver.findElement(By.xpath("//h1[@id='pageTitle']/span/span/span/a[text()='"+ folders[i] +"']")).click();
				}
			}
			
			String filename = folders[fileLength-1];
		    driver.findElement(By.xpath("//table/tbody/tr/td[3]/div/a[text()='"+filename+"']/ancestor::tr/td[4]/div")).click();
			Thread.sleep(2000);
			
			//try to open file according view in SC
			driver.findElement(By.xpath("//table/tbody/tr/td[3]/div/a[text()='"+filename+"']/ancestor::tr/td[4]/div/div/div/div/span/span[@class='js-callout-ecbMenu']/a[@title='Open Menu']")).click();
			Thread.sleep(2000);
			
			driver.findElement(By.xpath("//span[text()='View in Secure Collaboration']")).click();
			
			Thread.sleep(2000);
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
	}
	
	
	// open file from sharepoint in IE9,10
/*	public boolean accessFolderAndViewFile(String filePath,String userName, String password,String sourceFile,String desFile,String rights)
	{
		try
		{
			String[] folders = filePath.split("/");
			int fileLength = folders.length;
			String filename = folders[fileLength-1];

			accessFile(filePath,userName,password);		
			
			handleShowContentButton();
			
			Thread.sleep(4000);
			//swith to the new window,need to get window handle
			String currentWindow = driver.getWindowHandle();
			driver.switchTo().window(currentWindow);
			
			PageFactory.initElements(driver, SharepointOnPromise.class);
			driver.findElement(By.xpath("//table/tbody/tr/td[3]/div/a[text()='"+filename+"']/ancestor::tr/td[4]/div")).click();
			
			driver.findElement(By.xpath("//table/tbody/tr/td[3]/div/a[text()='"+filename+"']/ancestor::tr/td[4]/div/div/div/div/span/span[@class='js-callout-ecbMenu']/a[@title='Open Menu']")).click();
			Thread.sleep(2000);
			
			driver.findElement(By.xpath("//span[text()='View in Secure Collaboration']")).click();
			
			handleWindowsSecurity(userName, password);

			//return checkFileType(sourceFile,desFile,rights,type,errorMes);
			//return RMSUtility.checkFileType(sourceFile, desFile, rights, type, errorMes, driver);
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
			return false;
		}
	}*/

	// open file from sharepoint in IE11
	public void accessFileByIE11(String filePath,String userName, String password)
	{
		try
		{
			accessFile(filePath,userName,password);
			Thread.sleep(2000);
			
			handleWindowsSecurity(userName, password);
			Thread.sleep(3000);
			
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);				
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
	}

	// open file from sharepoint in 
	public void accessFileByChrome(String filePath,String userName, String password)
	{
		try
		{
			accessFile(filePath,userName,password);
			
			handleAuthentication(userName,password);
			
			Thread.sleep(3000);
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
	}
	
	// handle show content dialog in IE9,10, it used before
	private void handleShowContentButton()
	{
		try
		{
			Thread.sleep(2000);
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
			int X = x.controlGetPosWidth(winTitle, "","[Class:DirectUIHWND]")-135;
			int Y = x.controlGetPosHeight(winTitle, "","[Class:DirectUIHWND]")-25;
			
			x.controlFocus(winTitle, "Internet Explorer blocked this website from displaying content with security certificate errors", "[Class:DirectUIHWND]");
			x.winActivate(winTitle, "Internet Explorer blocked this website from displaying content with security certificate errors");
			x.controlFocus(winTitle, "Internet Explorer blocked this website from displaying content with security certificate errors", "[Class:DirectUIHWND]");
			x.winActivate(conTitle);
			
			Thread.sleep(2000);
			x.controlClick(winTitle, "", "[Class:DirectUIHWND]", "left", 2, X, Y);
			x.controlSend(winTitle, "", "[Class:DirectUIHWND]","{Enter}");
			
			Thread.sleep(3000);
			//x.mouseClick("left", 1060, 733, 1, 50);

			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
	}
	
	// handle windows security dialog IE9,10,11
	private void handleWindowsSecurity(String userName, String password)
	{
		try
		{
			Thread.sleep(3000);
			AutoItX x = new AutoItX();
			//get a handle of windows security dialog
			String winHandle = x.winGetHandle("Windows Security", "");
			
			//get title of the dialog
			String winTitle = "[HANDLE:" + winHandle + "]";
			System.out.println("winTitle" + winTitle);

			x.winActivate(winTitle);
			x.ControlSetText(winTitle, "", "Edit1", userName);
			Thread.sleep(2000);
			x.ControlSetText(winTitle, "", "Edit2", password);
			Thread.sleep(2000);
			x.controlClick(winTitle, "", "Button2");
			x.controlSend(winTitle, "", "Button2", "{Enter}");
			Thread.sleep(6000);
			
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
	}

	// handle Authentication required dialog in Chrome
	private void handleAuthentication(String userName, String password)
	{
		try{

			Thread.sleep(3000);
			RMSCommon.robot = new Robot();

			//type username
			RMSCommon.type(userName);
			Thread.sleep(2000);

			//click tab in keyboard
			RMSCommon.robot.keyPress(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			
			//type password
			RMSCommon.type(password);
			Thread.sleep(2000);

			//click enter in keyboard
			RMSCommon.robot.keyPress(KeyEvent.VK_ENTER);
			Thread.sleep(6000);

		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			RMSCommon.logs = RMSCommon.logs + e.getMessage();
		}
	}
	
}
