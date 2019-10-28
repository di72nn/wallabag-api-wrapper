package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import okhttp3.FormBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.*;

abstract class AbstractArticleBuilder<T extends AbstractArticleBuilder<T>> extends AbstractTagsBuilder<T> {

    protected String title;
    protected String content;
    protected String language;
    protected String previewPicture;
    protected Boolean starred;
    protected Boolean archive;
    protected Date publishedAt;
    protected List<String> authors;
    protected Boolean isPublic;
    protected String originUrl;

    /**
     * Sets the article title to this builder, returns the builder.
     * <p>Empty or {@code null} strings are not passed in the request.
     *
     * @param title the title to set, {@code null}able
     * @return this builder
     */
    public T title(String title) {
        this.title = title;
        return self();
    }

    /**
     * Sets the article content to this builder, returns the builder.
     * <p>Empty or {@code null} strings are not passed in the request.
     *
     * @param content the content to set, {@code null}able
     * @return this builder
     */
    public T content(String content) {
        this.content = content;
        return self();
    }

    /**
     * Sets the article language to this builder, returns the builder.
     * <p>Empty or {@code null} strings are not passed in the request.
     *
     * @param language the language to set, {@code null}able
     * @return this builder
     */
    public T language(String language) {
        this.language = language;
        return self();
    }

    /**
     * Sets the article preview picture URL to this builder, returns the builder.
     * <p>Empty or {@code null} strings are not passed in the request.
     *
     * @param previewPicture the preview picture URL to set, {@code null}able
     * @return this builder
     */
    public T previewPicture(String previewPicture) {
        this.previewPicture = previewPicture;
        return self();
    }

    /**
     * Sets the "starred" ("favorite") article parameter to this builder, returns the builder.
     * <p>{@code null} value is not passed in the request.
     *
     * @param starred the "starred" ("favorite") parameter to set, {@code null}able
     * @return this builder
     */
    public T starred(Boolean starred) {
        this.starred = starred;
        return self();
    }

    /**
     * Sets the "archive" ("read") article parameter to this builder, returns the builder.
     * <p>{@code null} value is not passed in the request.
     *
     * @param archive the "archive" ("read") parameter to set, {@code null}able
     * @return this builder
     */
    public T archive(Boolean archive) {
        this.archive = archive;
        return self();
    }

    /**
     * Sets the article publication date to this builder, returns the builder.
     * <p>{@code null} value is not passed in the request.
     *
     * @param publishedAt the publication date to set, {@code null}able
     * @return this builder
     */
    public T publishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
        return self();
    }

    /**
     * Adds an author parameter to this builder, returns the builder.
     * Duplicates are silently ignored.
     *
     * @param author the author to add
     * @return this builder
     * @throws NullPointerException     if the {@code author} is {@code null}
     * @throws IllegalArgumentException if the {@code author} is an empty {@code String}
     */
    public T author(String author) {
        nonEmptyString(author, "author");

        List<String> authors = this.authors;
        if (authors == null) {
            this.authors = authors = new ArrayList<>(1);
        }
        authors.add(author);

        return self();
    }

    /**
     * Adds authors from the specified {@code Collection} to this builder, returns the builder.
     * Duplicates are silently ignored.
     *
     * @param authors the authors to add
     * @return this builder
     * @throws NullPointerException     if the {@code authors} collection is {@code null} or if it contains a {@code null}
     * @throws IllegalArgumentException if the {@code authors} collection contains an empty {@code String}
     */
    public T authors(Collection<String> authors) {
        nonNullValue(authors, "authors");

        if (!authors.isEmpty()) {
            if (this.authors == null) {
                this.authors = new ArrayList<>(authors.size());
            }

            for (String author : authors) {
                author(author);
            }
        }

        return self();
    }

    /**
     * Resets the authors that were previously added to this builder, returns the builder.
     *
     * @return this builder
     */
    public T resetAuthors() {
        if (authors != null) authors.clear();
        return self();
    }

    /**
     * Sets the "public" article parameter to this builder, returns the builder.
     * Setting it to {@code true} makes the article public, see {@link Article#publicUid}.
     * <p>{@code null} value is not passed in the request.
     *
     * @param isPublic the "public" parameter to set, {@code null}able
     * @return this builder
     */
    public T isPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        return self();
    }

    /**
     * Sets the article origin URL to this builder, returns the builder.
     * <p>Empty or {@code null} strings are not passed in the request.
     *
     * @param originUrl the origin URL to set, {@code null}able
     * @return this builder
     */
    public T originUrl(String originUrl) {
        this.originUrl = originUrl;
        return self();
    }

    protected String getPublishedAtString() {
        if (publishedAt != null) {
            return String.valueOf(publishedAt.getTime() / 1000);
        }
        return null;
    }

    protected String getAuthorsString() {
        if (authors != null && !authors.isEmpty()) {
            return Utils.join(authors, ",");
        }
        return null;
    }

    protected FormBody.Builder populateFormBodyBuilder(FormBody.Builder bodyBuilder) {
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

    protected void addParameter(FormBody.Builder bodyBuilder, String paramName, String paramValue) {
        if (!isEmpty(paramValue)) bodyBuilder.add(paramName, paramValue);
    }

}
