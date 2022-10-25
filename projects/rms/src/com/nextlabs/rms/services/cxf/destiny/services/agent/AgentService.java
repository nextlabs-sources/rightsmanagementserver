package com.nextlabs.rms.services.cxf.destiny.services.agent;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class was generated by Apache CXF 3.0.4
 * 2015-04-13T13:24:04.155+08:00
 * Generated source version: 3.0.4
 * 
 */
@WebServiceClient(name = "AgentService", 
                  wsdlLocation = "file:/C:/Users/tbiegeleisen/to-Tim/JAX-WS test/src/main/config/AgentService.wsdl",
                  targetNamespace = "http://bluejungle.com/destiny/services/agent") 
public class AgentService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://bluejungle.com/destiny/services/agent", "AgentService");
    public final static QName AgentServiceIFPort = new QName("http://bluejungle.com/destiny/services/agent", "AgentServiceIFPort");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/Users/tbiegeleisen/to-Tim/JAX-WS test/src/main/config/AgentService.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(AgentService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/C:/Users/tbiegeleisen/to-Tim/JAX-WS test/src/main/config/AgentService.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public AgentService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public AgentService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public AgentService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    //public AgentService(WebServiceFeature ... features) {
    //    super(WSDL_LOCATION, SERVICE, features);
    //}

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    //public AgentService(URL wsdlLocation, WebServiceFeature ... features) {
    //    super(wsdlLocation, SERVICE, features);
    //}

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    //public AgentService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
    //    super(wsdlLocation, serviceName, features);
    //}

    /**
     *
     * @return
     *     returns AgentServiceIF
     */
    @WebEndpoint(name = "AgentServiceIFPort")
    public AgentServiceIF getAgentServiceIFPort() {
        return super.getPort(AgentServiceIFPort, AgentServiceIF.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns AgentServiceIF
     */
    @WebEndpoint(name = "AgentServiceIFPort")
    public AgentServiceIF getAgentServiceIFPort(WebServiceFeature... features) {
        return super.getPort(AgentServiceIFPort, AgentServiceIF.class, features);
    }

}
