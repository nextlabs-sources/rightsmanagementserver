package com.nextlabs.kms.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.entity.KeyRing;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.service.KeyRingService;

public class KeyRingJCEKSImpl implements IKeyRing, Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(KeyRingJCEKSImpl.class);

	public static final String KEY_STORE_TYPE = "JCEKS";

	private final KeyStore keyStore;
	private final PasswordProtection password;
	private final KeyRingService keyRingService;
	private final KeyRing keyRingDO;

	private boolean isClosed = false;

	public KeyRingJCEKSImpl(KeyRing keyRingDO, PasswordProtection password, KeyRingService keyRingService)
			throws KeyManagementException {
		if (keyRingDO == null) {
			throw new IllegalArgumentException("KeyRing is mandatory");
		}
		this.keyRingDO = keyRingDO;
		this.password = password;
		this.keyRingService = keyRingService;

		try {
			this.keyStore = KeyStore.getInstance(keyRingDO.getType());
			byte[] data = keyRingDO.getData();
			if (data != null) {
				ByteArrayInputStream is = new ByteArrayInputStream(data);
				keyStore.load(is, password.getPassword());
				// no need to close stream since it is already closed
				// and it may throw exception in some cases
			} else {
				keyStore.load(null);
			}
		} catch (KeyStoreException e) {
			throw new KeyManagementException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyManagementException(e);
		} catch (CertificateException e) {
			throw new KeyManagementException(e);
		} catch (IOException e) {
			throw new KeyManagementException(e);
		}
	}

	@Override
	public String getName() {
		return keyRingDO.getName();
	}

	@Override
	public KeyRing getKeyRingDO() {
		return keyRingDO;
	}

	private void checkClosed() throws KeyManagementException {
		if (isClosed) {
			throw new KeyManagementException("The keyRing is already closed.");
		}
	}

	/**
	 * @deprecated Calling this will permanently delete the key from keystore.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void deleteKey(IKeyId keyId) throws KeyManagementException {
		checkClosed();

		String alias = KeyRingUtil.convertToAlias(keyId);
		try {
			keyStore.deleteEntry(alias);
		} catch (KeyStoreException e) {
			throw new KeyManagementException(e);
		}

		flush();
	}

	protected SecretKey getKey(String alias) throws KeyManagementException {
		checkClosed();
		LOG.trace(">getting key: " + alias);
		KeyStore.SecretKeyEntry entry;
		try {
			entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, password);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyManagementException(e);
		} catch (UnrecoverableEntryException e) {
			throw new KeyManagementException(e);
		} catch (KeyStoreException e) {
			throw new KeyManagementException(e);
		}
		if (entry != null) {
			return entry.getSecretKey();
		}

		return null;
	}

	protected IKey getKey(String alias, IKeyId keyId) throws KeyManagementException {
		SecretKey key = getKey(alias);
		if (key != null) {
			KeyAlgorithm keyAlgorithm = KeyAlgorithm.convertToKeyAlgorithm(key);			
			return new Key(keyId, key.getEncoded(), keyAlgorithm);
		}
		return null;
	}

	public IKey getKey(IKeyId keyId) throws KeyManagementException {
		String alias = KeyRingUtil.convertToAlias(keyId);
		return getKey(alias, keyId);
	}

	public Collection<IKey> getKeys() throws KeyManagementException {
		checkClosed();

		Collection<IKey> keys = new LinkedList<IKey>();
		try {
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				keys.add(getKey(alias, KeyRingUtil.convertToKeyId(alias)));
			}
		} catch (KeyStoreException e) {
			throw new KeyManagementException(e);
		}
		return keys;
	}

	@Override
	public void setKey(final IKey key) throws KeyManagementException {
		checkClosed();

		String alias = KeyRingUtil.convertToAlias(key);

		LOG.trace("<setting key>: {}", alias);

		javax.crypto.SecretKey mySecretKey = convertToSecretKey(key);

		try {
			keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(mySecretKey), password);
		} catch (KeyStoreException e) {
			throw new KeyManagementException(e);
		}

		flush();
	}

	protected javax.crypto.SecretKey convertToSecretKey(IKey key) {
		return new SecretKeySpec(key.getEncoded(), key.getAlgorithm().getName());
		//return new SecretKeySpec(key.getEncoded(), "NX" + key.getStructureVersion());
	}

	public synchronized void flush() throws KeyManagementException {
		checkClosed();

		LOG.trace("flush");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			keyStore.store(baos, password.getPassword());
		} catch (GeneralSecurityException e) {
			throw new KeyManagementException(e);
		} catch (IOException e) {
			throw new KeyManagementException(e);
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				LOG.warn("unable to close the in memory outputstrem");
			}
		}

		byte[] oldData = keyRingDO.getData();
		keyRingDO.setKeyStoreData(baos.toByteArray());

		boolean isFailed = true;
		try {
			keyRingService.updateKeyRing(this);
			isFailed = false;
		} finally {
			if (isFailed) {
				// rollback
				keyRingDO.setKeyStoreData(oldData);
			}
		}
	}

	@Override
	public void close() {
		isClosed = true;
		try {
			password.destroy();
		} catch (DestroyFailedException e) {
			LOG.warn("Fail to destory password", e);
		}
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}
	
	@Override
	public Tenant getTenant() {
		return keyRingDO.getTenant();
	}
}
