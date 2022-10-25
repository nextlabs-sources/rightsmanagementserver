
/*
 * Created on Feb 28, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.rms.services.crypt;

/**
 * This is the reversible encryptor class. This class generates a random key and
 * uses this key to encrypt the real value of the text to protect. The random
 * key is also added to the encrypted result, so that the encrypted content can
 * be reversed.
 *
 * The format of the encrypted content is the following
 *
 * 1) DUMMY_KEY_HEADER_SIZE characters that are useless
 *
 * 2) Encrypted character (XOR of the real character and the encoding character
 * from the random key)
 *
 * 3)Random key character (that needs to be used for the XOR)
 *
 * @author ihanen
 * @version $Id:
 */
class NextlabsCrypto extends DefaultCrypto {

    private static final int DUMMY_KEY_HEADER_SIZE = 4;
    private static final int MIN_ASCII = 63;
    private static final int MIN_KEY_SIZE = DUMMY_KEY_HEADER_SIZE + 6;
    private static final int MAX_ASCII = 126;
    private static final int MAX_KEY_SIZE = 20;

    NextlabsCrypto() {
        super(ReversibleEncryptor.Crypto.n);
    }

    /**
     * @see com.bluejungle.destiny.server.secureConnector.IDecryptor#decrypt(java.lang.String)
     */
    public String decrypt(String cryptedMsg) {
        assert cryptedMsg != null;

        //Take out useless characters in front
        String realCryptedMessage = cryptedMsg.substring(DUMMY_KEY_HEADER_SIZE * 2);

        //Chops the string in hex sequence
        String[] stringArray = isolateHexValues(realCryptedMessage);
        //Now, treat each string as an HEX number.
        int size = stringArray.length;
        String result = "";
        for (int i = 0; i < size / 2; i++) {
            char cryptedChar = (char) Integer.parseInt(stringArray[2 * i], 16);
            char KeyChar = (char) Integer.parseInt(stringArray[2 * i + 1], 16);
            char uncryptedChar = (char) unmixChars(cryptedChar, KeyChar);
            result += uncryptedChar;
        }
        return result;
    }

    /**
     * Extracts the hexadecimal values from an encrypted message
     *
     * @param cryptedMsg
     *            encrypted message (contains HEX code only)
     * @return an array of string with all the HEX codes
     */
    private String[] isolateHexValues(String cryptedMsg) {
        String[] stringArray = null;
        if (cryptedMsg != null) {
            int size = cryptedMsg.length();
            int arraySize = size / 2;
            //Size should always be even
            stringArray = new String[arraySize];
            for (int i = 0; i < arraySize; i++) {
                stringArray[i] = cryptedMsg.substring(2 * i, 2 * i + 2);
            }
        }
        return (stringArray);
    }

    /**
     * @see com.bluejungle.destiny.server.secureConnector.IEncryptor#encrypt(java.lang.String)
     */
    String encryptWithoutPrefix(String original) {
        assert original != null;

        String result = "";
        String randomKey = RandomString.getRandomString(MIN_KEY_SIZE, MAX_KEY_SIZE, MIN_ASCII, MAX_ASCII);
        //Take the first four characters and put them in front

        for (int i = 0; i < DUMMY_KEY_HEADER_SIZE; i++) {
            char randomKeyChar = randomKey.charAt(i);
            String hexRandomKeyChar = Integer.toHexString(randomKeyChar);
            result += hexRandomKeyChar;
        }

        String realKey = randomKey.substring(DUMMY_KEY_HEADER_SIZE - 1);

        //Use the real key to scramble the data
        int size = original.length();
        int keySize = randomKey.length();
        String encrypted = "";
        for (int i = 0; i < size; i++) {
            char keyChar = randomKey.charAt(i % keySize);
            char cryptedChar = (char) mixChars(original.charAt(i), keyChar);
            String hexCryptedChar = Integer.toHexString(cryptedChar);
            if (hexCryptedChar.length() == 1) {
                hexCryptedChar = "0" + hexCryptedChar;
            }
            encrypted += hexCryptedChar;
            String hexKeyChar = Integer.toHexString(keyChar);
            if (hexKeyChar.length() == 1) {
                hexKeyChar = "0" + hexKeyChar;
            }
            encrypted += hexKeyChar;
        }
        result += encrypted;
        return result;
    }

    /**
     *
     * @param realChar
     *            intial character
     * @param keyChar
     *            character to mix (has to be in the range)
     * @return the mix of the two characters
     */
    private int mixChars(final int realChar, final int keyChar) {
        return (realChar ^ keyChar);
    }

    /**
     * Unmix two characters by performing a character substraction.
     *
     * @param mixed
     *            mixed character
     * @param keyChar
     *            character that was originally used to mix
     * @return the substracter character (between minRange and maxRange)
     */
    private int unmixChars(int mixed, int keyChar) {
        return (mixed ^ keyChar);
    }

}