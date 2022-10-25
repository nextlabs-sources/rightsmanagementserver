package com.nextlabs.rms.util;

public class Nvl {
	public static <T> T nvl(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}
	
	public static String nvl(String value) {
		return nvl(value, "");
	}
	
	public static Integer nvl(Integer value) {
		return nvl(value, 0);
	}
}
