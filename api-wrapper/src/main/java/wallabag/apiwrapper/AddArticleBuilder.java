package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Article;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import java.io.IOException;

import static wallabag.apiwrapper.Utils.nonEmptyString;

/**
 * The {@code AddArticleBuilder} class represents a builder for accumulating parameters
 * for submitting a new article entry.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class AddArticleBuilder extends AbstractArticleBuilder<AddArticleBuilder> {

    protected final WallabagService wallabagService;

    protected final String url;

    AddArticleBuilder(WallabagService wallabagService, String url) {
        this.wallabagService = wallabagService;
        this.url = nonEmptyString(url, "url");
    }

    @Override
    protected AddArticleBuilder self() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>Since 2.3 server supports adding articles with {@link #content(String)} provided directly
     * (without server-side fetching). In this case it is better to also provide {@link #title(String)}
     * otherwise it will be derived from the URL. See {@link CompatibilityHelper#isAddArticleWithContentSupported(String)}.
     */
    @Override
    public AddArticleBuilder content(String content) {
        return super.content(content);
    }

    protected RequestBody build() {
        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("url", url);

        return populateFormBodyBuilder(bodyBuilder).build();
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #execute()} method is a shortcut that executes this call.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Article> buildCall() {
        return wallabagService.addArticleCall(build());
    }

    /**
     * Returns an {@link Article} object that corresponds to a server-side entry
     * for the URL specified during the creation of this {@code AddArticleBuilder}.
     * <p>If an article with the specified URL was already present on the server,
     * it <i>may</i> be returned. If the URL has redirects, the server will create a new entry.
     *
     * @return an {@link Article} object
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     */
    public Article execute() throws IOException, UnsuccessfulResponseException {
        return wallabagService.addArticle(build());
    }

}
