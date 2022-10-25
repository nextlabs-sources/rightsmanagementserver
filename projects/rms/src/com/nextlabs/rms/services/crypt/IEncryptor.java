package com.nextlabs.rms.services.crypt;

/**
 * This is the encryption interface.
 *
 * @author ihanen
 * @version $Id$
 */

public interface IEncryptor {

    /**
     * Encrypts a message
     *
     * @param original
     *            original message
     * @return an encrypted version of the message
     */
    String encrypt(String original);

    /**
     *
     * @param original
     * @param algorithm case-insensitive
     * @return
     */
    String encrypt(String original, String algorithm);
}