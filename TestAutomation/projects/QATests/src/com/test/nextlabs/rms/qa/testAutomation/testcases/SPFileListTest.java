package com.test.nextlabs.rms.qa.testAutomation.testcases;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSUtility;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.UploadFilePage;

public class SPFileListTest extends BaseTest{
	
	public HomePage accessHomePage(String userName,String userPwd, String userDomain) {
		try
		{
			LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
			loginPage.userLogIn(userName, userPwd, userDomain);
			
			HomePage homePage = PageFactory.initElements(driver, HomePage.class);
			
			return homePage;
		}catch(Exception e)
		{
			logger.error("[Exception ]: login user failed", e);
			return null;
		}

	}
	
	@Test
	@Parameters({"Rights","userName","password","domain","filePath","CompareSourceFile","CompareDesFile","SheetsNum"})
	public void openExcelFileFromRepository(String rights, String userName,String userPwd, String userDomain, String filePath,String excelSourceFile, String desFile,String sheetNum)throws InterruptedException
	{
		String[] folders = filePath.split("/");	
		logger.info("[Starting]Test searchFileAndOpen starting");
		logger.info("[userName]: " + userName);
		logger.info("[password]: " + userPwd);
		logger.info("[domain]: " + userDomain);
		logger.info("[FilePath]: " + filePath);
		
		HomePage homePage = accessHomePage(userName, userPwd, userDomain);
		
		//if no this item, it should deal with this phenomenon
		homePage.clickSPList(folders);
		
		//after access folder, check file
		driver.findElement(By.xpath("//div[@id='repoDisplay']/div[@class='conatiner-sc']/span/a[contains(text(),'"+folders[folders.length-1]+"')]")).click();
		Thread.sleep(10000);
		
		
		try {
			Thread.sleep(3000);
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);
			
			RMSCommon.clickCerError(driver);
			
			/*boolean result = sp.checkFileType(sourceFile, desFile, rights, type, errorMes);*/
			boolean result = RMSUtility.CheckExcelFile(rights, excelSourceFile, desFile, sheetNum, driver);
			
			Assert.assertTrue(result, HomePage.errLog);
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("[Exception]: " + e.getMessage());
		}
	}

	@Test
	@Parameters({"Rights","userName","password","domain","filePath","CompareSourceFileInWin2012","CompareSourceFileInWin2008","CompareSourceFileInLinux","CompareDesFile",})
	public void opeDocFromRepo(String rights, String userName,String userPwd, String userDomain, String filePath,String filePathForWin2012, String filePathForWin2008,String filePathForLinux, String desFile)throws InterruptedException
	{
		String[] folders = filePath.split("/");	
		logger.info("[Starting]Test searchFileAndOpen starting");
		logger.info("[userName]: " + userName);
		logger.info("[password]: " + userPwd);
		logger.info("[domain]: " + userDomain);
		logger.info("[FilePath]: " + filePath);
		
		HomePage homePage = accessHomePage(userName, userPwd, userDomain);
		
		//if no this item, it should deal with this phenomenon
		homePage.clickSPList(folders);
		
		//after access folder, check file
		driver.findElement(By.xpath("//div[@id='repoDisplay']/div[@class='conatiner-sc']/span/a[contains(text(),'"+folders[folders.length-1]+"')]")).click();
		Thread.sleep(10000);
		
		/*SharepointOnPromise sp = PageFactory.initElements(driver, SharepointOnPromise.class);*/
		
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
				default:
					break;
		}
		
		try {
			Thread.sleep(3000);
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);
			
			RMSCommon.clickCerError(driver);
			
			/*boolean result = sp.checkFileType(sourceFile, desFile, rights, type, errorMes);*/
			boolean result = RMSUtility.checkDocFile(rights, fileSourcePath, desFile, driver);
			
			Assert.assertTrue(result, HomePage.errLog);
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test(alwaysRun = true)
	@Parameters({ "UserName", "UserPwd", "UserDomain","FilePath","ErrorMes"})
	public void openNotSupportFile(String userName, String userPassword, String userDomain, String filePath, String errorMes)
	{
		try
		{
			String[] folders = filePath.split("/");	
			logger.info("[Starting]Test searchFileAndOpen starting");
			logger.info("[userName]: " + userName);
			logger.info("[password]: " + userPassword);
			logger.info("[domain]: " + userDomain);
			logger.info("[FilePath]: " + filePath);
			
			HomePage homePage = accessHomePage(userName, userPassword, userDomain);
			
			//if no this item, it should deal with this phenomenon
			homePage.clickSPList(folders);
					
			//after access folder, check file
			driver.findElement(By.xpath("//div[@id='repoDisplay']/div[@class='conatiner-sc']/span/a[contains(text(),'"+folders[folders.length-1]+"')]")).click();
			Thread.sleep(10000);
			
			UploadFilePage upfile = PageFactory.initElements(driver, UploadFilePage.class);
			RMSCommon.switchTowindow(driver);
			RMSCommon.clickCerError(driver);
			boolean result = upfile.checkErrorMessage(errorMes,driver);
			Assert.assertTrue(result, "failed");
			
			
		}catch(Exception e)
		{
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test
	@Parameters({"userName","password", "domain","SearchText","SearchNum"})
	public void checkSearchFileNum(String userName,String userPwd, String userDomain, String searchText, String searchNum) {
		try {
			
			HomePage homePage = accessHomePage(userName, userPwd, userDomain);
			
			boolean result = homePage.checkSearchNum(searchText, searchNum);
			
			Assert.assertTrue(result, homePage.errLog + "  Expected search result num: " + "["+ searchNum +"]");
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Test
	@Parameters({"userName","password", "domain","SearchText","SearchNum", "ShowNum"})
	public void checkShowFileNum(String userName,String userPwd, String userDomain, String searchText, String searchNum, String showNum) {
		try {
			
			HomePage homePage = accessHomePage(userName, userPwd, userDomain);
			boolean result = homePage.checkSearchNum(searchText, searchNum, showNum);
			
			Assert.assertTrue(result, homePage.errLog);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Test
	@Parameters({"userName","password", "domain","SearchText","FilePath","CompareSourceFileInWin2012","CompareSourceFileInWin2008","CompareSourceFileInLinux","CompareDesFile","rights","type","SheetsNum","ErrorMes"})
	public void searchFileAndOpen(String userName,String userPwd, String userDomain, String searchText, String filePath,String filePathForWin2012, String filePathForWin2008,String filePathForLinux,String desFile,String rights, String type,String sheetNum,String errorMes) {
		try {
			
			logger.info("[Starting]Test searchFileAndOpen starting");
			logger.info("[userName]: " + userName);
			logger.info("[password]: " + userPwd);
			logger.info("[domain]: " + userDomain);
			logger.info("[SearchText]: " + searchText);
			logger.info("[FilePath]: " + filePath);
			
			HomePage homePage = accessHomePage(userName, userPwd, userDomain);
			homePage.searchFileToOpen(searchText, filePath);
			Thread.sleep(1000);
			
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
			
			Thread.sleep(3000);
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);
			
			RMSCommon.clickCerError(driver);
			
			boolean result = RMSUtility.checkFileType(fileSourcePath, desFile, rights, type, sheetNum, errorMes, driver);
			
			Assert.assertTrue(result, homePage.errLog);
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: " + e.getMessage());
		}
	}

	@Test
	@Parameters({"Rights","userName","password", "domain","SearchText","FilePath","CompareSourceFile","CompareDesFile","SheetsNum"})
	public void searchExcelFile(String rights, String userName,String userPwd, String userDomain, String searchText, String filePath,String excelSourceFile,String desFile,String sheetNum)
	{
		try {
			
			logger.info("[Starting]Test searchFileAndOpen starting");
			logger.info("[userName]: " + userName);
			logger.info("[password]: " + userPwd);
			logger.info("[domain]: " + userDomain);
			logger.info("[SearchText]: " + searchText);
			logger.info("[FilePath]: " + filePath);
			
			HomePage homePage = accessHomePage(userName, userPwd, userDomain);
			homePage.searchFileToOpen(searchText, filePath);
			Thread.sleep(1000);
			
			Thread.sleep(3000);
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);
			
			RMSCommon.clickCerError(driver);
			
			boolean result = RMSUtility.CheckExcelFile(rights, excelSourceFile, desFile, sheetNum, driver);
			
			Assert.assertTrue(result, homePage.errLog);
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test
	@Parameters({"Rights","userName","password", "domain","SearchText","FilePath","CompareSourceFileInWin2012","CompareSourceFileInWin2008","CompareSourceFileInLinux","CompareDesFile"})
	public void searchDocFile(String rights, String userName,String userPwd, String userDomain, String searchText, String filePath,String filePathForWin2012, String filePathForWin2008,String filePathForLinux,String desFile)
	{
		try {
			
			logger.info("[Starting]Test searchFileAndOpen starting");
			logger.info("[userName]: " + userName);
			logger.info("[password]: " + userPwd);
			logger.info("[domain]: " + userDomain);
			logger.info("[SearchText]: " + searchText);
			logger.info("[FilePath]: " + filePath);
			
			HomePage homePage = accessHomePage(userName, userPwd, userDomain);
			homePage.searchFileToOpen(searchText, filePath);
			Thread.sleep(1000);
			
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
			
			Thread.sleep(3000);
			RMSCommon.switchTowindow(driver);
			driver.manage().window().maximize();
			Thread.sleep(3000);
			
			RMSCommon.clickCerError(driver);
			
			boolean result = RMSUtility.checkDocFile(rights, fileSourcePath, desFile, driver);
			
			Assert.assertTrue(result, homePage.errLog);
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
	@Test
	@Parameters({"userName","password", "domain","SearchText","FilePath","ErrorMes"})
	public void searchNotSupportFile(String userName,String userPwd, String userDomain, String searchText, String filePath,String errorMes)
	{
		try {
			
			logger.info("[Starting]Test searchFileAndOpen starting");
			logger.info("[userName]: " + userName);
			logger.info("[password]: " + userPwd);
			logger.info("[domain]: " + userDomain);
			logger.info("[SearchText]: " + searchText);
			logger.info("[FilePath]: " + filePath);
			
			HomePage homePage = accessHomePage(userName, userPwd, userDomain);
			homePage.searchFileToOpen(searchText, filePath);
			Thread.sleep(1000);
			
			UploadFilePage upfile = PageFactory.initElements(driver, UploadFilePage.class);
			RMSCommon.switchTowindow(driver);
			RMSCommon.clickCerError(driver);
			boolean result = upfile.checkErrorMessage(errorMes,driver);
			
			Assert.assertTrue(result, homePage.errLog);
			logger.info("[Ending]Test ending");
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("[Exception]: " + e.getMessage());
		}
	}
	
}
