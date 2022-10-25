package com.nextlabs.rms.repository;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.java.security.SSLProtocolSocketFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.microsoft.schemas.sharepoint.soap.GetListCollection;
import com.microsoft.schemas.sharepoint.soap.GetListCollectionResponse;
import com.microsoft.schemas.sharepoint.soap.GetListItems;
import com.microsoft.schemas.sharepoint.soap.GetListItemsResponse;
import com.microsoft.schemas.sharepoint.soap.ListsStub;
import com.microsoft.schemas.sharepoint.soap.QueryOptions_type0;
import com.microsoft.schemas.sharepoint.soap.ViewFields_type0;
import com.microsoft.webservices.sharepoint.queryservice.QueryServiceStub;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.JCIFS_NTLMScheme;
import com.nextlabs.rms.entity.setting.ServiceProviderType;
import com.nextlabs.rms.eval.EvaluationHandler;
import com.nextlabs.rms.repository.exception.FileNotFoundException;
import com.nextlabs.rms.sharedutil.SelfSignedTrustManager;
import com.nextlabs.rms.util.HTTPUtil;

import search.microsoft.Query;
import search.microsoft.QueryResponse;

public class SharePointClient {

	// TODO: This is a dirty workaround for the issue where the Credentials are
	// being cached by JDK.
	// Need to find a cleaner solution for this issue.
	static class MyCache implements sun.net.www.protocol.http.AuthCache {
		public void put(String pkey,
				sun.net.www.protocol.http.AuthCacheValue value) {

		}

		public sun.net.www.protocol.http.AuthCacheValue get(String pkey,
				String skey) {
			return null;
		}

		public void remove(String pkey,
				sun.net.www.protocol.http.AuthCacheValue entry) {

		}
	}

	static {
		sun.net.www.protocol.http.AuthCacheValue.setAuthCache(new MyCache());

	}
	private long repoId;
	private String repoName;
	private String basesharepointUrl = "";
	private String subSitePath="";
	private ListsStub listsoapstub;
//	private String proxyHost = "";
//	private String proxyPort = "";
	private QueryServiceStub queryServiceStub;
	private CloseableHttpClient httpclient = null;
	private HttpHost target = null;
	private HttpClientContext context = null;
	private String userName;
	private String password;
	private String domain;
	private String scheme = "http";
	private String spHostName;
	private int spPortNumber = 80;
	private int spConnTimeOut = -1; 
	private static Logger logger = Logger.getLogger(SharePointClient.class);
	private int searchlimit=GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.SHAREPOINT2013_SEARCHLIMITCOUNT);;
	private String folderQueryOpt = "<QueryOptions><Folder>{URL}</Folder></QueryOptions>";  
	private String searchQueryOpt2013Count = "<QueryPacket xmlns=\"urn:Microsoft.Search.Query\"><Query><Context><QueryText language=\"en-us\" type=\"STRING\">{0} IsDocument:0 IsDocument:1</QueryText></Context><Range><Count>"+searchlimit+"</Count></Range><TrimDuplicates>false</TrimDuplicates></Query></QueryPacket>";
	private String searchQueryOpt2013 = "<QueryPacket xmlns=\"urn:Microsoft.Search.Query\"><Query><Context><QueryText language=\"en-us\" type=\"STRING\">{0} IsDocument:0 IsDocument:1</QueryText></Context><TrimDuplicates>false</TrimDuplicates></Query></QueryPacket>";
	private String searchQueryOpt2010 = "<QueryPacket xmlns=\"urn:Microsoft.Search.Query\"><Query><Context><QueryText language=\"en-us\" type=\"STRING\">{0} IsDocument:0 IsDocument:1</QueryText></Context><Range><Count>{1}</Count></Range><TrimDuplicates>false</TrimDuplicates></Query></QueryPacket>";
	private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private boolean isSP2013=false;
	private boolean isSP2010=false;
	private Set<String> docLibFlags=null;
	private HashSet<String> rootDocLib=null;
	private HashSet<String> docLibs=new HashSet<String>();
	public SharePointClient(String userName, char[] password, String domain,
			long repoId, String baseSharePointURL,String repoName) {
		try {
			if (baseSharePointURL.endsWith("/")) {

				this.basesharepointUrl = baseSharePointURL;
			} else {
				this.basesharepointUrl = baseSharePointURL + "/";

			}
			if (baseSharePointURL.toLowerCase().startsWith("https:")){
				this.basesharepointUrl = Pattern.compile("^https?:", Pattern.CASE_INSENSITIVE).matcher(this.basesharepointUrl).replaceFirst("https:");
			} else if(baseSharePointURL.toLowerCase().startsWith("http:")){
				this.basesharepointUrl = Pattern.compile("^http?:", Pattern.CASE_INSENSITIVE).matcher(this.basesharepointUrl).replaceFirst("http:");
			}
			this.userName = userName;
			this.password = new String(password);
			this.domain = domain;
			this.repoId=repoId;
			this.repoName=repoName;
			initialize();
		} catch (Exception e) {
			logger.error("Error occured while initializing SharePoint client",
					e);
		}
	}

	public List<SearchResult> searchsp(String searchText, String repoDispName) throws Exception {
		logger.debug("search criteria text is "+searchText.trim());
		if(queryServiceStub==null){
			initialize();
		}
		if(rootDocLib==null){
			getListCollection();
		}
		Query queryEx10 = new Query();
		int searchResCount=GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.SHAREPOINT_SEARCHRESULT_COUNT);
		if(searchResCount==-1){
			searchResCount = 10000;
		}
		//The search count is added for sp 2010
		boolean sp13Search=GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.SHAREPOINT2013_SEARCHWITHCOUNT);
		String[] parameters={searchText,Integer.toString(searchResCount)};
		if(isSP2013 ){
			if(sp13Search)
				queryEx10.setQueryXml(MessageFormat.format(searchQueryOpt2013Count, searchText));
			else
				queryEx10.setQueryXml(MessageFormat.format(searchQueryOpt2013, searchText));
		}
		//So the query would be made to SP2010 by default for the first time
		else{
			queryEx10.setQueryXml(MessageFormat.format(searchQueryOpt2010, parameters));
		}
		QueryResponse result = queryServiceStub.query(queryEx10);
		if(logger.isDebugEnabled()){
			logger.debug("Search Results:"+result.getQueryResult());
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dom = factory.newDocumentBuilder();
		String result1 = result.getQueryResult();
		if(!result1.startsWith("<?xml version=")){
			result1 = xmlHeader + result1;			
		}
		Document doc = dom.parse(new InputSource(new ByteArrayInputStream(
				result1.getBytes("utf-8"))));
		if(!isSP2010 && !isSP2013){
			NodeList errorNode=doc.getElementsByTagName("DebugErrorMessage");
			String error="";
			if(errorNode.getLength()!=0){
				NodeList errorChildren = errorNode.item(0).getChildNodes();	
				error=errorChildren.item(0).getTextContent();
				if(error.equalsIgnoreCase("System.ArgumentOutOfRangeException")){
					isSP2010=false;
					isSP2013=true;
					logger.debug("The Sharepoint version is 2013");
					if(sp13Search)
						queryEx10.setQueryXml(MessageFormat.format(searchQueryOpt2013Count, searchText));
					else
						queryEx10.setQueryXml(MessageFormat.format(searchQueryOpt2013, searchText));
					//queryEx10.setQueryXml(MessageFormat.format(searchQueryOpt2013Count, searchText));
					result = queryServiceStub.query(queryEx10);
					result1=result.getQueryResult();
					logger.debug("SP13 Search results are "+result1);
					doc = dom.parse(new InputSource(new ByteArrayInputStream(
							result1.getBytes("utf-8"))));
				}
			}
			else{
				isSP2013=false;
				isSP2010=true;
			}
		}
		NodeList tags = doc.getElementsByTagName("Document");
		List<SearchResult> srList = new ArrayList<SearchResult>();
		SearchResult sr;
		String fileExt="";
		for (int i = 0; i < tags.getLength(); i++) {
			NodeList children = tags.item(i).getChildNodes();
			if(isSP2013){
				for(int a=0;a<children.getLength();a++){
					if("Action".equalsIgnoreCase(children.item(a).getNodeName())){
						NodeList actionChildren=children.item(a).getChildNodes();
						for(int b=0;b<actionChildren.getLength();b++){
							if("LinkUrl".equalsIgnoreCase(actionChildren.item(b).getNodeName())){
								Node linkUrl=actionChildren.item(b);
								NamedNodeMap map=linkUrl.getAttributes();
								if(map!=null && map.getNamedItem("fileExt")!=null){
									fileExt=map.getNamedItem("fileExt").getNodeValue();
								}
								break;
							}
						}
						break;
					}
				}
			}
			if (children.getLength() > 2) {
				if (children.item(0) != null
						&& children.item(0).getNodeName()
								.equalsIgnoreCase("Title")) {
					String title = children.item(0).getTextContent().trim();
					logger.debug("Document title is "+title);
					if(isSP2013 && !fileExt.trim().equals("")){
						title+="."+fileExt.trim();
						logger.debug("The title after adding extension: "+title);
					}
					boolean allow = false;
					// StringTokenizer here
					StringTokenizer searchTokenizer=new StringTokenizer(searchText," ");
					while (searchTokenizer.hasMoreTokens()) {
						String token = searchTokenizer.nextToken().trim();
						if (!token.equals("") && Pattern.compile(Pattern.quote(token), Pattern.CASE_INSENSITIVE).matcher(title).find()) {
							allow = true;
							break;
						}
					}
					if (allow) {
						logger.debug("Search criteria matches for "+title);
						logger.debug("children.item(1)  null:"+children.item(1)==null);
						if (children.item(1) != null && children.item(1).getNodeName().equalsIgnoreCase("Action")) {
							String path = children.item(1).getTextContent();
							String[] basePathArray = basesharepointUrl.split("/");
							String[] pathArray = path.split("/");
							String baseSubsiteString = "";
							String pathSubsiteString = "";
							for (int j=3; j < basePathArray.length; j++) {
								baseSubsiteString+=basePathArray[j]+"/";
							}
							if (pathArray.length>=basePathArray.length && pathArray.length > 3) {
								for (int j=3; j < basePathArray.length; j++) {
									pathSubsiteString += pathArray[j] + "/";
								}
							}
							if (baseSubsiteString.equalsIgnoreCase(pathSubsiteString)) {
								logger.debug("Creating a search object for" + title);
								sr = new SearchResult();
								sr.setName(title);
								sr.setFileType(EvaluationHandler.getOriginalFileExtension(title));
								sr.setProtectedFile(title.toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
								path = "";
								for (int a=basePathArray.length; a < pathArray.length; a++) {
									path += pathArray[a] + "/";
								}
								path = path.substring(0, path.length()-1);
								sr.setRepoId(repoId);
								sr.setRepoName(repoName);
								sr.setRepoType(ServiceProviderType.SHAREPOINT_ONPREMISE.name());
								String[] paths = path.split("/");
								String libPath = paths[0];
								String updatedPath = path;
								if (paths[0].equals("Shared Documents") && docLibs.contains("Documents")) {
									libPath = "Documents";
									updatedPath = path.replace("Shared Documents", "Documents");
								}
								if ( (libPath.equalsIgnoreCase("sites")) || !docLibs.contains(libPath)) {
									continue;
								}
								sr.setPath("/"+updatedPath);
								sr.setPathId("/"+updatedPath);
								if (title.indexOf(".") == -1) {
									sr.setFolder(true);
									srList.add(sr);
								}
								else {
									srList.add(sr);
									logger.debug(sr.getName());
									logger.debug(sr.getPath());
								}
								logger.debug("The reult being displayed is: "+sr.getPath());
							}
						}
					}
					else {
						logger.debug("Search criteria doesn't match for "+title);
					}
				}
			}
		}
		logger.debug("The search result list for this SP has "+srList.size()+" elements");
		if(logger.isDebugEnabled()){
			for(int a=0;a<srList.size();a++){
				logger.debug("result "+a+"	"+srList.get(a));
			}			
		}
		return srList;
	}

	private void initialize() throws Exception {
		logger.info("basesharepointUrl :" + basesharepointUrl);
		docLibFlags=new HashSet<String>();
		docLibFlags.add("4104");
		docLibFlags.add("8392712");
		String configFlagIds= GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.SP_DOCLIB_FLAGID);
		if(configFlagIds.length()>0){
			String[] configFlagIdsArray=configFlagIds.split(",");
			for(String s: configFlagIdsArray){
				docLibFlags.add(s);
			}
		}
		String[] arr = basesharepointUrl.split("/");
		String hostNameWithPort = arr[2];
		String[] portArr = hostNameWithPort.split(":");
		spHostName = hostNameWithPort;
		if (portArr.length == 2) {
			spHostName = portArr[0];
			spPortNumber = Integer.parseInt(portArr[1]);
		}
		logger.info("Calling initContextForNTLM :");
		spConnTimeOut = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.SHAREPOINT_CONN_TIMEOUT_SECONDS);
		if(spConnTimeOut==-1){
			spConnTimeOut=100000;
		}else{
			spConnTimeOut = spConnTimeOut*1000;
		}
		StringTokenizer tokens = new StringTokenizer(basesharepointUrl, "/");
		tokens.nextToken();
		tokens.nextToken();
		subSitePath="";
		while(tokens.hasMoreTokens()){
			subSitePath += tokens.nextToken()+"/";
		}
		initContextForNTLM();
		/*
		 * String userNameWithDomain = userName; if (domain != null &&
		 * domain.length() > 0) { userNameWithDomain = domain + "\\" + userName;
		 * }
		 */
		/*
		 * Authenticator.setDefault(new SimpleAuthenticator(userNameWithDomain,
		 * password));
		 */
		// Authenticating and Opening the SOAP port of the Copy Web Service
		listsoapstub = getSPListSoapStub();
		queryServiceStub = getSPQueryServiceStub();
		getSPVersionNumber();
	}

	private void getSPVersionNumber() {
		HttpGet request = new HttpGet(basesharepointUrl); 
		try{
			HttpResponse response = httpclient.execute(request);
			logger.debug("Making request to "+basesharepointUrl+" to find the SharePoint Version");
			logger.debug("Response Code : " 
		                + response.getStatusLine().getStatusCode());
			Header[] headers=response.getAllHeaders();
			if(headers==null){
				isSP2013=true;
				return;
			}
			for(int i=0;i<headers.length;i++){
				if(headers[i].getName()!=null && headers[i].getValue()!=null && headers[i].getName().equals("MicrosoftSharePointTeamServices")){
					if(headers[i].getValue().startsWith("15")){
						isSP2013=true;
						break;
					}
					else{
						isSP2010=true;
						break;
					}
				}
			}
		}
		catch(Exception e){
			logger.error("Unable to get SharePointVersion",e);
		}
	}

	public QueryServiceStub getSPQueryServiceStub() throws Exception {
		QueryServiceStub stub = new QueryServiceStub(basesharepointUrl
				+ "_vti_bin/Search.asmx");

		stub._getServiceClient().getOptions()
				.setSoapVersionURI(Constants.URI_SOAP11_ENV);

		AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, JCIFS_NTLMScheme.class);
		HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
		List<String> authSchemes = new ArrayList<String>();
		authSchemes.add(HttpTransportProperties.Authenticator.NTLM);
		authenticator.setAuthSchemes(authSchemes);
		authenticator.setHost(spHostName);
		authenticator.setPort(spPortNumber);
		authenticator.setDomain(domain);
		authenticator.setUsername(userName);
		authenticator.setPassword(password);
		authenticator.setPreemptiveAuthentication(true);
		stub._getServiceClient().getOptions()
				.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
		stub._getServiceClient()
				.getOptions()
				.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
						new Integer(spConnTimeOut));
		if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.TRUST_SELF_SIGNED_CERTS)){
			Protocol prot = getCustomProtocol();
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER,
	                prot);
		}
		return stub;

	}

	private void initContextForNTLM() {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			logger.info(
					"Error occurred while trying to get name of localhost. Using blank string instead for NTLM.",
					e);
		}
		logger.info("Created Credential Provider");
		credsProvider.setCredentials(AuthScope.ANY, new NTCredentials(userName,
				password, hostName, domain));
		if (basesharepointUrl.startsWith("https:")) {
			scheme = "https";
		}
		target = new HttpHost(spHostName, spPortNumber, scheme);
		context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
		X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
		SSLContext sslcontext = SSLContexts.createSystemDefault();
		if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.TRUST_SELF_SIGNED_CERTS)){
			try {
				socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					    .register("http", PlainConnectionSocketFactory.INSTANCE)
					    .register("https", HTTPUtil.getTrustAllSocketFactory())
					    .build();
			} catch (Exception e) {
				logger.error("Error occured while using TrustAllSocketFactory. Using default.",e);
				socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					    .register("http", PlainConnectionSocketFactory.INSTANCE)
					    .register("https", new SSLConnectionSocketFactory(sslcontext, hostnameVerifier))
					    .build();
			} 
		}else{
			socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				    .register("http", PlainConnectionSocketFactory.INSTANCE)
				    .register("https", new SSLConnectionSocketFactory(sslcontext, hostnameVerifier))
				    .build();
		}
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		int maxConn = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.SHAREPOINT_MAX_CONNECTIONS);
		if(maxConn==-1){
			maxConn = 500;
		}				
		// Increase max total connection to 500
		connManager.setMaxTotal(maxConn);
		// Increase default max connection per route to 20
		connManager.setDefaultMaxPerRoute(maxConn);
		if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.TRUST_SELF_SIGNED_CERTS)){
			try {
				httpclient = HttpClients.custom()
					    .setConnectionManager(connManager)
					    .setSSLSocketFactory(HTTPUtil.getTrustAllSocketFactory())
					    .build();
			} catch (Exception e) {
				logger.error("Error occured while creating HTTPClient with Trust All SocketFactory. Creating a generic HTTPClient.",e);
				httpclient = HttpClients.custom()
					    .setConnectionManager(connManager)
					    .build();				
			} 		
		}else{
			httpclient = HttpClients.custom()
				    .setConnectionManager(connManager)
				    .build();
		}
	}

	public static URL convertToURLEscapingIllegalCharacters(String string) {
		try {
			//String decodedURL = URLDecoder.decode(string, "UTF-8");
			URL url = new URL(string);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(),
					url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			return uri.toURL();
		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}
	}

	public ListsStub getSPListSoapStub() throws Exception {
		ListsStub stub = new ListsStub(basesharepointUrl
				+ "_vti_bin/Lists.asmx");
		stub._getServiceClient().getOptions()
				.setSoapVersionURI(Constants.URI_SOAP11_ENV);
		AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, JCIFS_NTLMScheme.class);
		HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
		List<String> authSchemes = new ArrayList<String>();
		authSchemes.add(HttpTransportProperties.Authenticator.NTLM);
		authenticator.setAuthSchemes(authSchemes);
		authenticator.setHost(spHostName);
		authenticator.setPort(spPortNumber);
		authenticator.setDomain(domain);
		authenticator.setUsername(userName);
		authenticator.setPassword(password);
		authenticator.setPreemptiveAuthentication(true);
		stub._getServiceClient().getOptions()
				.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
		stub._getServiceClient().getOptions()
				.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
				new Integer(spConnTimeOut));
		if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.TRUST_SELF_SIGNED_CERTS) &&
                !basesharepointUrl.toLowerCase().startsWith("http:")) {
			Protocol prot = getCustomProtocol();
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER,
	                prot);
		}
		//Setting the 	 attempts to 0
		int retrycount=GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.SHAREPOINT_CONN_RETRY_ATTEMPTS);
		if(retrycount<=0){
			retrycount = 0;
		}
		HttpMethodParams methodParams = new HttpMethodParams();
		methodParams.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retrycount, false));
		stub._getServiceClient().getOptions()
				.setProperty(HTTPConstants.HTTP_METHOD_PARAMS, methodParams);
		return stub;
	}

	private Protocol getCustomProtocol() {
		SSLContext ctx;
		try {
		    ctx = SSLContext.getInstance("SSL");
		    SelfSignedTrustManager sstm = new SelfSignedTrustManager();
		    TrustManager[] tmArr = {sstm};
		    ctx.init(null, tmArr, null);
		} catch (Exception e) {
		    logger.error("Exception loading Bold trust store", e);
		    throw new RuntimeException(e);
		}
		SSLProtocolSocketFactory sslFactory = new SSLProtocolSocketFactory(ctx);
		Protocol prot = new Protocol("https",
		        (ProtocolSocketFactory) sslFactory, 443);
		return prot;
	}

	/**
	 * Creates a string from an XML file with start and end indicators
	 * 
	 * @param docToString
	 *            document to convert
	 * @return string of the xml document
	 */
	public static String xmlToString(Document docToString) {
		String returnString = "";
		try {
			// create string from xml tree
			// Output the XML
			// set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans;
			trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult streamResult = new StreamResult(sw);
			DOMSource source = new DOMSource(docToString);
			trans.transform(source, streamResult);
			String xmlString = sw.toString();
			// print the XML
			returnString = returnString + xmlString;
		} catch (TransformerException ex) {
			logger.error(
					"Error occurred while converting XML to String:"
							+ ex.getMessage(), ex);
		}
		return returnString;
	}

	/**
	 * 
	 * @param theXML
	 * @return
	 * @throws Exception
	 */
	protected static Node createSharePointCAMLNode(String theXML)
			throws Exception {
		logger.debug("CAML is: \n" + theXML);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.parse(new InputSource(
				new StringReader(theXML)));
		Node node = document.getDocumentElement();
		return node;
	}

	/**
	 * Connects to a SharePoint Lists Web Service through the given open port,
	 * and get all document libraries.
	 * 
	 * @throws Exception
	 */
	public List<RepositoryContent> getListCollection() throws Exception {
		List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
		try {
			GetListCollection getListCollection = new GetListCollection();
			GetListCollectionResponse response = listsoapstub
					.getListCollection(getListCollection);
			OMElement omElement = response.getGetListCollectionResult()
					.getExtraElement();
			Element node = toElement(omElement);
			Document document = node.getOwnerDocument();
			logger.debug("SharePoint Online Lists Web Service Response for getListCollection:"
					+ xmlToString(document));
			NodeList list = node.getElementsByTagName("List");
			logger.debug("LISTS Size" + list.getLength());
			boolean isFilterDefaultLib = GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.SHOW_DEFAULTLIBRARY_SHAREPOINT);			
			// Displaying every result received from SharePoint, with its ID
			for (int i = 0; i < list.getLength(); i++) {
				NamedNodeMap attributes = list.item(i).getAttributes();
				if (attributes.getNamedItem("ServerTemplate") == null
						|| !(attributes.getNamedItem("ServerTemplate")
								.getNodeValue().equals("101"))
						|| attributes.getNamedItem("Hidden") == null
						|| !(attributes.getNamedItem("Hidden").getNodeValue()
								.equalsIgnoreCase("False"))
						|| attributes.getNamedItem("IsApplicationList") == null
						|| !(attributes.getNamedItem("IsApplicationList")
								.getNodeValue().equalsIgnoreCase("False"))
						||(!isFilterDefaultLib && !docLibFlags.contains(attributes.getNamedItem("Flags")
						.getNodeValue()))){
					continue;
				}
				RepositoryContent repoContent = new RepositoryContent();
				repoContent.setFolder(true);
				if (attributes.getNamedItem("Title") != null) {
					String title = attributes.getNamedItem("Title")
							.getNodeValue();
					repoContent.setName(title);
					repoContent.setPathId("/"+title);
					repoContent.setPath("/"+title);
					repoContent.setRepoId(repoId);
					repoContent.setRepoName(repoName);
					docLibs.add(title);
					logger.debug("Title" + ": " + title);
					try{
						if(attributes.getNamedItem("Modified")
								.getNodeValue() != null){
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
							Date date = sdf.parse(attributes.getNamedItem("Modified").getNodeValue());
							repoContent.setLastModifiedTime(date.getTime());
						}
					}catch(ParseException ex){
						logger.error("Cannot parse the last modified time");
						repoContent.setLastModifiedTime(null);
					}		
				}
				contentList.add(repoContent);
			}

		} catch (Exception ex) {
			throw ex;
		}
		if(rootDocLib==null){
			rootDocLib=new HashSet<String>();
		}
		for(RepositoryContent content:contentList){
			StringTokenizer startPathTokenizer = new StringTokenizer(content.getPath(),"/");
			
			rootDocLib.add(startPathTokenizer.nextToken());
		}
		return contentList;
	}

	/**
	 * This method converts OMElement to Element
	 * 
	 * @param resRoot
	 * @return a dom element
	 * @throws XMLStreamException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private Element toElement(OMElement resRoot) throws XMLStreamException,
			SAXException, IOException, ParserConfigurationException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		resRoot.serialize(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(bais).getDocumentElement();
	}

	/**
	 * Connects to a SharePoint Lists Web Service through the given open port,
	 * and reads all the elements of the given list. Only the given column names
	 * are displayed.
	 * @throws Exception 
	 */
	public List<RepositoryContent> getFileList(String path, long repoId, String repoName) throws Exception {
		try {
			if(path.startsWith("/")){
				path = path.replaceFirst("/", "");
			}
			if(listsoapstub==null){
				initialize();
			}
			String rowLimit = "10000";
			String folderURL = "";
			String listName = "";
			
			if (path == null || path.length() == 0 || path.equals("/")) {
				return getListCollection();
			} else {
				String updatedPath= path;
				if (path.contains("/")) {
					StringTokenizer tokens = new StringTokenizer(path, "/");
					listName = tokens.nextToken();
					if(path.startsWith("Documents")){
						updatedPath=path.replace("Documents", "Shared Documents");
					}
					folderURL=basesharepointUrl+updatedPath;
				} else {
					listName = path;
					folderURL = basesharepointUrl + path;
				}
			}
			String queryOpt = folderQueryOpt.replace("{URL}", folderURL);
			Node queryoptionsnode = createSharePointCAMLNode(queryOpt);
			QueryOptions_type0 query = new QueryOptions_type0();
			query.setExtraElement(XMLUtils.toOM((Element) queryoptionsnode));
			logger.debug("root:" + query.getExtraElement());
			GetListItems request = new GetListItems();
			request.setRowLimit(rowLimit);
			request.setQueryOptions(query);
//			if(docLibs.contains("Documents")){
//				listName="Documents";
//			}
			request.setListName(listName);
			request.setQuery(null);
			ViewFields_type0 viewFields = new ViewFields_type0();
			String viewFieldsXML = "<ViewFields Properties=\"True\"><FieldRef Name=\"FileLeafRef\"></FieldRef><FieldRef Name=\"File_x0020_Size\"></FieldRef><FieldRef Name=\"LinkFilename\"></FieldRef></ViewFields>";
			Node viewFieldsNode = createSharePointCAMLNode(viewFieldsXML);
			viewFields.setExtraElement(XMLUtils.toOM((Element)viewFieldsNode));
			request.setViewFields(viewFields);
			GetListItemsResponse result = listsoapstub.getListItems(request);
			Object listResult = result.getGetListItemsResult()
					.getExtraElement();
			logger.debug("Web Service Authorized:SharePoint Online Lists Web Service Response:"
					+ listResult);

			if ((listResult != null) && (listResult instanceof OMElement)) {
				OMElement resRoot = (OMElement) listResult;
				// print((OMElement) listResult,0);
				Element node = toElement(resRoot);

				// Dumps the retrieved info in the console
				Document document = node.getOwnerDocument();
				logger.debug("SharePoint Online Lists Web Service Response:"
						+ xmlToString(document));
				// selects a list of nodes which have z:row elements
				NodeList list = node.getElementsByTagName("z:row");
				logger.debug("=> " + list.getLength()
						+ " results from SharePoint Online");

				List<RepositoryContent> contentList = new ArrayList<RepositoryContent>();
				// Displaying every result received from SharePoint, with its ID
				for (int i = 0; i < list.getLength(); i++) {
					RepositoryContent repoContent = new RepositoryContent();

					// Gets the attributes of the current row/element
					NamedNodeMap attributes = list.item(i).getAttributes();
					
					if (attributes.getNamedItem("ows_LinkFilename") != null) {
						repoContent.setName(attributes.getNamedItem(
								"ows_LinkFilename").getNodeValue());						
					}
					if (attributes.getNamedItem("ows_File_x0020_Size") != null){
						String fileSizeStr = attributes.getNamedItem("ows_File_x0020_Size").getNodeValue();
						int indx = fileSizeStr.indexOf("#");
						if(indx<fileSizeStr.length()-1){
							fileSizeStr = fileSizeStr.substring(indx+1);
							try{
								long fileSize = Long.parseLong(fileSizeStr);
								repoContent.setFileSize(fileSize);
							}catch(Exception e){
								logger.error("Error occurred while getting FileSize Attribute for file:"+repoContent.getName());
							}							
						}
					}
					try{
						if(attributes.getNamedItem("ows_Modified")!=null){
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date date = sdf.parse(attributes.getNamedItem("ows_Modified").getNodeValue());

							repoContent.setFileType(EvaluationHandler.getOriginalFileExtension(attributes.getNamedItem(
									"ows_LinkFilename").getNodeValue()));
							repoContent.setProtectedFile(attributes.getNamedItem(
									"ows_LinkFilename").getNodeValue().toLowerCase().endsWith(EvaluationHandler.NXL_FILE_EXTN));
							repoContent.setLastModifiedTime(date.getTime());
						}
					}
					catch (ParseException e){
						logger.error("Cannot parse the last modified time");
						repoContent.setLastModifiedTime(null);
					}
					if (attributes.getNamedItem("ows_FileRef") != null) {
						int index = attributes.getNamedItem("ows_FileRef")
								.getNodeValue().indexOf("#");
						String buildPath=attributes
								.getNamedItem("ows_FileRef").getNodeValue()
								.substring(index + 1);
						//String temp=buildPath.toLowerCase().replace(subSitePath.toLowerCase(), "");
						
						Pattern p = Pattern.compile(subSitePath, Pattern.CASE_INSENSITIVE);
						Matcher m = p.matcher(buildPath);
						String temp = m.replaceAll("");
						String libName = temp.substring(0,temp.indexOf("/"));// --> listcollection name
						String replacedPath=temp;
						if(!docLibs.contains(libName)){
							if(temp.startsWith("Shared Documents")){
								replacedPath=temp.replace("Shared Documents", "Documents");
							}
						}
						repoContent.setPath("/"+replacedPath);
						repoContent.setPathId("/"+replacedPath);
						repoContent.setRepoId(repoId);
						repoContent.setRepoName(repoName);
						repoContent.setRepoType(ServiceProviderType.SHAREPOINT_ONPREMISE.name());
					}

					if (attributes.getNamedItem("ows_FSObjType") != null) {
						String objType = attributes.getNamedItem(
								"ows_FSObjType").getNodeValue();
						if (objType.endsWith("#1")) {
							repoContent.setFolder(true);
						} else {
							repoContent.setFolder(false);
						}
					}
					contentList.add(repoContent);
				}
				return contentList;
			} else {
				throw new Exception(
						listName
								+ " list response from SharePoint is either null or corrupt\n");
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	public void downloadFile(String filePath, BufferedOutputStream bos)
			throws ClientProtocolException, IOException, FileNotFoundException {
		logger.debug("filePath to download:" + filePath);
		if(filePath.startsWith("/")){
			filePath = filePath.replaceFirst("/", "");
		}
		String urlOfFile;
		String updatedPath=filePath;
		if(filePath.startsWith("Documents") && docLibs.contains("Documents")){
			updatedPath=filePath.replace("Documents", "Shared Documents");
		}
		urlOfFile = basesharepointUrl + updatedPath;
		logger.debug("url of file to download:" + urlOfFile);
		URL convertedurl = convertToURLEscapingIllegalCharacters(urlOfFile);
		HttpGet httpget = new HttpGet(convertedurl.toString());
		CloseableHttpResponse response = httpclient.execute(target, httpget,
				context);
		try {
			if (response.getStatusLine().getStatusCode() == 404) {
				String errMsg = filePath + " does not exist in the repository."; 
				logger.error(errMsg);
				throw new FileNotFoundException(errMsg);
			}
			HttpEntity entity1 = response.getEntity();
			byte[] byteArr = EntityUtils.toByteArray(entity1);
			bos.write(byteArr);
			logger.debug("Download completed!");
		} finally {
			response.close();
			bos.close();
		}
	}
}
