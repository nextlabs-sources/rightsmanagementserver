package com.nextlabs.rms.repository.onedrive;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.repository.IRefreshTokenCallback;
import com.nextlabs.rms.repository.ITokenResponse;
import com.nextlabs.rms.repository.OneDriveRepository;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.InvalidTokenException;
import com.nextlabs.rms.repository.exception.RepositoryException;
import com.nextlabs.rms.sharedutil.RestletUtil;
import com.nextlabs.rms.sharedutil.RestletUtil.IHTTPResponseHandler;
import com.nextlabs.rms.util.StringUtils;

public class OneDrivePersonalClient extends Observable {
	public static final String API_ROOT = "https://api.onedrive.com/v1.0";
	private static final String ACCESS_TOKEN_PARAM = "access_token";
	private static final String ITEM_ATTR_FILTER = "name,file,folder,parentReference,@content.downloadUrl,lastModifiedDateTime,size";
	private static final String MAX_RESULT_SIZE = "200";
	private static final int HTTP_CLIENT_ERROR_RESPONSE_CODE = 400;
	private static final int HTTP_SUCCESS_RESPONSE_CODE = 200;
	private static final int HTTP_NO_BODY_RESPONSE_CODE = 204;
	private static final int HTTP_NOT_MODIFIED = 304;
	private final static Logger logger = Logger.getLogger(OneDrivePersonalClient.class);
	private OneDriveRepository repository;
	private static final JsonDeserializer<java.util.Date> DATE_JSON_DESERIALIZER = new JsonDeserializer<Date>() {
		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (json == null) {
				return null;
			}
			try {
				return ISO8601Utils.parse(json.getAsString(), new ParsePosition(0));
			} catch (final ParseException e) {
				logger.error("Parsing issue on " + json.getAsString() + " ! " + e.toString());
				return null;
			}
		}
	};
	
	public OneDrivePersonalClient(OneDriveRepository repository) {
		this.repository = repository;
	}

	private String getDriveURI() {
		return API_ROOT + "/drive/root:";
	}

	protected String getToken(){
		IRefreshTokenCallback callback = new IRefreshTokenCallback() {

			@Override
			public ITokenResponse execute() throws InvalidTokenException {
				if (logger.isDebugEnabled()) {
					logger.debug("About to get new access token (Repo ID: " + repository.getRepoId() + ")");
				}
				String uri = "https://login.live.com/oauth20_token.srf";
				Form form = new Form();
				form.add("client_id", repository.getClientId());
				form.add("client_secret", repository.getClientSecret());
				form.add("refresh_token", (String) repository.getAttributes().get(RepositoryManager.REFRESH_TOKEN));
				form.add("redirect_uri", repository.getRedirectUrl());
				form.add("grant_type", "refresh_token");
				OneDriveTokenResponse response = null;
				try {
					response = RestletUtil.sendRequest(uri, Method.POST, form.getWebRepresentation(CharacterSet.UTF_8), null,
							new OneDriveResponseHandler<>(OneDriveTokenResponse.class));
				} catch (Exception e) {
					logger.error(e.getMessage());
					throw new InvalidTokenException(e.getMessage(), e);
				}
				Map<String,Object> attrs = new HashMap<>();
				attrs.put(RepositoryManager.ACCESS_TOKEN, response.getAccessToken());
				attrs.put(RepositoryManager.REFRESH_TOKEN, response.getRefreshToken());
				long now = System.currentTimeMillis()/1000;
				attrs.put(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME, response.getExpiresInSeconds() + now);
				repository.getAttributes().clear();
				repository.getAttributes().putAll(attrs);
//				setChanged();
//				notifyObservers();
				return response;
			}
		};
		ITokenResponse response = null;
		try {
			Map<String, Object> attrMap = repository.getAttributes();
			if(StringUtils.hasText((String)attrMap.get(RepositoryManager.REFRESH_TOKEN)) && 
					(!StringUtils.hasText((String)attrMap.get(RepositoryManager.ACCESS_TOKEN)) || attrMap.get(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME)==null)) {
				response = callback.execute();
			}
			else {
				long now = System.currentTimeMillis()/1000;
				long expiresOn = (long) repository.getAttributes().get(RepositoryManager.ACCESS_TOKEN_EXPIRY_TIME);
				if(now > expiresOn - 180) {	//renew before 3 minutes to expire
					response = callback.execute();
				}
				else {
					return (String) repository.getAttributes().get(RepositoryManager.ACCESS_TOKEN);
				}
			}
			if (response != null) {
				return response.getAccessToken();
			}
		} catch (RepositoryException e) {
			logger.error(e);
			return null;
		}
		return null;
	}

	public List<OneDriveItems> listItems(String path) throws OneDriveServiceException, RepositoryException {
		String accessToken = getToken();
		List<OneDriveItems> results = new ArrayList<>();
		String uri = getDriveURI() + path + ":/children";
		Form form = new Form();
		form.add("select", ITEM_ATTR_FILTER);
		form.add("top", MAX_RESULT_SIZE);
		form.add(ACCESS_TOKEN_PARAM, accessToken);
		String queryString = "?" + form.getQueryString();
		OneDriveItems item = null;
		String token = null;
		do {
			if (form != null) {
				item = RestletUtil.sendRequest(uri + queryString, Method.GET, form.getWebRepresentation(CharacterSet.UTF_8), null, new OneDriveResponseHandler<>(OneDriveItems.class));
			} else {
				item = RestletUtil.sendRequest(uri, Method.GET, null, null,new OneDriveResponseHandler<>(OneDriveItems.class) );
			}		
			token = null;
			if (item != null) {
				results.add(item);
				token = item.getNextLink();
				uri = token;
				form = null;
			}
		} while (token != null && token.length() > 0);
		return results;
	}

	public List<OneDriveItems> searchItems(String dir, String searchString)
			throws OneDriveServiceException, RepositoryException {
		String accessToken = getToken();
		List<OneDriveItems> results = new ArrayList<>();
		String uri = getDriveURI() + dir + ":/view.search";
		Form form = new Form();
		form.add("q", searchString);
		form.add("select", ITEM_ATTR_FILTER);
		form.add("top", MAX_RESULT_SIZE);
		form.add(ACCESS_TOKEN_PARAM, accessToken);
		String queryString = "?" + form.getQueryString();
		OneDriveItems item = null;
		String token = null;
		do {
			if (form != null) {
				item = RestletUtil.sendRequest(uri + queryString, Method.GET, form.getWebRepresentation(CharacterSet.UTF_8), null, new OneDriveResponseHandler<>(OneDriveItems.class));
			} else {
				item = RestletUtil.sendRequest(uri, Method.GET, null, null,new OneDriveResponseHandler<>(OneDriveItems.class) );
			}	
			token = null;
			if (item != null) {
				results.add(item);
				token = item.getNextLink();
				uri = token;
				form = null;
			}
		} while (token != null && token.length() > 0);
		return results;
	}

	public OneDriveItem getItem(String filePath) throws OneDriveServiceException, RepositoryException, RMSException {
		String accessToken = getToken();
		String uri;
		try {
			uri = getDriveURI() + URLEncoder.encode(filePath, "UTF-8");
			uri = uri.replaceAll("\\+", "%20");	
			Form form = new Form();
			form.add("select", ITEM_ATTR_FILTER);
			form.add(ACCESS_TOKEN_PARAM, accessToken);
			String queryString = "?" + form.getQueryString();
			OneDriveItem result = RestletUtil.sendRequest(uri + queryString, Method.GET, form.getWebRepresentation(CharacterSet.UTF_8), null,
					new OneDriveResponseHandler<>(OneDriveItem.class));
			return result;
		} catch (UnsupportedEncodingException e) {
			OneDriveOAuthHandler.handleException(e);
		}
		return null;
	}

	static class OneDriveResponseHandler<T> implements IHTTPResponseHandler<T> {
		private Class<T> resultClass;

		public OneDriveResponseHandler(Class<T> resultClass) {
			this.resultClass = resultClass;
		}

		@Override
		public T handle(Representation representation, Status status) {
			return handleResponse(representation, status, resultClass);
		}

		private <Result> Result handleResponse(Representation representation, Status status, Class<Result> resultClass) {
			int code = status.getCode();
			if (code >= HTTP_CLIENT_ERROR_RESPONSE_CODE) {
				handleErrorResponse(representation, status);
			} else if (code == HTTP_NO_BODY_RESPONSE_CODE || code == HTTP_NOT_MODIFIED) {
				return null;
			} else if (code == HTTP_SUCCESS_RESPONSE_CODE) {
				return handleJSONResponse(representation, resultClass);
			}
			return null;
		}

		private <Result> Result handleJSONResponse(Representation representation, Class<Result> resultClass) {
			final Gson gson=  new GsonBuilder().registerTypeAdapter(java.util.Date.class, DATE_JSON_DESERIALIZER).create();
			StringWriter writer = new StringWriter();
			Result result = null;
			try {
				representation.write(writer);
				result = gson.fromJson(writer.toString(), resultClass);
			} catch (final Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			return result;
		}

		private void handleErrorResponse(Representation representation, Status status) {
			final String errorMessage = status.getDescription();
			final int code = status.getCode();
			final Gson gson = new Gson();
			StringWriter writer = new StringWriter();
			OneDriveErrorResponse error;
			try {
				representation.write(writer);
				error = gson.fromJson(writer.toString(), OneDriveErrorResponse.class);
			} catch (final Exception ex) {
				String msg = writer.toString();
				error = new OneDriveErrorResponse();
				com.nextlabs.rms.repository.onedrive.OneDriveError e = new OneDriveError();
				com.nextlabs.rms.repository.onedrive.OneDriveInnerError innerError = new OneDriveInnerError();
				innerError.setCode(ex.getMessage());
				e.setCode(String.valueOf(status.getCode()));
				e.setMessage("Raw error: " + (StringUtils.hasText(msg) ? msg : ex.getMessage()));
				e.setInnererror(innerError);
				error.setError(e);
			}
			throw new OneDriveServiceException(code, errorMessage, error);
		}
	}
}
