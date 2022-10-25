package com.nextlabs.rms.restlets;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public abstract class AbstractResource extends ServerResource {

	String jsonString = "";

	@Post("json")
	public Representation doPost(Representation entity) {
		Map json = null;
		jsonString = "";
		try {
			String text = entity.getText();
//			JsonRepresentation represent = new JsonRepresentation(entity);
//			JSONObject jsonobject = represent.getJsonObject();
//			JSONParser parser = new JSONParser();
//			String jsonText = jsonobject.toString();
//			json = (Map) parser.parse(jsonText);
			jsonString = processRequest(json, "post");
		} catch (Exception e) {
			e.printStackTrace();
//			jsonString = parseFactory.getFailureJsonString(e.getMessage());
		}
		return new StringRepresentation(jsonString, MediaType.APPLICATION_JSON);
	}

	@SuppressWarnings("rawtypes")
	@Get
	public Representation doGet() {
		Map json = null;
//		parseFactory = new ResponseParseFactory();
		jsonString = "";
		try {
			Request req = getRequest();
			json = getMapFromParam(getRequest().getResourceRef()
					.getQueryAsForm());
//			parseFactory = new ResponseParseFactory();
			jsonString = processRequest(json, "get");

		} catch (Exception e) {
			e.printStackTrace();
//			jsonString = parseFactory.getFailureJsonString(e.getMessage());
		}
		return new StringRepresentation(jsonString, MediaType.APPLICATION_JSON);
	}

	public abstract String processRequest(Map json, String method);

	public static Map<String, String> getMapFromParam(Form form) {
		Map<String, String> map = new HashMap<String, String>();
		for (Parameter parameter : form) {
			map.put(parameter.getName(), parameter.getValue());
		}
		return map;
	}
}
