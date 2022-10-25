package com.nextlabs.rms.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;

public class UtilMethods {
	
	private static Logger logger = Logger.getLogger(UtilMethods.class);
	
	public static String getIP(HttpServletRequest request){
		//is client behind something?
	    String ipAddress = request.getHeader("X-FORWARDED-FOR");  
	    if (ipAddress == null) {  
		   ipAddress = request.getRemoteAddr();  
	    }
	    if(ipAddress==null || ipAddress.length()==0){
	    	return "";
	    }
	    return ipAddress;
	}
	
	public static String getParamValue(String paramName, List<NameValuePair> paramList){
		for (NameValuePair nameValuePair : paramList) {
			if(nameValuePair.getName().equals(paramName)){
				return nameValuePair.getValue();
			}
		}
		return null;
	}
	
	public static long parseLong(String number, long defaultValue){
		
		try{
			if (number != null && !number.isEmpty()){
				return Long.parseLong(number);
			}
		}
		catch (NumberFormatException e){
			logger.error("Error parsing '" + number + "' to long");
		}
		return defaultValue;
	}
	
	public static Date getCurrentGMTDateTime(){
		Calendar time = Calendar.getInstance();
		time.add(Calendar.MILLISECOND, -time.getTimeZone().getOffset(time.getTimeInMillis()));
		return time.getTime();
	}
	
	public static String getServerUrl(HttpServletRequest request){
		StringBuilder redirectUrl=new StringBuilder();
		if(request.getLocalPort()!=0){
			redirectUrl.append(request.getScheme());
			redirectUrl.append("://");
			redirectUrl.append(request.getServerName());
			redirectUrl.append(":");
			redirectUrl.append(request.getLocalPort());
		}
		else{
			redirectUrl.append(request.getScheme());
			redirectUrl.append("://");
			redirectUrl.append(request.getServerName());
		}
		return redirectUrl.toString();
	}
}
