package com.test.nextlabs.rms;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.nextlabs.rms.wrapper.util.HTMLWrapperUtil;
public class WrapperSvcTest {
	
	public static void main(String[] args){
//		long heapSize = Runtime.getRuntime().totalMemory();

        //Print the jvm heap size.
//		System.out.println("Heap Size = " + heapSize);
		 WrapperSvcTest test = new WrapperSvcTest();
		String fileName="C:\\Temp\\NXLFiles\\HTMLWrapperAPI_WebService - copy.docx.nxl";
//		 String fileName="C:\\Temp\\Requirement_Spec.pdf.nxl";
		System.out.println(fileName);
		try {
			long time1=System.currentTimeMillis();
			System.out.println(time1);
			
//			getSPFile();
//			getFile();
//			test.sendRequest(fileName);
			wrapFile(fileName);
			long time2=System.currentTimeMillis();
			long difference=time2-time1;
			System.out.println("Time taken for testing class:"+difference);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private static void wrapFile(String fileName) {
		BufferedOutputStream bis = null;
		File inputFile = new File(fileName);
		try {
			String htmlContent = HTMLWrapperUtil.getHTMLWithFile(inputFile, "http", "ubin", "8080", "RMS");
			bis = new BufferedOutputStream(new FileOutputStream(new File("C:\\Temp\\NXLFiles\\HTMLWrapperAPI_WebService - copy.docx.nxl.html"))); 
            bis.write(htmlContent.getBytes());
            bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void getFile() throws IOException{
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		String hostName = InetAddress.getLocalHost().getHostName();
		credsProvider.setCredentials(AuthScope.ANY,
		        new NTCredentials("Federer", "123next!", hostName, ""));
		CloseableHttpClient httpclient = HttpClients.custom().build();
		HttpHost target = new HttpHost("10.63.0.156", 80, "http");
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		HttpGet httpget = new HttpGet("http://10.63.0.156/Shared%20Documents/DocConverterComparison.xlsx");
//		HttpGet httpget = new HttpGet("http://sharepoint.nextlabs.com/sites/engineering/Architecture/Shared%20Documents/Endpoint%20Coding%20Policy.docx");
		CloseableHttpResponse response = httpclient.execute(target, httpget, context);
		try {
		    HttpEntity entity1 = response.getEntity();
		    byte[] byteArr = EntityUtils.toByteArray(entity1);
            BufferedOutputStream bis = new BufferedOutputStream(new FileOutputStream(new File("C:\\temp\\Endpoint Coding Policy.docx"))); 
            bis.write(byteArr);
            bis.close();
		} finally {
		    response.close();
		}


	}
	
	public static void getSPFile() throws Exception{
		String url = "http://10.63.0.156/Shared%20Documents/DocConverterComparison.xlsx";
		
		RequestConfig defaultRequestConfig = RequestConfig.custom()
	            .setCookieSpec(CookieSpecs.BEST_MATCH)
	            .setExpectContinueEnabled(true)
	            .setStaleConnectionCheckEnabled(true)
	            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))//AuthSchemes.BASIC, AuthSchemes.DIGEST, 
	            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))
	            .build();
				
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials("Federer", "123next!"));
        credsProvider.setCredentials(
        	    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "ntlm"), 
        	    new UsernamePasswordCredentials("Federer", "123next!"));

        CloseableHttpClient httpclient = HttpClients.custom()
        		.setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultCredentialsProvider(credsProvider).build();
//        httpclient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
        try {
        	HttpGet httpGet = new HttpGet(url);
        	CloseableHttpResponse response = httpclient.execute(httpGet);

//            HttpResponse r = httpclient.execute(httpGet);
           
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = null;
            StringBuffer buff = new StringBuffer(); 
            while ((line = rd.readLine()) != null) {
            	buff.append(line);
            }
            BufferedOutputStream bis = new BufferedOutputStream(new FileOutputStream(new File("C:\\temp\\DocConverterComparison.xlsx"))); 
            bis.write(buff.toString().getBytes());
            bis.close();
             System.out.println(buff.toString());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
	}
	
	
	
	public static void sendRequest(String fileName) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost httpPost = new HttpPost("http://localhost:8080/RMS/WrapperSvc/GetHTMLWithFile");
            FileBody bin = new FileBody(new File(fileName));
            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
            MultipartEntityBuilder multipartBuilder=MultipartEntityBuilder.create();
            multipartBuilder.setCharset(Charset.forName(CharEncoding.UTF_8));
            HttpEntity reqEntity = multipartBuilder
                    .addPart("bin", bin)
                    .addPart("comment", comment)
                    .build();
            
            System.out.println(reqEntity.getContentEncoding());
            httpPost.setEntity(reqEntity);
            HttpResponse r = httpclient.execute(httpPost);
           
            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line = null;
            StringBuffer buff = new StringBuffer(); 
            while ((line = rd.readLine()) != null) {
            	buff.append(line);
            }
            BufferedOutputStream bis = new BufferedOutputStream(new FileOutputStream(new File("C:\\temp\\NextLabsRightsProtection.html"))); 
            bis.write(buff.toString().getBytes());
            bis.close();
             System.out.println(buff.toString());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
	}


}
