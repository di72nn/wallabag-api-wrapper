package wallabag.apiwrapper;

import retrofit2.Call;
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

    protected final WallabagService wallabagService;

    protected int page = 1;
    protected int perPage = 30;

    protected GenericPaginatingQueryBuilder(WallabagService wallabagService) {
        this.wallabagService = wallabagService;
    }

    protected abstract T self();

    WallabagService getWallabagService() {
        return wallabagService;
    }

    int getPage() {
        return page;
    }

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
     * <p>The returned iterator uses {@link NotFoundPolicy#SMART}.
     * See {@link ArticlesPageIterator} for details regarding {@link NotFoundPolicy}s.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @return an {@link ArticleIterator} object
     */
    public ArticleIterator articleIterator() {
        return articleIterator(NotFoundPolicy.SMART);
    }

    /**
     * Returns an {@link ArticleIterator} for iterating over all {@link Article}s
     * returned for the parameters provided by this builder.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     * <p>See {@link ArticlesPageIterator} for details regarding {@link NotFoundPolicy}s.
     *
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link ArticleIterator} object
     * @throws NullPointerException if the {@code notFoundPolicy} is {@code null}
     */
    public ArticleIterator articleIterator(NotFoundPolicy notFoundPolicy) {
        return new ArticleIterator(copy(), notFoundPolicy);
    }

    /**
     * Returns an {@link ArticlesPageIterator} for iterating over {@link Articles}
     * returned for the parameters provided by this builder.
     * <p>The returned iterator uses {@link NotFoundPolicy#SMART}.
     * See {@link ArticlesPageIterator} for details regarding {@link NotFoundPolicy}s.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @return an {@link ArticlesPageIterator} object
     */
    public ArticlesPageIterator pageIterator() {
        return pageIterator(NotFoundPolicy.SMART);
    }

    /**
     * Returns an {@link ArticlesPageIterator} for iterating over {@link Articles}
     * returned for the parameters provided by this builder.
     * <p>See {@link ArticlesPageIterator} for details regarding {@link NotFoundPolicy}s.
     * <p>The iteration starts from the page set with {@link #page(int)}.
     *
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link ArticlesPageIterator} object
     * @throws NullPointerException if the {@code notFoundPolicy} is {@code null}
     */
    public ArticlesPageIterator pageIterator(NotFoundPolicy notFoundPolicy) {
        return new ArticlesPageIterator(copy(), notFoundPolicy);
    }

    protected T copy() {
        T copy = createCopyObject();

        copy.page = page;
        copy.perPage = perPage;

        return copy;
    }

    protected abstract T createCopyObject();

    /**
     * Returns a {@link Call} that is represented by this builder.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public abstract Call<Articles> buildCall();

    /**
     * Returns an {@link Articles} object that is the result of a query
     * with the parameters provided by this builder or {@code null}
     * if {@link #page(int)} was set to a value {@code > }{@link Articles#pages}.
     * <p>This method is an alias for {@link #execute(NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @return an {@link Articles} object or {@code null}
     * if {@link #page(int)} was set to a value {@code > }{@link Articles#pages}
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     */
    public Articles execute() throws IOException, UnsuccessfulResponseException {
        return execute(NotFoundPolicy.SMART);
    }

    /**
     * Returns an {@link Articles} object that is the result of a query
     * with the parameters provided by this builder
     * or {@code null} if {@link #page(int)} was set to a value {@code > }{@link Articles#pages}
     * (depends on the {@code notFoundPolicy}).
     *
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Articles} object or {@code null}
     * if {@link #page(int)} was set to a value {@code > }{@link Articles#pages} (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if {@link #page(int)} was set to a value {@code > }{@link Articles#pages}
     *                                       (depends on the {@code notFoundPolicy})
     */
    public abstract Articles execute(NotFoundPolicy notFoundPolicy) throws IOException, UnsuccessfulResponseException;

}
