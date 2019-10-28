package wallabag.apiwrapper;

import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import okhttp3.HttpUrl;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static wallabag.apiwrapper.Utils.nonNullValue;

/**
 * The {@code BatchExistQueryBuilder} class simplifies batch article "exists" calls
 * ({@link WallabagService#articlesExist(Collection)}, {@link WallabagService#articlesExistWithId(Collection)}).
 * <p>The {@code exists} API methods accept GET parameters, which total length is limited
 * (the limit depends on the web server configuration). To work around that limitation
 * this class allows to split the query parameters into batches by calculating the request size.
 * <p>Objects of this class can be reused for making queries with different parameters.
 * <p>This class is not thread safe and cannot be shared between threads.
 */
public class BatchExistQueryBuilder {

    private final WallabagService wallabagService;

    private final int maxQueryLength;

    @SuppressWarnings("ConstantConditions") // constant URL
    private final HttpUrl.Builder builder = HttpUrl.parse("https://a").newBuilder();

    private final List<String> urls = new ArrayList<>();
    private int currentRequestLength;

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
        currentRequestLength = wallabagService.getApiBaseURL().length() + "/api/entries/exists.json".length();
    }

    /**
     * Adds the specified URL to the current batch, returns {@code true} if the URL was added
     * without exceeding the request size limit.
     *
     * @param url the URL to add
     * @return {@code true} if the URL was added without exceeding the request size limit,
     * {@code false} if the URL was not added
     */
    public boolean addUrl(String url) {
        nonNullValue(url, "url");

        // TODO: should probably rewrite the calculation
        @SuppressWarnings("ConstantConditions") // always non-empty query
                int parameterLength = builder.setQueryParameter("urls[]", url).build().encodedQuery().length() + 1;
        if (currentRequestLength + parameterLength <= maxQueryLength) {
            urls.add(url);
            currentRequestLength += parameterLength;

            return true;
        }

        return false;
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #execute()} method is a shortcut that executes this call.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Map<String, Boolean>> buildCall() {
        return wallabagService.articlesExistCall(urls);
    }

    /**
     * Returns a {@link Call} that is represented by this builder.
     * The {@link #executeWithId()} method is a shortcut that executes this call.
     *
     * @return a {@link Call} that is represented by this builder
     */
    public Call<Map<String, Integer>> buildCallWithId() {
        return wallabagService.articlesExistWithIdCall(urls);
    }

    /**
     * Performs the request and returns a {@code Map<String, Boolean>} with the results.
     * See {@link WallabagService#articlesExist(Collection)} for details.
     *
     * @return a {@code Map<String, Boolean>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Boolean> execute() throws IOException, UnsuccessfulResponseException {
        return wallabagService.articlesExist(urls);
    }

    /**
     * Performs the request and returns a {@code Map<String, Integer>} with the results.
     * See {@link WallabagService#articlesExistWithId(Collection)} for details.
     *
     * @return a {@code Map<String, Integer>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Integer> executeWithId() throws IOException, UnsuccessfulResponseException {
        return wallabagService.articlesExistWithId(urls);
    }

}
