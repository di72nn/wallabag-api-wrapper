package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

/**
 * The {@code Annotation} class represents an annotation entry.
 * <p>The annotations use the <a href="https://annotatorjs.org">AnnotatorJS</a> format.
 */
public class Annotation {

	/**
	 * The {@code Range} class represent the text range the annotation applies to.
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/API/Range">Web API Range</a>.
	 */
	public static class Range {

		public String start;
		public String end;
		public long startOffset;
		public long endOffset;

		@Override
		public String toString() {
			return "Range{" +
					"start='" + start + '\'' +
					", end='" + end + '\'' +
					", startOffset=" + startOffset +
					", endOffset=" + endOffset +
					'}';
		}
	}

	/** The ID of the annotation. */
	public int id;

	/** The annotator schema version. */
	@Json(name = "annotator_schema_version")
	public String annotatorSchemaVersion;

	/** The text of the annotation (the user provided comment). */
	public String text;

	/** The annotation creation date. */
	@Json(name = "created_at")
	public Date createdAt;

	/** The date of the annotation last modification. */
	@Json(name = "updated_at")
	public Date updatedAt;

	/** The quote of the annotation (the article text this annotation belongs to). */
	public String quote;

	/** One or more {@link Range}s for this annotation. */
	public List<Range> ranges;

	@Override
	public String toString() {
		return "Annotation{" +
				"id=" + id +
				", annotatorSchemaVersion='" + annotatorSchemaVersion + '\'' +
				", text='" + text + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", quote='" + quote + '\'' +
				", ranges=" + ranges +
				'}';
	}

}
