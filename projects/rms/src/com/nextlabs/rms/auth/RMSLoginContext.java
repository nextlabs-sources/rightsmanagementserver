package com.nextlabs.rms.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.ADServerInfo;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.locale.RMSMessageHandler;

public class RMSLoginContext {
	
	private String ldapHost;
	private String searchBase;
	private String domain;
	private String userSearchQuery;
	private String ldapType;
	//private String sslTrustStore;
	//private String sslTrustStorePassword;
	private boolean ldapSSL;
	private RMSUserPrincipal principal=null;
	private static Logger logger = Logger.getLogger(RMSLoginContext.class);
	private List<String> returnedAttsList=new ArrayList<>(Arrays.asList("sn", "givenName", "mail", "memberOf", "userPrincipalName"));
	private ADServerInfo adServerInfo;
	public static final String DEFAULT_UNIQUE_IDENTIFIER="objectSid" ;
	public static final String LDAP_AD="AD" ;
	
	public RMSLoginContext(ADServerInfo adServerInfo){
		this.adServerInfo = adServerInfo;
	}
	
	public boolean authenticate(String userName, String password) throws Exception{
		principal = authenticateUser(userName, password);
		if(principal==null){
			logger.debug("RMSUserPrincipal is null");
			return false;
		}
		return true;
	}
	
	public RMSUserPrincipal getUserPrincipal(){
		return principal;
	}
	
	private RMSUserPrincipal authenticateUser(String userName, String password) throws Exception{
		SearchControls searchCtls = new SearchControls();
		String uidIdentifier=adServerInfo.getUniqueId();
		if(uidIdentifier==null||uidIdentifier.length()==0){
			uidIdentifier=DEFAULT_UNIQUE_IDENTIFIER;
		}
		if(!returnedAttsList.contains(uidIdentifier))
			returnedAttsList.add(uidIdentifier);
		searchCtls.setReturningAttributes((String[]) returnedAttsList.toArray(new String[returnedAttsList.size()]));
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		LdapContext ldatCtxt = null;
		boolean error = false;
		try{
			ldatCtxt= getInitialContext(userName, password);
		}
		catch(CommunicationException e){
			error = true;
			throw new RMSLoginException(RMSMessageHandler.getClientString("adHostNameErr"));
		}
		catch(AuthenticationException e){
			error = true;
			throw new RMSLoginException(RMSMessageHandler.getClientString("invalidCredentials"));
		} finally {
			if (error) {
				if (ldatCtxt != null) {
					try {
						ldatCtxt.close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		NamingEnumeration<?> answer = null;
		try {
			String searchFilter = userSearchQuery.replace("$USERID$", userName);
	
			answer = ldatCtxt.search(searchBase, searchFilter,searchCtls);
			String userGroup=null;
			if(answer==null || !answer.hasMoreElements()){
				throw new RMSLoginException(RMSMessageHandler.getClientString("invalidCredentials"));
			}
			String rmsAdminUserName = adServerInfo.getRmsAdmin();
			RMSUserPrincipal rmsPrincipal=new RMSUserPrincipal();
			rmsPrincipal.setUserName(userName);
			rmsPrincipal.setPassword(password.toCharArray());
			rmsPrincipal.setDomain(domain);
			rmsPrincipal.setTenantId(ConfigManager.KMS_DEFAULT_TENANT_ID);
			rmsPrincipal.setRMSUser(true);
			if(userName.equalsIgnoreCase(rmsAdminUserName)){
				rmsPrincipal.setRole(RMSUserPrincipal.ADMIN_USER);
			}
			logger.debug("Role of user '"+userName+"' : "+ rmsPrincipal.getRole());
			SearchResult searchresult = (SearchResult) answer.next();
			Attributes attributes = searchresult.getAttributes();
			String uid=getUID(searchresult,uidIdentifier);
			logger.debug("Uid of user '"+userName+"' : "+uid);
			rmsPrincipal.setUid(uid);
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
			Attribute userPrincipalAttribute = attributes.get("userPrincipalName");
			if (userPrincipalAttribute != null && userPrincipalAttribute.size() > 0) {
				rmsPrincipal.setPrincipalName(String.valueOf(userPrincipalAttribute.get()));
			}
			
			return rmsPrincipal;
		} finally {
			if (answer != null) {
				try {
					answer.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (ldatCtxt != null) {
				try {
					ldatCtxt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private String getUID(SearchResult searchresult,String uidIdentifier) throws NamingException {
		String uid="";
		logger.debug("uidIdentifier of user is : "+uidIdentifier);
		if(searchresult.getAttributes().get(uidIdentifier)!=null){
			if(ldapType.equalsIgnoreCase(LDAP_AD) && uidIdentifier.equalsIgnoreCase(DEFAULT_UNIQUE_IDENTIFIER)){
				byte[] sidByteArr= (byte[])searchresult.getAttributes().get(DEFAULT_UNIQUE_IDENTIFIER).get();
				uid = getSIDAsString(sidByteArr);
			}
			else{
				uid=(String) searchresult.getAttributes().get(uidIdentifier).get();
			}
		}
		return uid;

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
	
	private LdapContext getInitialContext(String userName, String password)
			throws NamingException {		
		ldapHost = adServerInfo.getLdapHost();
		ldapType=adServerInfo.getLdapType();
		if(ldapType==null || ldapType.trim().length()==0){
			ldapType=LDAP_AD;
		}
		searchBase = adServerInfo.getSearchBase();
		domain = adServerInfo.getDomain();
		userSearchQuery = adServerInfo.getUserSearchQuery();
		ldapSSL = adServerInfo.isLdapSSL(); 
		//sslTrustStore = adServerInfo.getTrustStore();
		//sslTrustStorePassword = adServerInfo.getTrustStorePassword();
		if (ldapSSL) {
			ldapHost = "ldaps://" + ldapHost;
		} else {
			ldapHost = "ldap://" + ldapHost;
		}
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapHost);	
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, getSecurityPrincipal(userName));
		env.put(Context.SECURITY_CREDENTIALS, password);
		if(ldapType!=null && ldapType.equalsIgnoreCase(LDAP_AD))
			env.put("java.naming.ldap.attributes.binary","objectSID");
		
		if (ldapSSL) {
			/*System.setProperty("javax.net.ssl.trustStore", sslTrustStore);
			if (sslTrustStorePassword != null && !sslTrustStorePassword.equals("")) {
				System.setProperty("javax.net.ssl.trustStorePassword", sslTrustStorePassword);
			}*/
			env.put(DirContext.SECURITY_PROTOCOL, "ssl");
			// System.setProperty("javax.net.debug","ssl");
		}
		LdapContext ctx = new InitialLdapContext(env, null);
		return ctx;
	}
	
	private String getSecurityPrincipal(String userName) {
		String securityPrincipal="";
		if(ldapType.equalsIgnoreCase(LDAP_AD)){
			logger.debug("Using AD authentication for user : "+userName);
			if(adServerInfo.isSecurityPrincipalUseUserID())
				securityPrincipal= userName;
			else
				securityPrincipal= userName + "@" + domain;
		}
		else{
			logger.debug("Using OPENLDAP authentication for user : "+userName);
			securityPrincipal="cn="+userName+","+adServerInfo.getSearchBase();
		}
		return securityPrincipal;
	}

}