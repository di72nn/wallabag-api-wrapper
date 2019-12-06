package wallabag.apiwrapper;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.Article;

import java.io.IOException;
import java.util.Collection;

import static wallabag.apiwrapper.Utils.nonNegativeNumber;

/**
 * The {@code ModifyArticleBuilder} class represents a builder for accumulating parameters
 * for updating existing article entries.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ModifyArticleBuilder extends AbstractArticleBuilder<ModifyArticleBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyArticleBuilder.class);

    protected final WallabagService wallabagService;

    protected final int id;

    ModifyArticleBuilder(WallabagService wallabagService, int id) {
        this.wallabagService = wallabagService;
        this.id = nonNegativeNumber(id, "id");
    }

    @Override
    protected ModifyArticleBuilder self() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>If any tags are added to this builder, they will overwrite article's tags.
     * Server versions prior to 2.3.0 only add new tags.
     */
    @Override
    public ModifyArticleBuilder tag(String tag) {
        return super.tag(tag);
    }

    /**
     * {@inheritDoc}
     * <p>If any tags are added to this builder, they will overwrite article's tags.
     * Server versions prior to 2.3.0 only add new tags.
     */
    @Override
    public ModifyArticleBuilder tags(Collection<String> tags) {
        return super.tags(tags);
    }

    protected RequestBody build() {
        FormBody formBody = populateFormBodyBuilder(new FormBody.Builder()).build();

        if (formBody.size() == 0) {
            throw new IllegalStateException("No changes done");
        }

        return formBody;
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #execute()} method is a shortcut that executes this call.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Article> buildCall() {
        return wallabagService.modifyArticleCall(id, build());
    }

    /**
     * Performs the modification and returns an {@link Article} object
     * corresponding to the modified article entry
     * or {@code null} if the server responded with "not found".
     *
     * @return an {@link Article} object or {@code null} if the server responded with "not found"
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     */
    public Article execute() throws IOException, UnsuccessfulResponseException {
        return execute(true);
    }

    /**
     * Performs the modification and returns an {@link Article} object
     * corresponding to the modified article entry.
     * This method returns {@code null} if the server responded with "not found",
     * but the {@code ignoreNotFound} flag was set to {@code true}.
     *
     * @param nullIfNotFound flag indicating whether to return {@code null}
     *                       instead of throwing {@link NotFoundException}
     * @return an {@link Article} object or {@code null} if the server responded with "not found",
     * but the {@code ignoreNotFound} flag was set to {@code true}
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       and {@code nullIfNotFound} was not set to {@code true}
     */
    public Article execute(boolean nullIfNotFound) throws IOException, UnsuccessfulResponseException {
        try {
            return wallabagService.modifyArticle(id, build());
        } catch (NotFoundException nfe) {
            if (!nullIfNotFound) {
                throw nfe;
            }
            LOG.info("execute() returning null instead of throwing NotFoundException");
            LOG.debug("NFE", nfe);
        }
        return null;
    }

}
