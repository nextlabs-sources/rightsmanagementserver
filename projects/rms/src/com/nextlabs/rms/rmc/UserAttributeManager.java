/**
 * 
 */
package com.nextlabs.rms.rmc;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.RMSCacheManager;

/**
 * @author nnallagatla
 *
 */
public class UserAttributeManager {
	private static UserAttributeManager instance = new UserAttributeManager();
	private static final Logger logger = Logger.getLogger(UserAttributeManager.class);

	private UserAttributeManager() {

	}

	public static UserAttributeManager getInstance() {
		return instance;
	}

	public Map<String, String> getAttributes(String authRequestId) {
		Map<String, String> value = (Map<String, String>) RMSCacheManager.getInstance().removeFromCache(authRequestId,
				RMSCacheManager.CACHEID_USER_ATTR);
		return value;
	}

	public boolean putAttributes(String authRequestId, Map<String, String> attributes) {
		return RMSCacheManager.getInstance().putInCache(authRequestId, attributes,
				RMSCacheManager.CACHEID_USER_ATTR, 5, TimeUnit.MINUTES);
	}
}
