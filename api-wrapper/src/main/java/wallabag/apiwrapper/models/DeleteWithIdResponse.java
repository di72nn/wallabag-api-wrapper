package wallabag.apiwrapper.models;

import wallabag.apiwrapper.WallabagService;

/**
 * The {@code DeleteWithIdResponse} represents the response to
 * the {@link WallabagService#deleteArticleWithIdCall(int)} call.
 */
public class DeleteWithIdResponse {

    /** The ID of the deleted article. */
    public Integer id;

    @Override
    public String toString() {
        return "DeleteWithIdResponse{" +
                "id=" + id +
                '}';
    }

}
