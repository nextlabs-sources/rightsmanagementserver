package com.nextlabs.kms.impl;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.exception.KeyManagementException;

public final class KeyRingUtil {
	private static final char TOKEN = ' ';

	private KeyRingUtil() {
		throw new UnsupportedOperationException();
	}

	public static String convertToAlias(byte[] id, long timestamp) {
		return Hex.encodeHexString(id) + TOKEN + Long.toHexString(timestamp);
	}

	public static String convertToAlias(IKeyId keyId) {
		return convertToAlias(keyId.getHash(), keyId.getCreationTimeStamp());
	}

	public static KeyId convertToKeyId(String alias) throws KeyManagementException {
		alias = alias.toUpperCase();
		int sepIndex = alias.lastIndexOf(TOKEN);
		if (sepIndex == -1) {
			throw new KeyManagementException("invalid alias format, missing token. ");
		}

		String idEncoded = alias.substring(0, sepIndex);

		byte[] original;
		try {
			original = Hex.decodeHex(idEncoded.toCharArray());
		} catch (DecoderException e) {
			throw new KeyManagementException("invalid alias format, '" + alias + "'. " + e.getMessage());
		}

		long date = Long.parseLong(alias.substring(sepIndex + 1), 16);
		return new KeyId(original, date);
	}
}
