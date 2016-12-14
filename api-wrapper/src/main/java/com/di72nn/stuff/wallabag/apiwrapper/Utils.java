package com.di72nn.stuff.wallabag.apiwrapper;

import java.util.Iterator;

public class Utils {

	public static String join(Iterable<? extends CharSequence> iterable, String delimeter) {
		Iterator<? extends CharSequence> it = iterable.iterator();
		if(!it.hasNext()) return "";

		StringBuilder sb = new StringBuilder(it.next());
		while(it.hasNext()) {
			sb.append(delimeter).append(it.next());
		}

		return sb.toString();
	}

	public static String booleanToNumberString(boolean value) {
		return String.valueOf(value ? 1 : 0);
	}

}
