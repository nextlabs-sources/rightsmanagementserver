
/*
 * Created on Feb 27, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.rms.services.crypt;

import java.security.SecureRandom;
import java.util.Random;

/**
 * This class generates a random string.
 *
 * @author ihanen
 * @version $Id$
 */

public class RandomString {

    /**
     * A lower-case alphabetic character: [a-z]
     */
    public static final String LOWER;

    /**
     * An upper-case alphabetic character:[A-Z]
     */
    public static final String UPPER;

    /**
     *  All ASCII:[\x00-\x7F]
     */
    public static final String ASCII;

    /**
     *  An alphabetic character:[\p{Lower}\p{Upper}]
     */
    public static final String ALPHA;

    /**
     * A decimal digit: [0-9]
     */
    public static final String DIGIT;

    /**
     * An alphanumeric character:[\p{Alpha}\p{Digit}]
     */
    public static final String ALNUM;

    /**
     *  Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
     */
    public static final String PUNCT;

    /**
     * A visible character: [\p{Alnum}\p{Punct}]
     */
    public static final String GRAPH;

    /**
     * A printable character: [\p{Graph}\x20]
     */
    public static final String PRINT;

    /**
     *  A space or a tab: [ \t]
     */
    public static final String BLANK;

    /**
     * A control character: [\x00-\x1F\x7F]
     */
    public static final String CNTRL;

    /**
     * A hexadecimal digit: [0-9a-fA-F]
     */
    public static final String XDIGIT;

    /**
     * A whitespace character: [ \t\n\x0B\f\r]
     */
    public static final String SPACE;

    public static final String ALL;

    static{
        LOWER = generateString('a', 'z');
        UPPER = generateString('A', 'Z');
        ASCII = generateString((char)0x00, (char)0x7F);
        ALPHA = LOWER + UPPER;
        DIGIT = generateString('0', '9');
        ALNUM = ALPHA + DIGIT;
        PUNCT = generateString('!', '/') + generateString(':', '@') + generateString('[', '`') + generateString('{', '~');
        GRAPH = ALNUM + PUNCT;
        PRINT = GRAPH + ' ';
        BLANK = " \t";
        CNTRL = generateString((char)0x00, (char)0x1F) + (char)0x7F;
        XDIGIT = DIGIT + generateString('a', 'f') + generateString('A', 'F');
        SPACE = " \t\n\f\r" + (char)0x0B;
        ALL   = generateString((char)0x00, (char)0xFF);
    }

    private static String generateString(char start, char end) {
        StringBuilder sb = new StringBuilder(end - start + 1);
        for (char i = start; i <= end; i++) {
            sb.append(i);
        }
        return sb.toString();
    }

    //Secure Random could be slower than Random. Depends on JVM implementation.
    private static Random randomizer = new SecureRandom();

    /**
     * Generates a random number
     *
     * @param startNb
     *            lowest random number
     * @param endNb
     *            highest random number
     * @return a random character
     */
    private static int rand(int startNb, int endNb) {
        if (endNb < startNb) {
            throw new IllegalArgumentException("endNb must be greater than startNb");
        }

        int range = endNb - startNb + 1;
        int random = randomizer.nextInt() % range;
        if (random < 0) {
            random = -random;
        }
        return startNb + random;
    }

    /**
     * Returns a random ASCII, human readable string. The random string has a
     * random content, and a random size.
     *
     * @param minSize
     *            minimum string size
     * @param maxSize
     *            maximum string size
     * @param minRange
     *            ASCII code of the lowest character
     * @param maxRange
     *            ASCII code of the highest character
     * @return a random string
     */
    public static String getRandomString(int minSize, int maxSize, int minRange, int maxRange) {
        if (maxSize < minSize) {
            throw new IllegalArgumentException("maxSize must be greater than minSize");
        }

        if (maxRange < minRange) {
            throw new IllegalArgumentException("maxRange must be greater than minRange");
        }

        int n = rand(minSize, maxSize);
        byte b[] = new byte[n];
        for (int i = 0; i < n; i++) {
            b[i] = (byte) rand(minRange, maxRange);
        }
        return new String(b);
    }

    public static String getRandomString(int minSize, int maxSize, String possibleChars) {
        char[] chars = possibleChars.toCharArray();

        if (maxSize < minSize) {
            throw new IllegalArgumentException("maxSize must be greater than minSize");
        }

        int n = rand(minSize, maxSize);
        char c[] = new char[n];
        for (int i = 0; i < n; i++) {
            c[i] = chars[randomizer.nextInt(chars.length)];
        }
        return new String(c);
    }

    /**
     * Randomly mix the case of the input string
     * @param str
     * @return a string with mixed case
     */
    public static String mix(String str){
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = randomizer.nextBoolean()
                    ? Character.toLowerCase(chars[i])
                    : Character.toUpperCase(chars[i]);
        }
        return new String(chars);
    }
}