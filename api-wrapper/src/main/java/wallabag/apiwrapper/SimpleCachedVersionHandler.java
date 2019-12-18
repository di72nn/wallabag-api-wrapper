package wallabag.apiwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The {@code SimpleCachedVersionHandler} class provides a simple {@link CachedVersionHandler} implementation
 * that should suit most needs.
 * <p>In this implementation the version value is cached for no more than 24 hours.
 * An instance of this class should not be passed to multiple {@code WallabagService}s.
 * <p>This class can be extended to redefine some behavior.
 * <p>The implementation is thread-safe, however concurrent method calls may overwrite each other's results
 * (which shouldn't be a problem in case of version caching).
 */
public class SimpleCachedVersionHandler implements CachedVersionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCachedVersionHandler.class);

    /** The cached version value. */
    protected volatile String cachedVersion;
    /** The {@link System#nanoTime()} value saved the last time {@link #cachedVersion} was assigned. */
    protected volatile long nanoTimestamp;

    /**
     * @implSpec
     * <ol>
     *     <li>
     *         If there is a cached version value present, this method calls
     *         the {@link #shouldInvalidate(String, WallabagService)} method.
     *         <ul>
     *             <li>
     *                 If the call returns {@code true}, the {@link #invalidateCachedVersion(WallabagService)}
     *                 is called.
     *             </li>
     *             <li>
     *                 Otherwise the cached value is used.
     *             </li>
     *         </ul>
     *     </li>
     *     <li>
     *         If there is no cached version value present (or the invalidation was just performed),
     *         the {@link #getVersion(WallabagService)} is called.
     *     </li>
     *     <li>
     *         The method returns either a previously or newly cached value.
     *     </li>
     * </ol>
     */
    @Override
    public String getCachedVersion(WallabagService wallabagService) throws IOException, UnsuccessfulResponseException {
        String version = cachedVersion;

        if (version != null) {
            if (shouldInvalidate(version, wallabagService)) {
                LOG.trace("getCachedVersion() invalidating cached version");

                invalidateCachedVersion(wallabagService);
                version = null;
            }
        }

        if (version == null) {
            version = getVersion(wallabagService);
        }

        return version;
    }

    /**
     * @implSpec The method resets values of the {@link #cachedVersion} and {@link #nanoTimestamp} fields
     */
    @Override
    public void resetCachedVersion(WallabagService wallabagService) {
        LOG.trace("resetCachedVersion() resetting cached version");

        cachedVersion = null;
        nanoTimestamp = 0;
    }

    /**
     * Returns {@code true} if the cached version value should be invalidated.
     *
     * @implSpec Returns {@code true} if 24 hours have passed since {@link #nanoTimestamp}
     *
     * @param version         the version in question
     * @param wallabagService the {@code WallabagService} instance that requests a version
     * @return {@code true} if the cached version value should be invalidated
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    @SuppressWarnings("RedundantThrows") // for overriding methods
    protected boolean shouldInvalidate(@SuppressWarnings("unused") String version,
                                       @SuppressWarnings("unused") WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return System.nanoTime() > nanoTimestamp + TimeUnit.DAYS.toNanos(1);
    }

    /**
     * Invalidates the cached version value.
     *
     * @implSpec This method simply calls {@link #resetCachedVersion(WallabagService)}
     *
     * @param wallabagService the {@code WallabagService} instance that requests a version
     */
    protected void invalidateCachedVersion(WallabagService wallabagService) {
        resetCachedVersion(wallabagService);
    }

    /**
     * Retrieves and returns a new server version value.
     *
     * @implSpec This method must not return {@code null}.
     * Stores {@link WallabagService#getVersion()} result to {@link #cachedVersion},
     * stores {@link System#nanoTime()} result to {@link #nanoTimestamp},
     * returns the new version value.
     *
     * @param wallabagService the {@code WallabagService} instance that requests a version
     * @return a new server version value
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    protected String getVersion(WallabagService wallabagService) throws IOException, UnsuccessfulResponseException {
        String version = wallabagService.getVersion();

        cachedVersion = version;
        nanoTimestamp = System.nanoTime();

        LOG.trace("getVersion() got a version: {}", version);

        return version;
    }

}
