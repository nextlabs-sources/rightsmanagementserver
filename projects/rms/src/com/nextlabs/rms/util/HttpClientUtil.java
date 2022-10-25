package com.nextlabs.rms.util;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.nextlabs.kms.GetAllKeyRingsWithKeysRequest;
import com.nextlabs.rms.command.kms.KMSCommManager;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.eval.RMSException;

public class HttpClientUtil {
	
	private static final Logger logger = Logger.getLogger(HttpClientUtil.class);
	 
	public static String getAllKeyRingsWithKeys(String tenantId) throws RMSException{
		if (!StringUtils.hasText(tenantId)) {
			throw new IllegalArgumentException("Tenant ID is required");
		}
		GetAllKeyRingsWithKeysRequest keyRingRequest= new GetAllKeyRingsWithKeysRequest();
		//Need to get TenantId from the request
		keyRingRequest.setTenantId(tenantId);
		keyRingRequest.setVersion(1);
		StringWriter sw = new StringWriter();
		JAXB.marshal(keyRingRequest, sw);
		String xmlString = sw.toString();
		return postRequest(xmlString.getBytes(), KMSWebSvcUrl.GET_ALL_KEYRINGS_WITH_KEYS.getServiceUrl(), KMSCommManager.constructHeader());
	}
	
	private static String postRequest (byte[] input , String serviceName, Map<String,String> headers){
		StringWriter writer = new StringWriter();
		InputStream xml=null;
		HttpsURLConnection connection = null;
		OutputStream wr = null;
		try{	
			URL url = new URL(serviceName);
			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestProperty("Content-Type", "application/xml; charset=utf-8");
			connection.setRequestProperty("Content-Length", Integer.toString(input.length));
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			Iterator<Entry<String, String>> it = headers.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				connection.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
			}
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
				public X509Certificate[] getAcceptedIssuers() { return null; }
			};
			sslContext.init(null, new TrustManager[]{tm}, null);
			HttpsURLConnection.setDefaultHostnameVerifier(
					new HostnameVerifier(){
						public boolean verify(String hostname,SSLSession sslSession) {
							return true;
						}
					});
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
			wr = new DataOutputStream(connection.getOutputStream());
			wr.write(input);
			
			if(connection.getResponseCode()==200){
				xml = connection.getInputStream();
			} else {
				xml = connection.getErrorStream();
			}
			IOUtils.copy(xml, writer);
		} catch (Exception e) {
			logger.error("Error occurred while sending POST request: " + e.getMessage(), e);
		} finally {
			if (xml != null) {
				IOUtils.closeQuietly(xml);
			}
			if (wr != null) {
				IOUtils.closeQuietly(wr);
			}
			if (connection != null) {
				IOUtils.close(connection);
			}
		}
		return writer.toString();
	}
}
