package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.NotFoundException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import java.io.IOException;
import java.util.Collection;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonNegativeNumber;

/**
 * The {@code ModifyArticleBuilder} class represents a builder for accumulating parameters
 * for updating existing article entries.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class ModifyArticleBuilder extends AbstractArticleBuilder<ModifyArticleBuilder> {

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
     * corresponding to the modified article entry.
     *
     * @return an {@link Article} object
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     */
    public Article execute() throws IOException, UnsuccessfulResponseException {
        return wallabagService.modifyArticle(id, build());
    }

}
