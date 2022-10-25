package com.test.nextlabs.rms.qa.testAutomation.common;

/*******************************************************
 * @author: Michelle Xu
 * Created Date: 18/08/2015
 * This is the base test setting and control the test cases
  *********************************************************************/


import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.test.nextlabs.rms.qa.testAutomation.webpages.*;

//This class is create the setup test suite, setup for each test
//This is a super class
public class BaseTest {
	
	protected WebDriver driver;
	protected LoginPage adminlogInPage;
	protected LoginPage normalLogInPage;
	protected HomePage homePage;
	
	public static Logger logger = LogManager.getLogger(BaseTest.class.getName());
	
	//protected ManageRepository managePage;
	protected String sMethodName;
	
	public BaseTest() {
		PropertyConfigurator.configure ("config/log4j.properties");
	}
	
	//@Parameters ({"Browser", "RMSServer", "Port", "AdminUsr", "AdminPsd", "UserDomain"})//specify need these parameters,if request not contains these,will not accept this request 
	@BeforeSuite(alwaysRun = true)//when alwaysRun is true,this test method always will be execute
    public void setupBeforeSuite(){
		File folder = new File("test-output");
		if(folder.exists())
		{
			folder.delete();
		}
		setupTestConfig ();
		RMSUtility.rmsURL= "https://" + RMSUtility.rmsServer + ":" + RMSUtility.rmsPort + "/RMS";
		logger.info("[Prepare]Get RMSURL: "+RMSUtility.rmsURL);
		// Create a folder to save the screen shot for failed test cases
		RMSUtility.createScreenShotFolder();
	}
	
	@BeforeMethod
	public void setUp(ITestContext result) throws InterruptedException {
		setupBeforeMethod();
		logger.info("[test cases name]>>>>>>>" + result.getName() + " is running");
	}
	
	public void setupBeforeMethod() throws InterruptedException {	
		driver = RMSUtility.settingDriver();
		logger.info("[Prepare]Get RMS Testing Browser: " + RMSUtility.testingBrowser);
		//get browser version or name
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		RMSUtility.bName = cap.getBrowserName().toLowerCase();
		logger.info("[BrowserName]: " + RMSUtility.bName);
		if(RMSUtility.bName.equals("internet explorer"))
		{
			RMSUtility.bName = "IE";
		}
		
		//get browser version
		RMSUtility.bVersion = cap.getVersion().toLowerCase();
		logger.info("[BrowserVersion]: " + RMSUtility.bVersion);
		
		//get platform
		RMSUtility.bplatform = cap.getPlatform().toString();
		logger.info("[RunningPlatform]: " + RMSUtility.bplatform);
		
		RMSUtility.bFullName = RMSUtility.bName + RMSUtility.bVersion;
		
		if(RMSUtility.bName.equals("chrome"))
		{
			RMSUtility.bFullName = RMSUtility.bName;
		}
	
		driver.get(RMSUtility.rmsURL);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		//driver.manage().window().maximize();
		// this method to handle the IE secure page before going to RMS page
		if (RMSUtility.testingBrowser.equals("IE"))
			driver.get("javascript:document.getElementById('overridelink').click();");
	}
	
	public void accessToSharepoint(){	
		driver = RMSUtility.settingDriver();
		logger.info("[Prepare]Get RMS Testing Browser: " + RMSUtility.testingBrowser);
		driver.get(RMSUtility.sharepointURL);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	@AfterMethod
	public void tearDown() {
		driver.quit();
	}
	
	@AfterSuite(alwaysRun = true)
	public void processAfterSuite (){
		/*try {
			Thread.sleep(6000);
			sendEmail();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	private static void setupTestConfig (){
		int i;
		String DirName = System.getProperty("user.dir");
		String XMLFile = DirName + "\\config\\config.xml";

		try {
			File configile = new File(XMLFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(configile);
			doc.getDocumentElement().normalize();

//			System.out.println("root of xml file:\t " + doc.getDocumentElement().getNodeName());
			NodeList nodes = doc.getElementsByTagName("parameters");
//			System.out.println("==========================");

			for (i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					RMSUtility.testingBrowser = getValue("Browser", element);
					RMSUtility.rmsServer = getValue("RMSServer", element);
					RMSUtility.sharepointURL = getValue("SharePointURL", element);
					RMSUtility.platFrom = getValue("PlatForm",element);
					RMSUtility.rmsPort = getValue("Port", element);
					RMSUtility.adminUsr = getValue("AdminUsr", element);
					RMSUtility.adminPsd = getValue("AdminPsd", element);
					RMSUtility.userDomain = getValue("UserDomain", element);
				}   
			}
			//f0.close() ;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("[Exception:] read config file error " + ex.getMessage());
		}
		finally  {
			//System.out.println("root of xml file") ;
		} 
		// Separate and get one user domain
		while (RMSUtility.userDomain.contains(",")) {
			i = RMSUtility.userDomain.indexOf(",");
			RMSUtility.lsUserDomains.add(RMSUtility.userDomain.substring(0, i));
			RMSUtility.userDomain = RMSUtility.userDomain.substring(i+1);
		}
		RMSUtility.lsUserDomains.add(RMSUtility.userDomain);
		
		// Random pick an user domain from the list for testing
		RMSUtility.userDomain = RMSUtility.lsUserDomains.get((int)(Math.random()*RMSUtility.lsUserDomains.size()));
																		
		logger.info("Browser: " + RMSUtility.testingBrowser);
		logger.info("RMSServer: " + RMSUtility.rmsServer);
		logger.info("SharepointURL: " + RMSUtility.sharepointURL);
		logger.info("RMS server platForm: "+RMSUtility.platFrom);
		logger.info("Port: " + RMSUtility.rmsPort);
		logger.info("AdminUser: " + RMSUtility.adminUsr);
		logger.info("AdminPassword: " + RMSUtility.adminPsd);
		logger.info("UserDomain: " + RMSUtility.userDomain);
	}
	
	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}

}
