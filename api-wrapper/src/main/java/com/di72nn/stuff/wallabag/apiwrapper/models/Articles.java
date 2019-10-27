package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.di72nn.stuff.wallabag.apiwrapper.WallabagService;
import com.squareup.moshi.Json;

import java.util.List;

/**
 * The {@code Articles} class objects represent the response to article queries
 * ({@link WallabagService#getArticlesBuilder()}).
 * <p>This is a direct response mapping, hence the member names.
 */
public class Articles {

	/**
	 * The {@code Embedded} class wraps a list of {@link Article} objects.
	 */
	public static class Embedded {

		/** The list of {@link Article}s. */
		public List<Article> items;

		@Override
		public String toString() {
			return "Embedded{" +
					"items" + (items == null ? "=null" : ("[" + items.size() + "]")) +
					'}';
		}
	}

	/** Current page index (in {@code [1..pages]}). */
	public int page;
	/** Items per page limit. */
	public int limit;
	/** The total number of pages. */
	public int pages;
	/** The total number of articles on all pages. */
	public int total;

	/** URLs for navigating to the first, last or next pages. */
	@Json(name = "_links")
	public Links links;

	/** The object that contains the {@link Article} objects of the current page. */
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
