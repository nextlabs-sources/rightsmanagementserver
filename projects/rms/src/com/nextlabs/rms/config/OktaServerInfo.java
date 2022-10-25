package com.nextlabs.rms.config;

public class OktaServerInfo {
    
    private String clientId;
    private String clientSecret;
    private String serverUrl;
    private String authServerId;

    public OktaServerInfo(String clientId, String clientSecret, String serverUrl, String authServerId){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.serverUrl = serverUrl;
        this.authServerId = authServerId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getAuthServerId() {
        return authServerId;
    }
}