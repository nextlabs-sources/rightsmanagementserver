package com.nextlabs.kms;

import com.nextlabs.kms.entity.enums.KeyAlgorithm;

public interface IKey extends IKeyId{
    public IKeyId getKeyId();
    
    public byte[] getEncoded();
    
    public KeyAlgorithm getAlgorithm();
}
