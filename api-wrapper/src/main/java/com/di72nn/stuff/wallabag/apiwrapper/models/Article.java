package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.di72nn.stuff.wallabag.apiwrapper.models.adapters.NumericBoolean;
import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

public class Article {

	public int id = -1;

	public String uid;

	public String url;

	public String title;

	public String content;

	@NumericBoolean
	@Json(name = "is_archived")
	public boolean archived;

	@NumericBoolean
	@Json(name = "is_starred")
	public boolean starred;

	@Json(name = "starred_at")
	public Date starredAt;

	public List<Tag> tags;

	@Json(name = "created_at")
	public Date createdAt;

	@Json(name = "updated_at")
	public Date updatedAt;

	public List<Annotation> annotations;

	public String mimetype;

	public String language;

	@Json(name = "reading_time")
	public int readingTime;

	@Json(name = "domain_name")
	public String domainName;

	@Json(name = "preview_picture")
	public String previewPicture;

	@Json(name = "origin_url")
	public String originUrl;

	@Json(name = "published_at")
	public Date publishedAt;

	@Json(name = "published_by")
	public List<String> publishedBy;

}
