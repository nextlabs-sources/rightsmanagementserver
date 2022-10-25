/**
 * 
 */
package com.nextlabs.rms.pojo;

import java.util.HashMap;
import java.util.Map;

import com.nextlabs.rms.locale.RMSMessageHandler;

/**
 * @author nnallagatla
 *
 */
public class ServiceProviderSetting {
	public static final String APP_ID = "APP_ID";
	public static final String APP_SECRET = "APP_SECRET";
	public static final String REDIRECT_URL = "REDIRECT_URL";
	public static final String APP_NAME = "APP_NAME";
	public static final String REMOTE_WEB_URL = "REMOTE_WEB_URL";
	public static final String ALLOW_PERSONAL_REPO = "ALLOW_PERSONAL_REPO";
	public static final String APP_DISPLAY_MENU = "APP_DISPLAY_MENU";
	
	private long id = -1;
	private String tenantId;
	private String providerType;
	private String providerTypeDisplayName;
	private Map<String, String> attributes = new HashMap<>();
	private boolean downloadable = false;
	private String displayMenuString;
	
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getProviderType() {
		return providerType;
	}

	public void setProviderType(String providerType) {
		this.providerType = providerType;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProviderTypeDisplayName() {
		return providerTypeDisplayName;
	}

	public void setProviderTypeDisplayName(String providerTypeDisplayName) {
		this.providerTypeDisplayName = providerTypeDisplayName;
	}

	public boolean isDownloadable() {
		return downloadable;
	}

	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}
	
	public static String getProviderTypeDisplayName(String type){
		return RMSMessageHandler.getClientString(type+"_display_name");
	}

	public String getDisplayMenuString() {
		return displayMenuString;
	}

	public void setDisplayMenuString(String displayMenu) {
		this.displayMenuString = displayMenu;
	}
}
