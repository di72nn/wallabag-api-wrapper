package wallabag.apiwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;

/**
 * The {@code NotFoundPolicy} enum represents the available behaviors
 * of handling {@code HTTP 404 "not found"} server responses.
 */
public enum NotFoundPolicy {

    /**
     * Rethrows any exception. The client code have to handle all exceptions on its own.
     */
    THROW,

    /**
     * Consumes all {@code NotFoundException}s (not recommended, since exceptions may have different meanings).
     * <p>If this policy is used, instead of throwing {@code NotFoundException}s
     * methods return some kind of default value ({@code null}, {@code false}, etc.)
     * that is usually distinguishable from a normal return value.
     */
    DEFAULT_VALUE {
        @Override
        void handle(NotFoundException nfe, WallabagService service) {}
    },

    /**
     * Similar to {@link #DEFAULT_VALUE}, but tries to distinguish different causes
     * for {@code NotFoundException}s.
     * <p>This policy runs a test query to the server and if the query completes successfully,
     * the exception is treated as a data-specific "not found" (i.e. a referenced entity doesn't exist),
     * in which case the exception is consumed similarly to {@link #DEFAULT_VALUE}.
     * If the test fails, the exception is treated as an indication of the server (or an API method) being not available
     * (incorrect URL, misconfiguration, the specific API method is not available, etc.)
     * in which case the exception is rethrown for the client code to handle.
     * <p>The policy works on a "best effort" basis
     * since the performed test cannot provide 100% guarantee of correctness.
     * <p>The policy <em>does not</em> check the availability of the specific API methods used,
     * so an API method not being available (due to an older server)
     * may be erroneously treated as a data-specific "not found".
     */
    SMART {
        @Override
        void handle(NotFoundException originalNfe, WallabagService service)
                throws IOException, UnsuccessfulResponseException {
            LOG.debug("Smart handler: checking that the server is available");
            try {
                service.testServerAccessibility();
            } catch (NotFoundException nfe) {
                LOG.info("Smart handler: the server is actually not found");
                LOG.debug("Smart handler: NFE during test", nfe);
                throw originalNfe;
            } catch (IOException | UnsuccessfulResponseException e) {
                LOG.warn("Smart handler: unexpected exception during test, rethrowing", e);
                throw e;
            }

            LOG.info("Smart handler: the server is available, ignoring NFE");
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(NotFoundPolicy.class);

    void handle(NotFoundException nfe, WallabagService service)
            throws IOException, UnsuccessfulResponseException {
        throw nfe;
    }

    interface Callable<T> {
        T run() throws IOException, UnsuccessfulResponseException;
    }

    <T> T call(Callable<T> callable, WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return call(callable, null, wallabagService);
    }

    <T> T call(Callable<T> callable, T defaultValue, WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        try {
            return callable.run();
        } catch (NotFoundException nfe) {
            handle(nfe, wallabagService);

            LOG.info("NotFoundPolicy.call() returning {} instead of throwing NotFoundException", defaultValue);
            LOG.debug("NFE", nfe);
        }
        return defaultValue;
    }

}
