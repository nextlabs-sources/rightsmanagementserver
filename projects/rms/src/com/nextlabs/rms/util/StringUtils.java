package com.nextlabs.rms.util;

import java.util.List;

public class StringUtils {
	public static String normalize(String orig) {
		if (!hasText(orig)) {
			return orig;
		}
		int off = 0;
		int length = orig.length();
		char[] in = orig.toCharArray();
		char aChar;
		StringBuilder builder = new StringBuilder();
		while (off < length) {
			aChar = in[off++];
			if (aChar == '\\') {
				if (off < length) {
					aChar = in[off++];
					if (aChar == 'n') {
						aChar = '\n';
					} else if (aChar == 't') {
						aChar = '\t';
					} else if (aChar == 'r') {
						aChar = '\r';
					} else if (aChar == 'f') {
						aChar = '\f';
					}
				}
			}
			builder.append(aChar);
		}
		return builder.toString();
	}

	/**
	 * Check whether the given value has contains at least one non-whitespace character.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Compares two String, returning true if they represent equal sequences of characters, ignoring case.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean equalsIgnoreCase(String s1, String s2) {
		return nullSafeEquals(s1, s2, true);
	}

	/**
	 * Compares two String, returning true if they represent equal sequences of characters, case sensitive.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean equals(String s1, String s2) {
		return nullSafeEquals(s1, s2, false);
	}

	private static boolean nullSafeEquals(String s1, String s2, boolean ignoreCase) {
		if (s1 == null || s2 == null) {
			return s1 == s2;
		} else if (s1 == s2) {
			return true;
		} else if (s1.length() != s2.length()) {
			return false;
		} else {
			return ignoreCase ? s1.equalsIgnoreCase(s2) : s1.equals(s2);
		}
	}

	/**
	 * Check whether the given String ends with the specified suffix, ignoring upper/lower case.
	 * 
	 * @param str
	 * @param suffix
	 * @return
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		return endsWith(str, suffix, true);
	}

	/**
	 * Check whether the given String ends with the specified suffix, case sensitive.
	 * 
	 * @param str
	 * @param suffix
	 * @return
	 */
	public static boolean endsWith(String str, String suffix) {
		return endsWith(str, suffix, false);
	}

	private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
		if (str == null || suffix == null) {
			return false;
		}
		if (str.endsWith(suffix)) {
			return true;
		}
		if (str.length() < suffix.length()) {
			return false;
		}
		String substring = str.substring(str.length() - suffix.length());
		return ignoreCase ? substring.equalsIgnoreCase(suffix) : substring.equals(suffix);
	}

	/**
	 * Check whether the given String starts with the specified suffix, ignoring upper/lower case.
	 * 
	 * @param str
	 * @param prefix
	 * @return
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		return startsWith(str, prefix, true);
	}

	/**
	 * Check whether the given String starts with the specified prefix, case sensitive.
	 * 
	 * @param str
	 * @param prefix
	 * @return
	 */
	public static boolean startsWith(String str, String prefix) {
		return startsWith(str, prefix, false);
	}

	private static boolean startsWith(String str, String prefix, boolean ignoreCase) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String substring = str.substring(0, prefix.length());
		return ignoreCase ? substring.equalsIgnoreCase(prefix) : substring.equals(prefix);
	}

	/**
	 * Check whether the given array contains the given element.
	 * 
	 * @param array
	 * @param str
	 * @param ignoreCase
	 * @return
	 */
	public static boolean containsElement(String[] array, String str, boolean ignoreCase) {
		if (array == null) {
			return false;
		}
		for (String s : array) {
			if (nullSafeEquals(s, str, ignoreCase)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check that the given CharSequence is neither null nor of length 0.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	public static String trim(String str) {
		return str == null ? null : str.trim();
	}

	public static String trimToNull(String str) {
		String ts = trim(str);
		return !hasLength(ts) ? null : ts;
	}

	public static String trimToEmpty(String str) {
		return str == null ? "" : str.trim();
	}

	public static String getListAsCSV(List<String> items) {
		if (items == null) {
			return null;
		}

		StringBuilder result = new StringBuilder("");
		for (int i=0; i < items.size(); ++i) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(items.get(i));
		}

		return result.toString();
	}
}