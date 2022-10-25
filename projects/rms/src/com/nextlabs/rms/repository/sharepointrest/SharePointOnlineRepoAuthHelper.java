package com.nextlabs.rms.repository.sharepointrest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.SharePointClient;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.repository.exception.UnauthorizedRepositoryException;
import com.nextlabs.rms.util.JWTVerifier;

public class SharePointOnlineRepoAuthHelper {

	private static Logger logger = Logger.getLogger(SharePointOnlineRepoAuthHelper.class);

	private static final String IDENTIFIER = "00000003-0000-0ff1-ce00-000000000000";

	private static final String SECURITY_TOKEN_SERVICE_URL = "https://accounts.accesscontrol.windows.net/tokens/OAuth/2";

	public static Map<String, List<String>> getUserProperties(String accessToken, String spServer) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		URL url=SharePointClient.convertToURLEscapingIllegalCharacters("https://"+spServer+"/_api/SP.UserProfiles.PeopleManager/GetMyProperties");
		logger.debug(url.toString());
		HttpGet request = new HttpGet(url.toString());
		request.addHeader("Authorization", "Bearer "+accessToken);
		request.addHeader("accept", "application/json;odata=verbose");
		HttpResponse response = client.execute(request);
		logger.debug("Response Code for getting user properties: " + 
				response.getStatusLine().getStatusCode());
		logger.debug("Response Code  " + 
				response.toString());
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		response.getEntity().writeTo(outstream);
		byte [] responseBody = outstream.toByteArray();
		String responseString=new String(responseBody);
		logger.debug("response String is "+responseString);
		Map<String,List<String>> userPropsMap = new HashMap<String, List<String>>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		LinkedTreeMap<String, Object> gsonStr = gson.fromJson(responseString, LinkedTreeMap.class);
		LinkedTreeMap<String, Object> root = (LinkedTreeMap)gsonStr.get("d");
		String displayName=(((String)root.get("DisplayName")));
		userPropsMap.put("DisplayName", Arrays.asList(displayName));
		String email=(((String)root.get("Email")));
		userPropsMap.put("Email", Arrays.asList(email));
		Boolean isFollowed=(Boolean)root.get("IsFollowed");
		userPropsMap.put("IsFollowed", Arrays.asList(isFollowed.toString()));
		String latestPost =(String)root.get("LatestPost");
		userPropsMap.put("LatestPost", Arrays.asList(latestPost));
		String personalURL=(String)root.get("PersonalUrl");
		userPropsMap.put("PersonalUrl", Arrays.asList(personalURL));
		String pictureURL=(String)root.get("PictureUrl");
		userPropsMap.put("PictureUrl", Arrays.asList(pictureURL));
		String title=(String)root.get("Title");
		userPropsMap.put("Title", Arrays.asList(title));
		LinkedTreeMap<String, Object> userProps = (LinkedTreeMap<String, Object>)root.get("UserProfileProperties");
		ArrayList<LinkedTreeMap<String, Object>> results = (ArrayList<LinkedTreeMap<String, Object>>)userProps.get("results");
		for (LinkedTreeMap<String, Object> linkedTreeMap : results) {
			String key=(String)linkedTreeMap.get("Key");
			String val=(String)linkedTreeMap.get("Value");
			userPropsMap.put(key, Arrays.asList(val));
		}
		return userPropsMap;
	}

	public static Map<String,String> getOAuthToken(HttpServletRequest request, String secret) throws RMSException {
		String accessToken="";
		Map<String,String> tokens = null;
		try{
			tokens = new HashMap<>();
			String spAppToken=request.getParameter("SPAppToken");
			String appIdFromRequest=request.getParameter("clientID");
			JWTVerifier verifier= new JWTVerifier(secret);
			Map<String,Object> decodedPayload =verifier.verify(spAppToken);
			//Now decodedPayload has all the parameters required to make the post request
			String appctx=(String)decodedPayload.get("appctx");
			Map map = new Gson().fromJson(appctx, Map.class);
			String clientId=(String)decodedPayload.get("aud");
			String refreshToken=(String)decodedPayload.get("refreshtoken");
			String serverParameterName=request.getParameter("siteName");
			String spServer=serverParameterName;
			if(serverParameterName.indexOf("/")!=-1){
				spServer=serverParameterName.substring(0,serverParameterName.indexOf("/"));
			}
			String appCtxSender= ((String)decodedPayload.get("appctxsender"));
			String appId=appCtxSender.split("@")[1];
			
			RMSUserPrincipal userPrincipal = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
			if (userPrincipal != null) {
				SettingManager.saveSetting(userPrincipal.getTenantId(), ConfigManager.SP_ONLINE_APP_CONTEXT_ID, clientId);
				ConfigManager.getInstance(userPrincipal.getTenantId()).loadConfigFromDB();
			} else {
				logger.error("user principal null, cannot get tenantId to save " + ConfigManager.SP_ONLINE_APP_CONTEXT_ID);
			}
			Map resultMap = getAccessTokenFromRefreshToken(secret, clientId, refreshToken, spServer);
			accessToken=(String)resultMap.get(RepositoryManager.ACCESS_TOKEN);
			tokens.put(RepositoryManager.ACCESS_TOKEN, accessToken);
			tokens.put(RepositoryManager.REFRESH_TOKEN, refreshToken);
			tokens.put(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME, (String) resultMap.get("expires_on"));
			if(accessToken==null || accessToken.length()==0){
				logger.error("oAuth Token is null");
			}else{
				logger.debug("oAuth Token length is:"+accessToken.length());
			}
			//			logger.debug("OAuth token is "+accessToken);
			return tokens;
		}catch(Exception e){
			logger.error("Error occurred while getting oAuth Token", e);
			return tokens;
		}
	}

	public static Map getAccessTokenFromRefreshToken(String secret, String contextId, String refreshToken, String spServer)
			throws UnsupportedEncodingException, IOException, ClientProtocolException {
		
		String appId = contextId.split("@")[1];
		if(spServer.startsWith("https://")){
			spServer=spServer.substring(8);
		}
		spServer = spServer.split("/")[0];
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httppost = new HttpPost(SECURITY_TOKEN_SERVICE_URL);
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		params.add(new BasicNameValuePair("client_id", contextId));
		params.add(new BasicNameValuePair("client_secret", secret));
		params.add(new BasicNameValuePair("refresh_token", refreshToken));
		params.add(new BasicNameValuePair("resource", IDENTIFIER+"/"+spServer+"@"+appId));
		httppost.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse res =  (HttpResponse) httpclient.execute(httppost);
		logger.debug("Response Code from ACS for oAuth Request:" + 
				res.getStatusLine().getStatusCode());
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(((HttpEntity) res.getEntity()).getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		Map resultMap = new Gson().fromJson(result.toString(), Map.class);
		return resultMap;
	}
	
	public static void handleException(Exception e) throws RepositoryException, RMSException{
		if (e instanceof SPRestServiceException) {
			SPRestServiceException ex = (SPRestServiceException) e;
			String errorMsg = ex.getMessage();
			int statusCode = ex.getStatusCode();
			SPRestErrorResponse error = ex.getError();
			if (error != null) {
				SPRestError sperror = error.getError();
				if (sperror != null) {
					errorMsg = sperror.getMessage();
				}
			}
			if (statusCode == 401) {
				throw new UnauthorizedRepositoryException(errorMsg);
			} else if (statusCode == 404) {
				throw new com.nextlabs.rms.repository.exception.FileNotFoundException(errorMsg);
			}
			throw new RepositoryException(e.getMessage(), e);
		} else if (e instanceof RepositoryException) {
			throw (RepositoryException) e;
		} else if (e instanceof RMSException) {
			throw (RMSException) e;
		} else {
			throw new RMSException(e.getMessage(), e);
		}
	}
}
