package com.nextlabs.rms.services.manager;

// import com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligationsData;

import com.bluejungle.framework.security.KeyNotFoundException;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.KeyRequestDTO;
import com.bluejungle.pf.destiny.lib.KeyResponseDTO;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.engine.destiny.BundleVaultException;
import com.bluejungle.pf.engine.destiny.IBundleVault;
import com.nextlabs.kms.GetAllKeyRingsWithKeysResponse;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;
import com.nextlabs.rms.util.HttpClientUtil;

import noNamespace.*;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;

public class HeartbeatManager {
	
	private static final String NL_KM_CLIENT = "NL_KM_CLIENT";

	private static Logger logger = Logger.getLogger(HeartbeatManager.class);
	
	private static String rmcClassificationXML="";

    private static final Map<AgentCapability.Enum, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability> agentCapabilityMap;
    static {
        Map<AgentCapability.Enum, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability> map = new HashMap<AgentCapability.Enum, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability>();
        map.put(AgentCapability.EMAIL, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability.EMAIL);
        map.put(AgentCapability.FILESYSTEM, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability.FILESYSTEM);
        map.put(AgentCapability.PORTAL, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability.PORTAL);
        agentCapabilityMap = Collections.unmodifiableMap(map);
    }

    private static final Map<AgentTypeEnum.Enum, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum> agentTypeMap;
    static {
        Map<AgentTypeEnum.Enum, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum> map = new HashMap<AgentTypeEnum.Enum, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum>();
        map.put(AgentTypeEnum.ACTIVE_DIRECTORY, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum.ACTIVE_DIRECTORY);
        map.put(AgentTypeEnum.DESKTOP, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum.DESKTOP);
        map.put(AgentTypeEnum.FILE_SERVER, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum.FILE_SERVER);
        map.put(AgentTypeEnum.PORTAL, com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentTypeEnum.PORTAL);
        agentTypeMap = Collections.unmodifiableMap(map);
    }

    private static HeartbeatManager instance = new HeartbeatManager();

    private HeartbeatManager(){
    }

    public static HeartbeatManager getInstance(){
        return instance;
    }

    private static class BundleInfoImpl implements IBundleVault.BundleInfo {
        private final IDeploymentBundle bundle;
        private final String[] subjects;
        public BundleInfoImpl(IDeploymentBundle bundle, String[] subjects) {
            this.bundle = bundle;
            this.subjects = subjects;
        }
        public IDeploymentBundleV2 getBundle() {
            /* This function is only ever called by new, V2, agents,
             * who are expecting a V2 bundle.  The constructor,
             * however, is called on the server, which might have to
             * build bundles for either old or new systems.  This
             * explains why we take IDeploymentBundle, but only give
             * out IDeploymentBundleV2
             */
            if (bundle instanceof IDeploymentBundleV2) {
                return (IDeploymentBundleV2)bundle;
            } else {
                return null;
            }
        }
        public String[] getSubjects() {
            return subjects;
        }
    }

    public com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentHeartbeatData getCCAgentHeartBeatDataFromRMS(AgentHeartbeatData data) throws DatatypeConfigurationException {
        if (data == null) {
            return null;
        }
        KeyRequestDTO keyDto=new KeyRequestDTO();
        com.bluejungle.destiny.services.agent.types.AgentPluginData pluginData = new com.bluejungle.destiny.services.agent.types.AgentPluginData();
        com.bluejungle.destiny.services.agent.types.AgentPluginDataElement[] array= new com.bluejungle.destiny.services.agent.types.AgentPluginDataElement[1];
        com.bluejungle.destiny.services.agent.types.AgentPluginDataElement elem = new com.bluejungle.destiny.services.agent.types.AgentPluginDataElement();
 
        if(data.getKeyRingsMetaData()!=null && data.getKeyRingsMetaData().getKeyRingMetaDataArray()!=null && data.getKeyRingsMetaData().getKeyRingMetaDataArray().length>0 ){
        	KeyRingMetaDataType[] keyRingMetaDataArray = data.getKeyRingsMetaData().getKeyRingMetaDataArray();
        	for(KeyRingMetaDataType meta:keyRingMetaDataArray){
        		keyDto.addKeyRing(meta.getKeyRingName(),meta.getLastModifiedDate().getTimeInMillis());
        	}
        }
        array[0]=elem;
        elem.setId(NL_KM_CLIENT);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
        	os = new ObjectOutputStream(out);
			os.writeObject(keyDto);
		} catch (IOException e) {
			logger.error("Error occurred when getting CC agent heart beat data: " + e.getMessage(), e);
		}finally{
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
        elem.set_value(Base64.encodeBase64String(out.toByteArray()));

        pluginData.setElement(array);
        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentHeartbeatData ccRequest = new com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentHeartbeatData();
        ccRequest.setPluginData(getAgentPluginDataCC(pluginData));
        ccRequest.setPolicyAssemblyStatus(getDeploymentRequestCC(data.getPolicyAssemblyStatus()));
        ccRequest.setProfileStatus(getProfileStatusDataCC(data.getProfileStatus()));
        ccRequest.setSharedFolderDataCookie(getSharedFolderDataCookieCC(data.getSharedFolderDataCookie()));

        return ccRequest;
    }

    private com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData getAgentPluginDataCC(com.bluejungle.destiny.services.agent.types.AgentPluginData rmsPluginData) {
        if (rmsPluginData == null) {
            return null;
        }

        com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData ccPluginData = new com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData();

        List<com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement> ccPluginElementsList = new ArrayList<com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement>();
        com.bluejungle.destiny.services.agent.types.AgentPluginDataElement[] elementArray = rmsPluginData.getElement();
        for (int i=0; i < elementArray.length; ++i) {
            com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement ccPluginDataElement = new com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement();
            ccPluginDataElement.setId(elementArray[i].getId());
            ccPluginDataElement.setValue(elementArray[i].get_value());
            ccPluginElementsList.add(ccPluginDataElement);
        }

        ccPluginData.setElement(ccPluginElementsList);

        return ccPluginData;
    }
    
    
    /*private com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData getAgentPluginDataCC(AgentPluginData rmsPluginData) {
        if (rmsPluginData == null) {
            return null;
        }

        com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData ccPluginData = new com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData();

        List<com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement> ccPluginElementsList = new ArrayList<com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement>();
        AgentPluginDataElement[] elementArray = rmsPluginData.getElementArray();
        for (int i=0; i < elementArray.length; ++i) {
            com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement ccPluginDataElement = new com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement();
            ccPluginDataElement.setId(elementArray[i].getId());
            ccPluginDataElement.setValue(elementArray[i].getStringValue());
            ccPluginElementsList.add(ccPluginDataElement);
        }

        ccPluginData.setElement(ccPluginElementsList);

        return ccPluginData;
    }*/

    private com.nextlabs.rms.services.cxf.destiny.services.policy.types.DeploymentRequest getDeploymentRequestCC(DeploymentRequest rmsDeploymentRequest) throws DatatypeConfigurationException {
        if (rmsDeploymentRequest == null) {
            return null;
        }

//        System.out.println(rmsDeploymentRequest.toString());
        com.nextlabs.rms.services.cxf.destiny.services.policy.types.DeploymentRequest ccDeploymentRequest = new com.nextlabs.rms.services.cxf.destiny.services.policy.types.DeploymentRequest();

        AgentCapability.Enum[] rmsAgentCapabilitiesArray = rmsDeploymentRequest.getAgentCapabilitiesArray();
        List<com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability> ccAgentCapabilitiesList = new ArrayList<com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability>();
        for (AgentCapability.Enum rmsAgentCapability : rmsAgentCapabilitiesArray) {
            ccAgentCapabilitiesList.add(agentCapabilityMap.get(rmsAgentCapability));
        }
        com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability[] ccAgentCapabilitiesArray = ccAgentCapabilitiesList.toArray(new com.nextlabs.rms.services.cxf.destiny.services.policy.types.AgentCapability[ccAgentCapabilitiesList.size()]);
        ccDeploymentRequest.setAgentCapabilities(ccAgentCapabilitiesArray);

        ccDeploymentRequest.setAgentHost(rmsDeploymentRequest.getAgentHost());
        ccDeploymentRequest.setAgentType(agentTypeMap.get(rmsDeploymentRequest.getAgentType()));

        SystemUser[] rmsSystemUsersArray = rmsDeploymentRequest.getPolicyUsersArray();
        List<com.nextlabs.rms.services.cxf.destiny.services.policy.types.SystemUser> ccPolicyUsersList = new ArrayList<com.nextlabs.rms.services.cxf.destiny.services.policy.types.SystemUser>();
        for (SystemUser rmsSystemUser : rmsSystemUsersArray) {
            com.nextlabs.rms.services.cxf.destiny.services.policy.types.SystemUser ccSystemUser = new com.nextlabs.rms.services.cxf.destiny.services.policy.types.SystemUser();
            ccSystemUser.setSystemId(rmsSystemUser.getSystemId());
            ccSystemUser.setUserSubjectType(rmsSystemUser.getUserSubjectType());
            ccPolicyUsersList.add(ccSystemUser);
        }
        com.nextlabs.rms.services.cxf.destiny.services.policy.types.SystemUser[] ccSystemUsersArray = ccPolicyUsersList.toArray(new com.nextlabs.rms.services.cxf.destiny.services.policy.types.SystemUser[ccPolicyUsersList.size()]);
        ccDeploymentRequest.setPolicyUsers(ccSystemUsersArray);

        XMLGregorianCalendar ccTimestamp = getGregorianCalendar(rmsDeploymentRequest.getTimestamp().getTime());
        ccDeploymentRequest.setTimestamp(ccTimestamp);

        String[] rmsUserSubjectTypesArray = rmsDeploymentRequest.getUserSubjectTypesArray();
        List<String> ccUserSubjectTypesArray = new ArrayList<String>();
        for (int i=0; i < rmsUserSubjectTypesArray.length; ++i) {
            ccUserSubjectTypesArray.add(rmsUserSubjectTypesArray[i]);
        }
        ccDeploymentRequest.setUserSubjectTypes(ccUserSubjectTypesArray.toArray(new String[ccUserSubjectTypesArray.size()]));

        return ccDeploymentRequest;
    }

    private com.nextlabs.rms.services.cxf.destiny.services.profile.types.AgentProfileStatusData getProfileStatusDataCC(AgentProfileStatusData rmsProfileStatusData) throws DatatypeConfigurationException {
        if (rmsProfileStatusData == null) {
            return null;
        }

        com.nextlabs.rms.services.cxf.destiny.services.profile.types.AgentProfileStatusData ccProfileStatusData = new com.nextlabs.rms.services.cxf.destiny.services.profile.types.AgentProfileStatusData();
        ccProfileStatusData.setLastCommittedAgentProfileName(rmsProfileStatusData.getLastCommittedAgentProfileName());

        XMLGregorianCalendar ccTimestamp = getGregorianCalendar(rmsProfileStatusData.getLastCommittedAgentProfileTimestamp().getTime());
        ccProfileStatusData.setLastCommittedAgentProfileTimestamp(ccTimestamp);

        ccProfileStatusData.setLastCommittedCommProfileName(rmsProfileStatusData.getLastCommittedCommProfileName());

        ccTimestamp = getGregorianCalendar(rmsProfileStatusData.getLastCommittedCommProfileTimestamp().getTime());
        ccProfileStatusData.setLastCommittedCommProfileTimestamp(ccTimestamp);

        return ccProfileStatusData;
    }

    private com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderDataCookie getSharedFolderDataCookieCC(SharedFolderDataCookie rmsSharedFolderDataCookie) throws DatatypeConfigurationException {
        if (rmsSharedFolderDataCookie == null) {
            return null;
        }

        com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderDataCookie ccSharedFolderDataCookie = new com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderDataCookie();

        XMLGregorianCalendar ccTimestamp = getGregorianCalendar(rmsSharedFolderDataCookie.getTimestamp().getTime());
        ccSharedFolderDataCookie.setTimestamp(ccTimestamp);

        return ccSharedFolderDataCookie;
    }

	public static XMLGregorianCalendar getGregorianCalendar(Date date)
			throws DatatypeConfigurationException {
		GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        XMLGregorianCalendar ccTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		return ccTimestamp;
	}

    public com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentUpdateAcknowledgementData getCCAgentUpdateAcknowledgementDataDataFromRMS(AckHeartBeatData data) throws DatatypeConfigurationException {
        if (data == null) {
            return null;
        }

        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentUpdateAcknowledgementData ccRequest = new com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentUpdateAcknowledgementData();
        ccRequest.setProfileStatus(getProfileStatusDataCC(data.getProfileStatus()));
        ccRequest.setPolicyAssemblyStatus(getDeploymentRequestCC(data.getPolicyAssemblyStatus()));

        return ccRequest;
    }

    public AgentUpdates getRMSAgentUpdates(com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentUpdates ccResponse, SSLSocketFactoryGenerator sslSocketFactoryGenerator, String tenantId) throws IOException, ClassNotFoundException {
        if (ccResponse == null) {
            return null;
        }

        AgentUpdates rmsResponse = AgentUpdates.Factory.newInstance();

        if(ccResponse.getAgentProfile()!=null){
            rmsResponse.setAgentProfile(RegisterAgentManager.getInstance().getRMSAgentProfileDTO(ccResponse.getAgentProfile()));        	
        }
        com.nextlabs.rms.services.cxf.destiny.services.management.types.CommProfileDTO commProfile = ccResponse.getCommProfile();
        if(commProfile!=null){
            rmsResponse.setCommProfile(RegisterAgentManager.getInstance().getRMSCommProfileDTO(commProfile));        	
        }
        com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligationsData customObligationsData = ccResponse.getCustomObligationsData();
        rmsResponse.setCustomObligationsData(getCustomObligationsData(customObligationsData));
        // Don't know what to do for rmsResponse.setKeyRings();
        // for now we will ignore key rings -- currently the plugin is being used exclusively for key-value pairs
        com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData ccPluginData = ccResponse.getPluginData();
        com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData agentPluginData=ccResponse.getPluginData();
        byte[] val=Base64.decodeBase64(agentPluginData.getElement().get(0).getValue());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(val));
        Serializable ret = (Serializable)ois.readObject();
        String kmsurl=GlobalConfigManager.getInstance().getKMSUrl();
        if(kmsurl!=null&&kmsurl.length()>0 && GlobalConfigManager.getInstance().useKmsKeys()){
        	try {
        		String responseString = HttpClientUtil.getAllKeyRingsWithKeys(tenantId);
        		GetAllKeyRingsWithKeysResponse getKeyRingNamesResponse = JAXB.unmarshal(new StringReader(responseString), GetAllKeyRingsWithKeysResponse.class);
        		rmsResponse.setKeyRings(KeyRingTransformer.transform(getKeyRingNamesResponse));
        	} catch (RMSException e) {
        		logger.error("Error occured while getting the keyRingWithKeys from KMS",e);
        	}
        }
        else{
        	 KeyResponseDTO keyResponse=(KeyResponseDTO) ret;
             rmsResponse.setKeyRings(KeyRingTransformer.transform(keyResponse));
        }
//        rmsResponse.setPluginData(getAgentPluginDataRMS(ccPluginData));
        rmsResponse.setClassificationProfile(getClassificationProfile());
        rmsResponse.setSupportedCadFormats(getSupportedCadFormats());
        
        String bundleStr = ccResponse.getPolicyDeploymentBundle();
        IDeploymentBundleV2 bundle = null;
		try {
			bundle = updateBundle(bundleStr, sslSocketFactoryGenerator);
		} catch (GeneralSecurityException | BundleVaultException
				| InvalidBundleException e) {
			logger.error(e);
		}
        POLICYBUNDLETYPE policyBundleType = null;
        if (bundle != null) {
            PolicyTransformer transformer=new PolicyTransformer();
		    try {
    			policyBundleType = transformer.tranformPolicies(bundle,customObligationsData);
    		} catch (Exception e) {
    			logger.error("Error occurred while transforming policies", e);
    		}
        }

        rmsResponse.setPolicyDeploymentBundle(policyBundleType);
        com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderData sharedFolderData = ccResponse.getSharedFolderData();
        rmsResponse.setSharedFolderData(getSharedFolderData(sharedFolderData));

        return rmsResponse;
    }

    private SupportedCADFormatsType getSupportedCadFormats() {
    	SupportedCADFormatsType supportedCADFormatsType =SupportedCADFormatsType.Factory.newInstance() ;
    	StringBuilder supportedHOOPSNonAssemblyList=new StringBuilder();
    	StringBuilder supportedHOOPSAssemblyList=new StringBuilder();
    	List<String> HOOPSNonAssemblyFormats = GlobalConfigManager.getInstance().getSupportedHOOPSNonAssemblyFormatList();
    	List<String> HOOPSAssemblyFormats = GlobalConfigManager.getInstance().getSupportedHOOPSAssemblyFormatList();
    	
    	String prefix="";
    	for(String cadFormat:HOOPSNonAssemblyFormats){
    		supportedHOOPSNonAssemblyList.append(prefix);
    		prefix = ",";
    		supportedHOOPSNonAssemblyList.append(cadFormat);
    	}
    	
    	String prefixAssembly="";
    	for(String cadFormat:HOOPSAssemblyFormats){
    		supportedHOOPSAssemblyList.append(prefixAssembly);
    		prefixAssembly = ",";
    		supportedHOOPSAssemblyList.append(cadFormat);
    	}
    	
    	supportedCADFormatsType.setNonAssembly(supportedHOOPSNonAssemblyList.toString());
    	supportedCADFormatsType.setAssembly(supportedHOOPSAssemblyList.toString());
    	
    	return supportedCADFormatsType;
    }

	public static ClassificationProfileType getClassificationProfile() {
    	ClassificationProfileType classfnProfile = null;
		try {
			String openClassificationTag = "<ClassificationProfile>";
			String closeClassificationTag = "</ClassificationProfile>";
			if(rmcClassificationXML.length()==0){
				StringBuilder XMLbuilder = new StringBuilder(); 
				List<String> lines = Files.readAllLines(Paths.get(GlobalConfigManager.getInstance().getDataDir() + File.separator + "RMC_Classification.xml"), Charset.forName("UTF-8"));
	        	if(lines!=null && lines.get(0).contains(openClassificationTag) && lines.get(lines.size()-1).contains(closeClassificationTag)){
	        		lines.remove(0);
	        		lines.remove(lines.size()-1);
	        	}
	        	for(String s:lines){
	        		XMLbuilder.append(s.trim());
	        	}
	        	rmcClassificationXML=XMLbuilder.toString();
	        }
			classfnProfile = ClassificationProfileType.Factory.parse(rmcClassificationXML);
		} catch (Exception e) {
			logger.error("Error occurred while getting Classification Profile", e);			
		}
        return classfnProfile;
	}

    protected IDeploymentBundleV2 updateBundle(String bundleStr, SSLSocketFactoryGenerator sslSocketFactoryGenerator) throws GeneralSecurityException, BundleVaultException, InvalidBundleException {
        if (bundleStr == null) {
            logger.info("Policy Bundle is null; No policies to update.");
            return null;
        }

        DeploymentBundleSignatureEnvelope bundleEnvelope = DTOUtils.decodeDeploymentBundle(bundleStr);
        IBundleVault.BundleInfo bundleInfo = validateAndStore(bundleEnvelope, sslSocketFactoryGenerator);
        if (bundleInfo == null) {
            logger.error("bundleInfo cannot be null.");
            return null;
        }
        return bundleInfo.getBundle();
    }
    
    public IBundleVault.BundleInfo validateAndStore(DeploymentBundleSignatureEnvelope signedDeploymentBundle, SSLSocketFactoryGenerator sslSocketFactoryGenerator) throws InvalidBundleException, BundleVaultException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, GeneralSecurityException {
        IDeploymentBundleV2 deploymentBundle = null;
        String[] subjects = null;
        try {
            KeyStore tsSecure = sslSocketFactoryGenerator.getTrustStoreSecure();
            Certificate certificate = tsSecure.getCertificate(CommCCManager.SERVER_PUBLIC_KEY_ALIAS);
            PublicKey publicKey = certificate.getPublicKey();

            deploymentBundle = signedDeploymentBundle.getDeploymentBundle(publicKey);
            subjects = signedDeploymentBundle.getSubjects();

        } catch (IOException exception) {
            throw new BundleVaultException("Failed to store bundle", exception);
        } catch (InvalidKeyException exception) {
            throw new BundleVaultException("Failed to validate bundle", exception);
        } catch (KeyNotFoundException exception) {
            throw new BundleVaultException("Couldn't find key", exception);
        } catch (SignatureException exception) {
            throw new BundleVaultException("Failed to validate bundle", exception);
        }

        return new BundleInfoImpl(deploymentBundle, subjects);
    }
    
	private CustomObligationsData getCustomObligationsData (com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligationsData ccData) {
        if (ccData == null) {
            return null;
        }

        CustomObligationsData rmsData = CustomObligationsData.Factory.newInstance();

        List<com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligation> ccCustomObligations = ccData.getCustomObligation();
        List<CustomObligation> rmsCustomObligations = new ArrayList<CustomObligation>();
        for (com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligation ccCustomObligation : ccCustomObligations) {
            CustomObligation rmsCustomObligation = getCustomObligation(ccCustomObligation);
            rmsCustomObligations.add(rmsCustomObligation);
        }
        CustomObligation[] rmsObligations = new CustomObligation[rmsCustomObligations.size()];
        rmsObligations = rmsCustomObligations.toArray(rmsObligations);

        rmsData.setCustomObligationArray(rmsObligations);

        return rmsData;
    }

    private CustomObligation getCustomObligation(com.nextlabs.rms.services.cxf.destiny.types.custom_obligations.CustomObligation ccObligation) {
        CustomObligation rmsObligation = CustomObligation.Factory.newInstance();
        rmsObligation.setDisplayName(ccObligation.getDisplayName());
        rmsObligation.setInvocationString(ccObligation.getInvocationString());
        rmsObligation.setRunAt(ccObligation.getRunAt());
        rmsObligation.setRunBy(ccObligation.getRunBy());

        return rmsObligation;
    }

    // note: this is not doing any transformation to the bundle
    private PolicyDeploymentBundleType getPolicyDeploymentBundle (String ccPolicyBundle) {
        if (ccPolicyBundle == null) {
            return null;
        }

        PolicyDeploymentBundleType rmsPolicyBundleType = PolicyDeploymentBundleType.Factory.newInstance();
        rmsPolicyBundleType.setBundle(ccPolicyBundle);

        return rmsPolicyBundleType;
    }

    private SharedFolderData getSharedFolderData (com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderData ccData) {
        if (ccData == null) {
            return null;
        }

        SharedFolderData rmsData = SharedFolderData.Factory.newInstance();

        com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderAliasList ccSharedAliasList = ccData.getAliasList();
        SharedFolderAliasList rmsSharedAliasList = SharedFolderAliasList.Factory.newInstance();
        rmsData.setAliasList(rmsSharedAliasList);
        List<com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderAliases> ccAliasesList = ccSharedAliasList.getAliases();
        List<SharedFolderAliases> rmsAliasesList = new ArrayList<SharedFolderAliases>();
        for (com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderAliases ccAliases : ccAliasesList) {
            SharedFolderAliases rmsAliases = SharedFolderAliases.Factory.newInstance();
            List<com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderAliases.Alias> ccAliasList = ccAliases.getAlias();
            List<SharedFolderAliases.Alias> rmsAliasList = new ArrayList<SharedFolderAliases.Alias>();
            for (com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderAliases.Alias ccAlias : ccAliasList) {
                SharedFolderAliases.Alias rmsAlias = SharedFolderAliases.Alias.Factory.newInstance();
                rmsAlias.setName(ccAlias.getName());
                rmsAliasList.add(rmsAlias);
            }
            SharedFolderAliases.Alias[] rmsAliasArray = new SharedFolderAliases.Alias[rmsAliasList.size()];
            rmsAliasArray = rmsAliasList.toArray(rmsAliasArray);

            rmsAliases.setAliasArray(rmsAliasArray);
            rmsAliasesList.add(rmsAliases);
        }
        SharedFolderAliases[] rmsSharedAliasArray = new SharedFolderAliases[rmsAliasesList.size()];
        rmsSharedAliasArray = rmsAliasesList.toArray(rmsSharedAliasArray);

        rmsSharedAliasList.setAliasesArray(rmsSharedAliasArray);
        rmsData.setAliasList(rmsSharedAliasList);

        com.nextlabs.rms.services.cxf.destiny.types.shared_folder.SharedFolderDataCookie ccCookie = ccData.getCookie();
        SharedFolderDataCookie rmsCookie = SharedFolderDataCookie.Factory.newInstance();
        rmsCookie.setTimestamp(ccCookie.getTimestamp().toGregorianCalendar());

        rmsData.setCookie(rmsCookie);

        return rmsData;
    }

    private AgentPluginData getAgentPluginDataRMS(com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginData ccPluginData) {
        if (ccPluginData == null) {
            return null;
        }

        AgentPluginData rmsPluginData = AgentPluginData.Factory.newInstance();

        List<com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement> ccPluginElementsList = ccPluginData.getElement();
        List<AgentPluginDataElement> rmsPluginElementsList = new ArrayList<AgentPluginDataElement>();

        for (com.nextlabs.rms.services.cxf.destiny.services.plugin.types.AgentPluginDataElement ccPluginElement : ccPluginElementsList) {
            AgentPluginDataElement rmsPluginElement = AgentPluginDataElement.Factory.newInstance();
            rmsPluginElement.setId(ccPluginElement.getId());
            rmsPluginElement.setStringValue(ccPluginElement.getValue());

            rmsPluginElementsList.add(rmsPluginElement);
        }

        AgentPluginDataElement[] rmsPluginElementsArray = new AgentPluginDataElement[rmsPluginElementsList.size()];
        rmsPluginElementsArray = rmsPluginElementsList.toArray(rmsPluginElementsArray);
        rmsPluginData.setElementArray(rmsPluginElementsArray);

        return rmsPluginData;
    }
}
