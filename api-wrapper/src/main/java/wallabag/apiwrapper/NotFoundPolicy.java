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
        void handle(NotFoundException nfe, WallabagService service, AvailabilityChecker availabilityChecker) {
            LOG.info("Default value policy: consuming NFE");
            LOG.debug("Default value policy: consumed NFE", nfe);
        }
    },

    /**
     * Similar to {@link #DEFAULT_VALUE}, but tries to distinguish different causes
     * for {@code NotFoundException}s: this policy tries to swallow only "data-related" {@code NotFoundException}s
     * (e.g. a referenced entity doesn't exist), and rethrow any service or API method related ones
     * (e.g. the server is not found, incorrect configuration).
     * <p>This is the recommended policy. However, the policy works on a "best effort" basis
     * since the performed tests cannot guarantee 100% correct result.
     * <p>Algorithm outline:
     * <ol>
     *     <li>
     *         First, this policy runs a generic test query against the server
     *         {@link WallabagService#testServerAccessibility()}.
     *         <ul>
     *             <li>
     *                 If the query fails with a {@code NotFoundException}
     *                 (which means that the service cannot be found;
     *                 may be caused by an incorrect URL, server misconfiguration),
     *                 the original {@code NotFoundException} is rethrown.
     *             </li>
     *             <li>
     *                 If the query fails with another exception (unknown request error or I/O exception;
     *                 may also be caused by an incorrect URL, server misconfiguration),
     *                 that new exception is thrown.
     *             </li>
     *             <li>
     *                 Otherwise, the policy proceeds to the next step.
     *             </li>
     *         </ul>
     *     </li>
     *     <li>
     *         At this point the policy tries to determine whether the used API method is available.
     *         To achieve that, a corresponding {@link CompatibilityHelper} method is invoked.
     *         <ul>
     *             <li>
     *                 If it is concluded, that the API method is not available,
     *                 the original {@code NotFoundException} is rethrown.
     *             </li>
     *             <li>
     *                 Otherwise, the original {@code NotFoundException} is treated as a data-specific "not found"
     *                 (e.g. a referenced entity doesn't exist), in which case the exception is consumed
     *                 similarly to {@link #DEFAULT_VALUE}, a default value is returned.
     *             </li>
     *         </ul>
     *     </li>
     * </ol>
     */
    SMART {
        @Override
        void handle(NotFoundException originalNfe, WallabagService service, AvailabilityChecker availabilityChecker)
                throws IOException, UnsuccessfulResponseException {
            LOG.debug("Smart policy: checking that the server is available");
            try {
                service.testServerAccessibility();
            } catch (NotFoundException nfe) {
                LOG.info("Smart policy: the server is actually not found");
                LOG.debug("Smart policy: NFE during test", nfe);
                throw originalNfe;
            } catch (IOException | UnsuccessfulResponseException e) {
                LOG.warn("Smart policy: unexpected exception during test, rethrowing", e);
                throw e;
            }

            if (availabilityChecker != null) {
                LOG.info("Smart policy: the server is available, checking method availability");
                if (availabilityChecker.isAvailable(service)) {
                    LOG.info("Smart policy: method is available, ignoring NFE");
                } else {
                    LOG.info("Smart policy: method is not available, rethrowing original NFE");
                    throw originalNfe;
                }
            } else {
                LOG.info("Smart policy: the server is available, ignoring NFE");
            }
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(NotFoundPolicy.class);

    void handle(NotFoundException nfe, WallabagService service, AvailabilityChecker availabilityChecker)
            throws IOException, UnsuccessfulResponseException {
        throw nfe;
    }

    interface AvailabilityChecker {
        boolean isAvailable(WallabagService wallabagService) throws IOException, UnsuccessfulResponseException;
    }

    interface Callable<T> {
        T run() throws IOException, UnsuccessfulResponseException;
    }

    <T> T call(Callable<T> callable, WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return call(callable, wallabagService, null);
    }

    <T> T call(Callable<T> callable, WallabagService wallabagService, T defaultValue)
            throws IOException, UnsuccessfulResponseException {
        return call(callable, wallabagService, null, defaultValue);
    }

    <T> T call(Callable<T> callable, WallabagService wallabagService,
               AvailabilityChecker availabilityChecker, T defaultValue)
            throws IOException, UnsuccessfulResponseException {
        try {
            return callable.run();
        } catch (NotFoundException nfe) {
            handle(nfe, wallabagService, availabilityChecker);

            LOG.info("NotFoundPolicy.call() returning {} instead of throwing NotFoundException", defaultValue);
            LOG.debug("NFE", nfe);
        }
        return defaultValue;
    }

}
