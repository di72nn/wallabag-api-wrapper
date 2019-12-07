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
 * <p>{@link NotFoundException} handling varies depending on the provided {@link NotFoundPolicy}:
 * <ul>
 *     <li>
 *         {@link NotFoundPolicy#THROW} rethrows any exceptions.
 *     </li>
 *     <li>
 *         {@link NotFoundPolicy#DEFAULT_VALUE} consumes all {@code NotFoundException}s
 *         and stops iteration gracefully (not recommended, since exceptions may have different meanings).
 *     </li>
 *     <li>
 *         {@link NotFoundPolicy#SMART} tries to distinguish different causes
 *         for {@code NotFoundException}s: if the policy's test completes successfully,
 *         the exception is treated as an indication that there's no more data and the iteration stops gracefully,
 *         otherwise the exception is rethrown.
 *     </li>
 * </ul>
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ArticlesPageIterator {

    private static final Logger LOG = LoggerFactory.getLogger(ArticlesPageIterator.class);

    private final GenericPaginatingQueryBuilder<?> queryBuilder;
    private final NotFoundPolicy notFoundPolicy;

    private int currentPage;

    private Articles articles;
    private boolean lastPageReached;

    ArticlesPageIterator(GenericPaginatingQueryBuilder<?> queryBuilder, NotFoundPolicy notFoundPolicy) {
        this.queryBuilder = queryBuilder;
        this.notFoundPolicy = Utils.nonNullValue(notFoundPolicy, "notFoundPolicy");
        currentPage = queryBuilder.getPage();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * The iteration ends when:
     * <ul>
     *     <li>
     *         Current page has reached the {@link Articles#pages} value.
     *     </li>
     *     <li>
     *         A {@link NotFoundException} has been thrown (see {@link ArticlesQueryBuilder#execute()}).
     *         This may happen if the number of pages changed during the iteration.
     *         See the class description ({@link ArticlesPageIterator})
     *         for details regarding different {@link NotFoundPolicy}s.
     *     </li>
     * </ul>
     * <p>Implementation note: this method actually fetches the next "page",
     * so a subsequent {@link #next()} call only returns the value.
     *
     * @return {@code true} if the iteration has more elements
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if the requested page was set to value {@code > }{@link Articles#pages}
     *                                       <em>and</em> the used {@code NotFoundPolicy} rethrows exceptions
     */
    public boolean hasNext() throws IOException, UnsuccessfulResponseException {
        if (articles != null) return true;
        if (lastPageReached) return false;

        try {
            articles = queryBuilder.page(currentPage++).execute(NotFoundPolicy.THROW);
        } catch (NotFoundException nfe) {
            notFoundPolicy.handle(nfe, queryBuilder.getWallabagService());

            LOG.info("Handling NFE as empty");
            LOG.debug("NFE", nfe);
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
     *                                       <em>and</em> the used {@code NotFoundPolicy} rethrows exceptions
     * @throws NoSuchElementException        if the iteration has no more elements
     */
    public Articles next() throws IOException, UnsuccessfulResponseException {
        if (!hasNext()) throw new NoSuchElementException();

        Articles articles = this.articles;
        this.articles = null;
        return articles;
    }

}
