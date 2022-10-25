package com.nextlabs.kms.util;

import java.io.StringWriter;

import javax.xml.bind.JAXB;
import org.apache.log4j.Logger;

public class XMLUtil {

	private static Logger logger = Logger.getLogger(XMLUtil.class);
	
	public static String toXml(Object xmlObject) {
		try {
			StringWriter sw = new StringWriter();
			JAXB.marshal(xmlObject, sw);
			return sw.toString();
		} catch (Exception e){
			logger.error(e);
			return null;
		}
	}
}
