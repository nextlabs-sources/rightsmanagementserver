/*
 * Created on Feb 27, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.rms.services.crypt;

/**
 * This is the decryption interface
 *
 * @author ihanen
 * @version $Id$
 */

public interface IDecryptor {

    /**
     * Decrypts a crypted message
     *
     * @param cryptedMsg
     *            crypted message to decrypt
     * @return a decrypted message
     */
    public String decrypt(String cryptedMsg);
}