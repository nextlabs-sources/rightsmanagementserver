
package com.nextlabs.rms.services.cxf.destiny.services;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.0.4
 * 2015-04-13T13:24:04.066+08:00
 * Generated source version: 3.0.4
 */

@WebFault(name = "UnauthorizedCallerFault", targetNamespace = "http://bluejungle.com/destiny/framework/types")
public class UnauthorizedCallerFault extends Exception {
    
    private com.nextlabs.rms.services.cxf.destiny.framework.types.UnauthorizedCallerFault unauthorizedCallerFault;

    public UnauthorizedCallerFault() {
        super();
    }
    
    public UnauthorizedCallerFault(String message) {
        super(message);
    }
    
    public UnauthorizedCallerFault(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedCallerFault(String message, com.nextlabs.rms.services.cxf.destiny.framework.types.UnauthorizedCallerFault unauthorizedCallerFault) {
        super(message);
        this.unauthorizedCallerFault = unauthorizedCallerFault;
    }

    public UnauthorizedCallerFault(String message, com.nextlabs.rms.services.cxf.destiny.framework.types.UnauthorizedCallerFault unauthorizedCallerFault, Throwable cause) {
        super(message, cause);
        this.unauthorizedCallerFault = unauthorizedCallerFault;
    }

    public com.nextlabs.rms.services.cxf.destiny.framework.types.UnauthorizedCallerFault getFaultInfo() {
        return this.unauthorizedCallerFault;
    }
}