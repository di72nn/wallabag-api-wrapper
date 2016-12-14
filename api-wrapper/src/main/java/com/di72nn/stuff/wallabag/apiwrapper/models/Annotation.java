package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

public class Annotation {

	public static class Range {

		public String start;
		public String end;
		public long startOffset;
		public long endOffset;

	}

	public int id;

	@Json(name = "annotator_schema_version")
	public String annotatorSchemaVersion;

	public String text;

	@Json(name = "created_at")
	public Date createdAt;

	@Json(name = "updated_at")
	public Date updatedAt;

	public String quote;

	public List<Range> ranges;

}
