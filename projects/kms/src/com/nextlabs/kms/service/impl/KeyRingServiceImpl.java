package com.nextlabs.kms.service.impl;

import java.security.KeyStore.PasswordProtection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.dao.KeyRingDAO;
import com.nextlabs.kms.entity.KeyRing;
import com.nextlabs.kms.entity.ProviderAttribute;
import com.nextlabs.kms.entity.Tenant;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.entity.enums.Status;
import com.nextlabs.kms.exception.KeyAlreadyExistsException;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.exception.KeyNotFoundException;
import com.nextlabs.kms.exception.KeyRingAlreadyExistsException;
import com.nextlabs.kms.exception.KeyRingDisabledException;
import com.nextlabs.kms.exception.KeyRingNotFoundException;
import com.nextlabs.kms.impl.Key;
import com.nextlabs.kms.impl.KeyId;
import com.nextlabs.kms.impl.KeyRingJCEKSImpl;
import com.nextlabs.kms.impl.KeyRingUtil;
import com.nextlabs.kms.service.IKeyGeneratorManager;
import com.nextlabs.kms.service.KeyRingService;
import com.nextlabs.nxl.sharedutil.EncryptionUtil;

@Service
@Transactional(readOnly = false)
public class KeyRingServiceImpl implements KeyRingService {
	private static final int HASH_LENGTH = 32;
	public static final String STOREPASS_KEY = "storepass";
	private static final int MAX_KEY_RING_NAME_LIMIT = 16;
	private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";

	@Autowired(required = true)
	private KeyRingDAO keyRingDAO;
	@Autowired(required = true)
	private IKeyGeneratorManager keyGenManager;
	@Autowired(required = true)
	@Qualifier(value="kms.encryptionutil")
	private EncryptionUtil encryptionUtil;

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public IKeyRing createKeyRing(Tenant tenant, String keyRingName) throws KeyManagementException {
		PasswordProtection password = new PasswordProtection(retrievePassword(tenant).toCharArray());
		IKeyRing existing = getNonDeletedKeyRing(tenant, keyRingName);
		if (existing != null) {
			if (existing.getKeyRingDO().getStatus().equals(Status.INACTIVE)){
				throw new KeyRingDisabledException(keyRingName);
			}
			else {
				throw new KeyRingAlreadyExistsException(keyRingName);
			}
		}
		if (keyRingName.length() > MAX_KEY_RING_NAME_LIMIT) {
			throw new KeyManagementException(
					"Key name " + keyRingName + " exceeds length limit of " + MAX_KEY_RING_NAME_LIMIT);
		}

		final KeyRing keyRing = new KeyRing(keyRingName, null, KeyRingJCEKSImpl.KEY_STORE_TYPE, tenant);
		keyRingDAO.deleteKeyRing(tenant, keyRingName);
		keyRingDAO.save(keyRing);

		KeyRingJCEKSImpl keyRingImpl = new KeyRingJCEKSImpl(keyRing, password, this);
		keyRingImpl.flush();
		return keyRingImpl;
	}

	@Override
	public IKeyRing getKeyRing(Tenant tenant, final String name) throws KeyManagementException {
		KeyRing keyRingDO = keyRingDAO.getNonDeletedKeyRing(tenant, name);
		//KeyRing keyRingDO = keyRingDAO.getActiveKeyRing(tenant, name);
		if (keyRingDO == null) {
			return null;
		} else if (keyRingDO.getStatus().equals(Status.INACTIVE)){
			throw new KeyRingDisabledException(name);
		}
		PasswordProtection password = new PasswordProtection(retrievePassword(tenant).toCharArray());
		return new KeyRingJCEKSImpl(keyRingDO, password, this);
	}
	
	public IKeyRing getNonDeletedKeyRing(Tenant tenant, final String name) throws KeyManagementException {
		KeyRing keyRingDO = keyRingDAO.getNonDeletedKeyRing(tenant, name);
		if (keyRingDO == null) {
			return null;
		}
		PasswordProtection password = new PasswordProtection(retrievePassword(tenant).toCharArray());
		return new KeyRingJCEKSImpl(keyRingDO, password, this);
	}

	public List<IKeyRing> getKeyRings(Tenant tenant) throws KeyManagementException {
		List<KeyRing> keyRings = keyRingDAO.getActiveKeyRings(tenant);
		List<IKeyRing> results = new ArrayList<>(keyRings != null ? keyRings.size() : 0);
		if (keyRings != null && !keyRings.isEmpty()) {
			for (KeyRing keyRing : keyRings) {
				PasswordProtection password = new PasswordProtection(retrievePassword(tenant).toCharArray());
				results.add(new KeyRingJCEKSImpl(keyRing, password, this));
			}
		}
		return results;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void updateKeyRing(final IKeyRing keyRing) throws KeyManagementException {
		keyRingDAO.update(keyRing.getKeyRingDO());
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean deleteKeyRing(Tenant tenant, final String name) throws KeyManagementException {
		KeyRing keyRing = keyRingDAO.getNonDeletedKeyRing(tenant, name);
		if (keyRing == null) {
			throw new KeyRingNotFoundException(name);
		}
		keyRing.setDeleted(true);
		keyRingDAO.update(keyRing);
		return true;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean deleteKeyRing(IKeyRing keyRing) throws KeyManagementException {
		if (ObjectUtils.nullSafeEquals(Boolean.TRUE, keyRing.getKeyRingDO().getDeleted())) {
			return false;
		}
		KeyRing keyRingDO = keyRing.getKeyRingDO();
		keyRingDO.setDeleted(true);
		keyRingDAO.update(keyRingDO);
		return true;
	}

	@Override
	public Set<String> getKeyRingNames(Tenant tenant) throws KeyManagementException {
		Set<String> results = keyRingDAO.getNonDeletedKeyRings(tenant);
		return results;
	}

	@Override
	public long getLatestModifiedDate(Tenant tenant) throws KeyManagementException {
		Date latestModifiedDate = keyRingDAO.getLatestModifiedDate(tenant);
		return latestModifiedDate != null ? latestModifiedDate.getTime() : 0;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean disableKeyRing(Tenant tenant, String keyRingName) throws KeyManagementException {
		KeyRing keyRing = keyRingDAO.getNonDeletedKeyRing(tenant, keyRingName);
		if (keyRing == null) {
			throw new KeyRingNotFoundException(keyRingName);
		}
		doUpdateKeyRingStatus(keyRing, Status.INACTIVE);
		return true;
	}

	private void doUpdateKeyRingStatus(KeyRing keyRing, Status status) {
		keyRing.setStatus(status);
		keyRingDAO.update(keyRing);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public IKeyId generateKey(Tenant tenant, String keyRingName, KeyAlgorithm keyAlgorithm) throws KeyManagementException {
		IKeyRing keyRing = getKeyRing(tenant, keyRingName);

		if (keyRing == null) {
			throw new KeyRingNotFoundException(keyRingName);
		}

		byte[] keyValue = keyGenManager.generateKey(keyAlgorithm);
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException nsa) {
			throw new KeyManagementException(nsa);
		}
		byte[] hash = digest.digest(keyValue);
		
/*	if(MESSAGE_DIGEST_ALGORITHM == "SHA1") {
			assert hash.length == 20;
			hash = pad(hash, HASH_LENGTH); // the correct is up to a second on the KeyService
		}
*/
		assert hash.length == HASH_LENGTH;
		
		IKeyId keyId = new KeyId(hash, System.currentTimeMillis() / 1000 * 1000);
		IKey key = new Key(keyId, keyValue, keyAlgorithm);
		keyRing.setKey(key);
		return keyId;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean enableKeyRing(Tenant tenant, String keyRingName) throws KeyManagementException {
		KeyRing keyRing = keyRingDAO.getNonDeletedKeyRing(tenant, keyRingName);
		if (keyRing == null) {
			throw new KeyRingNotFoundException(keyRingName);
		}
		doUpdateKeyRingStatus(keyRing, Status.ACTIVE);
		return true;
	}

	@Override
	public IKey getKey(Tenant tenant, String keyRingName, IKeyId keyId) throws KeyManagementException {
		IKeyRing keyRing = getKeyRing(tenant, keyRingName);
		if (keyRing == null) {
			throw new KeyRingNotFoundException(keyRingName);
		}
		IKey key = keyRing.getKey(keyId);
		if (key == null) {
			throw new KeyNotFoundException(KeyRingUtil.convertToAlias(keyId));
		}
		return key;
	}

	@Override
	public IKey getLatestKey(Tenant tenant, String keyRingName) throws KeyManagementException {
		IKeyRing keyRing = getKeyRing(tenant, keyRingName);
		if (keyRing == null) {
			throw new KeyRingNotFoundException(keyRingName);
		}
		Collection<IKey> keys = keyRing.getKeys();
		IKey key = null;
		if (!CollectionUtils.isEmpty(keys)) {
			for (IKey iKey : keys) {
				if (key == null || iKey.getKeyId().getCreationTimeStamp() > key.getKeyId().getCreationTimeStamp()) {
					key = iKey;
				}
			}
		}
		return key;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void importKeyRing(Tenant tenant, String keyRingName, List<IKey> keys) throws KeyManagementException {
		IKeyRing existingKeyRing = getKeyRing(tenant, keyRingName);
		if (existingKeyRing == null) {
			existingKeyRing = createKeyRing(tenant, keyRingName);
		}

		List<IKey> existingKeys = new ArrayList<>(existingKeyRing.getKeys());
		if (!CollectionUtils.isEmpty(keys)) {
			for (IKey keyToImport : keys) {
				for (IKey existingKey : existingKeys) {
					if (ObjectUtils.nullSafeEquals(existingKey.getKeyId(), keyToImport.getKeyId())) {
						throw new KeyAlreadyExistsException(KeyRingUtil.convertToAlias(keyToImport));
					}
				}
			}
			for (IKey keyToImport : keys) {
				existingKeyRing.setKey(keyToImport);
			}
		}
	}
	
	private String retrievePassword(Tenant tenant) throws KeyManagementException{
		List<ProviderAttribute> attrs = tenant.getProvider().getAttributes();
		for(ProviderAttribute attr : attrs){
			if(attr.getName().equals(STOREPASS_KEY)) {
				return encryptionUtil.decrypt(attr.getValue());
			}
		}
		throw new KeyManagementException("Failed to retrieve keystore password.");
	}

	@SuppressWarnings("unused")
	private byte[] pad(byte[] input, int length) {
		return pad(input, length, (byte) 0);
	}

	private byte[] pad(byte[] input, int length, byte filler) {
		if (input.length < length) {
			byte[] output = new byte[length];
			Arrays.fill(output, filler);
			System.arraycopy(input, 0, output, 0, input.length);
			return output;
		}
		return input;
	}
}
