package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.squareup.moshi.Json;

import java.util.List;

public class Articles {

	public static class Embedded {

		public List<Article> items;

		@Override
		public String toString() {
			return "Embedded{" +
					"items" + (items == null ? "=null" : ("[" + items.size() + "]")) +
					'}';
		}
	}

	public int page;
	public int limit;
	public int pages;
	public int total;

	@Json(name = "_links")
	public Links links;

	@Json(name = "_embedded")
	public Embedded embedded;

	@Override
	public String toString() {
		return "Articles{" +
				"page=" + page +
				", limit=" + limit +
				", pages=" + pages +
				", total=" + total +
				", links=" + links +
				", embedded=" + embedded +
				'}';
	}

}
