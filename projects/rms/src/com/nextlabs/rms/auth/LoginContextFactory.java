package com.nextlabs.rms.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.nextlabs.rms.config.ADServerInfo;
import com.nextlabs.rms.config.GlobalConfigManager;

public class LoginContextFactory {
	
	private static LoginContextFactory instance = new LoginContextFactory();;
	
	private Map<String, RMSLoginContext> contextMap = new HashMap<String, RMSLoginContext>();
	
	private LoginContextFactory(){
		init();
	}
	
	public static LoginContextFactory getInstance(){
		return instance;
	}
	
	private void init(){
		ADServerInfo[] adServerArr=GlobalConfigManager.getInstance().getAdServerArr();
		for (ADServerInfo adSrvr : adServerArr) {
			RMSLoginContext ctxt=new RMSLoginContext(adSrvr);
			contextMap.put(adSrvr.getDomain(), ctxt);	
		}
	}
	
	public RMSLoginContext getContext(String domainName){
		RMSLoginContext ctxt = contextMap.get(domainName);
		if(ctxt==null){
			ADServerInfo adSrvr = GlobalConfigManager.getInstance().getAdServerInfo(domainName);
			if(adSrvr==null){
				return null;
			}
			ctxt = new RMSLoginContext(adSrvr);
			contextMap.put(domainName, ctxt);			
		}
		return ctxt;
	}

	public RMSLoginContext getContextWithStartName(String startingDomainName){
		for (Entry<String, RMSLoginContext> entry : contextMap.entrySet())
		{
			if(entry.getKey().split("\\.")[0].equalsIgnoreCase(startingDomainName)){
					return getContext(entry.getKey());
			}
		}
		return null;

	}
	
}
