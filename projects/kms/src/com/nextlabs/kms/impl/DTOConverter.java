package com.nextlabs.kms.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.IKeyRing;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.exception.KeyAlgorithmUnsupportedException;
import com.nextlabs.kms.exception.KeyManagementException;
import com.nextlabs.kms.types.KeyDTO;
import com.nextlabs.kms.types.KeyIdDTO;
import com.nextlabs.kms.types.KeyRingDTO;
import com.nextlabs.kms.types.KeyRingNamesDTO;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;
import com.nextlabs.kms.types.KeyRingsWithKeysDTO;

public final class DTOConverter {

	public static IKeyId convertToKeyId(KeyIdDTO keyIdDTO) {
		return new KeyId(keyIdDTO.getHash(), keyIdDTO.getTimestamp());
	}

	public static KeyIdDTO convertToKeyIdDTO(IKeyId keyId) {
		KeyIdDTO dto = new KeyIdDTO();
		dto.setHash(keyId.getHash());
		dto.setTimestamp(keyId.getCreationTimeStamp());
		dto.setId(keyId.getId());
		return dto;
	}

	public static KeyRingDTO convertToKeyRingDTO(IKeyRing keyRing) throws KeyManagementException {
		KeyRingDTO dto = new KeyRingDTO();
		dto.setName(keyRing.getName());
		dto.getKeyIds().addAll(Arrays.asList(toKeyIdDTOs(keyRing.getKeys()))); // java.lang.String[]
																				// keyNames
		dto.setCreatedDate(keyRing.getKeyRingDO().getCreatedDate().getTime());
		dto.setLastModifiedDate(keyRing.getKeyRingDO().getLastUpdatedDate().getTime());
		return dto;
	}

	public static KeyRingWithKeysDTO convertToKeyRingWithKeysDTO(IKeyRing keyRing) throws KeyManagementException {
		KeyRingWithKeysDTO dto = new KeyRingWithKeysDTO();
		dto.setName(keyRing.getName());
		dto.getKeys().addAll(Arrays.asList(toKeyDTOs(keyRing.getKeys())));
		dto.setCreatedDate(keyRing.getKeyRingDO().getCreatedDate().getTime());
		dto.setLastModifiedDate(keyRing.getKeyRingDO().getLastUpdatedDate().getTime());
		return dto;
	}

	public static KeyRingsWithKeysDTO convertToKeyRingsWithKeysDTO(List<IKeyRing> keyRings)
			throws KeyManagementException {
		KeyRingsWithKeysDTO dto = new KeyRingsWithKeysDTO();
		if (keyRings != null && !keyRings.isEmpty()) {
			for (IKeyRing keyRing : keyRings) {
				KeyRingWithKeysDTO keyRingWithKeysDTO = convertToKeyRingWithKeysDTO(keyRing);
				dto.getKeyRings().add(keyRingWithKeysDTO);
			}
		}
		return dto;
	}

	public static KeyIdDTO[] toKeyIdDTOs(Collection<IKey> keys) {
		KeyIdDTO[] result = new KeyIdDTO[keys.size()];
		int i = 0;	
		keys = sortKeysByTimeStamp(keys);		
		for (IKey key : keys) {
			KeyIdDTO dto = new KeyIdDTO();
			dto.setHash(key.getHash());
			dto.setTimestamp(key.getCreationTimeStamp());
			dto.setId(key.getId());
			result[i++] = dto;
		}
		return result;
	}

	public static KeyDTO[] toKeyDTOs(Collection<IKey> keys) {
		KeyDTO[] result = new KeyDTO[keys.size()];
		int i = 0;		
		keys = sortKeysByTimeStamp(keys);		
		for (IKey key : keys) {
			result[i++] = convertToKeyDTO(key);
		}
		return result;
	}
	
	private static Collection<IKey> sortKeysByTimeStamp(Collection<IKey> keys){
		List<IKey> keyList = new ArrayList<IKey>(keys);
		Collections.sort(keyList, new Comparator<IKey>() {
			public int compare(IKey key1, IKey key2) {
				return (key2.getCreationTimeStamp() < key1.getCreationTimeStamp()) ? -1
						: ((key2.getCreationTimeStamp() == key1.getCreationTimeStamp()) ? 0 : 1);
			}
		});
		return new LinkedList<IKey>(keyList);
	}

	public static KeyRingNamesDTO toKeyRingNamesDTO(Set<String> keyRingNames) {
		KeyRingNamesDTO dto = new KeyRingNamesDTO();
		dto.getName().addAll(keyRingNames);
		return dto;
	}

	public static IKey convertToKey(KeyDTO keyDTO) throws KeyAlgorithmUnsupportedException {
		KeyAlgorithm algorithm = KeyAlgorithm.convertToKeyAlgorithm(keyDTO.getKeyAlgorithm(), keyDTO.getKeyLength());
		return new Key(new KeyId(keyDTO.getKeyId().getHash(), keyDTO.getKeyId().getTimestamp()), keyDTO.getKeyValue(),algorithm);
	}

	public static KeyDTO convertToKeyDTO(IKey key) {
		KeyDTO dto = new KeyDTO();
		dto.setKeyValue(key.getEncoded());
		KeyIdDTO idDTO = new KeyIdDTO();
		idDTO.setHash(key.getHash());
		idDTO.setTimestamp(key.getCreationTimeStamp());
		idDTO.setId(key.getId());
		dto.setKeyId(idDTO);
		dto.setKeyAlgorithm(key.getAlgorithm().getName());
		dto.setKeyLength(key.getAlgorithm().getLength());
		return dto;
	}
	
	private DTOConverter() {
	}
}
