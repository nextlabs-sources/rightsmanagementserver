package com.nextlabs.rms.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;

public class HTTPUtil {

	private static Logger logger = Logger.getLogger(HTTPUtil.class);

	public static CloseableHttpClient getHTTPClient(){
		CloseableHttpClient httpclient = null;
		try {
			if(GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.TRUST_SELF_SIGNED_CERTS)){
				SSLConnectionSocketFactory sslsf = getTrustAllSocketFactory();
			    httpclient = HttpClients.custom().setSSLSocketFactory(
			            sslsf).build();				
			}else{
				httpclient = HttpClientBuilder.create().build();
			}
		} catch (Exception e) {
			logger.error("Error occurred while creating HTTPClient Object", e);
		}
	    return httpclient;
	}

	public static SSLConnectionSocketFactory getTrustAllSocketFactory()
			throws NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException {
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		        builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		return sslsf;
	}
	
	/**
	 * content disposition header value taking care of non-ascii & special characters
	 * @param filename
	 * @return
	 */
	public static String getContentDisposition(String filename) {
		StringBuilder contentDisposition = new StringBuilder("attachment");
		URI uri;
		String attachmentFilename = filename;
		try {
			uri = new URI(null, null, filename, null);
			attachmentFilename = uri.toASCIIString();
			attachmentFilename = attachmentFilename.replace("+", "%2B").replace(";", "%3B");
		} catch (URISyntaxException e) {
			uri = null;
		}
		contentDisposition.append("; filename*=UTF-8''").append(attachmentFilename);
		return contentDisposition.toString();
	}
}
