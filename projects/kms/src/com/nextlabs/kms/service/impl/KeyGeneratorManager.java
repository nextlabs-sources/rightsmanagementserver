package com.nextlabs.kms.service.impl;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.service.IKeyGeneratorManager;

@Service
public class KeyGeneratorManager implements IKeyGeneratorManager {
	private final Logger logger = LoggerFactory.getLogger(KeyGeneratorManager.class);

	/**
	 * return AES key and generate by using SHA1PRNG
	 */
	public byte[] generateKey(KeyAlgorithm keyAlgorithm) throws KeyManagementException {
		String algorithm = keyAlgorithm.getName();
		int length = keyAlgorithm.getLength();
		KeyGenerator keyGen = createKeyGenerator(algorithm, length);
		return keyGen.generateKey().getEncoded();
	}

	private KeyGenerator createKeyGenerator(String algorithm, int length) throws KeyManagementException {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Create key generator (Algorithm: {}, Key Size: {})", algorithm, length);
			}
			KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
			// This is an algorithm-specific metric, specified in number of bits.
			keyGen.init(length, SecureRandom.getInstance("SHA1PRNG"));

			return keyGen;
		} catch (NoSuchAlgorithmException e) {
			throw new KeyManagementException(e);
		} catch (InvalidParameterException e) {
			throw new KeyManagementException(e);
		}
	}
}
