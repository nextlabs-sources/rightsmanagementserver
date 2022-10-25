package com.test.nextlabs.rms.qa.testAutomation.webpages;

import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;

public class ApplicationSettingPage {
	final WebDriver driver;
	public static String logs;
	
	// Constructor the header section
	public ApplicationSettingPage(WebDriver wdriver){
		this.driver = wdriver;
	}
	
	public WebDriver getDriver (){
		return driver;
	}
	
	//general setting
	@FindBy(xpath="//h5[contains(text(), 'General Settings')]")
	public static WebElement GeneralSettings;
	
	@FindBy(xpath="//label[text()='Session Timeout (mins)']/ancestor::tr//input")
	public static WebElement TimeoutInput;
	
	@FindBy(xpath="//button[contains(text(), 'Save')]")
	private static WebElement SaveBtn;
	
	//pc setting
	@FindBy(xpath="//label[text()='Enable Remote PC']/ancestor::tr//input[@value='No']")
	private static WebElement noRadio;
	
	@FindBy(xpath="//label[text()='Enable Remote PC']/ancestor::tr//input[@value='Yes']")
	private static WebElement yesRadio;

	@FindBy(xpath = "//button[contains(text(),'Home')]")
	private static WebElement homeBtn;	
	
	@FindBy(xpath = "//button[contains(text(),'Test Connection')]")
	private static WebElement testConnectionBtn;
	
	@FindBy(xpath = "//label[contains(text(),'Policy Controller Hostname')]/ancestor::tr//input")
	private static WebElement PCHostName;
	
	@FindBy(xpath = "//label[contains(text(),'RMI Port Number for Key Management')]/ancestor::tr//input")
	private static WebElement KMPort;
	
	@FindBy(xpath = "//label[contains(text(),'RMI Port Number for Policy Evaluation')]/ancestor::tr//input")
	private static WebElement PEPort;
	
	@FindBy(xpath = "//label[contains(text(),'KeyStore file')]/ancestor::tr//input")
	private static WebElement KeyStoreFilepath;
	
	@FindBy(xpath = "//label[contains(text(),'KeyStore Password')]/ancestor::tr//input")
	private static WebElement KeyStorePassword;
	
	@FindBy(xpath = "//label[contains(text(),'TrustStore File')]/ancestor::tr//input")
	private static WebElement TrustStoreFilePath;
	
	@FindBy(xpath = "//label[contains(text(),'TrustStore Password')]/ancestor::tr//input")
	private static WebElement TrustStorePassword;
	
	//email server setting
	@FindBy(xpath="//label[text()='Allow Registration Request']/ancestor::tr//input[@value='No']")
	private static WebElement noMailRadio;
	
	@FindBy(xpath="//label[contains(text(),'Allow Registration Request')]/ancestor::tr//input[@value='Yes']")
	private static WebElement yesMailRadio;
	
	@FindBy(xpath="//div[@class='panel-heading-sc']/h5[contains(text(),'Mail Server Settings')]")
	private static WebElement MailServerSettingsButton;
	
	@FindBy(xpath = "//label[contains(text(),'SMTP Host')]/ancestor::tr//input")
	private static WebElement SMTPHost;
	
	@FindBy(xpath = "//label[contains(text(),'SMTP Port')]/ancestor::tr//input")
	private static WebElement SMIPPort;
	
	@FindBy(xpath = "//label[contains(text(),'SMTP User Name')]/ancestor::tr//input")
	private static WebElement SMIPUserName;
	
	@FindBy(xpath = "//label[contains(text(),'SMTP Password')]/ancestor::tr//input")
	private static WebElement SMIPPassword;
	
	@FindBy(xpath = "//label[contains(text(),'Reenter SMTP Password')]/ancestor::tr//input")
	private static WebElement ReenterSMIPPassword;
	
	@FindBy(xpath = "//label[contains(text(),'Email Subject')]/ancestor::tr//input")
	private static WebElement EmailSubject;
	
	@FindBy(xpath = "//label[contains(text(),'Rights Management Administrator Email')]/ancestor::tr//input")
	private static WebElement RMAdminEmail;
	
	//sharepoint setting
	@FindBy(xpath="//div[@id='sharepointOnlineAppSettings']/button[contains(text(),'Add SharePoint App')]")
	private static WebElement addSharepointApp;
	
	@FindBy(xpath="//tr/td/span[@title='Remove Sharepoint App']/i[@class='glyphicon glyphicon-trash']")
	private static WebElement deleteSharepoint;
	
	@FindBy(xpath="//a/span[@title='Download SharePoint App']")
	private static WebElement downloadSharepointApp;
	
	@FindBy(xpath="//div[@class='panel-heading-sc']/h5[contains(text(),'SharePoint App Settings')]")
	private static WebElement sharepointAppButton;
	
	@FindBy(xpath="//select[@class='ember-view ember-select']")
	private WebElement sharepointSelectListBtn;
	
	@FindBy(xpath = "//label[contains(text(),'Display Name')]/ancestor::tr//input")
	private static WebElement displayName;
	
	@FindBy(xpath = "//label[contains(text(),'Remote App URL')]/ancestor::tr//input")
	private static WebElement remoteAppURL;
	
	@FindBy(xpath = "//label[contains(text(),'App Client ID')]/ancestor::tr//input")
	private static WebElement appClientID;
	
	@FindBy(xpath = "//label[contains(text(),'App Client Secret')]/ancestor::tr//input")
	private static WebElement appClientSecret;
	
	//googledrive setting
	@FindBy(xpath="//div[@class='panel-heading-sc']/h5[contains(text(),'Personal Repo Settings')]")
	private static WebElement personalRepoSetting;
	
	@FindBy(xpath="//label[text()='Allow Personal Repositories']/ancestor::tr//input[@value='Yes']")
	private static WebElement yesRadioOfPersonalDrive;
	
	@FindBy(xpath="//label[text()='Allow Personal Repositories']/ancestor::tr//input[@value='No']")
	private static WebElement NoRadioOfPersonalDrive;
	
	@FindBy(xpath="//div[@class='panel-heading-sc']/h5[contains(text(),'Google Drive Settings')]")
	private static WebElement googleDriveSetting;
	
	@FindBy(xpath = "//label[contains(text(),'Google Drive App Key')]/ancestor::tr//input")
	private static WebElement googleDriveAppKey;
	
	@FindBy(xpath = "//label[contains(text(),'Google Drive App Secret')]/ancestor::tr//input")
	private static WebElement googleDriveAppSecret;
	
	@FindBy(xpath = "//label[contains(text(),'Google Drive Redirect URL')]/ancestor::tr//input")
	private static WebElement googleDriveRedirectURL;
	
	//dropbox setting
	@FindBy(xpath="//div[@class='panel-heading-sc']/h5[contains(text(),'Dropbox Settings')]")
	private static WebElement dropboxSetting;
	
	@FindBy(xpath = "//label[contains(text(),'Dropbox App Key')]/ancestor::tr//input")
	private static WebElement dropboxAppKey;
	
	@FindBy(xpath = "//label[contains(text(),'Dropbox App Secret')]/ancestor::tr//input")
	private static WebElement dropboxAppSecret;
	
	@FindBy(xpath = "//label[contains(text(),'Dropbox Redirect URL')]/ancestor::tr//input")
	private static WebElement dropboxRedirectURL;
	
	//error message
	@FindBy(css = "div#display-success")
	private static WebElement displaySuccessMessage;
	
	@FindBy(css = "div#display-error")
	private static  WebElement displayErrorMessage;
	
	@FindBy(css = "span#errmsg")
	private static WebElement popUPErrorMessage;
	
	// Set session time
	public ApplicationSettingPage changeTimeoutSession(String time) throws Exception{
		//GeneralSettings=driver.findElement(By.name("General Settings"));
		GeneralSettings.click();
		Thread.sleep(500);
		TimeoutInput.clear();
		TimeoutInput.sendKeys(time);
		SaveBtn.click();
		return PageFactory.initElements(driver, ApplicationSettingPage.class);
	}
	
	//Policy Controller Setting--all 
	public void setPolicyController(String hostName, String kmPort, String keyStorePath, String keyStorepassword, String trustStorePath,String trusStorePassword,String pePort) {
		
		try {
			PCHostName.clear();
			PCHostName.sendKeys(hostName);
			Thread.sleep(1000);
			
			KMPort.clear();
			KMPort.sendKeys(kmPort);
			Thread.sleep(1000);
			
			KeyStoreFilepath.clear();
			KeyStoreFilepath.sendKeys(keyStorePath);
			Thread.sleep(1000);
			
			KeyStorePassword.clear();
			KeyStorePassword.sendKeys(keyStorepassword);
			Thread.sleep(1000);
			
			TrustStoreFilePath.clear();
			TrustStoreFilePath.sendKeys(trustStorePath);
			Thread.sleep(1000);
			
			TrustStorePassword.clear();
			TrustStorePassword.sendKeys(trusStorePassword);
			Thread.sleep(1000);
			
			PEPort.clear();
			PEPort.sendKeys(pePort);
			Thread.sleep(2000);
			
			testConnectionBtn.click();
			
		} catch (Exception e) {
			// TODO: handle exception
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
		}		
	}

	//just set pc name
	public void setPolicyController(String hostName) {
		try {
			
			PCHostName.clear();
			PCHostName.sendKeys(hostName);
			Thread.sleep(1000);
			
		} catch (Exception e) {
			// TODO: handle exception
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
		}

	}

	public void disableRemotePC() {
		noRadio.click();
	}
	
	public void enableRemotePC() {
		yesRadio.click();
	}
	
	//set email
	public void displayEmailServerSetting()
	{
		MailServerSettingsButton.click();
	}
	
	public void enableSetEmail()
	{
		yesMailRadio.click();
	}

	public void disableSetEmail()
	{
		noMailRadio.click();
	}
	
	public void setEmailServer(String smtphost, String smtpPort, String smtpUserName, String smtpPassword,String reenterPassword, String emailSubject, String rmsAdminEmail)
	{
		try
		{
			SMTPHost.clear();
			SMTPHost.sendKeys(smtphost);
			Thread.sleep(1000);
			BaseTest.logger.info("[SMTPHost]: " + smtphost);

			SMIPPort.clear();
			SMIPPort.sendKeys(smtpPort);
			Thread.sleep(1000);
			BaseTest.logger.info("[SMTPPort]: " + smtpPort);

			SMIPUserName.clear();
			SMIPUserName.sendKeys(smtpUserName);
			Thread.sleep(1000);
			BaseTest.logger.info("[SMTPUserName]: " + smtpUserName);

			SMIPPassword.clear();
			SMIPPassword.sendKeys(smtpPassword);
			Thread.sleep(1000);
			BaseTest.logger.info("[SMTPPassword]: " + smtpPassword);

			ReenterSMIPPassword.clear();
			ReenterSMIPPassword.sendKeys(reenterPassword);
			Thread.sleep(1000);
			BaseTest.logger.info("[ReenterSMTPPassword]: " + reenterPassword);

			EmailSubject.clear();
			EmailSubject.sendKeys(emailSubject);
			Thread.sleep(1000);
			BaseTest.logger.info("[EmailSubject]: " + emailSubject);

			RMAdminEmail.clear();
			RMAdminEmail.sendKeys(rmsAdminEmail);
			Thread.sleep(1000);
			BaseTest.logger.info("[RMSAdminEmail]: " + rmsAdminEmail);
			
			clickSaveBtn();
			Thread.sleep(3000);
		}catch(Exception e)
		{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
		}
		
	}
	
	//set sharepoint
	public void displaySharpointAppSetting()
	{
		sharepointAppButton.click();
	}
	
	public void addSharepointApp()
	{
		addSharepointApp.click();
	}
	
	public boolean deleteSharepointApp(String errorMes)
	{
		try
		{
			deleteSharepoint.click();
			Thread.sleep(2000);
			
			Alert alert = driver.switchTo().alert();
			String error = alert.getText();
			alert.accept();
			Thread.sleep(2000);
			
			boolean result = error.equals(errorMes);
			
			if(!result)
			{
				RMSCommon.logs = "The delete error message is not right, please check it!";
				BaseTest.logger.info("[Check error message]:  The delete error message is not right, please check it!");
			}
			
			return result;
		}catch(Exception e)
		{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
			return false;
		}
	}
	
	public void setSharepointApp(String sharepointAppType, String displayname, String remoteURL, String clientID, String clientSecret)
	{
		try
		{
			Select select = new Select(sharepointSelectListBtn);
			select.selectByValue(sharepointAppType);
			Thread.sleep(2000);
			
			displayName.clear();
			displayName.sendKeys(displayname);
			BaseTest.logger.info("[DisplayName]: " + displayname);
			Thread.sleep(2000);
			
			if(sharepointAppType.contains("SharePoint On-Premise"))
			{
				remoteAppURL.clear();
				remoteAppURL.sendKeys(remoteURL);
				BaseTest.logger.info("[RemoteAppURL]: " + remoteURL);
				Thread.sleep(2000);
			}
			
			appClientID.clear();
			appClientID.sendKeys(clientID);
			BaseTest.logger.info("[AppClientID]: " + clientID);
			Thread.sleep(2000);
			
			appClientSecret.clear();
			appClientSecret.sendKeys(clientSecret);
			BaseTest.logger.info("[AppClientSecret]: " + clientSecret);
			Thread.sleep(2000);
			
		}catch(Exception e)
		{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
		}
	
	}
	
	public void downloadSharepoint()
	{
		try
		{
			downloadSharepointApp.click();

		}catch(Exception e)
		{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
		}
	}
	   
    public int checkSharepointAppSize()
    {
    	
    	List<WebElement> elements = driver.findElements(By.xpath("//div[@id='sharepointOnlineAppSettings']/div/div/table[@class='setting-style-table']"));

    	return elements.size();
    } 
    
    //set google or dropbox
    public void displayPersonalSetting()
    {
    	personalRepoSetting.click();
    }

    public void enableSetPersonalDrive()
    {
    	yesRadioOfPersonalDrive.click();
    }
    
    public void disableSetPersonalDrive()
    {
    	NoRadioOfPersonalDrive.click();
    }
    
    public void displayGoogleDriveSetting()
    {
    	googleDriveSetting.click();
    }
    
    public void displayDropboxSetting()
    {
    	dropboxSetting.click();
    }
    
    public void setGoogleDrive(String appkey, String appSecret, String redirectURL)
    {
    	try
    	{
    		googleDriveAppKey.clear();
    		googleDriveAppKey.sendKeys(appkey);
    		Thread.sleep(2000);
    		
    		googleDriveAppSecret.clear();
    		googleDriveAppSecret.sendKeys(appSecret);
    		Thread.sleep(2000);
    		
    		googleDriveRedirectURL.clear();
    		googleDriveRedirectURL.sendKeys(redirectURL);
    		Thread.sleep(2000);
    		
    	}catch(Exception e)
    	{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
    	}
    }
    
    public void setDropboxDrive(String appkey, String appSecret, String redirectURL)
    {
    	try
    	{
    		dropboxAppKey.clear();
    		dropboxAppKey.sendKeys(appkey);
    		Thread.sleep(2000);
    		
    		dropboxAppSecret.clear();
    		dropboxAppSecret.sendKeys(appSecret);
    		Thread.sleep(2000);
    		
    		dropboxRedirectURL.clear();
    		dropboxRedirectURL.sendKeys(redirectURL);
    		Thread.sleep(2000);
    		
    	}catch(Exception e)
    	{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
    	}
    }
    
	public void clickSaveBtn() {
		SaveBtn.click();
	}
	
	public HomePage clickHomeBtn() {
		homeBtn.click();
		return PageFactory.initElements(driver, HomePage.class);
	}
	
	//check error message
	public boolean  checkSuccessMessage(String successMessage) {
		try
		{
			BaseTest.logger.info("[Expected result]: " + successMessage );
			BaseTest.logger.info("[Actual result]: " + displaySuccessMessage.getText() );
			if(displaySuccessMessage.getText().equals(successMessage))
			{
				return true;
			}else {
			BaseTest.logger.error("[Expected result]: " + successMessage );
				return false;
			}
			
		}catch(Exception e)
		{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
			return false;
		}

	}
	
	public boolean checkErrorMessage(String errMessage) {
		try {
			if(displayErrorMessage.getText().equals(errMessage))
			{
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
			return false;
		}

	}
	
	public boolean checkPopErrorMessage(String popErrMessage)
	{
		try
		{
			String errorMes = popUPErrorMessage.getText();
			BaseTest.logger.info("Actual errorMessage:" + errorMes);
			BaseTest.logger.info("Expected errorMessage:" +popErrMessage);
			
			if(!(errorMes.equals(popErrMessage)))
			{
				RMSCommon.logs ="[Result]: " + "Failed! Actual errormessage and Excepected errorMessage are not the same.";
				return false;
			}
			
			BaseTest.logger.info("[Result]: " + "Successfully! Actual errormessage and Excepected errorMessage are the same.");
			return true;
			
		}catch(Exception e)
		{
			RMSCommon.logs = e.getMessage();
			BaseTest.logger.info("[Exception]: ", e);
			return false;
		}
	}

}
