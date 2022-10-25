package com.nextlabs.kms.entity.enums;

import com.nextlabs.kms.exception.KeyAlgorithmUnsupportedException;

public enum KeyAlgorithm {
	AES_128 ("AES", 128),
	AES_192 ("AES", 192),
	AES_256 ("AES", 256);
  
  private final String name;
  private final int length;
  public static final KeyAlgorithm DEFAULT_KEY_ALGORITHM = AES_256;
 

  private KeyAlgorithm (String algorithm, int length) {
      this.name = algorithm;
      this.length = length;
  }

  public String getName(){
  	return name;
  }
  
  public int getLength(){
  	return length;
  }
  
  public boolean equalsName(String otherName) {
      return (otherName == null) ? false : name.equals(otherName);
  }

  public String toString() {
     return this.name;
  }
  
  public static KeyAlgorithm convertToKeyAlgorithm (String algorithm, int length) throws KeyAlgorithmUnsupportedException{
		KeyAlgorithm keyAlgorithm = null;
		for(KeyAlgorithm algo: KeyAlgorithm.values()){
			if(algo.getName().equals(algorithm) && algo.getLength() == length) {
				keyAlgorithm = algo;
			}
		}
		if (keyAlgorithm == null) {
			throw new KeyAlgorithmUnsupportedException(algorithm, length);
		}
		return keyAlgorithm;
  }
  
  public static KeyAlgorithm convertToKeyAlgorithm (java.security.Key key) throws KeyAlgorithmUnsupportedException{
  	String algorithm = key.getAlgorithm();
  	if(algorithm.equals("NX1"))
  		algorithm = "AES";
  	int length = key.getEncoded().length*8;
  	return convertToKeyAlgorithm(algorithm, length);
  }
}