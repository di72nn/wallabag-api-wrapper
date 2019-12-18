package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;

/**
 * The {@code CachedVersionHandler} interface provides a way to customize version caching behavior
 * for the {@link WallabagService#getCachedVersion()} and {@link WallabagService#resetCachedVersion()} methods.
 * <p>There are two provided implementations: {@link SimpleCachedVersionHandler} and {@link NonCachingVersionHandler}.
 */
public interface CachedVersionHandler {

    /**
     * Returns a cached server version (in the same format as {@link WallabagService#getVersion()}).
     * @implSpec This method must not return {@code null}
     * @implNote Implementation is free to redefine the meaning of this operation (e.g. never cache the value)
     *
     * @param wallabagService the {@code WallabagService} instance that requests a version
     * @return a server version
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    String getCachedVersion(WallabagService wallabagService) throws IOException, UnsuccessfulResponseException;

    /**
     * Resets the cached server version so the next call to {@link #getCachedVersion(WallabagService)}
     * fetches and returns a new value.
     * @implNote Implementation is free to redefine the meaning of this operation.
     * This method is only called by {@link WallabagService#resetCachedVersion()}
     * (unless an implementing class also uses it internally).
     *
     * @param wallabagService the {@code WallabagService} instance that requests the reset
     */
    void resetCachedVersion(WallabagService wallabagService);

}
