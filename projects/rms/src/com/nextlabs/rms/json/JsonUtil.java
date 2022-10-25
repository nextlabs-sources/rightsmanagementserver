package com.nextlabs.rms.json;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {
	
	private static Logger logger = Logger.getLogger(JsonUtil.class);
	
	public static void writeJsonToResponse(Object obj, HttpServletResponse response){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jsonStr = gson.toJson(obj);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0); // Proxies.
		try {
			response.getWriter().write(jsonStr);
		} catch (IOException e) {
			logger.debug("Error occurred while writing response to the stream", e);
		}
	}
	
	public static JsonArray getJsonArray(Object arrayObj){
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(arrayObj);
		return element.getAsJsonArray();
	}
	
	public static JsonObject getJsonObject(Object obj){
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(obj);
		return element.getAsJsonObject();
	}
}
