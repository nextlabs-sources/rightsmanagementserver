package com.nextlabs.rms.services.manager;

import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.engine.destiny.BundleVaultException;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.services.application.RMSApplicationErrorCodes;
import com.nextlabs.rms.services.cxf.destiny.interfaces.log.v5.LogServiceIF;
import com.nextlabs.rms.services.cxf.destiny.services.ServiceNotReadyFault;
import com.nextlabs.rms.services.cxf.destiny.services.UnauthorizedCallerFault;
import com.nextlabs.rms.services.cxf.destiny.services.agent.AgentServiceIF;
import com.nextlabs.rms.services.cxf.destiny.services.management.types.AgentProfileDTO;
import com.nextlabs.rms.services.cxf.destiny.services.management.types.CommProfileDTO;
import com.nextlabs.rms.services.cxf.domain.types.AgentTypeDTO;
import com.nextlabs.rms.services.manager.ssl.CommArtifactsCache;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;
import com.nextlabs.rms.util.StringUtils;

import noNamespace.*;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.attachments.AttachmentsImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.xmlbeans.XmlException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import java.io.*;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.*;

public class CommCCManager {
    private static Log logger = LogFactory.getLog(CommCCManager.class.toString());

    public static final String SERVER_PUBLIC_KEY_ALIAS = "dcc";
    private static final String AGENT_ENDPOINT_URL = "AgentEndpoint";
    private static final String LOG_ENDPOINT_URL = "LogEndpoint";
    private static final String AGENT_KEYSTORE_ATTACHMENT = "AgentKeyStore";
    private static final String AGENT_TRUSTSTORE_ATTACHEMENT = "AgentTrustStore";
    private static final String AGENT_EXTRA_ATTACHMENT = "extra";
    private static final String DABS_AGENT_SERVICE_URL="dabs/services/AgentServiceIFPort";
    private static final String DABS_LOGS_SERVICE_URL="dabs/services/LogServiceIFPort.v5";
    

    private static final Map<String, String> sslFilenameMap;
    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put(AGENT_KEYSTORE_ATTACHMENT, "agent-keystore.jks");
        map.put(AGENT_TRUSTSTORE_ATTACHEMENT, "agent-truststore.jks");
        map.put(AGENT_EXTRA_ATTACHMENT, "agent-key.jks");
        sslFilenameMap = Collections.unmodifiableMap(map);
    }
    private static final Map<com.nextlabs.rms.services.cxf.destiny.types.log.v3.LogStatus, ResponseEnum.Enum> logStatusMap;
    static {
        Map<com.nextlabs.rms.services.cxf.destiny.types.log.v3.LogStatus, ResponseEnum.Enum> map = new HashMap<com.nextlabs.rms.services.cxf.destiny.types.log.v3.LogStatus, ResponseEnum.Enum>();
        map.put(com.nextlabs.rms.services.cxf.destiny.types.log.v3.LogStatus.SUCCESS, ResponseEnum.SUCCESS);
        map.put(com.nextlabs.rms.services.cxf.destiny.types.log.v3.LogStatus.FAILED, ResponseEnum.FAILED);
        logStatusMap = Collections.unmodifiableMap(map);
    }

    private static final QName AGENT_SERVICE_NAME = new QName("http://bluejungle.com/destiny/services/agent", "AgentService");
    public final static QName LOG_SERVICE_NAME = new QName("http://nextlabs.com/destiny/services/log/v5", "LogService.v5");

    private static CommCCManager instance = new CommCCManager();

    // store agent id for testing purposes only
    private static BigInteger agentId = null;
    private static AgentProfileDTO ccAgentProfile;
    private static CommProfileDTO ccCommProfile;
    private static IDeploymentBundleV2 bundle;

    private CommCCManager(){
    }

    public static CommCCManager getInstance(){
        return instance;
    }

    public RegisterAgentResponseDocument registerAgent(RegisterAgentRequest request) throws IOException, ServiceException, SOAPException, GeneralSecurityException, XmlException {
        AgentRegistrationData input = request.getRegistrationData();

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(AgentServiceIF.class);
        RegisterAgentResponseDocument rmsResponseDocument = RegisterAgentResponseDocument.Factory.newInstance();
        RegisterAgentResponse response = RegisterAgentResponse.Factory.newInstance();
        CommonFault cf = CommonFault.Factory.newInstance();
        AgentServiceIF port;

        try {
            factory.setAddress(getAgentServiceUrl());
            port = (AgentServiceIF)factory.create();
            Client client = ClientProxy.getClient(port);
            String tenantId = input.getTenantId();
            if (tenantId == null) {
                tenantId = GlobalConfigManager.DEFAULT_TENANT_ID;
            }
            setTsl(client, SSLSocketFactoryGenerator.MODE_UNSECURE, tenantId);
            addInterceptor(client, tenantId);
        } catch(Exception e){
            cf.setErrorCode(RMSApplicationErrorCodes.CONFIGURATION_ERROR);
            cf.setErrorMessage("Configuration Error");
            response.setFault(cf);
            rmsResponseDocument.setRegisterAgentResponse(response);
            return rmsResponseDocument;
        }

        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentRegistrationData data = new com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentRegistrationData();
        data.setHost(input.getHost());
        data.setType(AgentTypeDTO.DESKTOP);
        com.nextlabs.rms.services.cxf.version.types.Version version = new com.nextlabs.rms.services.cxf.version.types.Version();
        version.setBuild(input.getVersion().getBuild());
        version.setMaintenance(input.getVersion().getMaintenance());
        version.setMajor(input.getVersion().getMajor());
        version.setMinor(input.getVersion().getMinor());
        version.setPatch(input.getVersion().getPatch());
        data.setVersion(version);

        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentStartupConfiguration ccConfig = null;
        AgentStartupConfiguration rmsConfig = null;

        try {
            ccConfig = port.registerAgent(data);
            cf.setErrorCode(RMSApplicationErrorCodes.NO_FAULT);
        } catch (ServiceNotReadyFault e) {
            logger.error("Agent Registration: Unable to send request to server. May be because machine is disconnected.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.SERVICE_NOT_READY_FAULT);
            cf.setErrorMessage(e.getMessage());
            logger.error(e);
        } catch (UnauthorizedCallerFault e) {
            logger.error("Server denied access. Agent Registration unsuccessful", e);
            cf.setErrorCode(RMSApplicationErrorCodes.UNAUTHORIZED_CALLER_FAULT);
            cf.setErrorMessage(e.getMessage());
        }

        if (ccConfig != null) {
            agentId = BigInteger.valueOf(ccConfig.getId().intValue());
            ccAgentProfile = ccConfig.getAgentProfile();
            ccCommProfile = ccConfig.getCommProfile();

            rmsConfig = RegisterAgentManager.getInstance().getRMSAgentStartupConfiguration(ccConfig);
        }
        response.setStartupConfiguration(rmsConfig);
        String base64Cert = SSLSocketFactoryGenerator.getKeyStoreSecureCertificate();
        response.setCertificate(base64Cert);
        response.setFault(cf);
        rmsResponseDocument.setRegisterAgentResponse(response);

        return rmsResponseDocument;
    }

    private String getAgentServiceUrl() throws RMSException{
    	String agentServiceURL=ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(GlobalConfigManager.DABS_AGENT_SERVICE);
        if(agentServiceURL==null || agentServiceURL.length()==0){
        	agentServiceURL=DABS_AGENT_SERVICE_URL;
        }
        String icenet_url=ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.ICENET_URL);
        if(icenet_url==null){
        	throw new RMSException("ICENet URL not configured");
        }
        if(!icenet_url.endsWith("/")){
        	icenet_url+="/";
        }
        return icenet_url+agentServiceURL;
    }
    
    public HeartBeatResponseDocument sendHeartbeat(HeartBeatRequestDocument rmsRequestDocument) throws Exception {
        HeartBeatRequest request = rmsRequestDocument.getHeartBeatRequest();
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(AgentServiceIF.class);

        HeartBeatResponseDocument rmsResponseDocument = HeartBeatResponseDocument.Factory.newInstance();
        HeartBeatResponse rmsResponse = rmsResponseDocument.addNewHeartBeatResponse();
        CommonFault cf = CommonFault.Factory.newInstance();
        AgentServiceIF port;
        SSLSocketFactoryGenerator sslSocketFactoryGenerator;
        String tenantId = request.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
        	tenantId = GlobalConfigManager.DEFAULT_TENANT_ID;
        }
        try {
            factory.setAddress(getAgentServiceUrl());
            port = (AgentServiceIF)factory.create();
            Client client = ClientProxy.getClient(port);
            sslSocketFactoryGenerator = setTsl(client, SSLSocketFactoryGenerator.MODE_SECURE, tenantId);
        } catch(Exception e){
            logger.error("Error configuring port for heartbeat.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.CONFIGURATION_ERROR);
            cf.setErrorMessage("Configuration Error");
            rmsResponse.setFault(cf);
            return rmsResponseDocument;
        }

        long id = request.getAgentId();
        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentHeartbeatData ccRequest = HeartbeatManager.getInstance().getCCAgentHeartBeatDataFromRMS(request.getHeartbeat());

        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentUpdates ccResponse = null;

        AgentUpdates rmsAgentUpdates = null;

        try {
            ccResponse = port.checkUpdates(BigInteger.valueOf(id), ccRequest);
            cf.setErrorCode(RMSApplicationErrorCodes.NO_FAULT);
            rmsAgentUpdates = HeartbeatManager.getInstance().getRMSAgentUpdates(ccResponse,sslSocketFactoryGenerator, tenantId);
        } catch (ServiceNotReadyFault e) {
            logger.error("Heartbeat: Unable to send request to server. Maybe because machine is disconnected.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.SERVICE_NOT_READY_FAULT);
            cf.setErrorMessage(e.getMessage());
        } catch (UnauthorizedCallerFault e) {
            logger.error("Server denied access. Heartbeat unsuccessful", e);
            cf.setErrorCode(RMSApplicationErrorCodes.UNAUTHORIZED_CALLER_FAULT);
            cf.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Heartbeat: general error has occurred", e);
        }

        rmsResponse.setAgentUpdates(rmsAgentUpdates);
        rmsResponse.setFault(cf);
        rmsResponseDocument.setHeartBeatResponse(rmsResponse);

        return rmsResponseDocument;
    }

    public AcknowledgeHeartBeatResponseDocument ackHeartbeat(AcknowledgeHeartBeatRequestDocument ackRequestDocument) throws Exception {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(AgentServiceIF.class);

        AcknowledgeHeartBeatResponseDocument rmsResponseDocument = AcknowledgeHeartBeatResponseDocument.Factory.newInstance();
        AckHeartBeatResponse rmsResponse = AckHeartBeatResponse.Factory.newInstance();
        CommonFault cf = CommonFault.Factory.newInstance();
        AgentServiceIF port;

        try {
            factory.setAddress(getAgentServiceUrl());
            port = (AgentServiceIF)factory.create();
            Client client = ClientProxy.getClient(port);
            setTsl(client, SSLSocketFactoryGenerator.MODE_SECURE, GlobalConfigManager.DEFAULT_TENANT_ID);
        } catch(Exception e){
            logger.error("Error configuring port for ack heartbeat.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.CONFIGURATION_ERROR);
            cf.setErrorMessage("Configuration Error");
            rmsResponse.setFault(cf);
            rmsResponseDocument.setAcknowledgeHeartBeatResponse(rmsResponse);
            return rmsResponseDocument;
        }

        AckHeartBeatRequest request = ackRequestDocument.getAcknowledgeHeartBeatRequest();
        long id = request.getId();
        com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentUpdateAcknowledgementData ccRequest = HeartbeatManager.getInstance().getCCAgentUpdateAcknowledgementDataDataFromRMS(request.getAcknowledgementData());

        try {
            port.acknowledgeUpdates(BigInteger.valueOf(id), ccRequest);
            cf.setErrorCode(RMSApplicationErrorCodes.NO_FAULT);
        } catch (ServiceNotReadyFault e) {
            logger.error("Ack Heartbeat: Unable to send request to server. Maybe because machine is disconnected.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.SERVICE_NOT_READY_FAULT);
            cf.setErrorMessage(e.getMessage());
        } catch (UnauthorizedCallerFault e) {
            logger.error("Server denied access. Ack Heartbeat unsuccessful", e);
            cf.setErrorCode(RMSApplicationErrorCodes.UNAUTHORIZED_CALLER_FAULT);
            cf.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Ack Heartbeat: general error has occurred", e);
        }

        rmsResponse.setFault(cf);
        rmsResponseDocument.setAcknowledgeHeartBeatResponse(rmsResponse);

        return rmsResponseDocument;
    }

    public CheckUpdatesDocument checkUpdates(CheckUpdatesType checkUpdatesType) {
        CheckUpdatesRequestType request = checkUpdatesType.getCheckUpdatesRequest();
        String currentVersion = request.getCurrentVersion();
        OsTypeRestriction.Enum clientOSType = request.getOsType();
        ArchitectureType.Enum clientArch = request.getArchitecture();
        CheckUpdatesResponseType response = CheckUpdatesResponseType.Factory.newInstance();
        CheckUpdatesDocument doc = CheckUpdatesDocument.Factory.newInstance();

        if (!ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.RMC_CURRENT_VERSION).equals(currentVersion)) {
        	if(clientArch.equals(ArchitectureType.X_32_BIT)){
                int checkSum = ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getIntProperty(ConfigManager.RMC_CHECKSUM_32BITS);
                if (checkSum == -1) {
                    logger.error("Could not parse the checksum into a number.");
                    doc.setCheckUpdates(checkUpdatesType);
                    return doc;
                } else {
                    response.setCheckSum(checkSum);
                }
                response.setDownloadURL(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.RMC_UPDATE_URL_32BITS));        		
        	}else{
                int checkSum = ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getIntProperty(ConfigManager.RMC_CHECKSUM_64BITS);
                if (checkSum == -1) {
                    logger.error("Could not parse the checksum into a number.");
                    doc.setCheckUpdates(checkUpdatesType);
                    return doc;
                } else {
                    response.setCheckSum(checkSum);
                }
                response.setDownloadURL(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.RMC_UPDATE_URL_64BITS));        		
        	}
            response.setNewVersion(ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.RMC_CURRENT_VERSION));
            checkUpdatesType.setCheckUpdatesResponse(response);
            checkUpdatesType.setCheckUpdatesRequest(null);
        }
        doc.setCheckUpdates(checkUpdatesType);
        return doc;
    }

    private String getLogServiceUrl() throws RMSException{
		String logServiceURL=GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.DABS_LOG_SERVICE);
		if(logServiceURL==null || logServiceURL.length()==0){
			logServiceURL=DABS_LOGS_SERVICE_URL;
		}
		String icenet_url=ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.ICENET_URL);
		if(icenet_url==null){
        	throw new RMSException("ICENet URL not configured");
        }
		if(!icenet_url.endsWith("/")){
			icenet_url+="/";
		}
		return icenet_url+logServiceURL;
    }

    public LogServiceDocument sendLog(LogServiceDocument request) throws IOException, ServiceException, SOAPException, GeneralSecurityException, ClassNotFoundException {
        LogServiceType logService = request.getLogService();
        LogServiceDocument response = LogServiceDocument.Factory.newInstance();
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(LogServiceIF.class);
        // there is a problem with faults
        CommonFault cf = CommonFault.Factory.newInstance();
        LogResponseType type = LogResponseType.Factory.newInstance();
        LogServiceIF port;

        try {
            factory.setAddress(getLogServiceUrl());
            port = (LogServiceIF)factory.create();
            Client client = ClientProxy.getClient(port);
            String tenantId = request.getLogService().getTenantId();
            if (tenantId == null) {
                tenantId = GlobalConfigManager.DEFAULT_TENANT_ID;
            }
            setTsl(client, SSLSocketFactoryGenerator.MODE_SECURE, tenantId);
        } catch(Exception e) {
            cf.setErrorCode(RMSApplicationErrorCodes.CONFIGURATION_ERROR);
            cf.setErrorMessage("Configuration Error");
            type.setFault(cf);
            logService.setLogResponse(type);
            request.setLogService(logService);
            return request;
        }

        int logVersion = logService.getVersion();
        com.nextlabs.rms.services.cxf.destiny.types.log.v3.LogStatus status = null;
        ResponseEnum.Enum logStatus;
        try {
            String log = LogTransformer.transform(request);

        	switch(logVersion) {
                case 5:
                	status = port.logPolicyActivityV5(log);
                	break;
                default:
                    status = port.logPolicyActivityV5(log);
                    break;
            }
            cf.setErrorCode(RMSApplicationErrorCodes.NO_FAULT);
            if (status == null) {
                logStatus = ResponseEnum.FAILED;
            } else {
                logStatus = logStatusMap.get(status);
                if (logStatus.equals(ResponseEnum.SUCCESS)) {
                    logService.setLogRequest(null);
                }
            }
        } catch (ServiceNotReadyFault e) {
            logger.error("Log Service: Unable to send a log to server. The server might not be ready yet.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.SERVICE_NOT_READY_FAULT);
            cf.setErrorMessage(e.getMessage());
            logStatus = ResponseEnum.FAILED;
        } catch (UnauthorizedCallerFault e) {
            logger.error("Server denied access. Logging unsuccessful.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.UNAUTHORIZED_CALLER_FAULT);
            cf.setErrorMessage(e.getMessage());
            logStatus = ResponseEnum.FAILED;
        } catch (RMSException e) {
            logger.error("Error during conversion of log XML to serialized Java:", e);
            cf.setErrorCode(RMSApplicationErrorCodes.MALFORMED_INPUT_ERROR);
            cf.setErrorMessage("Malformed Input Error");
            logStatus = ResponseEnum.FAILED;
        } catch (Exception e) {
        	logger.error("Error occurred while processing Log Request. Logging unsuccessful.", e);
            cf.setErrorCode(RMSApplicationErrorCodes.GENERAL_ERROR);
            cf.setErrorMessage(e.getMessage());
            logStatus = ResponseEnum.FAILED;        	
        }
        type.setFault(cf);
        type.setResponse(logStatus);
        logService.setLogResponse(type);
        response.setLogService(logService);
        return response;
    }

    public SSLSocketFactoryGenerator setTsl(Client client, boolean isSecureMode, String tenantId) throws GeneralSecurityException, IOException {
        HTTPConduit http = (HTTPConduit) client.getConduit();
        TLSClientParameters tlsClientParameters = new TLSClientParameters();

        SSLSocketFactoryGenerator socketFactoryGenerator = new SSLSocketFactoryGenerator(isSecureMode);
        KeyManager[] keyMgrs = socketFactoryGenerator.getKeyManagers(tenantId);
        TrustManager[] trustManagers = socketFactoryGenerator.getTrustManagers(tenantId);

        tlsClientParameters.setDisableCNCheck(true);
        tlsClientParameters.setTrustManagers(trustManagers);
        tlsClientParameters.setKeyManagers(keyMgrs);
        http.setTlsClientParameters(tlsClientParameters);

        return socketFactoryGenerator;
    }

    public void addInterceptor(Client client, String tenantId) {
        client.getInInterceptors().add(new ResponseHandleInterceptor(tenantId));
    }

    class ResponseHandleInterceptor extends AbstractPhaseInterceptor<Message> {
        private String tenantId;

        public ResponseHandleInterceptor(String tenantId) {
            super(Phase.RECEIVE);
            this.tenantId = tenantId;
        }

        public void handleMessage(Message message) throws Fault {
            InputStream is = message.getContent(InputStream.class);
            CachedOutputStream bos = new CachedOutputStream();

            if (is == null) {
                return;
            }

            try {
                IOUtils.copy(is, bos);
                is.close();
                bos.flush();
                InputStream newStream = bos.getInputStream();

                // get attachments from message using Axis 1 library
                AttachmentsImpl impl = new AttachmentsImpl(newStream, "application/dime", null);
                Collection attachments = impl.getAttachments();
                Map<String, File> attachmentsToCache = new HashMap<>();
                if (attachments != null) {
                    for (Object attachment : attachments) {
                        AttachmentPart part = (AttachmentPart)attachment;
                        File file = saveAttachment(part);
                        attachmentsToCache.put(sslFilenameMap.get(part.getContentId()), file);
                    }
                }
                CommArtifactsCache.getInstance().writeFileToDB(attachmentsToCache, tenantId);

                byte[] bosBytes = bos.getBytes();
                bos.close();

                // only starting at index 100 does the actual XML we want appear
                InputStream backup = new ByteArrayInputStream(Arrays.copyOfRange(bosBytes, 100, bosBytes.length));
                message.setContent(InputStream.class, backup);
            } catch (IOException | Fault f) {
                logger.error("Error occurred while intercepting SOAP message:" + f, f);
            }
        }
    }

    private synchronized File saveAttachment(AttachmentPart p) {
        File file = null;
        InputStream inputStream = null;
        try {
            inputStream = p.getActivationDataHandler().getDataSource().getInputStream();

            file = new File(SSLSocketFactoryGenerator.CERT_PATH + sslFilenameMap.get(p.getContentId()));
            OutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
        } catch(IOException ie) {
            logger.error("Error occured while saving KeyStores from server", ie);
        } finally {
            try {
                inputStream.close();
            } catch(IOException ioe) {
                logger.error("Error trying to save an attachment " + p.getContentId() + ": " + ioe);
            }
        }

        return file;
    }

    public static void main(String[] args) throws IOException, ServiceException, SOAPException, GeneralSecurityException, XmlException, ClassNotFoundException, InvalidBundleException, BundleVaultException, DatatypeConfigurationException {

        //HeartBeatRequestDocument doc = HeartBeatRequestDocument.Factory.parse(new File("C:\\temp\\HeartBeat.xml"));
        //KeyRingsMetaDataType keyRingsMetaData = doc.getHeartBeatRequest().getHeartbeat().getKeyRingsMetaData();
        //KeyRingMetaDataType[] keyRingMetaDataArray = keyRingsMetaData.getKeyRingMetaDataArray();
//    	keyRingsMetaData.removeKeyRingMetaData(0);
        //System.out.println(keyRingsMetaData.toString());
        // test the dataDir here
        //System.out.println("The DataDir is: " + ConfigManager.getInstance().getDataDir());
        //SSLSocketFactoryGenerator socket = new SSLSocketFactoryGenerator(true);
        //byte[] cert = socket.getKeyStoreSecureCertificate();
        //System.out.println(cert);
//    	XmlOptions opt = new XmlOptions();
//    	opt.setSavePrettyPrint();
//        CommCCManager instance = CommCCManager.getInstance();
//        HeartBeatRequestDocument doc = HeartBeatRequestDocument.Factory.parse(new File("C:\\temp\\HeartBeat.xml"));
//        HeartBeatResponseDocument heartbeatResponse = instance.sendHeartbeat(doc);
//        heartbeatResponse.save(System.out, opt);

//        Collection<BaseLogEntry> logEntries = new ArrayList<BaseLogEntry>();
//        PolicyActivityInfoV2 info = PolicyActivityInfoTestData.generateRandomV2();
//        PolicyActivityLogEntryV2 entry = new PolicyActivityLogEntryV2(info, 12345, 12345);
//        logEntries.add(entry);
//        String encodedEntries = LogUtils.encodeLogEntries(logEntries);
//
//        LogServiceType logService = LogServiceType.Factory.newInstance();
//        logService.setVersion(2);
//        LogRequestType type = LogRequestType.Factory.newInstance();
//        type.setLogs(encodedEntries);
//        logService.setLogRequest(type);
//        logService.setLogType(LogTypes.POLICY_ACTIVITY);
//
//        LogServiceType logServiceType = instance.sendLog(logService);
//        logServiceType.save(System.out, opt);
   }
}
