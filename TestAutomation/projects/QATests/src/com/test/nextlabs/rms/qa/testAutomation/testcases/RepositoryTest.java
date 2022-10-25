package com.test.nextlabs.rms.qa.testAutomation.testcases;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.ManageRepository;

public class RepositoryTest extends BaseTest {
	public ManageRepository managePage;
	public static String errorMes;
	
	@BeforeMethod(alwaysRun = true)
	public void setUp(ITestContext result)
	{
		try {
			logger.info("[test cases name]>>>>>>>" + result.getName() + " is running");
			setupBeforeMethod();
			LoginPage adminLogIn = PageFactory.initElements(driver, LoginPage.class);
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			adminLogIn.LoginAdmin();
			HomePage repoListPage = PageFactory.initElements(driver, HomePage.class);
			repoListPage.accessRepoAdminPage();
			managePage = PageFactory.initElements(driver, ManageRepository.class);
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: ", e);
		}
	}
	
	@Test(alwaysRun = true)
	@Parameters({"SPName","SPURL"})
	public void addSP(String Reponame, String SPurl)
	{
		try{
			/***** Local variables ***************/
			logger.info("[Starting]: Test addSP starting");
			logger.info("[Repository Display Name]: " + Reponame);
			logger.info("[SharePoint URL]: " + SPurl);
			
			if(managePage.isHaveSites())
			{
				managePage.removeAllRepo();
			}
	
			managePage.addSharePointRepo(Reponame, SPurl);
			Thread.sleep(2000);
			boolean flag = RMSCommon.isTextPresent(driver, Reponame);

			Assert.assertTrue(flag, errorMes);
			logger.info("[Ending]:  Test addSP ending");
		}
		catch(Exception e){
			e.printStackTrace();
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test(alwaysRun = true)
	@Parameters({"SPName","SPURL"})
	public void addButNotDeleteBefore(String Reponame, String SPurl)
	{
		try{
			/***** Local variables ***************/
			logger.info("[Starting]: Test addSP starting");
			logger.info("[Repository Display Name]: " + Reponame);
			logger.info("[SharePoint URL]: " + SPurl);

			managePage.addSharePointRepo(Reponame, SPurl);
			Thread.sleep(2000);
			boolean flag = RMSCommon.isTextPresent(driver, Reponame);

			Assert.assertTrue(flag, errorMes);
			logger.info("[Ending]:  Test addSP ending");
		}
		catch(Exception e){
			e.printStackTrace();
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test
	@Parameters({"SPName","SPURL"})
	public void addMoreSPSites(String Reponame, String SPurl) {
		try {
			//get all sharepoint display name and sharepoit url
		String[] SPNames = Reponame.split(";");
		String[] SPurls = SPurl.split(";");
		
		logger.info("[Starting]: Test addSP starting");
		logger.info("[Repository Display Name]: " + Reponame);
		logger.info("[SharePoint URL]: " + SPurl);
		
		StringBuilder errorTestResult = new StringBuilder("\n"); // this list will contain all error verifications
		//delete sharepoint site if have
		if(managePage.isHaveSites())
		{
				managePage.removeAllRepo();
		}
		
		//add sharepoint sites
		managePage.addSharePointRepo(SPNames, SPurls);
		
		//check if sharepoint site is added
		boolean flag = RMSCommon.isSPListPresent(driver, SPNames);
		if(!flag)
		{
			errorTestResult.append("After add sites, present sites failed in homePage");
		}
		logger.info("[Ending]:  Test addSP ending");
		}
		catch(Exception e){
			e.printStackTrace();
			logger.info("[Exception]: " + e.getMessage());
		}
	}

	@Test(alwaysRun = true)
	@Parameters({"SPName","SPURL","errMessage"})
	public void addWrongSPSites(String Reponame, String SPurl, String errMessage) {
		try {
			//add sharepoint url with wrong words
			boolean result = managePage.addSharePointRepo(Reponame, SPurl, errMessage);

			//check result
			Assert.assertTrue(result, ManageRepository.errLog);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test(alwaysRun = true)
	@Parameters({"SPName","SPURL","userName","password","domain"})
	public void normalUserCheckSPList(String Reponame, String Spurl,String userName, String userPwd, String userDomain) {
		try {
			
			String[] SPNames = Reponame.split(";");
			String[] SPurls = Spurl.split(";");
			
			//delete sharepoint sites
			if(managePage.isHaveSites())
			{
					managePage.removeAllRepo();
			}
			
			//add sharepoint sites
			managePage.addSharePointRepo(SPNames, SPurls);
			Thread.sleep(3000);
			
			//return to homepage
			managePage = PageFactory.initElements(driver, ManageRepository.class);
			HomePage homePage = managePage.returnToHomePage();
			
			//logout sc, and another user login
			LoginPage login = homePage.logout();
			login.userLogIn(userName, userPwd, userDomain);		
			Thread.sleep(3000);
			
			//check sharepoint sites
			managePage = PageFactory.initElements(driver, ManageRepository.class);
			HomePage homePage2 = PageFactory.initElements(driver, HomePage.class);		
			boolean result = homePage2.checkSPList(Reponame);
			
			//check result
			Assert.assertTrue(result, HomePage.errLog);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@Parameters({"SPName","SPURL"})
	public void deleteSite(String Reponame, String SPurl) {
		try {
			logger.info("[Starting]: Test deleteSites starting");
			logger.info("[Repository Display Name]: " + Reponame);
			logger.info("[SharePoint URL]: " + SPurl);
			
			if(managePage.isHaveSites())
			{
				//managePage.removeAllRepo();
				managePage.removeRepo(SPurl);
			}
			
			boolean result = managePage.isDelete(driver, Reponame);
			Thread.sleep(2000);
			Assert.assertTrue(result, errorMes);
			logger.info("[Ending]:  Test deleteSites ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test
	@Parameters({"SPName","SPURL"})
	public void deleteMoreSites(String Reponame, String SPurl) {
		try {
			String[] SPNames = Reponame.split(";");
			String[] SPurls = SPurl.split(";");
			
			logger.info("[Starting]: Test deleteMoreSites starting");
			logger.info("[Repository Display Name]: " + Reponame);
			logger.info("[SharePoint URL]: " + SPurl);
			
			if(managePage.isHaveSites())
			{
				//remote several ursl
				managePage.removeRepo(SPurls);
			}
			
			boolean result = managePage.isDelete(driver, SPNames);
			Thread.sleep(2000);
			Assert.assertTrue(result, errorMes);
			logger.info("[Ending]:  Test deleteMoreSites ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test(alwaysRun = true)
	public void checkHomeButton() throws InterruptedException {
		// return to homepage
		Thread.sleep(3000);
		managePage.returnToHomePage();
		Thread.sleep(3000);

		//check if return true
		//...
	}
}
