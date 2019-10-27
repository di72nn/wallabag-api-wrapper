package com.di72nn.stuff.wallabag.apiwrapper;

import java.util.Collection;
import java.util.Iterator;

class Utils {

	static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	static String nonEmptyString(String value, String name) {
		if(value == null) throw new NullPointerException(name + "is null");
		if(value.isEmpty()) throw new IllegalArgumentException(name + " is empty");

		return value;
	}

	static <T> Collection<T> nonEmptyCollection(Collection<T> value, String name) {
		if(value == null) throw new NullPointerException(name + "is null");
		if(value.isEmpty()) throw new IllegalArgumentException(name + " is empty");

		return value;
	}

	static <T> T nonNullValue(T value, String name) {
		if(value == null) throw new NullPointerException(name + " is null");

		return value;
	}

	static int nonNegativeNumber(int value, String name) {
		if(value < 0) throw new IllegalArgumentException(name + " is less than zero: " + value);

		return value;
	}

	static int positiveNumber(int value, String name) {
		if(value <= 0) throw new IllegalArgumentException(name + " is not positive: " + value);

		return value;
	}

	static String join(Iterable<? extends CharSequence> iterable, String delimiter) {
		Iterator<? extends CharSequence> it = iterable.iterator();
		if(!it.hasNext()) return "";

		StringBuilder sb = new StringBuilder(it.next());
		while(it.hasNext()) {
			sb.append(delimiter).append(it.next());
		}

		return sb.toString();
	}

	static String booleanToNumberString(boolean value) {
		return String.valueOf(value ? 1 : 0);
	}

	static String booleanToNullableNumberString(Boolean value) {
		return value != null ? booleanToNumberString(value) : null;
	}

}
