package com.di72nn.stuff.wallabag.apiwrapper.adapters;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class NumericBooleanAdapter {

	@ToJson
	int toJson(@NumericBoolean boolean value) {
		return value ? 1 : 0; // TODO: check
	}

	@FromJson
	@NumericBoolean
	boolean fromJson(String value) {
		return "1".equals(value);
	}

}
