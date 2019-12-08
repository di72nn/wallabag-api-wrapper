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

    protected String term;

    ArticlesSearchBuilder(WallabagService wallabagService) {
        super(wallabagService);
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

    @Override
    public Call<Articles> buildCall() {
        return wallabagService.searchCall(build());
    }

    /**
     * {@inheritDoc}
     *
     * @throws NotFoundException if {@link #page(int)} was set to value {@code > }{@link Articles#pages}
     *                           and the {@code notFoundPolicy} allows it
     */
    @Override
    public Articles execute(NotFoundPolicy notFoundPolicy) throws IOException, UnsuccessfulResponseException {
        return wallabagService.search(build(), notFoundPolicy);
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

    @Override
    protected NotFoundPolicy.AvailabilityChecker getAvailabilityChecker() {
        return CompatibilityHelper::isSearchSupported;
    }

}
