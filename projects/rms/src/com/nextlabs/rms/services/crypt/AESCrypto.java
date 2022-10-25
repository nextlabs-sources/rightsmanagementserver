
/*
 * Created on Sep 14, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.rms.services.crypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author name
 * @version $Id$
 *
 * TODO add salt
 */
class AESCrypto extends DefaultCrypto {

    private static final byte[] key = new byte[] {
            -123,   -14,   -94,   19,   22
            ,  -41,  -108,    27, -128,  -73
            ,  -86,   -17,   -61,  -37,    4
            ,  -93
    };

    private final Cipher encrypt;
    private final Cipher decrypt;

    public AESCrypto() {
        super(ReversibleEncryptor.Crypto.s);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        try {
            encrypt = Cipher.getInstance("AES");
            encrypt.init(Cipher.ENCRYPT_MODE, skeySpec);
            decrypt = Cipher.getInstance("AES");
            decrypt.init(Cipher.DECRYPT_MODE, skeySpec);
        } catch (InvalidKeyException e) {
            throw new CryptoException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        } catch (NoSuchPaddingException e) {
            throw new CryptoException(e);
        }
    }

    @Override
    String encryptWithoutPrefix(String original) {
        byte[] encrypted;
        try {
            encrypted = encrypt.doFinal(CodecHelper.toBytes(original));
        } catch (IllegalBlockSizeException e) {
            throw new CryptoException(e);
        } catch (BadPaddingException e) {
            throw new CryptoException(e);
        }
        return CodecHelper.base16Encode(encrypted);
    }

    public String decrypt(String cryptedMsg) {
        byte[] decrypted;
        try {
            decrypted = decrypt.doFinal(CodecHelper.base16Decode(cryptedMsg));
        } catch (IllegalBlockSizeException e) {
            throw new CryptoException(e);
        } catch (BadPaddingException e) {
            throw new CryptoException(e);
        }
        return CodecHelper.toString(decrypted);
    }

}