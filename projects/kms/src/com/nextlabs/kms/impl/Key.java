package com.nextlabs.kms.impl;

import java.util.Arrays;

import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;

public class Key implements IKey, IKeyId {

    private static final long serialVersionUID = 1L;

    private final byte[] key;
    private final IKeyId keyId;
    private final KeyAlgorithm algorithm;
    
    public Key(IKeyId keyId, byte[] key, KeyAlgorithm algorithm) {
        this.keyId = keyId;
        assert key != null;
        assert key.length != 0;
        this.key = new byte[key.length];
        this.algorithm = algorithm;
        System.arraycopy(key, 0, this.key, 0, key.length);
    }
    
    @Override
    public String toString() {
        return "key" + keyId.toString().substring(5) + "\n"
        		+ " algorithm: " + algorithm.getName() + " " + algorithm.getLength()
            + "  key: " + Arrays.toString(key);
        		
    }
    
    public byte[] getEncoded(){
        return key.clone();
    }

    public IKeyId getKeyId() {
        return keyId;
    }
    
    public long getCreationTimeStamp() {
        return keyId.getCreationTimeStamp();
    }

    public byte[] getHash() {
        return keyId.getHash();
    }
    
		public String getId() {
			return keyId.getId();
		}

    public KeyAlgorithm getAlgorithm(){
    		return algorithm;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(key);
        result = prime * result + ((keyId == null) ? 0 : keyId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        
        if(obj instanceof IKeyId){
            return this.keyId.equals(obj);
        }
        
        if(!(obj instanceof IKey)){
            return false;
        }
        
        IKey other = (IKey) obj;
        if (!Arrays.equals(key, other.getEncoded()))
            return false;
        if (keyId == null) {
            if (other.getKeyId() != null)
                return false;
        } else if (!keyId.equals(other.getKeyId())) {
            return false;
        }
        if (!algorithm.equals(other.getAlgorithm()))
            return false;
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (key != null) {
            Arrays.fill(key, (byte) 0);
        }
        super.finalize();
    }
}
