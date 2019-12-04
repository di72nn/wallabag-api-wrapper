package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Article;
import wallabag.apiwrapper.models.Articles;
import retrofit2.Call;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static wallabag.apiwrapper.Utils.isEmpty;
import static wallabag.apiwrapper.Utils.positiveNumber;

/**
 * The {@code ArticlesQueryBuilder} class represents a builder for accumulating parameters
 * for querying for articles.
 * <p>Objects of this class can be reused for making queries with different parameters.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ArticlesQueryBuilder extends AbstractTagsBuilder<ArticlesQueryBuilder> {

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

    private final WallabagService wallabagService;

    protected Boolean archive;
    protected Boolean starred;
    protected SortCriterion sortCriterion;
    protected SortOrder sortOrder;
    protected int page = 1;
    protected int perPage = 30;
    protected long since = 0;
    protected Boolean isPublic;

    ArticlesQueryBuilder(WallabagService wallabagService) {
        this.wallabagService = wallabagService;
    }

    @Override
    protected ArticlesQueryBuilder self() {
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
     * Sets the number of the page to request from the server, returns this builder.
     * <p>1-based indexing. Defaults to {@code 1}.
     *
     * @param page the number of the page to request
     * @return this builder
     * @throws IllegalArgumentException if {@code page <= 0}
     */
    public ArticlesQueryBuilder page(int page) {
        this.page = positiveNumber(page, "page");
        return this;
    }

    /**
     * Sets the number of articles per page to request from the server, returns this builder.
     * <p>Defaults to {@code 30}.
     *
     * @param perPage the number of articles per page to request
     * @return this builder
     * @throws IllegalArgumentException if {@code perPage <= 0}
     */
    public ArticlesQueryBuilder perPage(int perPage) {
        this.perPage = positiveNumber(perPage, "perPage");
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

    protected Map<String, String> build() {
        Map<String, String> parameters = new HashMap<>();

        addParameter(parameters, "archive", Utils.booleanToNullableNumberString(archive));
        addParameter(parameters, "starred", Utils.booleanToNullableNumberString(starred));
        parameters.put("sort", getSortCriterionString());
        parameters.put("order", getOrderString());
        parameters.put("page", String.valueOf(page));
        parameters.put("perPage", String.valueOf(perPage));
        addParameter(parameters, "tags", getTagsString());
        parameters.put("since", String.valueOf(since / 1000));
        addParameter(parameters, "public", Utils.booleanToNullableNumberString(isPublic));

        return parameters;
    }

    protected void addParameter(Map<String, String> parameters, String paramName, String paramValue) {
        if (!isEmpty(paramValue)) parameters.put(paramName, paramValue);
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #execute()} method is a shortcut that executes this call.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Articles> buildCall() {
        return wallabagService.getArticlesCall(build());
    }

    /**
     * Returns an {@link Articles} object that is the result of a query
     * with the parameters provided by this builder.
     *
     * @return an {@link Articles} object
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if {@link #page(int)} was set to value {@code > }{@link Articles#pages}
     */
    public Articles execute() throws IOException, UnsuccessfulResponseException {
        return wallabagService.getArticles(build());
    }

    /**
     * Returns an {@link ArticleIterator} for iterating over all {@link Article}s
     * returned for the parameters provided by this builder.
     * <p>The returned iterator handles {@link NotFoundException} as empty internally.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @return an {@link ArticleIterator} object
     */
    public ArticleIterator articleIterator() {
        return articleIterator(true);
    }

    /**
     * Returns an {@link ArticleIterator} for iterating over all {@link Article}s
     * returned for the parameters provided by this builder.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @param notFoundAsEmpty whether to handle {@link NotFoundException} as empty internally
     * @return an {@link ArticleIterator} object
     */
    public ArticleIterator articleIterator(boolean notFoundAsEmpty) {
        return new ArticleIterator(copy(), notFoundAsEmpty);
    }

    /**
     * Returns an {@link ArticlesPageIterator} for iterating over {@link Articles}
     * returned for the parameters provided by this builder.
     * <p>The returned iterator handles {@link NotFoundException} as empty internally.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @return an {@link ArticlesPageIterator} object
     */
    public ArticlesPageIterator pageIterator() {
        return pageIterator(true);
    }

    /**
     * Returns an {@link ArticlesPageIterator} for iterating over {@link Articles}
     * returned for the parameters provided by this builder.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @param notFoundAsEmpty whether to handle {@link NotFoundException} as empty internally
     * @return an {@link ArticlesPageIterator} object
     */
    public ArticlesPageIterator pageIterator(boolean notFoundAsEmpty) {
        return new ArticlesPageIterator(copy(), notFoundAsEmpty);
    }

    protected ArticlesQueryBuilder copy() {
        ArticlesQueryBuilder copy = new ArticlesQueryBuilder(wallabagService);

        copy.archive = archive;
        copy.starred = starred;
        copy.sortCriterion = sortCriterion;
        copy.sortOrder = sortOrder;
        copy.page = page;
        copy.perPage = perPage;
        copyTags(copy);
        copy.since = since;
        copy.isPublic = isPublic;

        return copy;
    }

}
