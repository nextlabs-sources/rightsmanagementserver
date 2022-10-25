
package com.nextlabs.rms.services.cxf.destiny.services;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.0.4
 * 2015-04-13T13:24:04.040+08:00
 * Generated source version: 3.0.4
 */

@WebFault(name = "ServiceNotReadyFault", targetNamespace = "http://bluejungle.com/destiny/framework/types")
public class ServiceNotReadyFault extends Exception {
    
    private com.nextlabs.rms.services.cxf.destiny.framework.types.ServiceNotReadyFault serviceNotReadyFault;

    public ServiceNotReadyFault() {
        super();
    }
    
    public ServiceNotReadyFault(String message) {
        super(message);
    }
    
    public ServiceNotReadyFault(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotReadyFault(String message, com.nextlabs.rms.services.cxf.destiny.framework.types.ServiceNotReadyFault serviceNotReadyFault) {
        super(message);
        this.serviceNotReadyFault = serviceNotReadyFault;
    }

    public ServiceNotReadyFault(String message, com.nextlabs.rms.services.cxf.destiny.framework.types.ServiceNotReadyFault serviceNotReadyFault, Throwable cause) {
        super(message, cause);
        this.serviceNotReadyFault = serviceNotReadyFault;
    }

    public com.nextlabs.rms.services.cxf.destiny.framework.types.ServiceNotReadyFault getFaultInfo() {
        return this.serviceNotReadyFault;
    }
}