package com.test.nextlabs.rms.qa.testAutomation.testcases;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSUtility;
import com.test.nextlabs.rms.qa.testAutomation.webpages.ApplicationSettingPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.UploadFilePage;

public class SettingTest extends BaseTest{
	
	@BeforeMethod(alwaysRun = true)
	public void setUp(ITestContext result) throws InterruptedException
	{
		logger.info("[test cases name]>>>>>>>" + result.getName() + " is running");
		setupBeforeMethod();
		LoginPage adminLogIn = PageFactory.initElements(driver, LoginPage.class);
		adminLogIn.LoginAdmin();
		
		HomePage settingPage = PageFactory.initElements(driver, HomePage.class);
		settingPage.accessRMSsettingPage();
	}
	
	@Test
	@Parameters({"Time","ErrorMes"})
	public void RMS_User_Session_timeout_After_Specific_minutes(@Optional("1")String time, String error)
	{
		try{
			/***** Local variables ***************/
			logger.info("[Starting]Test RMS_User_Session_timeout_After_Specific_minutes starting");
			StringBuilder errorTestResult = new StringBuilder("\n"); // this list will contain all error verifications	
			String errorDetails;
			int millseconds = Integer.parseInt(time)*60000 + 1000;
			
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			manageSettingPage.changeTimeoutSession(time);
			ApplicationSettingPage.GeneralSettings.click();
			
			// Verify if session timeout is saved.
			boolean flag = RMSCommon.isTextPresent(driver, time);
			
			// Case 1: Verifying that session timeout is added correctly
		    errorDetails = "Fail1--Session Timeout should be '" + time + "mins" +" ==> But actual time is '" + ApplicationSettingPage.TimeoutInput.getText();
		    RMSUtility.assertSoft (flag, errorDetails, errorTestResult,manageSettingPage.getDriver(), 
					this.getClass().getName() + "_" + RMSUtility.getDateTime());
			
		    HomePage homePage =  manageSettingPage.clickHomeBtn();
			// Let test case sleep for specific minutes and 1 second
			Thread.sleep(millseconds*2);
			//HomePage.NextLabslink.click();
			homePage.uploadFile.click();
			Thread.sleep(2000);
			
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			LoginPage adminLogIn = PageFactory.initElements(driver, LoginPage.class);
			
			//check if do not log out in that time
			String logoutMSG = adminLogIn.getLogoutMSG();
			
			boolean result = false;
			
			if(logoutMSG.equals(error))
			{
				result = true;
			}
			
			Assert.assertTrue(result, "Do not logout after time out");
		}
		catch(Exception e){
			e.printStackTrace();
			BaseTest.logger.info("[Exception]: ", e);
		}
	}	
	
	//set pc
	@Test(alwaysRun = true)
	@Parameters({"FileName"})
	public void disablePC(String filePath)
	{
		try {
			logger.info("[Starting]: >>>>>>Test disablePC starting");
			
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			
			//disable remote pc
			manageSettingPage.disableRemotePC();
			Thread.sleep(3000);
			
			//click to save setting
			manageSettingPage.clickSaveBtn();
			Thread.sleep(3000);
			
			//return to homePage to check
			manageSettingPage.clickHomeBtn();
			Thread.sleep(3000);
			
			//upload and view
			HomePage homePage = PageFactory.initElements(driver, HomePage.class);
			homePage.clickUploadAndView();
			Thread.sleep(3000);
			
			UploadFilePage uploadFilePage = PageFactory.initElements(driver, UploadFilePage.class);
			uploadFilePage.uploadFile(filePath);
			Thread.sleep(3000);
			
			RMSCommon.switchTowindow(driver);
			
			boolean result = false;
			if (driver.getCurrentUrl().contains("/ShowError.jsp") && UploadFilePage.getUploadErrorLog().contains("Error occurred while processing the file."))
			{
				result = true;
			}
			
			Assert.assertTrue(result, "Check file if can be open or the errorLog if right!");
			
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: ", e);
		}
	}
	
	@Test(alwaysRun = true)
	@Parameters({"PCHostName","KMPort", "KeyStorePath", "KeyStorePassowrd", "TrustStorePath", "TrustStorePassword", "PEPort","ErrMessage"})
	public void setPolicyContorller(String hostName, String kmPort, String keyStorePath, String keyStorepassword, String trustStorePath, String trusStorePassword, String pePort, String errMessage)
	{
		try {
			logger.info("[Starting]: >>>>>>Test setPolicyContorller starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			
			manageSettingPage.enableRemotePC();
			Thread.sleep(3000);
			
			manageSettingPage.setPolicyController(hostName, kmPort,  keyStorePath, keyStorepassword, trustStorePath, trusStorePassword,pePort);
			Thread.sleep(3000);
			
			boolean result = manageSettingPage.checkSuccessMessage(errMessage);
			
			Assert.assertTrue(result, manageSettingPage.logs);
			
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: ", e);
		}
	}
	
	@Test(alwaysRun = true)
	@Parameters({"PCHostName","KMPort", "KeyStorePath", "KeyStorePassowrd", "TrustStorePath", "TrustStorePassword", "PEPort","ErrMessage"})
	public void setWrongPolicyController(String hostName, String kmPort, String keyStorePath, String keyStorepassword, String trustStorePath, String trusStorePassword, String pePort, String errMessage)
	{
		try {
			logger.info("[Starting]: >>>>>>Test setWrongPolicyController starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			
			manageSettingPage.enableRemotePC();
			Thread.sleep(3000);
			
			manageSettingPage.setPolicyController(hostName, kmPort,  keyStorePath, keyStorepassword, trustStorePath, trusStorePassword,pePort);
			Thread.sleep(3000);
			
			boolean result = manageSettingPage.checkErrorMessage(errMessage);
			
			Assert.assertTrue(result, manageSettingPage.logs);
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: ", e);
		}
	}
	
	@Test(alwaysRun = true)
	@Parameters({"PCHostName","KMPort", "KeyStorePath", "KeyStorePassowrd", "TrustStorePath", "TrustStorePassword", "PEPort","ErrMessage"})
	public void setPCWithWrongPort(String hostName, String kmPort, String keyStorePath, String keyStorepassword, String trustStorePath, String trusStorePassword, String pePort, String errMessage)
	{
		try {
			logger.info("[Starting]: >>>>>>Test setPCWithWrongPort starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			
			manageSettingPage.enableRemotePC();
			Thread.sleep(3000);
			
			manageSettingPage.setPolicyController(hostName, kmPort,  keyStorePath, keyStorepassword, trustStorePath, trusStorePassword,pePort);
			Thread.sleep(3000);
			
			boolean result = manageSettingPage.checkPopErrorMessage(errMessage);
			
			Assert.assertTrue(result, RMSCommon.logs);
			
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: ", e);
		}
	}

	//set email
	@Test
	@Parameters({"SMTPHost","SMTPPort", "SMTPUserName", "SMTPPassord", "ReenterSMTPPassord", "EmailSubject", "RMSAdminEmail","errorMes"})
	public void setMailServer(String smtphost, String smtpPort, String smtpUserName, String smtpPassword,String reenterPassword, String emailSubject, String rmsAdminEmail,String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setMailServer starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			logger.info("display email server setting");
			manageSettingPage.displayEmailServerSetting();
			Thread.sleep(2000);
			
			logger.info("enable set email");
			manageSettingPage.enableSetEmail();
			Thread.sleep(2000);
			
			logger.info("set email begining");
			manageSettingPage.setEmailServer(smtphost, smtpPort, smtpUserName, smtpPassword, reenterPassword, emailSubject, rmsAdminEmail);
		
			boolean result = manageSettingPage.checkSuccessMessage(errorMes);
			
			Assert.assertTrue(result, "failed!");
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
		}
	}

	@Test
	@Parameters({"SMTPHost","SMTPPort", "SMTPUserName", "SMTPPassord", "ReenterSMTPPassord", "EmailSubject", "RMSAdminEmail","errorMes"})
	public void setMailServerWithError(String smtphost, String smtpPort, String smtpUserName, String smtpPassword,String reenterPassword, String emailSubject, String rmsAdminEmail,String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setMailServerWithError starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			logger.info("display email server setting");
			manageSettingPage.displayEmailServerSetting();
			Thread.sleep(2000);
			
			logger.info("enable set email");
			manageSettingPage.enableSetEmail();
			Thread.sleep(2000);
			
			logger.info("set email begining");
			manageSettingPage.setEmailServer(smtphost, smtpPort, smtpUserName, smtpPassword, reenterPassword, emailSubject, rmsAdminEmail);
		
			boolean result = manageSettingPage.checkPopErrorMessage(errorMes);
			
			Assert.assertTrue(result, "failed!" + RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}

	//set sharepoint
	@Test
	@Parameters({"sharepointAppType","DisplayName", "RemoteAppURL", "AppClientID", "AppClientSecret","errorMes"})
	public void setShareppointApp(String sharepointAppType, String displayname, String remoteURL, String clientID,String clientSecret,String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setShareppointApp starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displaySharpointAppSetting();
			Thread.sleep(2000);
			
			manageSettingPage.addSharepointApp();
			Thread.sleep(2000);
			
			manageSettingPage.setSharepointApp(sharepointAppType, displayname, remoteURL, clientID, clientSecret);
			
			manageSettingPage.clickSaveBtn();
			
			boolean result = manageSettingPage.checkSuccessMessage(errorMes);
			
			Assert.assertTrue(result, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}

	@Test
	@Parameters({"errorMes"})
	public void deleteSharepointApp(String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test deleteSharepointApp starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displaySharpointAppSetting();
			Thread.sleep(2000);
			
			boolean result2 = manageSettingPage.deleteSharepointApp(errorMes);
			
			int size = manageSettingPage.checkSharepointAppSize();
			
			boolean result3 = false;
			
			if(size==0)
			{
				result3 = true;
			}else
			{
				RMSCommon.logs = "sharepoint app size is " + size + ", This is not right" + RMSCommon.logs;
			}
			
			Assert.assertTrue(result2&result3, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}

	@Test
	@Parameters({"sharepointAppType","DisplayName", "RemoteAppURL", "AppClientID", "AppClientSecret","errorMes"})
	public void setErrorSharePointApp(String sharepointAppType, String displayname, String remoteURL, String clientID,String clientSecret,String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setErrorSharePointApp starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displaySharpointAppSetting();
			Thread.sleep(2000);
			
			manageSettingPage.addSharepointApp();
			Thread.sleep(2000);
			
			manageSettingPage.setSharepointApp(sharepointAppType, displayname, remoteURL, clientID, clientSecret);
			
			manageSettingPage.clickSaveBtn();
			Thread.sleep(2000);
			
			boolean result = manageSettingPage.checkPopErrorMessage(errorMes);
			Assert.assertTrue(result, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}

	//set google drive
	@Test
	@Parameters({"AppKey","AppSecret","RedirectURL","errorMes"})
	public void setGoogleDrive(String appkey, String appSecret, String redirectURL, String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setGoogleDrive starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displayPersonalSetting();
			Thread.sleep(2000);
			
			manageSettingPage.enableSetPersonalDrive();
			Thread.sleep(2000);
			
			manageSettingPage.displayGoogleDriveSetting();
			Thread.sleep(2000);
			
			manageSettingPage.setGoogleDrive(appkey, appSecret, redirectURL);
			
			manageSettingPage.clickSaveBtn();
			Thread.sleep(2000);
			
			boolean result = manageSettingPage.checkSuccessMessage(errorMes);
			Assert.assertTrue(result, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}

	@Test
	@Parameters({"AppKey","AppSecret","RedirectURL","errorMes"})
	public void setDropboxDrive(String appkey, String appSecret, String redirectURL, String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setDropboxDrive starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displayPersonalSetting();
			Thread.sleep(2000);
			
			manageSettingPage.enableSetPersonalDrive();
			Thread.sleep(2000);
						
			manageSettingPage.displayDropboxSetting();
			Thread.sleep(2000);
			
			manageSettingPage.setDropboxDrive(appkey, appSecret, redirectURL);
			
			manageSettingPage.clickSaveBtn();
			Thread.sleep(2000);
			
			boolean result = manageSettingPage.checkSuccessMessage(errorMes);
			Assert.assertTrue(result, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}

	@Test
	@Parameters({"AppKey","AppSecret","RedirectURL","errorMes"})
	public void setErrorGoogleDrive(String appkey, String appSecret, String redirectURL, String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setErrorGoogleDrive starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displayPersonalSetting();
			Thread.sleep(2000);
			
			manageSettingPage.enableSetPersonalDrive();
			Thread.sleep(2000);
			
			manageSettingPage.displayGoogleDriveSetting();
			Thread.sleep(2000);
			
			manageSettingPage.setGoogleDrive(appkey, appSecret, redirectURL);
			
			manageSettingPage.clickSaveBtn();
			Thread.sleep(2000);
			
			boolean result = manageSettingPage.checkPopErrorMessage(errorMes);
			Assert.assertTrue(result, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}
	
	@Test
	@Parameters({"AppKey","AppSecret","RedirectURL","errorMes"})
	public void setErrorDropboxDrive(String appkey, String appSecret, String redirectURL, String errorMes)
	{
		try
		{
			logger.info("[Starting]: >>>>>>Test setErrorDropboxDrive starting");
			ApplicationSettingPage manageSettingPage = PageFactory.initElements(driver, ApplicationSettingPage.class);
			Thread.sleep(2000);
			
			manageSettingPage.displayPersonalSetting();
			Thread.sleep(2000);
			
			manageSettingPage.enableSetPersonalDrive();
			Thread.sleep(2000);
						
			manageSettingPage.displayDropboxSetting();
			Thread.sleep(2000);
			
			manageSettingPage.setDropboxDrive(appkey, appSecret, redirectURL);
			
			manageSettingPage.clickSaveBtn();
			Thread.sleep(2000);
			
			boolean result = manageSettingPage.checkPopErrorMessage(errorMes);
			Assert.assertTrue(result, "Failed! "+ RMSCommon.logs);
			
		}catch(Exception e)
		{
			logger.info("[Exception]: ", e);
		}
	}
	
}
