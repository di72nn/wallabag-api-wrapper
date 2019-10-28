package wallabag.apiwrapper.models;

import wallabag.apiwrapper.WallabagService;

/**
 * The {@code ExistsResponse} class represents the response to
 * the {@link WallabagService#articleExistsCall(String)} call.
 */
public class ExistsResponse {

    /** The flag that indicates whether the article exists. */
    public boolean exists;

    @Override
    public String toString() {
        return "ExistsResponse{" +
                "exists=" + exists +
                '}';
    }

}
