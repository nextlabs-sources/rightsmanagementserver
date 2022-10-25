package com.test.nextlabs.rms.qa.testAutomation.webpages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DownloadRMCPage {
	
	@FindBy(id = "description")
	private static WebElement description;
	
	@FindBy(css = "a[href='/RMS/RMSViewer/DownloadRMCPackage']>button")
	private static WebElement downloadButton;
	
	@FindBy(css = "div>button[class*='btn btn-sc']")
	private static WebElement homeButton;
	
	//click download button to download RMC package
	public void clickDownloadButton() throws InterruptedException {
		downloadButton.click();
		Thread.sleep(8000);
	}
	
	//click home button to retun homepage
	public void  clickHomeButton() {
		homeButton.click();
	}
	
}
