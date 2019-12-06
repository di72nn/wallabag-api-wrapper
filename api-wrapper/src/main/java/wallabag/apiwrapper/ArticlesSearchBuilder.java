package wallabag.apiwrapper;

import retrofit2.Call;
import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Articles;

import java.io.IOException;
import java.util.Map;

/**
 * The {@code ArticlesSearchBuilder} class represents a builder for accumulating parameters
 * for doing search querying for articles.
 * <p>Objects of this class can be reused for making queries with different parameters.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ArticlesSearchBuilder extends GenericPaginatingQueryBuilder<ArticlesSearchBuilder> {

    private final WallabagService wallabagService;

    protected String term;

    ArticlesSearchBuilder(WallabagService wallabagService) {
        this.wallabagService = wallabagService;
    }

    @Override
    protected ArticlesSearchBuilder self() {
        return this;
    }

    /**
     * Sets the search term to this builder, returns the builder.
     *
     * @param term the search term to set, {@code null}able
     * @return this builder
     */
    public ArticlesSearchBuilder term(String term) {
        this.term = term;
        return this;
    }

    @Override
    protected Map<String, String> build() {
        Map<String, String> parameters = super.build();

        addParameter(parameters, "term", term);

        return parameters;
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #execute()} method is a shortcut that executes this call.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Articles> buildCall() {
        return wallabagService.searchCall(build());
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
    @Override
    public Articles execute() throws IOException, UnsuccessfulResponseException {
        return wallabagService.search(build());
    }

    @Override
    protected ArticlesSearchBuilder copy() {
        ArticlesSearchBuilder copy = super.copy();

        copy.term = term;

        return copy;
    }

    @Override
    protected ArticlesSearchBuilder createCopyObject() {
        return new ArticlesSearchBuilder(wallabagService);
    }

}
