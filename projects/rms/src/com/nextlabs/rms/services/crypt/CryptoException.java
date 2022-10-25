/*
 * Created on Sep 14, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.rms.services.crypt;
/**
 * @author name
 * @version $Id$
 *
 * FIXME: This should be checked exception but I don't have time.
 */
public class CryptoException extends RuntimeException {

    CryptoException() {
    }

    CryptoException(String message) {
        super(message);
    }

    CryptoException(Exception exception) {
        super(exception);
    }

}
