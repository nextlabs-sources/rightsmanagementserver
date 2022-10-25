package com.test.nextlabs.rms.qa.testAutomation.testcases;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSUtility;
import com.test.nextlabs.rms.qa.testAutomation.webpages.DownloadRMCPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;

public class DownloadRightsManagementClient extends BaseTest{
	
	@Test
	@Parameters({"userName","password","domain","cer"})
	public void downloadRMC(String userName, String password, String domain, String cers) throws Exception {
		
		//login first
		LoginPage login = PageFactory.initElements(driver, LoginPage.class);
		login.userLogIn(userName,password,domain);
		
		//after access home page, then go to RMSDownLoadPage
		HomePage home = PageFactory.initElements(driver, HomePage.class);
		home.accessRMSDownloadPage();
		
		DownloadRMCPage downloadPage = PageFactory.initElements(driver, DownloadRMCPage.class);
		Thread.sleep(1000);
		
		//before download package, clear download folder
		String source = "c:\\Users\\"+System.getProperty("user.name").toLowerCase()+"\\Downloads";
		String directory = source +"\\RightManagementClientPackage";
		
		File file = new File(source);
		FileUtils.cleanDirectory(file);
		
		//click download button
		downloadPage.clickDownloadButton();
		Thread.sleep(3000);
		
		switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
		{
			case IE9:
			case IE10:
			case IE11:
				RMSCommon.dealWithDownload();
				break;
			case chrome:
				break;
			default:
				break;
		}
		
		File[] files = file.listFiles();
		System.out.println(files[0].getName());
		source = source + "\\" + files[0].getName();
		
		Assert.assertTrue(RMSCommon.isFileDownload(directory,source,cers), "Fail to download RMC package");
	}
	
	@Test(invocationCount=1)
	@Parameters({"userName","password","domain"})
	public void checkHomeButton(String userName, String password, String domain) throws InterruptedException {
		StringBuilder errorTestResult = new StringBuilder("\n");
		String errorDetails;
		
		LoginPage login = PageFactory.initElements(driver, LoginPage.class);
		login.userLogIn(userName, password, domain);
		
		HomePage home = PageFactory.initElements(driver, HomePage.class);
		home.accessRMSDownloadPage();
		
		DownloadRMCPage downloadPage = PageFactory.initElements(driver, DownloadRMCPage.class);
		downloadPage.clickHomeButton();
		Thread.sleep(2000);
		
		errorDetails = "Faild to go to HomePage,please check!";
		home = PageFactory.initElements(driver, HomePage.class);
		
		//judge if return to homePage
		RMSUtility.assertSoft (home.headerCheckUsername().getText().equals(userName), errorDetails, 
				errorTestResult,home.getDriver(), 
				this.getClass().getName() + "_" + RMSUtility.getDateTime());
	}
}
