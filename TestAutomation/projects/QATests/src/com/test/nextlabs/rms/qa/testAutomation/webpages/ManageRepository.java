package com.test.nextlabs.rms.qa.testAutomation.webpages;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.test.nextlabs.rms.qa.testAutomation.common.BaseTest;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSCommon;
import com.test.nextlabs.rms.qa.testAutomation.testcases.RepositoryTest;

public class ManageRepository {
	final WebDriver driver;
	
	public static String errLog;

	// Constructor of Home page
	public ManageRepository (WebDriver driver){
		this.driver = driver;
	}
	
	public WebDriver getDriver (){
		return driver;
	}
	
	@FindBy(css = "table.table")
	private static WebElement table;

	// get repository title
	@FindBy (xpath = "//div[h5='Settings > Manage Repositories']")
	private static WebElement repository;

	// get Home button
	@FindBy(css = "div>button[class*='btn btn-sc']")
	public static WebElement homebtn;
	
	// get AddRepository button
	@FindBy(xpath = "//button[text()='Add Repository']")
	private WebElement addRepobtn ;
	
	// get Repository Type List 
	@FindBy(xpath="//select[@class='ember-view ember-select']")
	private WebElement RepoTypeListBtn;
	
	// get Repository Type: SharePoint
	@FindBy(xpath="//select/option[@value='SharePoint']")
	private WebElement RepoTypeSharePointBtn;
	
	// Input SharePoint Repository Name
	@FindBy(xpath="//label[text()='Repository Display Name']/ancestor::tr/descendant::input")
	private WebElement RepoNameTxt;
	
	// Input SharePoint URL
	@FindBy(xpath="//label[text()='SharePoint URL']/ancestor::tr/descendant::input")
	private WebElement SPURLTxt;
	
	// Add button
	@FindBy(xpath="//label[text()='Repository Display Name']/ancestor::div/descendant::button[text()='Add']")
	private WebElement AddBtn;
	
	// Delete repository button
	private WebElement RemoveBtn;
	
	@FindBy(css="label.unhold")
	private WebElement unHold;
	
	//Add SharePoint Repository
	public ManageRepository addSharePointRepo(String RepoName, String RepoURL){
		addRepobtn.click();
		RepoTypeListBtn.click();
		Select select = new Select(RepoTypeListBtn);
		select.selectByValue("SharePoint");
		RepoNameTxt.sendKeys(RepoName);
		SPURLTxt.sendKeys(RepoURL);
		AddBtn.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
		return PageFactory.initElements(driver, ManageRepository.class);
	}
	
	//Add wrong sharepoint site
	public boolean addSharePointRepo(String RepoName, String RepoURL, String errMessage) {
		try {
			addRepobtn.click();
			
			RepoTypeListBtn.click();
			Select select = new Select(RepoTypeListBtn);
			select.selectByValue("SharePoint");
			
			RepoNameTxt.sendKeys(RepoName);
			SPURLTxt.sendKeys(RepoURL);
			
			AddBtn.click();
			Thread.sleep(2000);
			
			String alertText = driver.findElement(By.cssSelector("div#error>span#errmsg")).getText();
			
			if(!alertText.equals(errMessage))
			{
				errLog = "Actual Result:" + alertText + "\n" + "Expected Result:" +errMessage;
				return false;
			}
			
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

	}
	
	//Add more sites
	public ManageRepository addSharePointRepo(String[] sitesName, String[] sitesURL) throws InterruptedException {
		//System.out.println(sitesName.length);
		for(int i=0; i<sitesName.length; i++)
		{
			addRepobtn.click();
			Thread.sleep(2000);
			RepoTypeListBtn.click();
			Thread.sleep(2000);
			Select select = new Select(RepoTypeListBtn);
			select.selectByValue("SharePoint");
			Thread.sleep(2000);
			RepoNameTxt.clear();
			RepoNameTxt.sendKeys(sitesName[i]);
			SPURLTxt.clear();
			SPURLTxt.sendKeys(sitesURL[i]);
			AddBtn.click();
			Thread.sleep(2000);
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			PageFactory.initElements(driver, ManageRepository.class);
		}
		return PageFactory.initElements(driver, ManageRepository.class);
	}
		
	// Add Dropbox Repositoy
	// This needs to do check if it same as SharePoint
	public ManageRepository addDropboxRepo(String RepoName){
		addRepobtn.click();
		RepoTypeListBtn.click();
		Select select = new Select(RepoTypeListBtn);
		select.selectByValue("Dropbox");
		RepoNameTxt.sendKeys(RepoName);
		AddBtn.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//wait 5 second for find element
		return PageFactory.initElements(driver, ManageRepository.class);
	}
	
	// Remove one url
	public ManageRepository removeRepo(String RepoURL) throws Exception{
		BaseTest.logger.info("remove repository list starting...");
		RemoveBtn=driver.findElement(By.xpath("//td[text()='" + RepoURL + "/" + "']/ancestor::tr/descendant::a"));
		Thread.sleep(2000);
		RemoveBtn.click();
		Thread.sleep(3000);
		RMSCommon.dealPotentialAlert(driver, true);		
		BaseTest.logger.info("remove repository list ending...");
		return PageFactory.initElements(driver, ManageRepository.class);	
	}

	public ManageRepository removeRepo(String[] sitesURL) throws Exception{
		BaseTest.logger.info("remove repository list starting...");
		for(int i=0; i<sitesURL.length; i++)
		{
			RemoveBtn=driver.findElement(By.xpath("//td[text()='" + sitesURL[i] + "/" + "']/ancestor::tr/descendant::a"));
			Thread.sleep(2000);
			RemoveBtn.click();
			Thread.sleep(3000);
			RMSCommon.dealPotentialAlert(driver, true);
			PageFactory.initElements(driver, ManageRepository.class);
			Thread.sleep(2000);
		}
		BaseTest.logger.info("remove repository list ending...");
		return PageFactory.initElements(driver, ManageRepository.class);
	}
	
	//Remove all sharepoint sites
	public ManageRepository removeAllRepo(){
		try{
			BaseTest.logger.info("remove all repository list starting...");
			List<WebElement> rows = table.findElements(By.tagName("tr"));
			while(rows.size()>1)
			{
				
				List<WebElement> tds = rows.get(1).findElements(By.tagName("td"));
				Thread.sleep(2000);
				String RepoURL = tds.get(2).getText();
				BaseTest.logger.info("remove the sp list: " + RepoURL);
				WebElement delete = driver.findElement(By.xpath("//td[text()='" + RepoURL.substring(0, RepoURL.length()-1 ) + "/" + "']/ancestor::tr/descendant::a"));
				delete.click();
				Thread.sleep(3000);
				Alert alert = driver.switchTo().alert();
				alert.accept();
				 
				PageFactory.initElements(driver, ManageRepository.class);
				if(rows.size()==2)
				{
					break;
				}
				rows = table.findElements(By.tagName("tr"));
				Thread.sleep(2000);
		}
			BaseTest.logger.info("remove all repository list ending...");
		}
		catch(Exception e)
		{
			RepositoryTest.errorMes = e.getMessage();
			BaseTest.logger.info("[Exception]: " + RepositoryTest.errorMes);
		}
		return PageFactory.initElements(driver, ManageRepository.class);
	}

	public boolean isDelete(WebDriver driver, String text) throws Exception{
		if(driver.findElement(By.tagName("body")).getText().contains(text))
		{
			RepositoryTest.errorMes = "[Result]: delete splist failed!!";
			return false;
		}else {
			BaseTest.logger.info("[Result]: delete splist successfully!!");
			return true;
		}
	}
	
	public boolean isDelete(WebDriver driver, String[] sitesName) throws Exception{
		BaseTest.logger.info("check delete...");
		for(int i=0; i<sitesName.length; i++)
		{
			BaseTest.logger.info("Begining to delete " + sitesName[i]);
			if(driver.findElement(By.tagName("body")).getText().contains(sitesName[i]))
			{
				RepositoryTest.errorMes = "[Result]: delete splist failed!!";
				BaseTest.logger.info("[Result]: delete splist failed!!");
				return false;
			}
		}
		BaseTest.logger.info("delete finished...");
		return true;
	}
		
	//judge sharepint sites or dropbox sites have
	public boolean isHaveSites() {
		
		String text = "You have no repository associated with this account. Click the 'Add Repository' button to add a repository.";
		try {
			if(unHold.getText().equals(text))
			{
				return false;
			}else {
				return true;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			RepositoryTest.errorMes = e.getMessage();
			BaseTest.logger.info("[Exception]: " + e.getMessage());
			return true;
		}
	}

	public HomePage returnToHomePage() {
		System.out.println("---return to homepage---");
		homebtn.click();
		return PageFactory.initElements(driver, HomePage.class);
	}
}
