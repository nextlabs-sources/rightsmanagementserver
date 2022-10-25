package com.nextlabs.rms.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.entity.setting.ServiceProviderAttributeDO;
import com.nextlabs.rms.entity.setting.ServiceProviderDO;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.persistence.PersistenceManager;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UtilMethods;
import com.nextlabs.rms.util.ZipUtil;

public class ConfigureSharePointAppCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(ConfigureSharePointAppCommand.class);
	
	public static final String SHAREPOINT_ONLINE_APP_NAME="SecureCollaboration_SPOnlineApp.zip";
	public static final String SHAREPOINT_ON_PREMISE_APP_NAME="SecureCollaboration_SPOnPremiseApp.zip";
	public static final String SHAREPOINT_ONLINE_REPOSITORY_APP_NAME="SecureCollaboration_SPOnlineRepositoryApp.zip";
	public static final String SP_DATA_DIR = "sharepoint";
	private static final String SP_APP_VERSION_FILE = "spVersionFile";
	private static final String SP_ONLINE_REPO_APP_VERSION_FILE = "spOnlineRepoVersionFile";
	private static final String SP_APP_VERSION_FILE_ON_PREMISE = "spVersionFileOnPremise";

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		try{
			String type=request.getParameter("type");			
			String appPath="";
			if (ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.name().equals(type)) {
				appPath = GlobalConfigManager.getInstance().getDataDir() + File.separator + SP_DATA_DIR + File.separator
						+ SHAREPOINT_ONLINE_APP_NAME;
			}else if(ServiceProviderType.SHAREPOINT_ONLINE.name().equals(type)){
				appPath = GlobalConfigManager.getInstance().getDataDir() + File.separator + SP_DATA_DIR + File.separator
						+ SHAREPOINT_ONLINE_REPOSITORY_APP_NAME;
			}
			if (ServiceProviderType.SHAREPOINT_CROSSLAUNCH.name().equals(type)) {
				appPath = GlobalConfigManager.getInstance().getDataDir() + File.separator + SP_DATA_DIR + File.separator
						+ SHAREPOINT_ON_PREMISE_APP_NAME;
			}
			String serviceProviderId = request.getParameter("id");
			long id = 0;
			if(serviceProviderId!=null){
				id = Long.parseLong(serviceProviderId);
			}
			ServiceProviderDO fetchServiceProviderByServiceProviderId = PersistenceManager.getInstance().fetchServiceProviderByServiceProviderId(id);
			List<ServiceProviderAttributeDO> attributes = fetchServiceProviderByServiceProviderId.getAttributes();
			Map<String,String> attributeMap = new HashMap<String, String>();
			for(ServiceProviderAttributeDO attribute: attributes){
				attributeMap.put(attribute.getName(), attribute.getValue());
			}
			//Unzip files into a folder
			String unzippedApp=appPath.substring(0,appPath.lastIndexOf(".zip"));
			StringTokenizer unzippedAppTokenizer=new StringTokenizer(unzippedApp,"/\\");
			String unzippedAppName="";
			while(unzippedAppTokenizer.hasMoreTokens()){
				unzippedAppName=unzippedAppTokenizer.nextToken();
			}
			String unzippedAppPath=(String)request.getSession().getAttribute(AuthFilter.USER_TEMP_DIR)+File.separator+unzippedAppName;
			ZipUtil.getInstance().unZip(appPath,unzippedAppPath);
			//Read the appManifest.xml file and change the values
			File appFile=new File(unzippedAppPath+File.separator+"AppManifest.xml");
			String newClientId=attributeMap.get(ServiceProviderSetting.APP_ID);
			String appRedirectUrl=attributeMap.get(ServiceProviderSetting.REDIRECT_URL);
			
			if (!StringUtils.hasText(appRedirectUrl)) {
				appRedirectUrl = UtilMethods.getServerUrl(request);
			}
			
			String newStartPageUrl=appRedirectUrl+GlobalConfigManager.RMS_CONTEXT_NAME+"/SharepointApp.jsp";//request.getParameter("newStartPageUrl");
			String remoteWebUrl=attributeMap.get(ServiceProviderSetting.REMOTE_WEB_URL);
			String appMenuDisplayString=attributeMap.get(ServiceProviderSetting.APP_DISPLAY_MENU);
			
			if (!StringUtils.hasText(appMenuDisplayString)) {
				appMenuDisplayString = RMSMessageHandler.getClientString("sp_default_app_display_menu");
			}
			
			modifyManifestFile(appFile,newClientId,newStartPageUrl,type);
			File unzippedDir=new File(unzippedAppPath);
			File[] listOfFiles= unzippedDir.listFiles();
			StringBuilder redirectUrl = new StringBuilder();
			RMSUserPrincipal userPrincipal = (RMSUserPrincipal) request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			String tenantId = userPrincipal.getTenantId();
			if(tenantId==null || tenantId.length()==0){
				tenantId = GlobalConfigManager.DEFAULT_TENANT_ID;
			}
			if(type.equals(ServiceProviderType.SHAREPOINT_CROSSLAUNCH.name())){
				redirectUrl.append(remoteWebUrl);
				redirectUrl.append("?isFromMenuItem=yes&SPHostUrl={HostUrl}&siteName={HostUrl}&path={ItemUrl}&clientID={ClientID}&RMSURL=");
				redirectUrl.append(appRedirectUrl);
				redirectUrl.append(GlobalConfigManager.RMS_CONTEXT_NAME);
				redirectUrl.append("/SharePointAuth/OnPremiseAuth");
			}
			else if(type.equals(ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.name())){
				redirectUrl.append(appRedirectUrl);
				redirectUrl.append(GlobalConfigManager.RMS_CONTEXT_NAME);
				redirectUrl.append("/ExternalViewer.jsp?siteName={HostUrl}&path={ItemUrl}&clientID=");
				redirectUrl.append(newClientId);
				redirectUrl.append("&tenantID=");
				redirectUrl.append(tenantId);
			}
			//Read the element file and change the value
			for(File file : listOfFiles){
				if(file.getName().contains("elements")){
					modifyElementFile(redirectUrl.toString(),appMenuDisplayString,file);
					break;
				}
			}
			//zip back the file
			ZipUtil.zipFolder(unzippedAppPath,unzippedAppPath+".zip");
			writeToStream(response,unzippedAppPath+".zip");
		}
		catch(Exception e){
			response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+RMSMessageHandler.getClientString("appCreateErr"));
			logger.error(e);
		}
	}

	private void writeToStream(HttpServletResponse response, String zippedAppPath ) throws IOException {
		StringTokenizer strTokenizer=new StringTokenizer(zippedAppPath,File.separator);
		String pathName=null;
		while(strTokenizer.hasMoreTokens()){
			pathName=strTokenizer.nextToken();
			logger.debug(pathName);
		}
		File file=new File(zippedAppPath);
        FileInputStream fileIn = new FileInputStream(file);
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Transfer-Encoding","binary");
        response.setHeader("Content-Disposition",
        "attachment;filename="+pathName);
        response.setContentLength((int)file.length());
        ServletOutputStream out = response.getOutputStream();   
        byte[] outputByte = new byte[4096];
        //copy binary content to output stream
        while(fileIn.read(outputByte, 0, 4096) != -1)
        {
        	out.write(outputByte, 0, 4096);
        }
        fileIn.close();
        out.flush();
        out.close();
	}

	private void modifyElementFile(String redirectUrl, String appMenuDisplayString, File elementFile) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(elementFile);
		doc.getDocumentElement().normalize();
		doc.setXmlStandalone(true);
		if(redirectUrl!=null){
			NodeList urlActionList = doc.getElementsByTagName("UrlAction");
			Node urlActionNode=urlActionList.item(0);
			Element urlActionElement=(Element)urlActionNode;
			String url=urlActionElement.getAttribute("Url");
			logger.debug("Old URL is "+url);
			urlActionElement.setAttribute("Url", redirectUrl);
			logger.debug("New URL is "+urlActionElement.getAttribute("Url"));
			
			NodeList customActionList = doc.getElementsByTagName("CustomAction");
			Node customActionNode=customActionList.item(0);
			Element customActionElement=(Element)customActionNode;
			String title=customActionElement.getAttribute("Title");
			logger.debug("Old Title is "+title);
			customActionElement.setAttribute("Title", appMenuDisplayString);
			logger.debug("New Title is "+customActionElement.getAttribute("Title"));
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(elementFile);
		transformer.transform(source, result);
		logger.debug("Sharepoint App File updated.");
	}

	private void modifyManifestFile(File appFile, String newClientId, String newStartPageUrl,String type) throws ParserConfigurationException, SAXException, IOException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(appFile);
			doc.getDocumentElement().normalize();
			doc.setXmlStandalone(true);
			logger.debug("Root element :" + doc.getDocumentElement().getNodeName());
			if(newClientId!=null){
				NodeList remoteWebAppList = doc.getElementsByTagName("RemoteWebApplication");
				Node remoteWebAppNode=remoteWebAppList.item(0);
				Element remoteWebAppElement=(Element)remoteWebAppNode;
				String clientId=remoteWebAppElement.getAttribute("ClientId");
				logger.debug("Old Client Id is "+clientId);
				remoteWebAppElement.setAttribute("ClientId", newClientId);
				logger.debug("New Client Id is "+remoteWebAppElement.getAttribute("ClientId"));
			}
			if(newStartPageUrl!=null){
				NodeList startPageList = doc.getElementsByTagName("StartPage");
				logger.debug("StartPage list length is "+startPageList.getLength());
				Node startPageNode=startPageList.item(0);
				Element startPageElement=(Element)startPageNode;
				logger.debug("Old StartPage URL is "+startPageElement.getTextContent());
				startPageElement.setTextContent(newStartPageUrl);
				logger.debug("New StartPage URL is "+startPageElement.getTextContent());
			}	
			int version = 0;
			logger.debug("About to change version number for SharePoint App");
			if(type.equalsIgnoreCase(ServiceProviderType.SHAREPOINT_ONLINE_CROSSLAUNCH.name())) {
				version=findVersion(newClientId, SP_APP_VERSION_FILE);
			} else if(type.equalsIgnoreCase(ServiceProviderType.SHAREPOINT_CROSSLAUNCH.name())){
				version=findVersion(newClientId, SP_APP_VERSION_FILE_ON_PREMISE);
			} else if(type.equalsIgnoreCase(ServiceProviderType.SHAREPOINT_ONLINE.name())){
				version=findVersion(newClientId, SP_ONLINE_REPO_APP_VERSION_FILE);
			}
			NodeList AppList = doc.getElementsByTagName("App");
			Node AppNode=AppList.item(0);
			Element AppElement=(Element)AppNode;
			String appCurrentVersion=AppElement.getAttribute("Version");
			logger.debug("The current version of the app is"+appCurrentVersion);
			String[] versionArray=appCurrentVersion.split(Pattern.quote("."));
			StringBuilder sb=new StringBuilder();
			for(int a=0;a<versionArray.length-1;a++){
				sb.append(versionArray[a]);
				sb.append(".");
			}
			String versionString=sb.append(String.valueOf(version)).toString();
			AppElement.setAttribute("Version", versionString);
			logger.debug("The new app version set in the XML file is "+AppElement.getAttribute("Version"));					
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(appFile);
			transformer.transform(source, result);
			logger.debug("Sharepoint App Manifest File updated.");
		} catch (Exception e) {
			logger.error("Error occurred while updating manifest file", e);
		}
		
	}
	
	private int findVersion(String clientId, String fileName) {			
		//Find the correct name based on OS				
		if(GlobalConfigManager.getInstance().isUnix){
			fileName="."+fileName;
		}
		File file=new File(GlobalConfigManager.getInstance().getDataDir(),fileName);
		logger.debug("SharePoint App version file is "+file.getPath());
		//If the file is not found
		if(!file.exists()){
			Properties prop = new Properties();
			OutputStream output = null;
			prop.setProperty(clientId, "0");
			try {
				output = new FileOutputStream(file);
				prop.store(output, null);
				if(!GlobalConfigManager.getInstance().isUnix){
					Process hide = Runtime.getRuntime().exec("attrib +H " + file.getPath());
					hide.waitFor();
				}
			} catch (Exception e) {
				logger.error("Error occured while creating Sharepoint App version file",e);
			}finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
			logger.debug("SharePoint App Version file created");
			return 0;
		}else{//If file exists
			//Read the file
			Properties prop = new Properties();
			OutputStream output = null;
			FileInputStream input=null;
			int newVersion=0;
			try {
				if(!GlobalConfigManager.getInstance().isUnix){
					Process unhide = Runtime.getRuntime().exec("attrib -H " + file.getPath());
					unhide.waitFor();
				}
				input = new FileInputStream(file);
				// load a properties file
				prop.load(input);
				//Check the version
				String version=prop.getProperty(clientId);
				input.close();
				output = new FileOutputStream(file);
				if(version!=null){
					newVersion=Integer.parseInt(version);
					prop.setProperty(clientId, Integer.toString(++newVersion));
				}
				else{
					prop.setProperty(clientId, Integer.toString(0));
				}
				//Update the file 
				prop.store(output, null);
				if(!GlobalConfigManager.getInstance().isUnix){
					Process hide = Runtime.getRuntime().exec("attrib +H " + file.getPath());
					hide.waitFor();
				}
			} catch (Exception e) {
				logger.error("Error occured while updating SharePoint App version file",e);
			} finally {
				if (output != null) {
					try {
						input.close();
						output.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
			//If update is successful, return the correct version else return 0
			return newVersion;
		}
	}
}

