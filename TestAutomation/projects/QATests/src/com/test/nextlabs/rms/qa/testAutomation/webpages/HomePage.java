package com.test.nextlabs.rms.qa.testAutomation.webpages;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;


public class HomePage {
	WebDriver driver;
	public static String errLog;
	
	// Constructor the Home Page
	public HomePage(WebDriver driver){
		this.driver = driver;
	}
	
	public WebDriver getDriver (){
		return driver;
	}
	
	public String getPageTitle(){
		return driver.getTitle();
	}
	

	/*
	/*
	*******************
	Header Information (header)
	*******************
	*/
	
	//get Nextlabs home page
	@FindBy(xpath="//a[contains(@href, '#/home/repo/displayRepoContents')]")
	public static WebElement NextLabslink;
	
	// get help button
	@FindBy(xpath="//div[@class = 'bigIcons helpIcon']")
	public WebElement helpLink;
	
	public WebElement headerHelpButton(){
		return helpLink;
	}
	
	// get help page
	@FindBy (xpath="//ul[@class='rmsdropdown dropdown-menu']/li[1]/a")
	public WebElement help;
	
	public WebElement headerHelp(){
		return help;
	}
	
	//get about page
	@FindBy(xpath="//ul[@class='rmsdropdown dropdown-menu']/li[2]/a")
	private WebElement about;
	
	public WebElement headerAbout(){
		return about;
	}

	// get Setting button 
	@FindBy (xpath = "//div[@class='bigIcons settingsIcon']")
	public WebElement dropdownBtn;
	
	/*public WebElement headerSettingButton(){
		return dropdownBtn;
	}*/
	
	// get manage repository page
	@FindBy (xpath="//a[text()='Manage Repositories']")
	private WebElement repoAdminLink;
	
	public WebElement headerRepoAdmin(){
		return repoAdminLink;
	}

	
	// get application setting page
	@FindBy (xpath="//a[text()='Application Settings']")
	public WebElement rmsSettingLink;
	
	public WebElement headerRMSSetting(){
		return rmsSettingLink;
	}
	
	//get download RMC page
	@FindBy (xpath = "//a[text()='Download Rights Management Client']")
	public WebElement downloadRMCLink;
	
	public WebElement getRmcDownloadLink() {
		return rmcDownloadLink;
	}


	// get download RMC
	@FindBy (xpath="//a[text()='Download Rights Management Client']")
	public WebElement rmcDownloadLink;
	
	public WebElement headerDownloadRMC(){
		return rmcDownloadLink;
	}
	
	// get logged user
	@FindBy (xpath="//label[@class='HeaderLabel']")
	public WebElement loggedUser;
	
	public WebElement headerCheckUsername(){
		return loggedUser;
	}
	
	// get signout button
	@FindBy (xpath="//ul[@class='rmsdropdown dropdown-menu pull-left']/li/a")
	public WebElement signoutBtn;
	
	public WebElement headerSignoutButton(){
		return signoutBtn;
	}
	
	//get userIcon
	@FindBy(css="div.bigIcons.userIcon")
	public WebElement userIcon;
	
	@FindBy(css = "div#search>input.ember-view.ember-text-field")
	public WebElement searchTextField;
	
	@FindBy(css = "div#search>a.ember-view>i.glyphicon.glyphicon-search")
	public WebElement searchBtn;
	
	/*
	*******************
	Left Panel Information (left)
	*******************
	*/
	
	// get uploadfile
	@FindBy (xpath="//a[text()='Upload and View']")
	public WebElement uploadFile;
	//WebElement uploadFile2 = driver.findElement(By.linkText("Upload and View"));
	public WebElement uploadFileButton(){
		return uploadFile;
	}
	
	// get search text
	@FindBy(xpath="//div[@id='search']/input")
	public WebElement searchText;
	
	public WebElement getSearchText(){
		return searchText;
	}
	
	// get Nextlabs web site
	@FindBy(xpath="//a[@href, 'http://www.nextlabs.com']")
	public WebElement NextlabsLink;
	
	public WebElement NextlabsWebSite(){
		return NextlabsLink;
	}
	
	@FindBy(css = "table.ember-view.dataTable>tbody>tr")
	public List<WebElement> elements;
	
	@FindBy(css="div.dataTables_length>label>select ")
	public WebElement selector;
	
	@FindBy(xpath = "//div[@class='dataTables_paginate paging_two_button']/a[contains(@id,'next')]")
	public WebElement nextBtn;
	
	@FindBy(xpath = "//div[@class='dataTables_paginate paging_two_button']/a[contains(@id,'previous')]")
	public WebElement previousBtn;
	
	//get header text
	public String headerCheckHelpHome() throws InterruptedException{
		
		Thread.sleep(1000);
		String title; 
		//title=driver.getTitle();
		//URL=driver.getCurrentUrl();
			
		//driver.switchTo().defaultContent();
		driver.switchTo().window("help");
		//driver.switchTo().frame(0);
		title=driver.getTitle();

		return title;
	}
	
	/*
	*******************
	Manage Repository (MR)
	*******************
	*/
	public void clickUploadAndView() {
		uploadFileButton().click();
	}
	
	public HomePage accessRepoAdminPage(){
		dropdownBtn.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		repoAdminLink.click();
		return PageFactory.initElements(driver, HomePage.class); 
	}
	
	// Application setting page
	public HomePage accessRMSsettingPage(){
		dropdownBtn.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		rmsSettingLink.click();
		return PageFactory.initElements(driver, HomePage.class);
	}
	
	// Click Upload and View
	public HomePage accessUploadPage(){
		uploadFile.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		return PageFactory.initElements(driver, HomePage.class); 
	}
	
	// Download Rights Management Client
	public DownloadRMCPage accessRMSDownloadPage() {
		dropdownBtn.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		rmcDownloadLink.click();
		return PageFactory.initElements(driver, DownloadRMCPage.class);
	}
	
	//log out
	public LoginPage logout() {
		userIcon.click();
		signoutBtn.click();
		return PageFactory.initElements(driver, LoginPage.class);
	}

	//check if sharepoint sites exists
	public boolean checkSPList(String spName) {
		try {
			String[] names = spName.split(";");
			for(int i=0; i<names.length; i++)
			{
				WebElement e = driver.findElement(By.cssSelector("li#"+names[i]));
				if(e==null)
				{
					errLog = "Error List: no this Sharepoint site: " + names[i]; 
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}	
	}

	//check sharepoinst folder and click in spLsit
	public void clickSPList(String [] folders)
	{
		try {
			String displayName = folders[0];
			WebElement e = driver.findElement(By.cssSelector("li#"+displayName));
			e.click();
		    Thread.sleep(3000);
		    
			for(int i=1; i<folders.length-1; i++)
			{
				String folderName = folders[i];
				System.out.println(folderName);
				driver.findElement(By.xpath("//div[@id='repoDisplay']/div[@class='conatiner-sc']/span/a[contains(text(),'"+folders[i]+"')]")).click();
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			errLog = "No such folder: " + e.getMessage();
		}
	}

	//check search files 
	public boolean checkSearchNum(String fileName, String num) {
		try {
			
			searchTextField.sendKeys(fileName);
			Thread.sleep(2000);
			
			searchBtn.click();
			Thread.sleep(20000);
			
			//List<WebElement> e = driver.findElements(By.cssSelector("table.ember-view.dataTable>tbody>tr"));
			
			int size = elements.size();
			
			if(size == 0)
			{
				errLog = "No search result found, please check!!";
			}else
			{
				while (nextBtn.getAttribute("class").equals("paginate_enabled_next")) {
					nextBtn.click();
					Thread.sleep(3000);
					size = size + elements.size();
				}
			}
			
			if(String.valueOf(size).equals(num))
			{
				return true;
			}else
			{
				errLog = errLog + "Actual search result is not the same with Expected result: " + size;
				return false;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}

	public boolean checkSearchNum(String fileName, String orignum, String showNum) {
		try {
			
			searchTextField.sendKeys(fileName);
			Thread.sleep(2000);
			
			searchBtn.click();
			Thread.sleep(5000);
			
			Select select = new Select(selector);
			select.selectByValue(showNum);
			Thread.sleep(2000);
			int size = elements.size();
			
			if(Integer.valueOf(orignum)>(Integer.valueOf(showNum)) && Integer.valueOf(size)==(Integer.valueOf(showNum)))
			{
				return true;
			}else if (Integer.valueOf(orignum)<=(Integer.valueOf(showNum)) && Integer.valueOf(size)==(Integer.valueOf(orignum))) {
				return true;
			}else {
				return false;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			errLog = e.getMessage();
			return false;
		}
	}
	
	public boolean searchFileToOpen(String fileName, String filePath)
	{
		try {
			searchTextField.sendKeys(fileName);
			Thread.sleep(2000);
			
			searchBtn.click();
			Thread.sleep(5000);
		
			int size = elements.size();
			Thread.sleep(2000);
			
			while (nextBtn.getAttribute("class").equals("paginate_enabled_next")) {
				nextBtn.click();
				Thread.sleep(3000);
				size = size + elements.size();
			}
			
			while (previousBtn.getAttribute("class").equals("paginate_enabled_previous")) {
				previousBtn.click();
				Thread.sleep(3000);
			}
			int a = size/10;
			int b = size%10;
			if(b>0)
			{
				a++;
			}	
			if(a>1)
			{
				for(int i=0; i<a; i++)
				{
					if(findFile(filePath))
					{
						break;
					}else
					{
						nextBtn.click();	
					}		
				}
			}
			
			if(a==1)
			{
				findFile(filePath);
			}
			
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			errLog = "Exception:  "+e.getMessage();
			return false;
		}
	}
	
	//open file if find file
	public boolean findFile(String filePath) {
		try {
			driver.findElement(By.xpath("//table[@class='ember-view dataTable']/tbody/tr/td/a[contains(text(),'"+filePath+"')]")).click();
			/*Thread.sleep(6000);
			RMSCommon.switchTowindow(driver);
			
			driver.manage().window().maximize();
			Thread.sleep(3000);
			driver.get("javascript:document.getElementById('overridelink').click();");*/
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
}
