package com.test.nextlabs.rms.qa.testAutomation.webpages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;


/*******************************************************
 * @author: Michelle Xu

 * Created Date: 28/09/2015
 * This is Viewer page 
  *********************************************************************/

public class PictureViewerPage {
	
	WebDriver driver;
	public PictureViewerPage(WebDriver driver){
		this.driver = driver;
	}
	
	//get fit to height button
	@FindBy(xpath="//button[@title='Fit to Height']")
	public WebElement FitToHeightBtn;
	
	//get fit to width button
	@FindBy(xpath="//button[@title='Fit to Width']")
	public WebElement FitToWidthBtn;
	
	//get zoom in button
	@FindBy(xpath="//button[@title='Zoom in']")
	public WebElement ZoomInBtn;
	
	//get zoom out button
	@FindBy(xpath="//button[@title='Zoom out']")
	public WebElement ZoomOutBtn;
	
	//get Rotate Anticlockwise button
	@FindBy(xpath="//button[@title='Rotate Anticlockwise']")
	public WebElement RotateAntiBtn;
	
	//get Rotate Clockwise button
	@FindBy(xpath="//button[@title='Rotate Clockwise']")
	public WebElement RotateClockBtn;
	
	//get total page numbers
	@FindBy(xpath="//label[@id='numPages']")
	public WebElement ActualTotalPageNum;
	
	//get first page button
	@FindBy(xpath="//button[@title='First Page']")
	public WebElement FirstPageBtn;
	
	//get previous page button
	@FindBy(xpath="//button[@title='Previous Page']")
	public WebElement PreviousPageBtn;
	
	//get page redirect input
	//@FindBy(xpath="//button[@title='First Page']/ancestor::div[@id='toolbarContainer']/descendant::input")
	@FindBy(xpath="//input[@class='ember-view ember-text-field']")
	public WebElement PageInput;
	
	//get next page button
	@FindBy(xpath="//button[@title='Next Page']")
	public WebElement NextPageBtn;
	
	//get last page button
	@FindBy(xpath="//button[@title='Last Page']")
	public WebElement LastPageBtn;
	
	//get help button
	@FindBy(xpath="//button[@title='Help']")
	public WebElement HelpBtn;
	
	//get print button
	@FindBy(xpath="//div[@id='toolbarContainer']/div/button[@title='Print']")
	public WebElement printBtn;
	
	public void clickPrint()
	{
			printBtn.click();
	}
	
	public boolean clickDisablePrint()
	{
		try
		{
			printBtn.click();
			BaseTest.logger.info("[clickResult]: Expected Result is that the print button is can't clickable, but actually, can print!");
			return false;
		}catch(Exception e)
		{
			BaseTest.logger.info("[Exception]: ", e);
			if(e.getMessage().contains("Element is not clickable"))
			{
				BaseTest.logger.info("[Acutal Result]: Deny_Print");
				return true;
			}
			return false;
		}
	}

	//get total num
	public String getTotalPageNum() {
		return ActualTotalPageNum.getText();
	}
	
	//go to first page
	public void goToFirstPage() throws InterruptedException{
		FirstPageBtn.click();
		Thread.sleep(7000);
	}
	
	//go to previous page
	public void goToPrePage() throws InterruptedException{
		PreviousPageBtn.click();
		Thread.sleep(7000);
	}
	
	//input page to direct
	public void inputPage(String pageNumber) throws InterruptedException{
		PageInput.clear();
		PageInput.sendKeys(pageNumber);
		PageInput.sendKeys(Keys.ENTER);
		Thread.sleep(7000);
	}
	
	//go to next page
	public void goToNextPage()throws InterruptedException{
		NextPageBtn.click();
		Thread.sleep(7000);
	}
	
	//go to last page
	public void goToLastPage() throws InterruptedException{
		LastPageBtn.click();
		Thread.sleep(7000);
	}
	
	//get total page numbers
	public boolean checkTotalPage(int ExpectTotalPageNum){
		int a = Integer.parseInt(ActualTotalPageNum.getText());
		try {
			if(a==ExpectTotalPageNum){
			return true;	}
			else{return false;}
		}catch (Exception e){
			return false;
		}
	}
	
	//get current page number
	public int getCurrentPage(){
		int a = Integer.parseInt(PageInput.getAttribute("value"));
		return a;
	}
	
	//Check current page
	public boolean CheckCurrentPage(int ExpectedCurrentPageNum){
//		String b = PageInput.getAttribute("value").toString();
//		System.out.println(b);
		int a = Integer.parseInt(PageInput.getAttribute("value"));

		try{
			if(a==ExpectedCurrentPageNum){
				return true;}
				else{return false;}
			}catch(Exception e){
				return false;
			}
		}

	public boolean checkPageNum(String filePath) {
		boolean result = false;
		try
		{	
			int totalPageNum = Integer.parseInt(getTotalPageNum());
			switch (totalPageNum) {
			case 1:
				result = checkOnlyOnePageFile(filePath);
				break;
			case 2:
				result = checkOnlyTwoPagesFile(filePath);
				break;
			default:
				result = checkMoreThanTwoPagesFile(filePath);
				break;
			}
			return result;
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]:  --" + e.getMessage());
		   return false;
		}
	}
	
	//if file only have one page, click some button, check page
	private  boolean checkOnlyOnePageFile(String filePath) {
		File file = new File(filePath);
		if(file.exists())
		{
			file.delete();
		}
		RMSCommon.getFileContent(driver, filePath);
		try {
			goToLastPage();
			String lastPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[lastPageNum]: " + lastPageNum);
			goToPrePage();
			String prePageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[prePageNum]: " + prePageNum);
			goToFirstPage();
			String firstPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[firstPageNum]: " + firstPageNum);
			goToNextPage();
			String nextPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[nextPageNum]: " + nextPageNum);
			
			if(lastPageNum.equals("1") && prePageNum.equals("1") && firstPageNum.equals("1") && nextPageNum.equals("1"))
			{
				BaseTest.logger.info("The total page is just one page");
				return true;
			}else {
				BaseTest.logger.info("The total page num is not the one");
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			return false;
		}
	}

	//check file content which has two pages
	private boolean checkOnlyTwoPagesFile(String filePath) {
		
		File file = new File(filePath);
		if(file.exists())
		{
			file.delete();
		}
		RMSCommon.getFileContent(driver, filePath);
		
		try {
			String num = getTotalPageNum();
			String lastPageNum;
			String prePageNum;
			String firstPageNum;
			String nextPageNum;
			
			goToLastPage();
			lastPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[lastPageNum]: " + lastPageNum);
			RMSCommon.getFileContent(driver, filePath);
			goToPrePage();
			prePageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[prePageNum]: " + prePageNum);
			goToFirstPage();
			firstPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[firstPageNum]: " + firstPageNum);
			goToNextPage();
			nextPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[nextPageNum]: " + nextPageNum);
			
			if(!(lastPageNum.equals(num) && Integer.parseInt(prePageNum)==(Integer.parseInt(lastPageNum) - 1) && firstPageNum.equals("1") && Integer.parseInt(nextPageNum)==(Integer.parseInt(firstPageNum)+1)))
			{
				BaseTest.logger.info("Maybe input page number is not the same");
				return false;
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			return false;
		}
	}

	//check file content which has three or more pages
	private boolean checkMoreThanTwoPagesFile(String filePath) {
		List<String> pageNumList = new ArrayList<String>();
		pageNumList.add("1");
		File file = new File(filePath);
		if(file.exists())
		{
			file.delete();
		}
		RMSCommon.getFileContent(driver, filePath);
		try {
			String num = getTotalPageNum();
			int totalNm = Integer.parseInt(num);
			BaseTest.logger.info("[TotalPageNum]: " + num);
			String lastPageNum;
			String prePageNum;
			String firstPageNum;
			String nextPageNum;
			
			//go to last page
			goToLastPage();
			lastPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[lastPageNum]: " + lastPageNum);
			addContent(lastPageNum, pageNumList, filePath);
			Thread.sleep(2000);
			if(!(lastPageNum.equals(num)))
			{
				BaseTest.logger.info("last page num is not the same with total pageNum");
				return false;
			}
			
			// go to previous page
			for(int j=1; j<=2; j++)
			{
				goToPrePage();
				prePageNum = PageInput.getAttribute("value");
				BaseTest.logger.info("[prePageNum]: " + prePageNum);
				addContent(prePageNum, pageNumList, filePath);
				if(!(Integer.parseInt(prePageNum)== (Integer.parseInt(lastPageNum) - j)))
				{
					BaseTest.logger.info("pre " + j + "pages, but the input page text is not the same");
					return false;
				}
			}			
			
			// go to first page
			goToFirstPage();
			firstPageNum = PageInput.getAttribute("value");
			BaseTest.logger.info("[firstPageNum]: " + firstPageNum);
			addContent(firstPageNum, pageNumList, filePath);
			if(!(firstPageNum.equals("1")))
			{
				BaseTest.logger.info("The first page num is not 1");
				return false;
			}
			
			//go to next page
			for(int i=1; i<=2; i++)
			{
				goToNextPage();
				nextPageNum = PageInput.getAttribute("value");
				BaseTest.logger.info("[nextPageNum]: " + nextPageNum);
				addContent(nextPageNum, pageNumList, filePath);
				if(!(Integer.parseInt(nextPageNum)==(Integer.parseInt(firstPageNum)+i)))
				{
					BaseTest.logger.info("next " + i + "pages, but the num is not the same with input text");
					return false;
				}
			}
			
			//input page num and check content
			int midNum = 0;
			if(totalNm%2==0)
			{
				midNum = totalNm/2;
			}else {
				midNum = totalNm/2 + 1;
			}
			inputPage(midNum+"");
			addContent(midNum+"", pageNumList, filePath);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			return false;
		}
	}	

	//add file content to text file
	private void addContent(String pageNum, List<String> str, String filePath)
	{
		try {
			int a=0;
			for(int i=0; i<str.size(); i++)
			{
				if(pageNum.equals(str.get(i)))
				{
					break;
				}else {
					a++;
				}
			}
			if(a==str.size())
			{
				str.add(pageNum);
				RMSCommon.getFileContent(driver, filePath);
			}
			Thread.sleep(2000);
			
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
