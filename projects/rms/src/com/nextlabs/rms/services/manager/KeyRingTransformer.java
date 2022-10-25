package com.nextlabs.rms.services.manager;

import com.bluejungle.pf.destiny.lib.KeyResponseDTO;
import com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyDTO;
import com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyRingDTO;
import com.nextlabs.kms.GetAllKeyRingsWithKeysResponse;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;

import jcifs.util.Base64;
import noNamespace.KeyRingType;
import noNamespace.KeyRingsType;
import noNamespace.KeyType;

import java.util.Calendar;
import java.util.List;

public class KeyRingTransformer {

	public static KeyRingsType transform(KeyResponseDTO keyResponse) {
		KeyRingsType keyRingsType=KeyRingsType.Factory.newInstance();
		List<KeyRingDTO> keyRings=keyResponse.getKeyRings();
		for(KeyRingDTO keyRing: keyRings){
			KeyRingType keyRingType= keyRingsType.addNewKeyRing();
			keyRingType.setKeyRingName(keyRing.getName());
			Calendar keyRingTimeCal=Calendar.getInstance();
			keyRingTimeCal.setTimeInMillis(keyRing.getTimestamp());
			keyRingType.setLastModifiedDate(keyRingTimeCal);
			List<KeyDTO> keys=keyRing.getKeys();
			for(KeyDTO key:keys){
				Calendar keyTimeCal=Calendar.getInstance();
				keyTimeCal.setTimeInMillis(key.getTimestamp());
				KeyType keyType=keyRingType.addNewKey();
				keyType.setKeyId(Base64.encode(key.getId()));
				keyType.setKeyData(Base64.encode(key.getKey()));
				keyType.setTimeStamp(keyTimeCal);
				keyType.setKeyVersion(key.getVersion());
			}
		}
		/*System.out.println("--------------Printing Key Transformer Data----------------------");
		System.out.println(keyRingsType.toString());*/
		return keyRingsType;		
	}
	
	public static KeyRingsType transform(GetAllKeyRingsWithKeysResponse getAllKeyRingsWithKeysResponse) {
		KeyRingsType keyRingsType=KeyRingsType.Factory.newInstance();
		List<KeyRingWithKeysDTO> keyRings=getAllKeyRingsWithKeysResponse.getKeyRingWithKeys().getKeyRings();
		for(KeyRingWithKeysDTO keyRing: keyRings){
			KeyRingType keyRingType= keyRingsType.addNewKeyRing();
			keyRingType.setKeyRingName(keyRing.getName());
			Calendar keyRingTimeCal=Calendar.getInstance();
			keyRingTimeCal.setTimeInMillis(keyRing.getLastModifiedDate());
			keyRingType.setLastModifiedDate(keyRingTimeCal);
			List<com.nextlabs.kms.types.KeyDTO> keys=keyRing.getKeys();
			for(com.nextlabs.kms.types.KeyDTO key:keys){
				Calendar keyTimeCal=Calendar.getInstance();
				keyTimeCal.setTimeInMillis(key.getKeyId().getTimestamp());
				KeyType keyType=keyRingType.addNewKey();
				keyType.setKeyId(Base64.encode(key.getKeyId().getId().getBytes()));
				keyType.setKeyData(Base64.encode(key.getKeyValue()));
				keyType.setTimeStamp(keyTimeCal);
				keyType.setKeyVersion(1);
			}
		}
		return keyRingsType;		
	}

}
