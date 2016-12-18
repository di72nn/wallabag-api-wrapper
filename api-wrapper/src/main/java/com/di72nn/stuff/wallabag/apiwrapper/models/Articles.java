package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.squareup.moshi.Json;

import java.util.List;

public class Articles {

	public static class Embedded {

		public List<Article> items;

	}

	public int page;
	public int limit;
	public int pages;
	public int total;

	@Json(name = "_links")
	public Links links;

	@Json(name = "_embedded")
	public Embedded embedded;

}
