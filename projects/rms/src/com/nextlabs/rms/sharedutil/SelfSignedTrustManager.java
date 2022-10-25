package com.nextlabs.rms.sharedutil;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class SelfSignedTrustManager implements X509TrustManager
{
  /**
   * Doesn't throw an exception, so this is how it approves a certificate.
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
   **/
  public void checkClientTrusted ( X509Certificate[] cert, String authType ) 
  {
  }

  /**
   * Doesn't throw an exception, so this is how it approves a certificate.
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
   **/
  public void checkServerTrusted ( X509Certificate[] cert, String authType ) 
  {
  }

  /**
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   **/
  public X509Certificate[] getAcceptedIssuers ()
  {
    return null;   
  }
}