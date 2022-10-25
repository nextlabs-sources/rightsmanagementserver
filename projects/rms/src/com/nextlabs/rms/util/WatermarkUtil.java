package com.nextlabs.rms.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.conversion.WaterMark;
import com.nextlabs.rms.eval.Attribute;
import com.nextlabs.rms.eval.Obligation;

public class WatermarkUtil {
	
	private static Logger logger = Logger.getLogger(WatermarkUtil.class);
	
	public static String updateWaterMark(String inputStr, String domainName, String userName, WaterMark waterMarkObj) {
		String firstSubDomainName = "";
		if(domainName!=null){
			if (userName != null) {
				if (userName.indexOf(domainName) != 0){	//userName doesn't start with domainName
					if(!domainName.contains("."))	// domain name can be just subDomainName
						userName = domainName + "\\" + userName;
					else {
						firstSubDomainName = domainName.substring(0, domainName.indexOf("."));
						if (userName.indexOf(firstSubDomainName) != 0)
							userName = firstSubDomainName + "\\" + userName;
					}
				}
			}
		}
		
		Date date = new Date();
		DateFormat df=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM, Locale.getDefault());
		String localTime=df.format(date);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String gmtTime=df.format(date);
		String localDate="";
		
		String dateFormat=waterMarkObj.getWaterMarkDateFormat();
		String timeFormat=waterMarkObj.getWaterMarkTimeFormat();
		
		try {
			if (timeFormat == null) {								// coming from config
				if (dateFormat != null && dateFormat.length() > 0) {
					SimpleDateFormat ft = new SimpleDateFormat(dateFormat);
					localTime = ft.format(date);
					ft.setTimeZone(TimeZone.getTimeZone("GMT"));
					gmtTime = ft.format(date);
				} 
			} else {
				SimpleDateFormat dft = new SimpleDateFormat(dateFormat);
				SimpleDateFormat tft = new SimpleDateFormat(timeFormat);
				localTime = tft.format(date);
				localDate = dft.format(date);
			}
				
		} catch (IllegalArgumentException e) {
			logger.error("Invalid Date Format: " + e.getMessage(), e);
		}
		if(userName!=null){
			inputStr = inputStr.replace(GlobalConfigManager.WATERMARK_USERNAME, userName);
		}
		inputStr=inputStr.replace(GlobalConfigManager.WATERMARK_LOCALTIME,localTime);
		inputStr=inputStr.replace(GlobalConfigManager.WATERMARK_LOCALDATE,localDate);
		inputStr=inputStr.replace(GlobalConfigManager.WATERMARK_HOST,"");
		inputStr=inputStr.replace(GlobalConfigManager.WATERMARK_GMTTIME,gmtTime+" GMT");
		
		return inputStr;
	}
	
	public static WaterMark build(Obligation obligation) {
		WaterMark waterMark = new WaterMark();
		Map<String, String> watermarkValues = new HashMap<>();
		if (obligation != null) {
			List<Attribute> attributes = obligation.getAttributes();
			for (Attribute attribute : attributes) {
				watermarkValues.put(attribute.getName(), attribute.getValue());
			}
		}
		waterMark.setWaterMarkValues((HashMap<String, String>) watermarkValues);
		return waterMark;
	}
}
