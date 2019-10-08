package com.di72nn.stuff.wallabag.apiwrapper;

import okhttp3.FormBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.*;

abstract class AbstractArticleBuilder<T extends AbstractArticleBuilder<T>> extends AbstractTagsBuilder<T> {

	String title;
	String content;
	String language;
	String previewPicture;
	Boolean starred;
	Boolean archive;
	Date publishedAt;
	List<String> authors;
	Boolean isPublic;
	String originUrl;

	public T title(String title) {
		this.title = nonEmptyString(title, "title");
		return self();
	}

	public T content(String content) {
		this.content = nonEmptyString(content, "content");
		return self();
	}

	public T language(String language) {
		this.language = nonEmptyString(language, "language");
		return self();
	}

	public T previewPicture(String previewPicture) {
		this.previewPicture = nonEmptyString(previewPicture, "previewPicture");
		return self();
	}

	public T starred(boolean starred) {
		this.starred = starred;
		return self();
	}

	public T archive(boolean archive) {
		this.archive = archive;
		return self();
	}

	public T publishedAt(Date publishedAt) {
		this.publishedAt = nonNullValue(publishedAt, "publishedAt");
		return self();
	}

	public T author(String author) {
		nonEmptyString(author, "author");

		List<String> authors = this.authors;
		if(authors == null) {
			this.authors = authors = new ArrayList<>(1);
		}
		authors.add(author);

		return self();
	}

	public T authors(Collection<String> authors) {
		nonEmptyCollection(authors, "authors");

		List<String> authorsLocal = this.authors;
		if(authorsLocal == null) {
			this.authors = authorsLocal = new ArrayList<>(authors.size());
		}
		authorsLocal.addAll(authors);

		return self();
	}

	public T isPublic(boolean isPublic) {
		this.isPublic = isPublic;
		return self();
	}

	public T originUrl(String originUrl) {
		this.originUrl = nonEmptyString(originUrl, "originUrl");
		return self();
	}

	String getPublishedAtString() {
		if (publishedAt != null) {
			return String.valueOf(publishedAt.getTime() / 1000);
		}
		return null;
	}

	String getAuthorsString() {
		if(authors != null && !authors.isEmpty()) {
			return Utils.join(authors, ",");
		}
		return null;
	}

	FormBody.Builder populateFormBodyBuilder(FormBody.Builder bodyBuilder) {
		addParameter(bodyBuilder, "title", title);
		addParameter(bodyBuilder, "content", content);
		addParameter(bodyBuilder, "language", language);
		addParameter(bodyBuilder, "preview_picture", previewPicture);
		addParameter(bodyBuilder, "starred", Utils.booleanToNullableNumberString(starred));
		addParameter(bodyBuilder, "archive", Utils.booleanToNullableNumberString(archive));
		addParameter(bodyBuilder, "published_at", getPublishedAtString());
		addParameter(bodyBuilder, "authors", getAuthorsString());
		addParameter(bodyBuilder, "tags", getTagsString());
		addParameter(bodyBuilder, "public", Utils.booleanToNullableNumberString(isPublic));
		addParameter(bodyBuilder, "origin_url", originUrl);

		return bodyBuilder;
	}

	void addParameter(FormBody.Builder bodyBuilder, String paramName, String paramValue) {
		if (paramValue != null) bodyBuilder.add(paramName, paramValue);
	}

}
