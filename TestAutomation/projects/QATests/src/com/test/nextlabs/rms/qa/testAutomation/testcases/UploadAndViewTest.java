package com.test.nextlabs.rms.qa.testAutomation.testcases;

import java.io.File;

import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSUtility;
import com.test.nextlabs.rms.qa.testAutomation.webpages.ExcelViewPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.HomePage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.LoginPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.PictureViewerPage;
import com.test.nextlabs.rms.qa.testAutomation.webpages.UploadFilePage;

public class UploadAndViewTest extends BaseTest {
	
	public void initail( String userName, String userPWD, String userDomain) {
		try
		{
			LoginPage userLog = PageFactory.initElements(driver, LoginPage.class);
			userLog.userLogIn(userName, userPWD, userDomain);
			HomePage homePage = PageFactory.initElements(driver, HomePage.class);
			homePage.accessUploadPage();
		}catch(Exception e)
		{
			Reporter.log("[Exception ]: Log in failed or access upload page failed. \n"+e.getMessage());
			logger.error("[Exception ]: Log in failed or access upload page failed ", e);
		}
	}
	
	@Test(alwaysRun = true)
	//@Test()
	@Parameters({"Rights", "UserName", "UserPwd", "UserDomain","FileName","CompareSourceFileInWin2012","CompareSourceFileInWin2008","CompareSourceFileInLinux","CompareDesFile",})
	public void uploadDocFile(String rights, String userName, String userPWD, String userDomain,String fileName, String filePathForWin2012, String filePathForWin2008,String filePathForLinux,String fileDesPath)
	{
		try{
			/***** Local variables ***************/
			logger.info("[Starting]: Test uploadFile starting");

			initail(userName, userPWD, userDomain);
			UploadFilePage uploadpage = PageFactory.initElements(driver, UploadFilePage.class);
			uploadpage.uploadFile(fileName);
			Thread.sleep(3000);
			
			//driver.get("javascript:document.getElementById('overridelink').click();");
			RMSCommon.clickCerError(driver);
			PictureViewerPage page = PageFactory.initElements(driver, PictureViewerPage.class);
			Thread.sleep(2000);
			
			boolean result1 = RMSCommon.isDOCViewer(driver, rights);
			boolean result2 = false;
			RMSCommon.makeDirs(fileDesPath);
			boolean result3 = page.checkPageNum(fileDesPath);
			
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
			
			File file = new File(fileSourcePath);
			if(!(file.exists()))
			{
				file.createNewFile();
			}
			
			if(result1)
			{
				logger.info("[result]:open file, switch to file successfully!");
				
				//compare file content with original file
				result2 = RMSCommon.compareFiles(fileSourcePath, fileDesPath);
			}
			Assert.assertTrue(result3 && result1 && result2, RMSCommon.logs);
			logger.info("[Ending]:  Test uploadFile ending");
		}  
		catch(Exception e){
			e.printStackTrace();
			logger.info("[Exception]: " + e.getMessage());
		}
	}

	@Test(alwaysRun = true)
	//@Test()
	@Parameters({ "UserName", "UserPwd", "UserDomain","FilePath","ErrorMes"})
	public void uploadNotSupportFile(String userName, String userPassword, String userDomain, String filePath, String errorMes) 
	{
		try{
			logger.info("[Starting]: Test uploadNormalFile starting");
			
			initail(userName, userPassword, userDomain);
			UploadFilePage uploadpage = PageFactory.initElements(driver, UploadFilePage.class);
			
			boolean result = uploadpage.checkFileUpload(filePath,errorMes);
			Thread.sleep(2000);
			Assert.assertTrue(result, "Error message not right");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Test(alwaysRun = true)
	//@Test()
	@Parameters({"Rights", "UserName", "UserPwd", "UserDomain","FileName","CompareDesFile","CompareSourFile","SheetsNum"})
	public void uploadExcelFile(String rights,String userName, String userPWD, String userDomain,String fileName, String fileDesPath,String filesourcePath, String num) {
		try{
			/***** Local variables ***************/
			logger.info("[Starting]: Test uploadFile starting");

			initail(userName, userPWD, userDomain);
			UploadFilePage uploadpage = PageFactory.initElements(driver, UploadFilePage.class);
			uploadpage.uploadFile(fileName);
			Thread.sleep(6000);
			
			RMSCommon.clickCerError(driver);
			Thread.sleep(2000);
			
			ExcelViewPage page = PageFactory.initElements(driver, ExcelViewPage.class);
			
			//check if redirect right
			boolean checkURL = RMSCommon.isExcelViewer(driver, rights);
			
			
			//check excel sheets number
			boolean checkSheets = page.checkSheesNum(Integer.parseInt(num));
			Thread.sleep(3000);
			
			//get temp file path
			String fileURL = driver.getCurrentUrl();
			String fileTempPath = fileURL.substring(fileURL.indexOf("temp"), fileURL.length());

			String[] tempFolder = RMSUtility.rmsServer.split("\\.");
			String server = "\\\\"+tempFolder[0];
			
			String[] folders = fileTempPath.split("/");
			for(int i=0;i<folders.length;i++)
			{
				if(folders[i].contains("%20"))
				{
					folders[i] = folders[i].replace("%20", " ");
				}
				server = server + "\\" + folders[i];
			}
			
			Thread.sleep(2000);	
			System.out.println(server);
			
			//get temp file content
			RMSCommon.getFileContent(server, filesourcePath);
			
			Thread.sleep(3000);
			
			//compare temp file
			File sourceFile = new File(filesourcePath);
			File desFile = new File(fileDesPath);
			String soureFileMD5 = RMSCommon.getFileMD5(sourceFile);
			String desFileMD5 = RMSCommon.getFileMD5(desFile);
			System.out.println(soureFileMD5);
			System.out.println(desFileMD5);
			boolean checkFileContent = soureFileMD5.equals(desFileMD5);
			
			Assert.assertTrue(checkURL && checkSheets && checkFileContent, RMSCommon.logs);
			logger.info("[Ending]:  Test uploadFile ending");
		}  
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(false, e.getMessage());
			logger.info("[Exception]: " + e.getMessage());
		}
	}

	//check print button in picture
	@Test
	@Parameters({"UserName", "UserPwd", "UserDomain","FileName","PrintType"})
	public void Pictureprint(String userName, String userPwd, String userDomain, String fileName,String printType)
	     {
	        try{
	        	 BaseTest.logger.info("[Starting]: ******print file starting******");
	        	 initail(userName, userPwd, userDomain);
	        	 UploadFilePage uploadpage = PageFactory.initElements(driver, UploadFilePage.class);
	        	 uploadpage.uploadFile(fileName);
	        	 Thread.sleep(2000);
	        	 boolean checkResult = false;
	        	RMSCommon.clickCerError(driver);
	        	
				PictureViewerPage page = PageFactory.initElements(driver, PictureViewerPage.class);
				Thread.sleep(2000);
				
		   	 	switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
				{
					case IE9:
					case IE10:
					case IE11:
						checkResult = uploadpage.iEPrintFile(printType,driver,page);
						break;
					case chrome:
						checkResult = uploadpage.chromePrintFile(printType,driver);
						break;
					default:
						break;
				}
	        	
				Assert.assertTrue(checkResult, "[Result]: Failed");
	           }
	         catch (Exception e)
	         {
	 			e.printStackTrace();
	 			logger.info("[Exception]: " + e.getMessage());
	         }
		}

	//check print button in excel
	@Test
	@Parameters({"UserName", "UserPwd", "UserDomain", "FileName","PrintType"})
	public void PrintExcel(String userName, String userPwd, String userDomain, String fileName, String printType)
	{
			try
			{
				logger.info("[Starting]: Test PrintExcel starting");
				initail(userName, userPwd, userDomain);
				UploadFilePage uploadpage = PageFactory.initElements(driver, UploadFilePage.class);
				uploadpage.uploadFile(fileName);
				Thread.sleep(3000);
				
				//upload file in ie, it will pop to " There is a problem with this website's security certificate." page
	        	 switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
	 			{
	 				case IE9:
	 				case IE10:
	 				case IE11:
	 					driver.get("javascript:document.getElementById('overridelink').click();");
	 					break;
	 				case chrome:
	 					break;
	 				default:
	 					break;
	 			}
				
				ExcelViewPage p = PageFactory.initElements(driver, ExcelViewPage.class);
				
				boolean checkResult = false;
				boolean checkProcess = RMSCommon.checkApplication("splwow64.exe");
				if(checkProcess)
				{
					Runtime.getRuntime().exec("taskkill /F /IM splwow64.exe");
				}
				
				checkProcess = RMSCommon.checkApplication("splwow64.exe");
				
				if(printType.equals("Deny_print"))
				{
					//IE and chrome is different, click disableprint button in chrome, it will catche exception, otherwise, it can click print button
					logger.info("[Expected Result]: Deny_print");
		        	 switch(RMSCommon.browserTypes.valueOf(RMSUtility.bFullName))
			 			{
			 				case IE9:
			 				case IE10:
			 				case IE11:
			 					p.clickDisabledPrint();
			 					checkProcess = RMSCommon.checkApplication("splwow64.exe");
			 					if(!checkProcess)
			 					{
			 						checkResult = true;
			 					}
			 					break;
			 				case chrome:
								boolean checkDisablePrint = p.clickDisablePrint();
								if(checkDisablePrint)
								{
									checkResult = true;
								}
			 					break;
			 				default:
			 					break;
			 			}
				}
				else
				{
					//according to judge process splwow64.exe to distinguish print window open or not
					logger.info("[Expected Result]: Allow_print");
					if(!checkProcess)
					{
						Thread.sleep(6000);
						BaseTest.logger.info("splwow64.exe is not exist: " + checkProcess);
						p.clickPrint();
						Thread.sleep(3000);
						
						checkProcess = RMSCommon.checkApplication("splwow64.exe");
						if(checkProcess)
						{
							logger.info("[Actual Result]: Allow_print");
							checkResult = true;
						}
					}
				}
				Assert.assertTrue(checkResult, "[Result]: Failed");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				logger.info("[Exception]: " + e.getMessage());
			}
	}	
}
