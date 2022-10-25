/**
 * 
 */
package com.nextlabs.rms.services.manager;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.nextlabs.rms.rmc.types.Attribute;
import com.nextlabs.rms.rmc.ServiceUtil;
import com.nextlabs.rms.rmc.StatusTypeEnum;
import com.nextlabs.rms.rmc.UserAttributeManager;
import com.nextlabs.rms.rmc.UserAttributeRequestDocument.UserAttributeRequest;
import com.nextlabs.rms.rmc.UserAttributeResponseDocument;
import com.nextlabs.rms.rmc.UserAttributeResponseDocument.UserAttributeResponse;
import com.nextlabs.rms.rmc.types.UserAttributesType;

/**
 * @author nnallagatla
 *
 */
public class UserAttributeServiceManager {
	
	private static final Logger logger = Logger.getLogger(UserAttributeServiceManager.class);
	
	public static UserAttributeResponseDocument getUserAttributes(UserAttributeRequest request) {
		UserAttributeResponseDocument doc = UserAttributeResponseDocument.Factory.newInstance();
		UserAttributeResponse response = doc.addNewUserAttributeResponse();
		String authRequestId = request.getAuthRequestId();

		Map<String, String> attributes = UserAttributeManager.getInstance().getAttributes(authRequestId);
		
		if (attributes != null) {
			UserAttributesType attrList = response.addNewAttributes();
			for (Iterator<Map.Entry<String, String>> it = attributes.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = it.next();
				Attribute attr = attrList.addNewAttribute();
				attr.setName(entry.getKey());
				attr.setValue(entry.getValue());
			}
			response.setStatus(
					ServiceUtil.getStatus(StatusTypeEnum.SUCCESS.getCode(), StatusTypeEnum.SUCCESS.getDescription()));
		}
		else {
			response.setStatus(
					ServiceUtil.getStatus(StatusTypeEnum.USER_ATTR_NOT_FOUND.getCode(), StatusTypeEnum.USER_ATTR_NOT_FOUND.getDescription()));
		}
		return doc;
	}

}
