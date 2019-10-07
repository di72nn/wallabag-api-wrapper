package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.squareup.moshi.Json;

public class ExistsWithIdResponse {

	@Json(name = "exists")
	public Integer id;

	@Override
	public String toString() {
		return "ExistsWithIdResponse{" +
				"id=" + id +
				'}';
	}

}
