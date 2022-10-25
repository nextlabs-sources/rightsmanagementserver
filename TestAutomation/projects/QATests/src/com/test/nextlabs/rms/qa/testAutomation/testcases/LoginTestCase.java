package com.test.nextlabs.rms.qa.testAutomation.testcases;

/*******************************************************
 * @author: Michelle Xu

 * Created Date: 18/08/2015
 * This is Login case with valid users and check UI 
  *********************************************************************/

import org.openqa.selenium.Alert;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.Assert;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;

public class LoginTestCase extends BaseTest {
	public static String errorMes;
	
	public LoginTestCase() {
		//BasicConfigurator.configure();
		//DOMConfigurator.configure( "/config/log4j.xml" );
		//PropertyConfigurator.configure ("config/log4j.properties");
		super();
	}
	
	@Parameters ({"susername","spassword", "sdomain"})
	@Test 
	public void validLoginTest(@Optional("john.tyler")String usernames, @Optional("john.tyler")String passwords, @Optional("qapf1.qalab01.nextlabs.com") String domain) {		
		/***** Local variables ***************/
		try {
			logger.info("[Starting]Test validLoginTest starting");
			
			//StringBuilder errorTestResult = new StringBuilder("\n"); // this list will contain all error verifications
			boolean result = RMS_Valid_Login(usernames,passwords, domain);
			
			// Sign in to Home page
			//RMSUtility.assertFinal (errorTestResult);
			Assert.assertTrue(result, errorMes);			
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("[Exception]:", e);
		}		
	}
	
	@Parameters ({"susername","spassword", "sdomain", "serrorMessage"})
	@Test 
	public void invalidLoginUser(@Optional("User1") String usernames,
			@Optional("User1") String passwords, @Optional("qapf1.qalab01.nextlabs.com") String domain,
			@Optional("The username or password you entered is incorrect") String errorMessage
			){
		
		try {
			logger.info("[Starting]Test invalidLoginUser starting");
			
			logger.info("[Username]: " + usernames);
			logger.info("[Password]: " + passwords);
			logger.info("[Domain]: " + domain);
			
			boolean result = RMS_Invalid_Login_With_Diff_UserName_Pwd(usernames,passwords,domain, errorMessage);
			
			Assert.assertTrue(result, errorMes);

			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("[Exception]:", e);
		}
	}
	
	@Parameters ({"susername","spassword", "sdomain", "serrorMessage"})
	@Test 
	public void emptyUserNameORPassword(@Optional("") String usernames,
			@Optional("john.tyler") String passwords, @Optional("qapf1.qalab01.nextlabs.com") String domain,
			@Optional("This is a required field") String errorMessage
			){
		try {
			logger.info("[Starting]Test emptyUserNameORPassword starting");
			
			logger.info("[Username]: " + usernames);
			logger.info("[Password]: " + passwords);
			logger.info("[Domain]: " + domain);
			
			//StringBuilder errorTestResult = new StringBuilder("\n"); // this list will contain all error verifications
			
			boolean result = RMS_Invalid_Login_Without_Username_Password(usernames, passwords,domain, errorMessage);
			//RMSUtility.assertFinal (errorTestResult);
			Assert.assertTrue(result, errorMes);
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]:", e);
		}
	}
	
	@Parameters ({"susername","spassword", "sdomain", "serrorMessage"})
	@Test 
	public void invalidLoginDomain(@Optional("john.tyler") String usernames,
			@Optional("john.tyler") String passwords, @Optional("") String domain, @Optional("Please select a domain") String errorMessage
			){
		
		try {
			logger.info("[Starting]Test emptyUserNameORPassword starting");
			
			logger.info("[Username]: " + usernames);
			logger.info("[Password]: " + passwords);
			logger.info("[Domain]: " + domain);
			
			//StringBuilder errorTestResult = new StringBuilder("\n"); // this list will contain all error verifications
			
			boolean result = RMS_Invalid_Login_Without_Domain(usernames,passwords, errorMessage);
			Assert.assertTrue(result, errorMes);
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]:", e);
		}

	}
	
	/***************************************************************************
	 * This section is for common functions for this class
	 ***************************************************************************/
	// This is common method for all test cases in this class 
	// It will log in to RMS Home page and verify the log in error message 
	// base on different combinations of valid or invalid user name and password
	private boolean RMS_Invalid_Login_With_Diff_UserName_Pwd (String sUserName, String sPwd, String sDomain, String errorMess)  {
		try {

			boolean bCorrectError;
			
			// An user sign in to RMS Home page
			LoginPage logInPage = PageFactory.initElements(driver, LoginPage.class);
		    logInPage.userLogIn(sUserName, sPwd, sDomain);
			Thread.sleep(2000);
			
			bCorrectError = logInPage.getRepErrLog().equals(errorMess);	
			logger.info("[Expected errorMess]:" + errorMess);
			logger.info("[Actual errorMess]:" + logInPage.getRepErrLog());
			
			if(bCorrectError)
			{
				logger.info("[result]: invalid login,successfully!!");
				return true;
			}else {
				logger.info("[result]: invalid login,failed, errormessage is not the same!");
				errorMess = "invalid login, errormessage is not the same!";
				return false;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]:" + e);
			errorMess = e.getMessage();
			return false;
		}
	}
	
	// base on login user without domain, it will pops to another error dialog.	
	private boolean RMS_Invalid_Login_Without_Domain (String sUserName, String sPwd, String errorMess) 
	{
		try {
			//String errorDetails;
			boolean bCorrectError;//, bCorrectError;
			String wrongDomain = "select a domain";

			// An user sign in to RMS Home page
			LoginPage logInPage = PageFactory.initElements(driver, LoginPage.class);
		    logInPage.userLogInNoDomain(sUserName, sPwd, wrongDomain);
		    Thread.sleep(2000);
		    
		    Alert alert = driver.switchTo().alert();
		    String alertText = alert.getText();
		    alert.accept();
			bCorrectError = alertText.equals(errorMess);
			
			logger.info("[Expected errorMess]:" + errorMess);
			logger.info("[Actual errorMess]:" + alertText);
			
			if(bCorrectError)
			{
				logger.info("[result]:Login without domain,successfully!!");
				return true;
			}else {
				logger.info("[result]:Login without domain,failed!!, errormessage is not the same!");
				errorMess = "Login without domain,failed!!, errormessage is not the same!";
				return false;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]:" + e);
			errorMess = e.getMessage();
			return false;
		}
	}
	
	// This is invalid user without username or password
	// This needs to check with other people	 
	private boolean RMS_Invalid_Login_Without_Username_Password (String sUserName, String sPwd, String sDomain, String errorMess)  {
		try {
			//String errorDetails;
			boolean bCorrectError;//, bCorrectError;
			
			// An user sign in to RMS Home page
			LoginPage logInPage = PageFactory.initElements(driver, LoginPage.class);
			logInPage.userLogIn(sUserName, sPwd, sDomain);
		    Thread.sleep(2000);
			logger.info("[Expected errorMess]:" + errorMess);
			
			WebDriverWait wait = new WebDriverWait(driver, 2);
			wait.until(ExpectedConditions.alertIsPresent());
			//driver.navigate().refresh();
			Thread.sleep(2000);
		    Alert alert = driver.switchTo().alert();
		    //Thread.sleep(2000);
		    String alertText = alert.getText();
		    alert.accept();
			bCorrectError = alertText.equals(errorMess);		      
			logger.info("[Actual errorMess]:" + alertText);
			
			if(bCorrectError)
			{
				logger.info("[Result]:login without username or password, successfully!!");
				return true;
			}else {
				logger.info("[result]:login without username or password, failed!! errormessage is not the same!");
				errorMess = "login without username or password, failed!! errormessage is not the same!";
				return false;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: "+ e.getMessage());
			errorMes = e.getMessage();
			return false;
		}
	}
	
	// This is valid user log in and check logged in user is same
	private boolean RMS_Valid_Login(String sUserName, String sPwd, String sDomain)  {
		try {
			// An user sign in to RMS Home page
			LoginPage logInPage = PageFactory.initElements(driver, LoginPage.class);
			
			logger.info("[Username]: " + sUserName);
			logger.info("[Password]: " + sPwd);
			logger.info("[Domain]: " + sDomain);
			
		    HomePage validLogin = logInPage.userLogIn(sUserName, sPwd, sDomain);
		    Thread.sleep(3000);
		    
		    //check result
		    if(validLogin.headerCheckUsername().getText().equals(sUserName))
		    {
		    	logger.info("[Result]Login successfully!!");
		    	return true;
		    }else {
		    	logger.info("[Result]Login failed!!");
		    	errorMes = "Login Failed!!";
				return false;
			}	    
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: "+ e.getMessage());
			errorMes = e.getMessage();
			return false;
		}
	}

}
