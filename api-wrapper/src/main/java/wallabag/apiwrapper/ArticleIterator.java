package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Article;
import wallabag.apiwrapper.models.Articles;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The {@code ArticleIterator} class allows for easier iteration over all articles
 * returned as a result for {@link ArticlesQueryBuilder} queries.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ArticleIterator {

    protected ArticlesPageIterator pageIterator;
    protected Iterator<Article> articles;

    ArticleIterator(GenericPaginatingQueryBuilder<?> queryBuilder, boolean notFoundAsEmpty) {
        pageIterator = new ArticlesPageIterator(queryBuilder, notFoundAsEmpty);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * <p>See {@link ArticlesPageIterator#hasNext()} regarding {@code NotFoundException}.
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
        if (articles != null && articles.hasNext()) return true;
        if (!pageIterator.hasNext()) return false;

        articles = pageIterator.next().embedded.items.iterator();

        return articles.hasNext();
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
    public Article next() throws IOException, UnsuccessfulResponseException {
        if (!hasNext()) throw new NoSuchElementException();

        Article item = articles.next();

        // not necessary; frees resources earlier
        if (!articles.hasNext()) articles = null;

        return item;
    }

}
