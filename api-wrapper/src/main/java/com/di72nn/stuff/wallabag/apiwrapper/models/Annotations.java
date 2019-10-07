package com.di72nn.stuff.wallabag.apiwrapper.models;

import java.util.List;

public class Annotations {

	public int total;

	public List<Annotation> rows;

	@Override
	public String toString() {
		return "Annotations{" +
				"total=" + total +
				", rows" + (rows == null ? "=null" : ("[" + rows.size() + "]")) +
				'}';
	}

}
