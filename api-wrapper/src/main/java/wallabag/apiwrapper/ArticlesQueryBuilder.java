package wallabag.apiwrapper;

import retrofit2.Call;
import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Article;
import wallabag.apiwrapper.models.Articles;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The {@code ArticlesQueryBuilder} class represents a builder for accumulating parameters
 * for querying for articles.
 * <p>Objects of this class can be reused for making queries with different parameters.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ArticlesQueryBuilder extends GenericPaginatingQueryBuilder<ArticlesQueryBuilder> {

    /**
     * The {@code SortCriterion} enum represent the available sort criteria.
     */
    public enum SortCriterion {

        /** Sorting by the creation date ({@link Article#createdAt}). */
        CREATED("created"),
        /** Sorting by the modification date ({@link Article#updatedAt}). */
        UPDATED("updated"),
        /** Sorting by the archival date ({@link Article#archivedAt}). */
        ARCHIVED("archived");

        private String value;

        SortCriterion(String value) {
            this.value = value;
        }

        String apiValue() {
            return value;
        }

    }

    /**
     * The {@code SortOrder} enum represents the available sort orders.
     */
    public enum SortOrder {

        /** Ascending order. */
        ASCENDING("asc"),
        /** Descending order. */
        DESCENDING("desc");

        private String value;

        SortOrder(String value) {
            this.value = value;
        }

        String apiValue() {
            return value;
        }

    }

    /**
     * The {@code DetailLevel} enum represents the available detail levels.
     */
    public enum DetailLevel {

        /** Load all data. */
        FULL("full"),
        /** Load everything except {@link Article#content}. Makes for lighter responses. */
        METADATA("metadata");

        private String value;

        DetailLevel(String value) {
            this.value = value;
        }

        String apiValue() {
            return value;
        }

    }

    static class TagsBuilder extends AbstractTagsBuilder<TagsBuilder> {
        @Override
        protected TagsBuilder self() {
            return this;
        }
    }

    protected TagsBuilder tagsBuilder;
    protected Boolean archive;
    protected Boolean starred;
    protected SortCriterion sortCriterion;
    protected SortOrder sortOrder;
    protected DetailLevel detailLevel;
    protected long since = 0;
    protected Boolean isPublic;

    ArticlesQueryBuilder(WallabagService wallabagService) {
        super(wallabagService);
        tagsBuilder = new TagsBuilder();
    }

    @Override
    protected ArticlesQueryBuilder self() {
        return this;
    }

    /**
     * Adds a tag to this builder, returns the builder.
     * Duplicates are silently ignored.
     *
     * @param tag the tag to add
     * @return this builder
     * @throws NullPointerException     if the {@code tag} is {@code null}
     * @throws IllegalArgumentException if the {@code tag} is an empty {@code String}
     */
    public ArticlesQueryBuilder tag(String tag) {
        tagsBuilder.tag(tag);
        return this;
    }

    /**
     * Adds tags from the specified {@code Collection} to this builder, returns the builder.
     * Duplicates are silently ignored.
     *
     * @param tags a {@code Collection} with tags to add
     * @return this builder
     * @throws NullPointerException     if the {@code tags} collection is {@code null} or if it contains a {@code null}
     * @throws IllegalArgumentException if the {@code tags} collection contains an empty {@code String}
     */
    public ArticlesQueryBuilder tags(Collection<String> tags) {
        tagsBuilder.tags(tags);
        return this;
    }

    /**
     * Resets the tags that were previously added to this builder, returns the builder.
     *
     * @return this builder
     */
    public ArticlesQueryBuilder resetTags() {
        tagsBuilder.resetTags();
        return this;
    }

    /**
     * Sets the "archived" ("read") article parameter to this builder, returns the builder.
     * <p>{@code true} queries archived articles, {@code false} - not archived, {@code null} - all articles.
     * Defaults to {@code null}.
     *
     * @param archive the "archive" ("read") parameter to set, {@code null}able
     * @return this builder
     */
    public ArticlesQueryBuilder archive(Boolean archive) {
        this.archive = archive;
        return this;
    }

    /**
     * Sets the "starred" ("favorite") article parameter to this builder, returns the builder.
     * <p>{@code true} queries starred articles, {@code false} - not starred, {@code null} - all articles.
     * Defaults to {@code null}.
     *
     * @param starred the "starred" ("favorite") parameter to set, {@code null}able
     * @return this builder
     */
    public ArticlesQueryBuilder starred(Boolean starred) {
        this.starred = starred;
        return this;
    }

    /**
     * Sets the sort criterion for the result returned by the server, returns this builder.
     * <p>Defaults to {@link SortCriterion#CREATED}.
     *
     * @param sortCriterion the sort criterion to set, {@code null}able
     * @return this builder
     */
    public ArticlesQueryBuilder sortCriterion(SortCriterion sortCriterion) {
        this.sortCriterion = sortCriterion;
        return this;
    }

    /**
     * Sets the sort order for the result returned by the server, returns this builder.
     * <p>Defaults to {@link SortOrder#DESCENDING}.
     *
     * @param sortOrder the sort order to set, {@code null}able
     * @return this builder
     */
    public ArticlesQueryBuilder sortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /**
     * Sets the detail level for the result returned by the server, returns this builder.
     * <p>Defaults to {@link DetailLevel#FULL}.
     *
     * @param detailLevel the detail level to set, {@code null}able
     * @return this builder
     */
    public ArticlesQueryBuilder detailLevel(DetailLevel detailLevel) {
        this.detailLevel = detailLevel;
        return this;
    }

    /**
     * Sets the timestamp in milliseconds since when the entries will be filtered by
     * {@link Article#updatedAt}, returns this builder.
     * <p>{@code 0} means no filtering. Defaults to {@code 0}.
     *
     * @param since the timestamp in milliseconds to filter by {@code updatedAt}
     * @return this builder
     */
    public ArticlesQueryBuilder since(long since) {
        this.since = since;
        return this;
    }

    /**
     * Sets the "isPublic" article parameter to this builder, returns the builder.
     * {@code true} queries public articles, {@code false} - not public, {@code null} - all articles.
     * <p>Defaults to {@code null}.
     *
     * @param isPublic the "public" parameter to set
     * @return this builder
     */
    public ArticlesQueryBuilder isPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    protected String getSortCriterionString() {
        return (sortCriterion != null ? sortCriterion : SortCriterion.CREATED).apiValue();
    }

    protected String getOrderString() {
        return (sortOrder != null ? sortOrder : SortOrder.DESCENDING).apiValue();
    }

    protected String getDetailLevelString() {
        return (detailLevel != null ? detailLevel : DetailLevel.FULL).apiValue();
    }

    @Override
    protected Map<String, String> build() {
        Map<String, String> parameters = super.build();

        addParameter(parameters, "archive", Utils.booleanToNullableNumberString(archive));
        addParameter(parameters, "starred", Utils.booleanToNullableNumberString(starred));
        parameters.put("sort", getSortCriterionString());
        parameters.put("order", getOrderString());
        parameters.put("detail", getDetailLevelString());
        addParameter(parameters, "tags", tagsBuilder.getTagsString());
        parameters.put("since", String.valueOf(since / 1000));
        addParameter(parameters, "public", Utils.booleanToNullableNumberString(isPublic));

        return parameters;
    }

    @Override
    public Call<Articles> buildCall() {
        return wallabagService.getArticlesCall(build());
    }

    /**
     * {@inheritDoc}
     *
     * @throws NotFoundException if {@link #page(int)} was set to a value {@code > }{@link Articles#pages}
     *                           (depends on the {@code notFoundPolicy})
     */
    @Override
    public Articles execute(NotFoundPolicy notFoundPolicy) throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> wallabagService.getArticles(build()), wallabagService,
                getAvailabilityChecker(), null);
    }

    @Override
    protected ArticlesQueryBuilder copy() {
        ArticlesQueryBuilder copy = super.copy();

        copy.archive = archive;
        copy.starred = starred;
        copy.sortCriterion = sortCriterion;
        copy.sortOrder = sortOrder;
        copy.detailLevel = detailLevel;
        tagsBuilder.copyTags(copy.tagsBuilder);
        copy.since = since;
        copy.isPublic = isPublic;

        return copy;
    }

    @Override
    protected ArticlesQueryBuilder createCopyObject() {
        return new ArticlesQueryBuilder(wallabagService);
    }

    @Override
    protected NotFoundPolicy.AvailabilityChecker getAvailabilityChecker() {
        return CompatibilityHelper::isGetArticlesSupported;
    }

}
