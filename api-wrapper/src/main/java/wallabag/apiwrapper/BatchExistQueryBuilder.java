package wallabag.apiwrapper;

import okhttp3.HttpUrl;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;
import java.util.*;

import static wallabag.apiwrapper.Utils.nonEmptyString;

/**
 * The {@code BatchExistQueryBuilder} class simplifies batch article "exists" calls
 * ({@link WallabagService#articlesExistByUrls(Collection)}, {@link WallabagService#articlesExistByUrlsWithId(Collection)}).
 * <p>The {@code exists} API methods accept GET parameters, which total length is limited
 * (the limit depends on the web server configuration). To work around that limitation
 * this class allows to split the query parameters into batches by calculating the request size.
 * <p>Since version 2.4.0 the wallabag server supports checking for existing articles by SHA-1 hashes of their URLs
 * ({@link CompatibilityHelper#isArticleExistsByHashSupported(WallabagService)}).
 * The builder will try to detect the support of this feature and use it internally.
 * You can explicitly use hash-based check using {@link #addHash(String)}.
 * <p>Objects of this class can be reused for making queries with different parameters.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class BatchExistQueryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BatchExistQueryBuilder.class);

    private static final String PLAIN_URL_QUERY_PARAMETER = "urls[]";
    private static final String HASHED_URL_QUERY_PARAMETER = "hashed_urls[]";

    private final WallabagService wallabagService;

    private final int maxQueryLength;

    @SuppressWarnings("ConstantConditions") // constant URL
    private final HttpUrl.Builder builder = HttpUrl.parse("https://a").newBuilder();

    private final List<String> urls = new ArrayList<>();
    private final List<String> urlHashes = new ArrayList<>();
    private int currentRequestLength;

    private boolean usingPlainUrls;
    private boolean usingHashedUrls;
    private boolean usingHashesExplicitly;

    BatchExistQueryBuilder(WallabagService wallabagService) {
        this(wallabagService, 3990);
    }

    BatchExistQueryBuilder(WallabagService wallabagService, int maxQueryLength) {
        this.wallabagService = wallabagService;
        this.maxQueryLength = maxQueryLength;

        reset();
    }

    /**
     * Resets the added URLs.
     */
    public void reset() {
        urls.clear();
        urlHashes.clear();
        currentRequestLength = wallabagService.getApiBaseURL().length() + "/api/entries/exists.json".length();

        usingPlainUrls = false;
        usingHashedUrls = false;
        usingHashesExplicitly = false;
    }

    /**
     * Adds the specified URL to the current batch, returns {@code true} if the URL was added
     * without exceeding the request size limit.
     * <p>This method will try to automatically detect whether "exists by url hash" is supported
     * and use the proper method accordingly.
     * <p>It is not allowed to use this method and {@link #addHash(String)} in the same batch
     * (you can start using a different one after calling {@link #reset()}).
     *
     * @param url the URL to add
     * @return {@code true} if the URL was added without exceeding the request size limit,
     * {@code false} if the URL was not added
     * @throws IllegalStateException    if {@link #addHash(String)} was called on this builder previously
     * @throws NullPointerException     if the {@code url} is {@code null}
     * @throws IllegalArgumentException if the {@code url} is an empty {@code String}
     */
    public boolean addUrl(String url) {
        nonEmptyString(url, "url");

        boolean plain;
        if (usingPlainUrls || usingHashedUrls) {
            plain = usingPlainUrls;
        } else {
            if (usingHashesExplicitly) {
                throw new IllegalStateException("Can't use \"addUrl()\" in combination with \"addHash()\"");
            }

            plain = !CompatibilityHelper.isArticleExistsByHashSupportedSafe(wallabagService);
        }

        if (add(url, plain ? null : DigestUtils.sha1Hex(url), plain)) {
            usingPlainUrls = plain;
            usingHashedUrls = !plain;
        }

        return false;
    }

    /**
     * Adds the specified URL hash to the current batch, returns {@code true} if the hash was added
     * without exceeding the request size limit.
     * <p>This method makes this builder explicitly use hashes as keys in its return values
     * ({@link #execute()}, {@link #executeWithId()}).
     * <p>It is not allowed to use this method and {@link #addUrl(String)} in the same batch
     * (you can start using a different one after calling {@link #reset()}).
     *
     * @param urlHash the hash of the URL to add
     * @return {@code true} if the hash was added without exceeding the request size limit,
     * {@code false} if the hash was not added
     * @throws IllegalStateException    if {@link #addUrl(String)} was called on this builder previously
     * @throws NullPointerException     if the {@code urlHash} is {@code null}
     * @throws IllegalArgumentException if the {@code urlHash} is an empty {@code String}
     */
    public boolean addHash(String urlHash) {
        nonEmptyString(urlHash, "urlHash");

        if (usingPlainUrls || usingHashedUrls) {
            throw new IllegalStateException("Can't use \"addHash()\" in combination with \"addUrl()\"");
        }

        if (add(null, urlHash, false)) {
            usingHashesExplicitly = true;
            return true;
        }

        return false;
    }

    protected boolean add(String url, String urlHash, boolean plain) {
        int parameterLength = calculateParameterLength(plain ? url : urlHash, plain);
        if (currentRequestLength + parameterLength <= maxQueryLength) {
            if (url != null) urls.add(url);
            if (urlHash != null) urlHashes.add(urlHash);
            currentRequestLength += parameterLength;

            return true;
        }

        return false;
    }

    protected int calculateParameterLength(String value, boolean plain) {
        builder.removeAllQueryParameters(PLAIN_URL_QUERY_PARAMETER);
        builder.removeAllQueryParameters(HASHED_URL_QUERY_PARAMETER);

        String query = builder
                .setQueryParameter(plain ? PLAIN_URL_QUERY_PARAMETER : HASHED_URL_QUERY_PARAMETER, value)
                .build()
                .encodedQuery();

        @SuppressWarnings("ConstantConditions") // always non-empty query
        int parameterLength = query.length() + 1; // +1 for a parameter delimiter

        return parameterLength;
    }

    /**
     * Returns {@code true} if the builder is empty
     * (i.e. no URL or hash has been added since creation or last {@link #reset()} call),
     * {@code false} otherwise.
     *
     * @return {@code true} is the builder is empty
     */
    public boolean isEmpty() {
        return urls.isEmpty() && urlHashes.isEmpty();
    }

    /**
     * Returns {@code true} if this builder is using hashes for the "exists" check.
     * Hashes are used if either the support of hashes was auto-detected
     * or a hash was explicitly added with {@link #addHash(String)}.
     * <p>The use of hashes is determined only after anything was added to the builder,
     * so calling this method on an empty builder is not allowed ({@link #isEmpty()}).
     *
     * @return {@code true} if this builder is using hashes for the "exists" check
     * @throws IllegalStateException if the builder is empty
     */
    public boolean isUsingHashes() {
        if (isEmpty()) {
            throw new IllegalStateException("The builder is empty!");
        }
        return isUsingHashesInternal();
    }

    protected boolean isUsingHashesInternal() {
        return usingHashedUrls || usingHashesExplicitly;
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * It is not allowed to use this method if URL hashes were explicitly added ({@link #addHash(String)}),
     * in which case use {@link #buildAdaptiveCall()}.
     *
     * @return a {@link Call} that is represented by this builder
     * @throws IllegalStateException if hashes were added to the builder
     */
    public Call<Map<String, Boolean>> buildCall() {
        if (usingHashesExplicitly) {
            throw new IllegalStateException("Hashes were added to the builder");
        }
        return wallabagService.articlesExistByUrlsCall(urls);
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #executeWithId()} method is a shortcut that executes this call.
     * It is not allowed to use this method if URL hashes were explicitly added ({@link #addHash(String)}),
     * in which case use {@link #buildAdaptiveCallWithId()}.
     *
     * @return a {@link Call} that is represented by this builder
     * @throws IllegalStateException if hashes were added to the builder
     */
    public Call<Map<String, Integer>> buildCallWithId() {
        if (usingHashesExplicitly) {
            throw new IllegalStateException("Hashes were added to the builder");
        }
        return wallabagService.articlesExistByUrlsWithIdCall(urls);
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The builder uses URL hashes as keys if possible,
     * use {@link #isUsingHashes()} to determine whether the keys of the result map are URLs or hashes.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Map<String, Boolean>> buildAdaptiveCall() {
        return isUsingHashesInternal()
                ? wallabagService.articlesExistByHashesCall(urlHashes)
                : wallabagService.articlesExistByUrlsCall(urls);
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The builder uses URL hashes as keys if possible,
     * use {@link #isUsingHashes()} to determine whether the keys of the result map are URLs or hashes.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Map<String, Integer>> buildAdaptiveCallWithId() {
        return isUsingHashesInternal()
                ? wallabagService.articlesExistByHashesWithIdCall(urlHashes)
                : wallabagService.articlesExistByUrlsWithIdCall(urls);
    }

    /**
     * Performs the request and returns a {@code Map<String, Boolean>} with the results.
     * See {@link WallabagService#articlesExistByUrls(Collection)} for details.
     * <p>If {@link #addHash(String)} was used, the keys of the result map are the URL hashes.
     *
     * @return a {@code Map<String, Boolean>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Boolean> execute() throws IOException, UnsuccessfulResponseException {
        Map<String, Boolean> map = isUsingHashesInternal()
                ? wallabagService.articlesExistByHashes(urlHashes)
                : wallabagService.articlesExistByUrls(urls);

        if (usingHashedUrls) map = remap(map, new HashMap<>(), Boolean.FALSE);

        return map;
    }

    /**
     * Performs the request and returns a {@code Map<String, Integer>} with the results.
     * See {@link WallabagService#articlesExistByUrlsWithId(Collection)} for details.
     * <p>If {@link #addHash(String)} was used, the keys of the result map are the URL hashes.
     *
     * @return a {@code Map<String, Integer>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Integer> executeWithId() throws IOException, UnsuccessfulResponseException {
        Map<String, Integer> map = isUsingHashesInternal()
                ? wallabagService.articlesExistByHashesWithId(urlHashes)
                : wallabagService.articlesExistByUrlsWithId(urls);

        if (usingHashedUrls) map = remap(map, new HashMap<>(), null);

        return map;
    }

    protected <T> Map<String, T> remap(Map<String, T> src, Map<String, T> dst, T defaultValue) {
        for (String url : urls) {
            String hash = DigestUtils.sha1Hex(url);
            T value = src.get(hash);
            if (value == null) {
                if (!src.containsKey(hash)) {
                    LOG.warn("remap() no value for {}", url);
                }
                value = defaultValue;
            }
            dst.put(url, value);
        }

        return dst;
    }

}
