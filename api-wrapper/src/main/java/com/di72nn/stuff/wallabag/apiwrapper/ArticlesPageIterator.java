package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.NotFoundException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.Articles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ArticlesPageIterator {

	private static final Logger LOG = LoggerFactory.getLogger(ArticlesPageIterator.class);

	private final ArticlesQueryBuilder queryBuilder;
	private final boolean notFoundAsEmpty;

	private int currentPage = 1;

	private Articles articles;
	private boolean ready;
	private boolean lastPageReached;

	ArticlesPageIterator(ArticlesQueryBuilder articlesQueryBuilder, boolean notFoundAsEmpty) {
		this.queryBuilder = articlesQueryBuilder;
		this.notFoundAsEmpty = notFoundAsEmpty;
	}

	public boolean hasNext() throws IOException, UnsuccessfulResponseException {
		if(ready) return true;
		if(lastPageReached) return false;

		Articles articles;
		try {
			articles = queryBuilder.page(currentPage++).execute();
		} catch(NotFoundException nfe) {
			if(!notFoundAsEmpty) {
				throw nfe;
			}

			LOG.debug("Handling NFE as empty", nfe);
			articles = null;
		}

		this.articles = articles;

		if(articles != null) {
			LOG.trace("Page: {}/{}, total articles: {}", articles.page, articles.pages, articles.total);

			ready = true;
			if(articles.page == articles.pages) lastPageReached = true;
		} else {
			LOG.trace("articles == null");
		}

		ready = articles != null;
		return ready;
	}

	public Articles next() throws IOException, UnsuccessfulResponseException {
		if(!hasNext()) throw new NoSuchElementException();

		ready = false;
		return articles;
	}

}
