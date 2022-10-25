package com.nextlabs.rms.locale;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;

public class RMSMessageHandler {

	private static Logger logger = Logger.getLogger(RMSMessageHandler.class);
	/**
	 * Returns a localized string corresponding to the key if it is available based on the default locale
	 * else returns the key
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
        try {
			ClassLoader cl = RMSMessageHandler.class.getClassLoader();
			ResourceBundle bundle = ResourceBundle.getBundle("com.nextlabs.rms.locale.RMSMessages",
					Locale.getDefault(), cl);
			return bundle.getString(key);
        } catch (Exception ex1) {
            logger.error(ex1);
        	return key;
        }
    }
	
	/**
	 * Returns a localized string corresponding to the key if it is available based on the client locale
	 * else returns key
	 * @param key
	 * @return
	 */
	public static String getClientString(String key) {
		try {
			Locale loc = GlobalConfigManager.getInstance().getCurrentUserLocale();
			if (loc == null) {
				return key;
			}
			ClassLoader cl = RMSMessageHandler.class.getClassLoader();
			ResourceBundle bundle = ResourceBundle.getBundle("com.nextlabs.rms.locale.RMSMessages",
					loc, cl);
			return bundle.getString(key);
		} catch (Exception ex1) {
			logger.error(ex1);
			return key;
		}
    }
	
	public static String getClientString(String key, Object... params){
		return MessageFormat.format(getClientString(key), params);
	}
	
	public static String getString(String key, Object... params){
		return MessageFormat.format(getString(key), params);
	}
}
