package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Article;
import wallabag.apiwrapper.models.Articles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static wallabag.apiwrapper.Utils.isEmpty;
import static wallabag.apiwrapper.Utils.positiveNumber;

abstract class GenericPaginatingQueryBuilder<T extends GenericPaginatingQueryBuilder<T>> {

    protected int page = 1;
    protected int perPage = 30;

    protected abstract T self();

    /**
     * Sets the number of the page to request from the server, returns this builder.
     * <p>1-based indexing. Defaults to {@code 1}.
     *
     * @param page the number of the page to request
     * @return this builder
     * @throws IllegalArgumentException if {@code page <= 0}
     */
    public T page(int page) {
        this.page = positiveNumber(page, "page");
        return self();
    }

    /**
     * Sets the number of articles per page to request from the server, returns this builder.
     * <p>Defaults to {@code 30}.
     *
     * @param perPage the number of articles per page to request
     * @return this builder
     * @throws IllegalArgumentException if {@code perPage <= 0}
     */
    public T perPage(int perPage) {
        this.perPage = positiveNumber(perPage, "perPage");
        return self();
    }

    protected Map<String, String> build() {
        Map<String, String> parameters = new HashMap<>();

        addParameter(parameters, "page", String.valueOf(page));
        addParameter(parameters, "perPage", String.valueOf(perPage));

        return parameters;
    }

    protected void addParameter(Map<String, String> parameters, String paramName, String paramValue) {
        if (!isEmpty(paramValue)) parameters.put(paramName, paramValue);
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

    protected T copy() {
        T copy = createCopyObject();

        copy.page = page;
        copy.perPage = perPage;

        return copy;
    }

    protected abstract T createCopyObject();

    protected abstract Articles execute() throws IOException, UnsuccessfulResponseException;

}
