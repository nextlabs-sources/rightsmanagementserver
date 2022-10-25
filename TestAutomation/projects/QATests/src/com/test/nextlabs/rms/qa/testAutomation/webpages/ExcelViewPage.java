package com.test.nextlabs.rms.qa.testAutomation.webpages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;

public class ExcelViewPage {
	
	WebDriver driver;
	
	//get print button disabled
	@FindBy(xpath="//button[@title='Print disabled']")
	public WebElement printBtndisabled;
	
	//get print button
	@FindBy(xpath="//button[@title='Print']")
	public WebElement printBtnenabled;
	
	// Constructor the UploadFile Page
	public ExcelViewPage(WebDriver driver){
		this.driver = driver;
	}
	
	public void clickPrint()
	{
		printBtnenabled.click();
	}
	
	public void clickDisabledPrint()
	{
		printBtndisabled.click();
	}
	
	public boolean clickDisablePrint()
	{
		try
		{
			printBtndisabled.click();
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
	
	private  List<WebElement> sheets() {
		return driver.findElements(By.xpath("//div[@id='toolbar']/ul[@class='ISYS_TOC']/li"));
	}
	
	public boolean checkSheesNum(int num) {	
		if(num<=1)
		{
			BaseTest.logger.info("This excel file just have one page");
			return true;
		}
		BaseTest.logger.info("Expected sheets num is " + num);
		int size = sheets().size()-2;
		BaseTest.logger.info("Actual sheets num is " + size);
		return (size==num);	
	}
	
	public void clickSheets(String sheetsname)
	{
		try {
			driver.findElement(By.xpath("//div[@id='toolbar']/ul[@class='ISYS_TOC']/li/a[contains(text(),'"+sheetsname+"')]")).click();
		} catch (Exception e) {
			// TODO: handle exception
			BaseTest.logger.info("[Exception]: " + e.getMessage());
		}
	}
}
