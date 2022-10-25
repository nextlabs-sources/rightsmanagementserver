package com.test.nextlabs.rms.qa.testAutomation.webpages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.test.nextlabs.rms.qa.testAutomation.common.*;

import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.*;

public class LoginPage {
	final WebDriver driver;
	
	// Constructor the Login Page
	public LoginPage(WebDriver driver){
		this.driver = driver;
	}
	
	/*
	*******************
	Login Screen(login)
	*******************
	*/
	
	//private static WebElement UserNameTxt;
	@FindBy(xpath = "//input[starts-with(@id, 'userName_')]")
	private static WebElement userName;
	
	@FindBy(xpath = ("//input[starts-with(@id, 'password_')]"))
	private static WebElement passWord;
	
	@FindBy(id="domainName")
	private static WebElement userDomain;
	
	@FindBy(xpath = ("//button[@type='submit']"))
	public static WebElement submit;
	
	@FindBy(xpath = "//button[@type='button']")
	private WebElement helpBtn;

	@FindBy(xpath = "//a[text()='Request an account']")
	public WebElement newAccountBtn;
	
	public WebElement newAccountButton()
	{
		return newAccountBtn;
	}
	//get login error message
	@FindBy(id="display-error")
	private WebElement loginError;
	
	//get logout message
	@FindBy(id="display-msg")
	private WebElement logoutMSG;

	// clear and send text to User Name text field
	public static void enterUsername (String username){	
		userName.clear();
		userName.sendKeys(username);
	}
	
//	// clear and send text to UPassword text field
	public static void enterPassword (String userPwd){
		passWord.clear();
		passWord.sendKeys(userPwd);
	}
	
	public static void enterDomain(String domain){
		userDomain.sendKeys(domain);
	}
	
	// click on SignIn button
	public void clickSignInBtn (){
		submit.click();
	}
	
	public boolean isRepErrLogDisplayed (){
		return loginError.isDisplayed();
	}

	public String getRepErrLog (){
		return loginError.getText();
	}
	
	public String getLogoutMSG() {
		return logoutMSG.getText();
	}
	
	public WebDriver getCurrentDriver(){
		return driver;
	}
	
	/*public NewAccountPage newAccountlinkclick(){
		newAccountLink.click();
		return PageFactory.initElements(driver, NewAccountPage.class);
	}*/
	
	// Method sign in to Home page
	public HomePage userLogIn(String userName, String userPwd, String userDomain) //throws InterruptedException
	{
		enterUsername(userName);
		enterPassword(userPwd);
	    enterDomain(userDomain);
		clickSignInBtn();
		//Thread.sleep(2000);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
		return PageFactory.initElements(driver, HomePage.class);//init the page
	}
	
	// Method sign in to Home page without domain
	public HomePage userLogInNoDomain(String userName, String userPwd, String wrongDomain) //throws InterruptedException
	{
		enterUsername(userName);
		enterPassword(userPwd);
		enterDomain(wrongDomain);
		clickSignInBtn();
		//Thread.sleep(2000);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
		return PageFactory.initElements(driver, HomePage.class);//init the page
	}
	
	// Method to login as Admin user
	public HomePage LoginAdmin()
	{
		enterUsername(RMSUtility.adminUsr);
		enterPassword(RMSUtility.adminPsd);
		enterDomain(RMSUtility.userDomain);
		clickSignInBtn();
		assertEquals("NextLabs Rights Management", driver.getTitle());
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		return PageFactory.initElements(driver, HomePage.class);
	}
	
}
