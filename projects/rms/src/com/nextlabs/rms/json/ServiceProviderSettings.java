/**
 * 
 */
package com.nextlabs.rms.json;

import java.util.List;
import java.util.Map;

import com.nextlabs.rms.pojo.ServiceProviderSetting;

/**
 * @author nnallagatla
 *
 */
public class ServiceProviderSettings {

	private List<ServiceProviderSetting> serviceProviderSettingList;
	
	private Map<String, String> supportedProvidersMap;
	
	private Map<String, String> crossLaunchProvidersMap;
	
	private String redirectUrl;
	
	/**
	 * @param crossLaunchProviders 
	 * 
	 */
	public ServiceProviderSettings(List<ServiceProviderSetting> serviceProviderSettingList, 
			Map<String, String> supportedProviders, Map<String, String> crossLaunchProviders,String redirectUrl) {
		this.serviceProviderSettingList = serviceProviderSettingList;
		this.supportedProvidersMap = supportedProviders;
		this.crossLaunchProvidersMap = crossLaunchProviders;
		this.redirectUrl=redirectUrl;
	}
	
	public List<ServiceProviderSetting> getServiceProviderSettingList() {
		return serviceProviderSettingList;
	}

	public Map<String, String> getSupportedProvidersMap() {
		return supportedProvidersMap;
	}

	public Map<String, String> getCrossLaunchProvidersMap() {
		return crossLaunchProvidersMap;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}
	
}
