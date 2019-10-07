package com.di72nn.stuff.wallabag.apiwrapper.models;

public class Links {

	public static class Link {

		public String href;

		@Override
		public String toString() {
			return href;
		}
	}

	public Link self;
	public Link first;
	public Link last;
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
