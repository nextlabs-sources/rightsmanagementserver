package com.nextlabs.rms.sharedutil;

import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

public class RestletUtil {
	private static final Logger logger = Logger.getLogger(RestletUtil.class);
	
	public static final String RESTLET_HEADERS = "org.restlet.http.headers";

	public static interface IHTTPResponseHandler<Result> {
		public Result handle(Representation representation, Status status);
	}
	
	public static <Result> Result sendRequest(String uri, Method method, Representation representation, Map<String,String> headersMap, IHTTPResponseHandler<Result> handler) {
		return sendRequest(uri, method, representation, headersMap, null, null, handler);
	}

	private static void addRequestHeaders(org.restlet.Request request, Map<String, String> additionalHeaders,
			ChallengeResponse challengeResponse, List<Preference<MediaType>> mediaTypes) {
		if (challengeResponse != null) {
			request.setChallengeResponse(challengeResponse);
		}
		if (mediaTypes != null && !mediaTypes.isEmpty()) {
			ClientInfo clientInfo = request.getClientInfo();
			if (clientInfo != null) {
				clientInfo.setAcceptedMediaTypes(mediaTypes);
			}
		}
		if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
			Series<org.restlet.data.Header> headers = request.getHeaders();
			for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
				headers.set(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public static <Result> Result sendRequest(String uri, Method method, Representation representation, Map<String, String> headersMap,
			ChallengeResponse challengeResponse, List<Preference<MediaType>> mediaTypes,
			IHTTPResponseHandler<Result> handler){
		ClientResource clientResource = new ClientResource(new Context(), uri);
 
		try {
			final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			TrustManager tm = new SelfSignedTrustManager();
			sslContext.init(null, new TrustManager[] {tm}, null);
			Context context = clientResource.getContext();
			context.getAttributes().put("sslContextFactory", new SslContextFactory() {
			    public void init(Series<Parameter> parameters) { }
			    public SSLContext createSslContext() { return sslContext; }
			});
			context.getAttributes().put("hostnameVerifier", new HostnameVerifier() {
			  @Override
			  public boolean verify(String arg0, SSLSession arg1) {
			  	return true; 
			  }
			});
    } catch (Exception e){
    	logger.error(e.getMessage(),e);
    }
    
    addRequestHeaders(clientResource.getRequest(), headersMap, challengeResponse, mediaTypes);
		if (method == Method.GET) {
			return doGet(clientResource, handler);
		} else if (method == Method.POST) {
			return doPost(clientResource, representation, handler);
		} else {
			throw new UnsupportedOperationException("Unsupported method: " + method);
		}
	}

	private static <Result> Result doGet(ClientResource clientResource, IHTTPResponseHandler<Result> handler) {
		Representation representation = null;
		try {
			representation = clientResource.get();
		} catch (Exception e) {
			representation = clientResource.getResponseEntity();
		}
		Status status = clientResource.getStatus();
		int code = status.getCode();
		if (logger.isTraceEnabled()) {
			logger.trace("doGet with response code: " + code);
		}
		return handler.handle(representation, status);
	}

	private static <Result> Result doPost(ClientResource clientResource, Representation rep, IHTTPResponseHandler<Result> handler) {
		Representation representation = null;
		try {
			representation = clientResource.post(rep);
		} catch (Exception e) {
			representation = clientResource.getResponseEntity();
		}
		Status status = clientResource.getStatus();
		int code = status.getCode();
		if (logger.isTraceEnabled()) {
			logger.trace("doPost with response code: " + code);
		}
		return handler.handle(representation, status);
	}
}
