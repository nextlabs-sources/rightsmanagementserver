package com.nextlabs.kms;

import java.io.Serializable;

public interface IKeyId extends Serializable{
	
		String getId();

    byte[] getHash();
    
    long getCreationTimeStamp();
    
}
