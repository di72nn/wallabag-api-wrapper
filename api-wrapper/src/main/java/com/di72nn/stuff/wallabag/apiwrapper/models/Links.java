package com.di72nn.stuff.wallabag.apiwrapper.models;

/**
 * The {@code Links} class represents the container for URLs for navigating between "pages" of data.
 */
public class Links {

	/**
	 * The {@code Link} class is a plain container for a single URL.
	 */
	public static class Link {

		/** The URL. */
		public String href;

		@Override
		public String toString() {
			return href;
		}
	}

	/** The link to the current page. */
	public Link self;
	/** The link to the first page. */
	public Link first;
	/** The link to the last page. */
	public Link last;
	/** The link to the next page. */
	public Link next;

	@Override
	public String toString() {
		return "Links{" +
				"self=" + self +
				", first=" + first +
				", last=" + last +
				", next=" + next +
				'}';
	}

}
