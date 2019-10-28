package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.di72nn.stuff.wallabag.apiwrapper.models.adapters.NumericBoolean;
import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

/**
 * The {@code Article} class represents an article entry.
 * <p>Some fields may be {@code null}. If the server returns {@code null}s for primitive types
 * ({@link #id}, {@link #readingTime}, etc.), then it should be fixed on the server.
 */
public class Article {

    /** The ID of the article. */
    public int id = -1;

    /** The URL of the article (the URL from which the article was fetched). */
    public String url;

    /** The title of the article. */
    public String title;

    /** The content of the article. */
    public String content;

    /** The flag that indicates whether the article was marked as "archived" (marked as "read"). */
    @NumericBoolean
    @Json(name = "is_archived")
    public boolean archived;

    /** The flag that indicates whether the article was marked as "starred" (marked as "favorite"). */
    @NumericBoolean
    @Json(name = "is_starred")
    public boolean starred;

    /** The date when the {@link #starred} flag was changed. {@code null}able. */
    @Json(name = "starred_at")
    public Date starredAt;

    /** The tags of the article. */
    public List<Tag> tags;

    /** The date when the article was saved. */
    @Json(name = "created_at")
    public Date createdAt;

    /** The date of the last change made to the article. */
    @Json(name = "updated_at")
    public Date updatedAt;

    /** The annotations of the article. {@code null}able. */
    public List<Annotation> annotations;

    /** The media type of the article. {@code null}able. */
    public String mimetype;

    /** The language of the article (usually auto-detected). {@code null}able. */
    public String language;

    /** The reading time of the article in minutes, calculated for the reading speed of 200 words per minute. */
    @Json(name = "reading_time")
    public int readingTime;

    /** The domain of the URL the article was saved from (without the protocol). */
    @Json(name = "domain_name")
    public String domainName;

    /** The preview picture URL. {@code null}able. */
    @Json(name = "preview_picture")
    public String previewPicture;

    /** The origin URL (e.g. another article that links to this article). {@code null}able. */
    @Json(name = "origin_url")
    public String originUrl;

    /** The publication date of the article. {@code null}able. */
    @Json(name = "published_at")
    public Date publishedAt;

    /** The authors of the article. {@code null}able. May contain a single empty {@code String}. */
    @Json(name = "published_by")
    public List<String> authors;

    /** The flag that indicates whether the article was made public. {@code null}able. See {@link #publicUid}. */
    @Json(name = "is_public")
    public Boolean isPublic;

    /**
     * The UID part of the URL this article is publicly available at. {@code null}able.
     * The URL takes the form of {@code https://your.wallabag.instance/share/<publicUid>}.
     * {@code null}able. See {@link #isPublic}.
     */
    @Json(name = "uid")
    public String publicUid;

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

}
