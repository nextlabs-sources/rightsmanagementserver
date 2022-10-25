
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
 */

abstract class DefaultCrypto implements IEncryptor, IDecryptor {

    protected final ReversibleEncryptor.Crypto crypto;

    DefaultCrypto(ReversibleEncryptor.Crypto crypto) {
        this.crypto = crypto;
    }

    public final String encrypt(String original) {
        return crypto.name() + encryptWithoutPrefix(original);
    }

    abstract String encryptWithoutPrefix(String original);

    public String encrypt(String original, String algorithm) {
        if (crypto.name().equalsIgnoreCase(algorithm)) {
            return encrypt(original);
        }
        throw new UnsupportedOperationException();
    }

}