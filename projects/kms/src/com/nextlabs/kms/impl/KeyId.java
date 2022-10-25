package com.nextlabs.kms.impl;

import java.util.Arrays;

import com.nextlabs.kms.IKeyId;

public class KeyId implements IKeyId{

    private static final long serialVersionUID = 1L;
    
    private final String id;

    private final byte[] hash;
    
    private final long creationTimeStamp;
    
    public KeyId(byte[] hash, long creationTimeStamp) {
        this.hash = hash;
        this.creationTimeStamp = creationTimeStamp;
        this.id = KeyRingUtil.convertToAlias(hash, creationTimeStamp);
    }
    
    public byte[] getHash() {
        return hash;
    }
    
    public long getCreationTimeStamp(){
        return creationTimeStamp;
    }
    
    public String getId(){
    	return id;
    }
    
    @Override
    public String toString() {
        return "KeyId" + "\n" 
            + "  id: " + Arrays.toString(hash) + "\n" 
            + "  time: " + creationTimeStamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (creationTimeStamp ^ (creationTimeStamp >>> 32));
        result = prime * result + Arrays.hashCode(hash);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof IKeyId)) {
            return false;
        }
        IKeyId other = (IKeyId) obj;
        if (creationTimeStamp != other.getCreationTimeStamp())
            return false;
        if (!Arrays.equals(hash, other.getHash()))
            return false;
        return true;
    }
}
