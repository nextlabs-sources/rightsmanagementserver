package com.nextlabs.rms.services.cxf.destiny.services.log.v4;

import com.nextlabs.rms.services.cxf.destiny.interfaces.log.v4.LogServiceIF;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class was generated by Apache CXF 3.0.4
 * 2015-05-07T14:32:14.791+08:00
 * Generated source version: 3.0.4
 *
 */
@WebServiceClient(name = "LogService.v4",
        wsdlLocation = "file:/C:/Users/tbiegeleisen/to-Tim/JAX-WS test/src/main/config/LogService.v4.wsdl",
        targetNamespace = "http://nextlabs.com/destiny/services/log/v4")
public class LogServiceV4 extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://nextlabs.com/destiny/services/log/v4", "LogService.v4");
    public final static QName LogServiceIFPortV4 = new QName("http://nextlabs.com/destiny/services/log/v4", "LogServiceIFPort.v4");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/Users/tbiegeleisen/to-Tim/JAX-WS test/src/main/config/LogService.v4.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(LogServiceV4.class.getName())
                    .log(java.util.logging.Level.INFO,
                            "Can not initialize the default wsdl from {0}", "file:/C:/Users/tbiegeleisen/to-Tim/JAX-WS test/src/main/config/LogService.v4.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public LogServiceV4(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public LogServiceV4(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public LogServiceV4() {
        super(WSDL_LOCATION, SERVICE);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public LogServiceV4(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public LogServiceV4(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public LogServiceV4(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns LogServiceIF
     */
    @WebEndpoint(name = "LogServiceIFPort.v4")
    public LogServiceIF getLogServiceIFPortV4() {
        return super.getPort(LogServiceIFPortV4, LogServiceIF.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns LogServiceIF
     */
    @WebEndpoint(name = "LogServiceIFPort.v4")
    public LogServiceIF getLogServiceIFPortV4(WebServiceFeature... features) {
        return super.getPort(LogServiceIFPortV4, LogServiceIF.class, features);
    }

}