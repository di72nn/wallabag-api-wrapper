package wallabag.apiwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Articles;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * The {@code ArticlesPageIterator} class allows for easier iteration over "pages" of data
 * returned as a result for {@link ArticlesQueryBuilder} queries.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ArticlesPageIterator {

    private static final Logger LOG = LoggerFactory.getLogger(ArticlesPageIterator.class);

    private final GenericPaginatingQueryBuilder<?> queryBuilder;
    private final boolean notFoundAsEmpty;

    private int currentPage;

    private Articles articles;
    private boolean lastPageReached;

    ArticlesPageIterator(GenericPaginatingQueryBuilder<?> queryBuilder, boolean notFoundAsEmpty) {
        this.queryBuilder = queryBuilder;
        this.notFoundAsEmpty = notFoundAsEmpty;
        currentPage = queryBuilder.page;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * The iteration ends if:
     * <ul>
     *     <li>
     *         Current page has reached the {@link Articles#pages} value.
     *     </li>
     *     <li>
     *         A {@link NotFoundException} has been thrown (see {@link ArticlesQueryBuilder#execute()}).
     *         This may happen if the number of pages changed during the iteration.
     *         The iteration ends gracefully, if {@code notFoundAsEmpty} set to {@code true} during iterator creation
     *         (see {@link ArticlesQueryBuilder#pageIterator(boolean)}), otherwise the exception is rethrown.
     *     </li>
     * </ul>
     * <p>Implementation note: this method actually fetches the next "page",
     * so a subsequent {@link #next()} call only returns the value.
     *
     * @return {@code true} if the iteration has more elements
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if the requested page was set to value {@code > }{@link Articles#pages}
     *                                       <em>and</em> {@code notFoundAsEmpty} was not set to {@code true}
     */
    public boolean hasNext() throws IOException, UnsuccessfulResponseException {
        if (articles != null) return true;
        if (lastPageReached) return false;

        try {
            articles = queryBuilder.page(currentPage++).execute();
        } catch (NotFoundException nfe) {
            if (!notFoundAsEmpty) {
                throw nfe;
            }

            LOG.debug("Handling NFE as empty", nfe);
            lastPageReached = true;
        }

        if (articles != null) {
            LOG.trace("Page: {}/{}, total articles: {}", articles.page, articles.pages, articles.total);

            if (articles.page == articles.pages) lastPageReached = true;
        } else {
            LOG.trace("articles == null");
        }

        return articles != null;
    }

    /**
     * Returns the next element in the iteration.
     * <p>The checked exceptions thrown by this method are actually the exceptions thrown by {@link #hasNext()}.
     *
     * @return the next element in the iteration
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if the requested page was set to value {@code > }{@link Articles#pages}
     *                                       <em>and</em> {@code notFoundAsEmpty} was not set to {@code true}
     * @throws NoSuchElementException        if the iteration has no more elements
     */
    public Articles next() throws IOException, UnsuccessfulResponseException {
        if (!hasNext()) throw new NoSuchElementException();

        Articles articles = this.articles;
        this.articles = null;
        return articles;
    }

}
