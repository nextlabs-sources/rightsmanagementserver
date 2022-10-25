package com.nextlabs.rms.auth;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.ADServerInfo;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.locale.RMSMessageHandler;

public class RMSTCLoginContext {
	private String ldapHost;
	private String searchBase;
	private String domain;
	private String userSearchQuery;
	private String sslTrustStore;
	private String sslTrustStorePassword;
	private boolean sslAuth;
	private RMSUserPrincipal principal=null;
	private static Logger logger = Logger.getLogger(RMSTCLoginContext.class);
	private String[] returnedAtts = { "sn", "givenName", "mail", "objectSid", "memberOf" };
	private ADServerInfo adServerInfo;
	
	public RMSTCLoginContext(ADServerInfo adServerInfo){
		this.adServerInfo = adServerInfo;
	}
	
	public boolean authenticate(String userName) throws Exception{
		principal = authenticateUser(userName);
		if(principal==null){
			logger.debug("RMSUserPrincipal is null");
			return false;
		}
		return true;
	}
	
	public RMSUserPrincipal getUserPrincipal(){
		return principal;
	}
	
	private RMSUserPrincipal authenticateUser(String userName) throws Exception{
		SearchControls searchCtls = new SearchControls();
		searchCtls.setReturningAttributes(returnedAtts);
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		LdapContext ldatCtxt = null;
		try{
			ldatCtxt= getInitialContext();
		}
		catch(CommunicationException e){
			throw new RMSLoginException(RMSMessageHandler.getClientString("adHostNameErr"));
		}
		catch(AuthenticationException e){
			throw new RMSLoginException(RMSMessageHandler.getClientString("invalidCredentials"));
		}
		String searchFilter = userSearchQuery.replace("$USERID$", userName);

		NamingEnumeration<?> answer = ldatCtxt.search(searchBase, searchFilter,searchCtls);
		String userGroup=null;
		if(answer==null || !answer.hasMoreElements()){
			throw new RMSLoginException(RMSMessageHandler.getClientString("invalidCredentials"));
		}
		String rmsAdminUserName = adServerInfo.getRmsAdmin();
		RMSUserPrincipal rmsPrincipal=new RMSUserPrincipal();
		rmsPrincipal.setUserName(userName);
		//rmsPrincipal.setPassword(password.toCharArray());
		rmsPrincipal.setDomain(domain);
		rmsPrincipal.setTenantId(ConfigManager.KMS_DEFAULT_TENANT_ID);
		rmsPrincipal.setRMSUser(true);
		if(userName.equalsIgnoreCase(rmsAdminUserName)){
			rmsPrincipal.setRole(RMSUserPrincipal.ADMIN_USER);
		}
		logger.debug("Role of user '"+userName+"' : "+ rmsPrincipal.getRole());
		SearchResult searchresult = (SearchResult) answer.next();
		byte[] sidByteArr = (byte[])searchresult.getAttributes().get("objectSid").get();
		String sid = getSIDAsString(sidByteArr);
		logger.debug("Sid of user '"+userName+"' : "+sid);
		rmsPrincipal.setUid(sid);
		String rmsUserGrp=adServerInfo.getRmsGroup();
		if(rmsUserGrp!=null && rmsUserGrp.length()>0){
			Attribute memberOf = (Attribute) searchresult.getAttributes().get("memberOf");
			if(memberOf==null){
				//RMSUserGroup sepcified in config, but user not part of any group
				throw new RMSLoginException(RMSMessageHandler.getClientString("userNotInADGroup"));
			}
			String[] confUserGrpArr =  rmsUserGrp.split(",");	
			userGroup= memberOf.toString().split(":")[1];		
			String[] groups=userGroup.split(",");
			boolean isMemberOfGroup = false;
			for(String groupName:groups)
			{
				for (String confUserGrp : confUserGrpArr) {
					if(groupName.trim().equalsIgnoreCase("CN="+confUserGrp)){
						isMemberOfGroup = true;	
						break;
					}
				}
			}
			if(!isMemberOfGroup){
				//RMSUserGroup sepcified in config, but user not part of that group
				throw new RMSLoginException(RMSMessageHandler.getClientString("userNotInADGroup"));
			}
		}
		return rmsPrincipal;
	}
	
	public static String getSIDAsString(byte[] SID) {
		   // Add the 'S' prefix
		   StringBuilder strSID = new StringBuilder("S-");

		   // bytes[0] : in the array is the version (must be 1 but might 
		   // change in the future)
		   strSID.append(SID[0]).append('-');

		   // bytes[2..7] : the Authority
		   StringBuilder tmpBuff = new StringBuilder();
		   for (int t=2; t<=7; t++) {
		      String hexString = Integer.toHexString(SID[t] & 0xFF);
		      tmpBuff.append(hexString);
		   }
		   strSID.append(Long.parseLong(tmpBuff.toString(),16));

		   // bytes[1] : the sub authorities count
		   int count = SID[1];

		   // bytes[8..end] : the sub authorities (these are Integers - notice
		   // the endian)
		   for (int i = 0; i < count; i++) {
		      int currSubAuthOffset = i*4;
		      tmpBuff.setLength(0);
		      tmpBuff.append(String.format("%02X%02X%02X%02X", 
		        (SID[11 + currSubAuthOffset]& 0xFF),
		        (SID[10 + currSubAuthOffset]& 0xFF),
		        (SID[9 + currSubAuthOffset]& 0xFF),
		        (SID[8 + currSubAuthOffset]& 0xFF)));

		      strSID.append('-').append(Long.parseLong(tmpBuff.toString(), 16));
		   }
		   return strSID.toString();
	}
	
	private LdapContext getInitialContext()
			throws NamingException {		
		ldapHost = adServerInfo.getLdapHost();
		searchBase = adServerInfo.getSearchBase();
		domain = adServerInfo.getDomain();
		userSearchQuery = adServerInfo.getUserSearchQuery();
		sslTrustStore = adServerInfo.getTrustStore();
		sslAuth = false;
		if (sslTrustStore != null && !sslTrustStore.equals("")) {
			sslAuth = true;
		}

		if (sslAuth) {
			ldapHost = "ldaps://" + ldapHost;
		} else {
			ldapHost = "ldap://" + ldapHost;
		}
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapHost);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, adServerInfo.getQueryDN() + "@" + domain);
		env.put(Context.SECURITY_CREDENTIALS, adServerInfo.getQueryDNPassword());
		env.put("java.naming.ldap.attributes.binary","objectSID");
		
		if (sslAuth) {
			System.setProperty("javax.net.ssl.trustStore", sslTrustStore);
			if (sslTrustStorePassword != null && !sslTrustStorePassword.equals("")) {
				System.setProperty("javax.net.ssl.trustStorePassword", sslTrustStorePassword);
			}
			env.put(DirContext.SECURITY_PROTOCOL, "ssl");
			// System.setProperty("javax.net.debug","ssl");
		}
		LdapContext ctx = new InitialLdapContext(env, null);
		return ctx;
	}

}
