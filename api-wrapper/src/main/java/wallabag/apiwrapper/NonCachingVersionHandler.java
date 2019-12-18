package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;

/**
 * The {@code NonCachingVersionHandler} is a non-caching implementation of {@link CachedVersionHandler}.
 * That is, the {@link #getCachedVersion(WallabagService)} method in this class
 * always requests a version from the server.
 */
public class NonCachingVersionHandler implements CachedVersionHandler {

    /**
     * Returns a server version. This implementation simply calls {@link WallabagService#getVersion()}.
     */
    @Override
    public String getCachedVersion(WallabagService wallabagService) throws IOException, UnsuccessfulResponseException {
        return wallabagService.getVersion();
    }

    /**
     * This method does nothing.
     */
    @Override
    public void resetCachedVersion(WallabagService wallabagService) {}

}
