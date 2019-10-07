package com.di72nn.stuff.wallabag.apiwrapper.models;

public class Tag {

	public int id;
	public String label;
	public String slug;

	@Override
	public String toString() {
		return "Tag{" +
				"id=" + id +
				", label='" + label + '\'' +
				", slug='" + slug + '\'' +
				'}';
	}

}
