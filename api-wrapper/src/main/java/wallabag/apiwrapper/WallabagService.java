package wallabag.apiwrapper;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import wallabag.apiwrapper.exceptions.AuthorizationException;
import wallabag.apiwrapper.exceptions.NotFoundException;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.*;
import wallabag.apiwrapper.models.adapters.NumericBooleanAdapter;
import wallabag.apiwrapper.services.WallabagApiService;

import java.io.IOException;
import java.util.*;

import static wallabag.apiwrapper.Utils.*;

/**
 * The {@code WallabagService} class serves as an entry point
 * for accessing various wallabag API methods.
 *
 * <p>See {@link CompatibilityHelper} for determining which features are available in a particular server version.
 *
 * <p>Most of the methods that perform network requests have a corresponding {@code *Call} methods
 * that allow for a richer API of {@link Call}.
 *
 * <p>Methods marked to throw {@link UnsuccessfulResponseException} (or directly {@link NotFoundException})
 * throw {@link NotFoundException} as a result of 404 HTTP code returned by server.
 * In some cases the "not found" result may be considered as an acceptable result,
 * for example a {@link #deleteArticle(int)} call for an already deleted article.
 * Note however that {@link NotFoundException} is also thrown if the {@code apiBaseURL} is incorrect
 * or the server is not available or misconfigured.
 * Most of the methods use {@link NotFoundPolicy#SMART} by default
 * to throw {@code NotFoundException}s only when necessary.
 * Some other methods accept {@link NotFoundPolicy} that can alter that behavior.
 * <p>Methods marked to throw {@link UnsuccessfulResponseException} (or directly {@link AuthorizationException})
 * throw {@link AuthorizationException} if it was not possible to get access
 * using the provided {@link ParameterHandler}.
 *
 * <p>Unless noted otherwise, any method accepting non-nullable parameters
 * (parameters that are not marked as "nullable" in their description)
 * throws {@code NullPointerException} if the argument is {@code null}
 * or {@code IllegalArgumentException} if the argument is an empty {@code String}.
 * Unless noted otherwise, any method accepting a {@code Collection} parameter
 * throws {@code NullPointerException} if the argument is {@code null}
 * or {@code IllegalArgumentException} if the argument is an empty {@code Collection}.
 * All methods accepting {@code int} IDs throw {@code IllegalArgumentException}
 * if the argument is less than zero.
 *
 * <p>This class is thread safe. For additional thread safety limitations see {@link ParameterHandler} description.
 */
public class WallabagService {

    private final WallabagApiService wallabagApiService;

    private final String apiBaseURL;

    private final CachedVersionHandler cachedVersionHandler;

    /**
     * The {@code ResponseFormat} enum represents the formats available
     * for exporting articles as raw data (e.g. for saving as files).
     */
    public enum ResponseFormat {
        XML, JSON, TXT, CSV, PDF, EPUB, MOBI, HTML;

        String apiValue() {
            return toString().toLowerCase();
        }

    }

    /**
     * Returns an instance of {@code WallabagService}.
     * <p>This method calls {@link #instance(String, ParameterHandler, OkHttpClient, CachedVersionHandler)}
     * with extra parameters being {@code null}.
     * <p>It should be assumed that each call may create a new instance
     * (which may not be a light-weight operation),
     * so it is advisable to store the returned object for repeated API calls.
     *
     * @param apiBaseURL       the URL of the wallabag instance in the form of
     *                         {@code https://wallabag.example.com/subdir-if-any/}.
     *                         If the trailing slash is missing, it will be appended.
     *                         See {@link Retrofit.Builder#baseUrl(String)} for more details
     * @param parameterHandler a {@link ParameterHandler} instance
     * @return an instance of {@code WallabagService}
     */
    public static WallabagService instance(String apiBaseURL, ParameterHandler parameterHandler) {
        return instance(apiBaseURL, parameterHandler, null, null);
    }

    /**
     * Returns an instance of {@code WallabagService}.
     * <p>If {@code cachedVersionHandler} is not provided,
     * a new instance of {@link SimpleCachedVersionHandler} is used.
     * <p>It should be assumed that each call may create a new instance
     * (which may not be a light-weight operation),
     * so it is advisable to store the returned object for repeated API calls.
     *
     * @param apiBaseURL           the URL of the wallabag instance in the form of
     *                             {@code https://wallabag.example.com/subdir-if-any/}.
     *                             If the trailing slash is missing, it will be appended.
     *                             See {@link Retrofit.Builder#baseUrl(String)} for more details
     * @param parameterHandler     a {@link ParameterHandler} instance
     * @param okHttpClient         a {@code null}able {@link OkHttpClient} instance
     * @param cachedVersionHandler a {@code null}able {@link CachedVersionHandler} instance
     * @return an instance of {@code WallabagService}
     */
    public static WallabagService instance(String apiBaseURL, ParameterHandler parameterHandler,
                                           OkHttpClient okHttpClient, CachedVersionHandler cachedVersionHandler) {
        return new WallabagService(apiBaseURL, parameterHandler, okHttpClient, cachedVersionHandler);
    }

    private WallabagService(String apiBaseURL, ParameterHandler parameterHandler,
                            OkHttpClient okHttpClient, CachedVersionHandler cachedVersionHandler) {
        nonEmptyString(apiBaseURL, "apiBaseURL");
        nonNullValue(parameterHandler, "parameterHandler");

        if (!apiBaseURL.endsWith("/")) apiBaseURL += "/";

        this.apiBaseURL = apiBaseURL;

        if (okHttpClient == null) okHttpClient = new OkHttpClient();

        okHttpClient = okHttpClient.newBuilder()
                .addInterceptor(new TokenRefreshingInterceptor(apiBaseURL, okHttpClient, parameterHandler))
                .build();

        wallabagApiService = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(
                        new Moshi.Builder()
                                .add(new NumericBooleanAdapter())
                                .add(Date.class, new Rfc3339DateJsonAdapter().nullSafe())
                                .build()))
                .client(okHttpClient)
                .baseUrl(apiBaseURL)
                .build()
                .create(WallabagApiService.class);

        if (cachedVersionHandler == null) cachedVersionHandler = new SimpleCachedVersionHandler();
        this.cachedVersionHandler = cachedVersionHandler;
    }

    String getApiBaseURL() {
        return apiBaseURL;
    }

    /**
     * Returns an {@link Articles} query builder.
     *
     * @return an {@link Articles} query builder
     */
    public ArticlesQueryBuilder getArticlesBuilder() {
        return new ArticlesQueryBuilder(this);
    }

    /**
     * Returns an {@link Articles} search query builder.
     *
     * @return an {@link Articles} search query builder
     */
    public ArticlesSearchBuilder searchArticlesBuilder() {
        return new ArticlesSearchBuilder(this);
    }

    /**
     * Returns a builder for setting parameters for submitting a new {@link Article} entry.
     *
     * @param url the URL of the article
     * @return a builder for setting parameters for submitting a new {@link Article} entry
     */
    public AddArticleBuilder addArticleBuilder(String url) {
        return new AddArticleBuilder(this, url);
    }

    /**
     * Submits the URL to the server and returns an {@link Article} object
     * that corresponds to a server-side entry for the specified URL.
     *
     * <p>See {@link AddArticleBuilder#execute()} for details.
     * This method is a shortcut for {@link #addArticleBuilder(String)} without any extra parameters.
     *
     * @param url the URL of the article
     * @return an {@link Article} object for the specified {@code url}
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Article addArticle(String url) throws IOException, UnsuccessfulResponseException {
        return addArticleBuilder(url).execute();
    }

    /**
     * Returns a builder for setting parameters for submitting article changes.
     *
     * @param id the ID of the article to modify
     * @return a builder for submitting article changes
     */
    public ModifyArticleBuilder modifyArticleBuilder(int id) {
        return new ModifyArticleBuilder(this, id);
    }

    Call<Articles> getArticlesCall(Map<String, String> parameters) {
        return wallabagApiService.getArticles(parameters);
    }

    Articles getArticles(Map<String, String> parameters, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(getArticlesCall(parameters)), this);
    }

    Call<Articles> searchCall(Map<String, String> parameters) {
        return wallabagApiService.search(parameters);
    }

    Articles search(Map<String, String> parameters, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(searchCall(parameters)), this,
                CompatibilityHelper::isSearchSupported, null);
    }

    Call<Article> addArticleCall(RequestBody requestBody) {
        return wallabagApiService.addArticle(requestBody);
    }

    Article addArticle(RequestBody requestBody) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(addArticleCall(requestBody));
    }

    /**
     * See {@link #reloadArticle(int)}.
     *
     * @param articleID id
     * @return a {@link Call}
     */
    public Call<Article> reloadArticleCall(int articleID) {
        return wallabagApiService.reloadArticle(nonNegativeNumber(articleID, "articleID"));
    }

    /**
     * Performs a server-side "reload" ("refetch") of an article with the specified ID
     * and returns a resulting {@link Article} object.
     * <p>This method is an alias for {@link #reloadArticle(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to reload
     * @return a reloaded article, or {@code null} if the server failed to reload the article
     * or an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Article reloadArticle(int articleID) throws IOException, UnsuccessfulResponseException {
        return reloadArticle(articleID, NotFoundPolicy.SMART);
    }

    /**
     * Performs a server-side "reload" ("refetch") of an article with the specified ID
     * and returns a resulting {@link Article} object.
     * This methods returns {@code null} if the server failed to reload the article
     * or if an article with the specified ID was not found (depends on the {@code notFoundPolicy}).
     *
     * @param articleID      the ID of the article to reload
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return a reloaded article, or {@code null} if the server failed to reload the article
     * or if an article with the specified ID was not found (in accordance with the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if an article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Article reloadArticle(int articleID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        Response<Article> response = reloadArticleCall(articleID).execute();

        if (response.code() == 304) { // couldn't update
            return null;
        }

        return notFoundPolicy.call(() -> checkResponseBody(response), this,
                CompatibilityHelper::isReloadArticleSupported, null);
    }

    /**
     * See {@link #articleExists(String)}.
     *
     * @param url URL
     * @return a {@link Call}
     */
    public Call<ExistsResponse> articleExistsCall(String url) {
        nonEmptyString(url, "URL");

        if (CompatibilityHelper.isArticleExistsByHashSupportedSafe(this)) {
            return wallabagApiService.exists(null, sha1Hex(url));
        } else {
            return wallabagApiService.exists(url, null);
        }
    }

    /**
     * Returns {@code true} if an article with the specified URL is present on the server.
     * This call may produce false-negative results in some cases (redirects).
     * See server documentation for details.
     * <p>This method does auto-detection of the proper endpoint method
     * based on the result of {@link #getCachedVersion()}.
     *
     * @param url the URL to check
     * @return {@code true} if the article exists
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public boolean articleExists(String url) throws IOException, UnsuccessfulResponseException {
        nonEmptyString(url, "URL");

        Call<ExistsResponse> call;
        if (CompatibilityHelper.isArticleExistsByHashSupported(this)) {
            call = wallabagApiService.exists(null, sha1Hex(url));
        } else {
            call = wallabagApiService.exists(url, null);
        }

        return execAndCheckBody(call).exists;
    }

    /**
     * See {@link #articleExistsByUrl(String)}.
     *
     * @param url URL
     * @return a {@link Call}
     */
    public Call<ExistsResponse> articleExistsByUrlCall(String url) {
        return wallabagApiService.exists(nonEmptyString(url, "URL"), null);
    }

    /**
     * Returns {@code true} if an article with the specified URL is present on the server.
     * This call may produce false-negative results in some cases (redirects).
     * See server documentation for details.
     * <p>N.B. This method is present for completeness, the recommended methods are
     * {@link #articleExists(String)} and {@link #articleExistsWithId(String)}.
     *
     * @param url the URL to check
     * @return {@code true} if the article exists
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public boolean articleExistsByUrl(String url) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articleExistsByUrlCall(url)).exists;
    }

    /**
     * See {@link #articleExistsByUrl(String)}.
     *
     * @param urlHash a SHA-1 hash of the URL to check
     * @return a {@link Call}
     */
    public Call<ExistsResponse> articleExistsByHashCall(String urlHash) {
        return wallabagApiService.exists(null, nonEmptyString(urlHash, "urlHash"));
    }

    /**
     * Returns {@code true} if an article with the specified URL hash is present on the server.
     * This call may produce false-negative results in some cases (redirects).
     * See server documentation for details.
     * <p>N.B. This method is present for completeness, the recommended methods are
     * {@link #articleExists(String)} and {@link #articleExistsWithId(String)}.
     *
     * @param urlHash a SHA-1 hash of the URL to check
     * @return {@code true} if the article exists
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public boolean articleExistsByHash(String urlHash) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articleExistsByHashCall(urlHash)).exists;
    }

    /**
     * See {@link #articleExistsWithId(String)}.
     *
     * @param url URL
     * @return a {@link Call}
     */
    public Call<ExistsWithIdResponse> articleExistsWithIdCall(String url) {
        nonEmptyString(url, "URL");

        if (CompatibilityHelper.isArticleExistsByHashSupportedSafe(this)) {
            return wallabagApiService.existsWithId(null, sha1Hex(url), "1");
        } else {
            return wallabagApiService.existsWithId(url, null, "1");
        }
    }

    /**
     * Returns an ID of the article with the specified URL if it is present on the server or {@code null} otherwise.
     * This call may produce false-negative results in some cases (redirects).
     * See server documentation for details.
     * <p>This method does auto-detection of the proper endpoint method
     * based on the result of {@link #getCachedVersion()}.
     *
     * @param url the URL to check
     * @return an ID if the article exists, {@code null} otherwise
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Integer articleExistsWithId(String url) throws IOException, UnsuccessfulResponseException {
        nonEmptyString(url, "URL");

        Call<ExistsWithIdResponse> call;
        if (CompatibilityHelper.isArticleExistsByHashSupported(this)) {
            call = wallabagApiService.existsWithId(null, sha1Hex(url), "1");
        } else {
            call = wallabagApiService.existsWithId(url, null, "1");
        }

        return execAndCheckBody(call).id;
    }

    /**
     * See {@link #articleExistsByUrlWithId(String)}.
     *
     * @param url URL
     * @return a {@link Call}
     */
    public Call<ExistsWithIdResponse> articleExistsByUrlWithIdCall(String url) {
        return wallabagApiService.existsWithId(nonEmptyString(url, "URL"), null, "1");
    }

    /**
     * Returns an ID of the article with the specified URL if it is present on the server or {@code null} otherwise.
     * This call may produce false-negative results in some cases (redirects).
     * See server documentation for details.
     * <p>N.B. This method is present for completeness, the recommended methods are
     * {@link #articleExists(String)} and {@link #articleExistsWithId(String)}.
     *
     * @param url the URL to check
     * @return an ID if the article exists, {@code null} otherwise
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Integer articleExistsByUrlWithId(String url) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articleExistsByUrlWithIdCall(url)).id;
    }

    /**
     * See {@link #articleExistsByHashWithId(String)}.
     *
     * @param urlHash a SHA-1 hash of the URL to check
     * @return a {@link Call}
     */
    public Call<ExistsWithIdResponse> articleExistsByHashWithIdCall(String urlHash) {
        return wallabagApiService.existsWithId(null, nonEmptyString(urlHash, "urlHash"), "1");
    }

    /**
     * Returns an ID of the article with the specified URL hash if it is present on the server or {@code null} otherwise.
     * This call may produce false-negative results in some cases (redirects).
     * See server documentation for details.
     * <p>N.B. This method is present for completeness, the recommended methods are
     * {@link #articleExists(String)} and {@link #articleExistsWithId(String)}.
     *
     * @param urlHash a SHA-1 hash of the URL to check
     * @return an ID if the article exists, {@code null} otherwise
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Integer articleExistsByHashWithId(String urlHash) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articleExistsByHashWithIdCall(urlHash)).id;
    }

    /**
     * See {@link #articlesExistByUrls(Collection)}.
     *
     * @param urls URLs
     * @return a {@link Call}
     */
    public Call<Map<String, Boolean>> articlesExistByUrlsCall(Collection<String> urls) {
        return wallabagApiService.exists(nonEmptyCollection(urls, "urls"), null);
    }

    /**
     * Returns a {@code Map} with the results of a bulk "exists" call.
     * The {@code String} key of the map is the article URL
     * and the {@code Boolean} value is the corresponding result (non-{@code null} value).
     * <p>See {@link #articleExistsByUrl(String)} for details.
     * <p>N.B. This method is present for completeness, the recommended way is to use
     * {@link #getArticlesExistQueryBuilder()}.
     * <p>Warning: this call uses {@code HTTP GET} which may be limited in terms of total arguments' length.
     * Don't use this method for more than a couple of URLs or extra lengthy URLs.
     * If unsure, use {@link #getArticlesExistQueryBuilder()}.
     *
     * @param urls a {@code Collection} of URLs to check
     * @return a {@code Map<String, Boolean>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Boolean> articlesExistByUrls(Collection<String> urls)
            throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articlesExistByUrlsCall(urls));
    }

    /**
     * See {@link #articlesExistByHashes(Collection)}.
     *
     * @param urlHashes SHA-1 hashes of the URLs to check
     * @return a {@link Call}
     */
    public Call<Map<String, Boolean>> articlesExistByHashesCall(Collection<String> urlHashes) {
        return wallabagApiService.exists(null, nonEmptyCollection(urlHashes, "urlHashes"));
    }

    /**
     * Returns a {@code Map} with the results of a bulk "exists" call.
     * The {@code String} key of the map is the article URL hash
     * and the {@code Boolean} value is the corresponding result (non-{@code null} value).
     * <p>See {@link #articleExistsByHash(String)} for details.
     * <p>N.B. This method is present for completeness, the recommended way is to use
     * {@link #getArticlesExistQueryBuilder()}.
     * <p>Warning: this call uses {@code HTTP GET} which may be limited in terms of total arguments' length.
     * Don't use this method for more than a couple of URLs.
     * If unsure, use {@link #getArticlesExistQueryBuilder()}.
     *
     * @param urlHashes a {@code Collection} SHA-1 hashes of the URLs to check
     * @return a {@code Map<String, Boolean>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Boolean> articlesExistByHashes(Collection<String> urlHashes)
            throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articlesExistByHashesCall(urlHashes));
    }

    /**
     * See {@link #articlesExistByUrlsWithId(Collection)}.
     *
     * @param urls URLs
     * @return a {@link Call}
     */
    public Call<Map<String, Integer>> articlesExistByUrlsWithIdCall(Collection<String> urls) {
        return wallabagApiService.existsWithId(nonEmptyCollection(urls, "urls"), null, "1");
    }

    /**
     * Returns a {@code Map} with the results of a bulk "exists" call.
     * The {@code String} key of the map is the article URL
     * and the {@code Integer} value is the corresponding article ID if it exists, {@code null} otherwise.
     * <p>See {@link #articleExistsByUrlWithId(String)} for details.
     * <p>N.B. This method is present for completeness, the recommended way is to use
     * {@link #getArticlesExistQueryBuilder()}.
     * <p>Warning: this call uses {@code HTTP GET} which may be limited in terms of total arguments' length.
     * Don't use this method for more than a couple of URLs or extra lengthy URLs.
     * If unsure, use {@link #getArticlesExistQueryBuilder()}.
     *
     * @param urls a {@code Collection} of URLs to check
     * @return a {@code Map<String, Integer>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Integer> articlesExistByUrlsWithId(Collection<String> urls)
            throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articlesExistByUrlsWithIdCall(urls));
    }

    /**
     * See {@link #articlesExistByUrlsWithId(Collection)}.
     *
     * @param urlHashes URL hashes
     * @return a {@link Call}
     */
    public Call<Map<String, Integer>> articlesExistByHashesWithIdCall(Collection<String> urlHashes) {
        return wallabagApiService.existsWithId(null, nonEmptyCollection(urlHashes, "urlHashes"), "1");
    }

    /**
     * Returns a {@code Map} with the results of a bulk "exists" call.
     * The {@code String} key of the map is the article URL hash
     * and the {@code Integer} value is the corresponding article ID if it exists, {@code null} otherwise.
     * <p>See {@link #articleExistsByHashWithId(String)} for details.
     * <p>N.B. This method is present for completeness, the recommended way is to use
     * {@link #getArticlesExistQueryBuilder()}.
     * <p>Warning: this call uses {@code HTTP GET} which may be limited in terms of total arguments' length.
     * Don't use this method for more than a couple of URLs.
     * If unsure, use {@link #getArticlesExistQueryBuilder()}.
     *
     * @param urlHashes a {@code Collection} of SHA-1 hashes of the URLs to check
     * @return a {@code Map<String, Integer>} with the results
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Map<String, Integer> articlesExistByHashesWithId(Collection<String> urlHashes)
            throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(articlesExistByHashesWithIdCall(urlHashes));
    }

    /**
     * Returns a {@link BatchExistQueryBuilder} instance for performing batch "exists" queries.
     * <p>See {@link BatchExistQueryBuilder} description for details.
     *
     * @return a {@link BatchExistQueryBuilder} instance
     */
    public BatchExistQueryBuilder getArticlesExistQueryBuilder() {
        return new BatchExistQueryBuilder(this);
    }

    /**
     * Returns a {@link BatchExistQueryBuilder} instance with a custom maximum query length
     * for performing batch "exists" queries.
     * <p>See {@link BatchExistQueryBuilder} description for details.
     *
     * @param maxQueryLength custom maximum query length
     * @return a {@link BatchExistQueryBuilder} instance
     */
    public BatchExistQueryBuilder getArticlesExistQueryBuilder(int maxQueryLength) {
        return new BatchExistQueryBuilder(this, maxQueryLength);
    }

    /**
     * Performs a server-side deletion of an article with the specified ID,
     * returns {@code true} if the article was deleted successfully,
     * {@code false} if an article with the specified ID was not found.
     * <p>This method does auto-detection of the proper endpoint method
     * based on the result of {@link #getCachedVersion()}.
     * <p>This method is an alias for {@link #deleteArticle(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to delete
     * @return {@code true} if the article was deleted successfully,
     * {@code false} if an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public boolean deleteArticle(int articleID) throws IOException, UnsuccessfulResponseException {
        return deleteArticle(articleID, NotFoundPolicy.SMART);
    }

    /**
     * Performs a server-side deletion of an article with the specified ID,
     * returns {@code true} if the article was deleted successfully,
     * {@code false} if an article with the specified ID was not found (depends on the {@code notFoundPolicy}).
     * <p>This method does auto-detection of the proper endpoint method
     * based on the result of {@link #getCachedVersion()}.
     * The {@code notFoundPolicy} is not used for the {@link #getCachedVersion()} call.
     *
     * @param articleID      the ID of the article to delete
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return {@code true} if the article was deleted successfully,
     * {@code false} otherwise (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public boolean deleteArticle(int articleID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        boolean deleteArticleWithIdSupported = CompatibilityHelper
                .isDeleteArticleWithIdSupported(this);
        return notFoundPolicy.call(() -> {
            if (deleteArticleWithIdSupported) {
                deleteArticleWithId(articleID);
            } else {
                deleteArticleWithObject(articleID);
            }
            return true;
        }, this, null, false);
    }

    /**
     * See {@link #deleteArticleWithObject(int)}.
     *
     * @param articleID ID
     * @return a {@link Call}
     */
    public Call<Article> deleteArticleWithObjectCall(int articleID) {
        return wallabagApiService.deleteArticle(nonNegativeNumber(articleID, "articleID"));
    }

    /**
     * Performs a server-side deletion of an article and returns a corresponding {@link Article} object.
     * <p>Due to a server-side issue (which was present somewhere around 2.3.*-2.3.6),
     * the {@link Article#id} in the returned object is not guaranteed to contain a correct value:
     * it may be 0 or {@code null}; in case of {@code null} the method throws
     * {@link com.squareup.moshi.JsonDataException}.
     * <p>N.B. This method is present for completeness, the recommended method is {@link #deleteArticle(int)}.
     *
     * @param articleID the ID of the article to delete
     * @return an {@link Article} object corresponding to the deleted article
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found.
     *                                       See {@link WallabagService} description for additional details
     */
    public Article deleteArticleWithObject(int articleID) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(deleteArticleWithObjectCall(articleID));
    }

    /**
     * See {@link #deleteArticleWithId(int)}.
     *
     * @param articleID ID
     * @return a {@link Call}
     */
    public Call<DeleteWithIdResponse> deleteArticleWithIdCall(int articleID) {
        return wallabagApiService.deleteArticle(nonNegativeNumber(articleID, "articleID"), "id");
    }

    /**
     * Performs server-side deletion of an article, returns the ID of the deleted article.
     * <p>N.B. This method is present for completeness, the recommended method is {@link #deleteArticle(int)}.
     *
     * @param articleID the ID of the article to delete
     * @return an {@link Integer} ID of the deleted article
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found.
     *                                       See {@link WallabagService} description for additional details
     */
    public Integer deleteArticleWithId(int articleID) throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(deleteArticleWithIdCall(articleID)).id;
    }

    /**
     * See {@link #getArticle(int)}.
     *
     * @param articleID ID
     * @return a {@link Call}
     */
    public Call<Article> getArticleCall(int articleID) {
        return wallabagApiService.getArticle(nonNegativeNumber(articleID, "articleID"));
    }

    /**
     * Retrieves an article with the specified ID from the server,
     * returns an {@link Article} object or {@code null} if an article with the specified ID was not found.
     * <p>The returned object contains all of its {@link Tag}s and {@link Annotation}s.
     * <p>This method is an alias for {@link #getArticle(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to request
     * @return an {@link Article} object for the specified ID
     * or {@code null} if an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Article getArticle(int articleID) throws IOException, UnsuccessfulResponseException {
        return getArticle(articleID, NotFoundPolicy.SMART);
    }

    /**
     * Retrieves an article with the specified ID from the server,
     * returns an {@link Article} object or {@code null} if an article with the specified ID was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>The returned object contains all of its {@link Tag}s and {@link Annotation}s.
     *
     * @param articleID      the ID of the article to request
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Article} object for the specified ID
     * or {@code null} if an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Article getArticle(int articleID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(getArticleCall(articleID)), this);
    }

    /**
     * See {@link #exportArticle(int, ResponseFormat)}.
     *
     * @param articleID ID
     * @param format    response format
     * @return a {@link Call}
     */
    public Call<ResponseBody> exportArticleCall(int articleID, ResponseFormat format) {
        nonNegativeNumber(articleID, "articleID");
        nonNullValue(format, "format");

        return wallabagApiService.exportArticle(articleID, format.apiValue());
    }

    /**
     * See {@link #exportArticle(int, ResponseFormat)}.
     * <p>This method is an alias for {@link #exportArticleRaw(int, ResponseFormat, NotFoundPolicy)}
     * with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID ID
     * @param format    response format
     * @return a {@link Response} typed with {@link ResponseBody}
     * or {@code null} if an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Response<ResponseBody> exportArticleRaw(int articleID, ResponseFormat format)
            throws IOException, UnsuccessfulResponseException {
        return exportArticleRaw(articleID, format, NotFoundPolicy.SMART);
    }

    /**
     * See {@link #exportArticle(int, ResponseFormat)}.
     *
     * @param articleID      ID
     * @param format         response format
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return a {@link Response} typed with {@link ResponseBody}
     * or {@code null} if an article with the specified ID was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Response<ResponseBody> exportArticleRaw(int articleID, ResponseFormat format, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> checkResponse(exportArticleCall(articleID, format).execute()), this,
                CompatibilityHelper::isExportArticleSupported, null);
    }

    /**
     * Returns a {@link ResponseBody} containing an article specified by {@code articleID}
     * in the specified {@code format} or {@code null} if the article was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>This method is an alias for {@link #exportArticle(int, ResponseFormat, NotFoundPolicy)}
     * with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to export
     * @param format    the desired response format
     * @return a {@link ResponseBody} containing the article
     * or {@code null} if an article with the specified ID was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public ResponseBody exportArticle(int articleID, ResponseFormat format)
            throws IOException, UnsuccessfulResponseException {
        return exportArticle(articleID, format, NotFoundPolicy.SMART);
    }

    /**
     * Returns a {@link ResponseBody} containing an article specified by {@code articleID}
     * in the specified {@code format} or {@code null} if the article was not found
     * (depends on the {@code notFoundPolicy}).
     *
     * @param articleID      the ID of the article to export
     * @param format         the desired response format
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return a {@link ResponseBody} containing the article
     * or {@code null} if the article was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public ResponseBody exportArticle(int articleID, ResponseFormat format, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(exportArticleCall(articleID, format)), this,
                CompatibilityHelper::isExportArticleSupported, null);
    }

    Call<Article> modifyArticleCall(int articleID, RequestBody requestBody) {
        return wallabagApiService.modifyArticle(nonNegativeNumber(articleID, "articleID"), requestBody);
    }

    Article modifyArticle(int articleID, RequestBody requestBody, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(modifyArticleCall(articleID, requestBody)), this);
    }

    /**
     * See {@link #getTags(int)}.
     *
     * @param articleID ID
     * @return a {@link Call}
     */
    public Call<List<Tag>> getTagsCall(int articleID) {
        return wallabagApiService.getTags(nonNegativeNumber(articleID, "articleID"));
    }

    /**
     * Returns a {@code List} of {@link Tag}s for the article specified with the {@code articleID}
     * or {@code null} if an article with the specified ID was not found.
     * <p>This method is an alias for {@link #getTags(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to retrieve tags for
     * @return a {@code List} of {@link Tag}s for the specified article
     * or {@code null} if an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public List<Tag> getTags(int articleID) throws IOException, UnsuccessfulResponseException {
        return getTags(articleID, NotFoundPolicy.SMART);
    }

    /**
     * Returns a {@code List} of {@link Tag}s for the article specified with the {@code articleID}
     * or {@code null} if an article with the specified ID was not found
     * (depends on the {@code notFoundPolicy}).
     *
     * @param articleID      the ID of the article to retrieve tags for
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return a {@code List} of {@link Tag}s for the specified article
     * or {@code null} if an article with the specified ID was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public List<Tag> getTags(int articleID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(getTagsCall(articleID)), this);
    }

    /**
     * See {@link #addTags(int, Collection)}.
     *
     * @param articleID ID
     * @param tags      tags
     * @return a {@link Call}
     */
    public Call<Article> addTagsCall(int articleID, Collection<String> tags) {
        nonNegativeNumber(articleID, "articleID");
        nonEmptyCollection(tags, "tags");

        return wallabagApiService.addTags(articleID, Utils.join(tags, ","));
    }

    /**
     * Sends the specified tags (as {@code String}s) to be added to the specified article on the server,
     * returns the {@link Article} with the tags added
     * or {@code null} if an article with the specified ID was not found.
     * <p>Adding already present tags do not create duplicates.
     * <p>This method is an alias for {@link #addTags(int, Collection, NotFoundPolicy)}
     * with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to add tags to
     * @param tags      a collection of {@code String} tags to add
     * @return an {@link Article} after the tags were added
     * or {@code null} if an article with the specified ID was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Article addTags(int articleID, Collection<String> tags) throws IOException, UnsuccessfulResponseException {
        return addTags(articleID, tags, NotFoundPolicy.SMART);
    }

    /**
     * Sends the specified tags (as {@code String}s) to be added to the specified article on the server,
     * returns the {@link Article} with the tags added or {@code null} if the article was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>Adding already present tags do not create duplicates.
     *
     * @param articleID      the ID of the article to add tags to
     * @param tags           a collection of {@code String} tags to add
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Article} after the tags were added
     * or {@code null} if an article with the specified ID was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Article addTags(int articleID, Collection<String> tags, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(addTagsCall(articleID, tags)), this);
    }

    /**
     * See {@link #deleteTag(int, int)}.
     *
     * @param articleID ID
     * @param tagID     ID
     * @return a {@link Call}
     */
    public Call<Article> deleteTagCall(int articleID, int tagID) {
        nonNegativeNumber(articleID, "articleID");
        nonNegativeNumber(tagID, "tagID");

        return wallabagApiService.deleteTag(articleID, tagID);
    }

    /**
     * Deletes the specified by {@code tagID} tag from the specified by {@code articleID} article,
     * returns the {@link Article} after operation or {@code null} if the article or the tag was not found.
     * <p>This method is an alias for {@link #deleteTag(int, int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to delete tag from
     * @param tagID     the ID of the tag to delete
     * @return an {@link Article} after the tag was deleted
     * or {@code null} if the article or the tas was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Article deleteTag(int articleID, int tagID) throws IOException, UnsuccessfulResponseException {
        return deleteTag(articleID, tagID, NotFoundPolicy.SMART);
    }

    /**
     * Deletes the specified by {@code tagID} tag from the specified by {@code articleID} article,
     * returns the {@link Article} after operation or {@code null} if the article or the tag was not found
     * (depends on the {@code notFoundPolicy}).
     *
     * @param articleID      the ID of the article to delete tag from
     * @param tagID          the ID of the tag to delete
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Article} after the tag was deleted
     * or {@code null} if the article or the tag was not found (depends on the {@code notFoundPolicy}).
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if either the article or the tag with the specified ID was not found.
     *                                       The exception is not thrown if the tag exists, but the article doesn't have it.
     *                                       Depends on the {@code notFoundPolicy}.
     */
    public Article deleteTag(int articleID, int tagID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(deleteTagCall(articleID, tagID)), this);
    }

    /**
     * See {@link #getTags()}.
     *
     * @return a {@link Call}
     */
    public Call<List<Tag>> getTagsCall() {
        return wallabagApiService.getTags();
    }

    /**
     * Returns a {@code List} of all {@link Tag}s present on the server (for current user).
     *
     * @return a {@code List} of all {@link Tag}s
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public List<Tag> getTags() throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(getTagsCall());
    }

    /**
     * See {@link #deleteTag(String)}.
     *
     * @param tagLabel label
     * @return a {@link Call}
     */
    public Call<Tag> deleteTagCall(String tagLabel) {
        return wallabagApiService.deleteTag(nonEmptyString(tagLabel, "tagLabel"));
    }

    /**
     * Deletes the specified by {@code tagLabel} tag from all articles on the server (for current user),
     * returns the deleted {@link Tag} or {@code null} if the tag was not found.
     * <p>The value of {@link Tag#id} of the returned object may be 0.
     * <p>This method is an alias for {@link #deleteTag(String, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param tagLabel the label of the tag to delete
     * @return the deleted {@link Tag} or {@code null} if the tag was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Tag deleteTag(String tagLabel) throws IOException, UnsuccessfulResponseException {
        return deleteTag(tagLabel, NotFoundPolicy.SMART);
    }

    /**
     * Deletes the specified by {@code tagLabel} tag from all articles on the server (for current user),
     * returns the deleted {@link Tag} or {@code null} if the tag was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>The value of {@link Tag#id} of the returned object may be 0.
     *
     * @param tagLabel       the label of the tag to delete
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return the deleted {@link Tag}
     * or {@code null} if the tag was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if a tag with the specified {@code tagLabel} was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Tag deleteTag(String tagLabel, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(deleteTagCall(tagLabel)), this,
                CompatibilityHelper::isDeleteTagByLabelSupported, null);
    }

    /**
     * See {@link #deleteTag(int)}.
     *
     * @param tagID ID
     * @return a {@link Call}
     */
    public Call<Tag> deleteTagCall(int tagID) {
        return wallabagApiService.deleteTag(nonNegativeNumber(tagID, "tagID"));
    }

    /**
     * Deletes the specified by {@code tagID} tag from all articles on the server (for current user),
     * returns the deleted {@link Tag} or {@code null} if the tag was not found.
     * <p>The value of {@link Tag#id} of the returned object may be 0.
     * <p>This method is an alias for {@link #deleteTag(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param tagID the ID of the tag to delete
     * @return the deleted {@link Tag} or {@code null} if the tag was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Tag deleteTag(int tagID) throws IOException, UnsuccessfulResponseException {
        return deleteTag(tagID, NotFoundPolicy.SMART);
    }

    /**
     * Deletes the specified by {@code tagID} tag from all articles on the server (for current user),
     * returns the deleted {@link Tag} or {@code null} if the tag was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>The value of {@link Tag#id} of the returned object may be 0.
     *
     * @param tagID          the ID of the tag to delete
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return the deleted {@link Tag}
     * or {@code null} if the tag was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the tag with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Tag deleteTag(int tagID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(deleteTagCall(tagID)), this);
    }

    /**
     * See {@link #deleteTags(Collection)}.
     *
     * @param tags tags
     * @return a {@link Call}
     */
    public Call<List<Tag>> deleteTagsCall(Collection<String> tags) {
        return wallabagApiService.deleteTags(join(nonNullValue(tags, "tags"), ","));
    }

    /**
     * Deletes the tags specified by labels from all articles on the server (for current user),
     * returns a list of deleted {@link Tag}s. Not found tags are ignored,
     * unless all of the specified tags are not found, in which case a {@code null} is returned.
     * <p>The value of {@link Tag#id} of the returned objects may be 0.
     * <p>This method is an alias for {@link #deleteTags(Collection, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param tags a collection of {@code String} labels of tags to delete
     * @return a {@code List} of deleted {@link Tag}s or {@code null} if none of the tags were found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public List<Tag> deleteTags(Collection<String> tags) throws IOException, UnsuccessfulResponseException {
        return deleteTags(tags, NotFoundPolicy.SMART);
    }

    /**
     * Deletes the tags specified by labels from all articles on the server (for current user),
     * returns a list of deleted {@link Tag}s. Not found tags are ignored,
     * unless all of the specified tags are not found, in which case a {@code null} is returned
     * (depends on the {@code notFoundPolicy}).
     * <p>The value of {@link Tag#id} of the returned objects may be 0.
     *
     * @param tags           a collection of {@code String} labels of tags to delete
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return a {@code List} of deleted {@link Tag}s
     * or {@code null} if none of the tags were found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if none of the specified tags were found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public List<Tag> deleteTags(Collection<String> tags, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(deleteTagsCall(tags)), this,
                CompatibilityHelper::isDeleteTagsByLabelSupported, null);
    }

    /**
     * See {@link #getAnnotations(int)}.
     *
     * @param articleID ID
     * @return a {@link Call}
     */
    public Call<Annotations> getAnnotationsCall(int articleID) {
        return wallabagApiService.getAnnotations(nonNegativeNumber(articleID, "articleID"));
    }

    /**
     * Retrieves and returns an {@link Annotations} object for the article specified by {@code articleID}
     * or {@code null} if the article was not found.
     * <p>This method is an alias for {@link #getAnnotations(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to retrieve annotations for
     * @return an {@link Annotations} object for the specified article
     * or {@code null} if the article was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Annotations getAnnotations(int articleID) throws IOException, UnsuccessfulResponseException {
        return getAnnotations(articleID, NotFoundPolicy.SMART);
    }

    /**
     * Retrieves and returns an {@link Annotations} object for the article specified by {@code articleID}
     * or {@code null} if the article was not found (depends on the {@code notFoundPolicy}).
     *
     * @param articleID      the ID of the article to retrieve annotations for
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Annotations} object for the specified article
     * or {@code null} if the article was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Annotations getAnnotations(int articleID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(getAnnotationsCall(articleID)), this,
                CompatibilityHelper::isGetAnnotationsSupported, null);
    }

    /**
     * See {@link #addAnnotation(int, List, String, String)}.
     *
     * @param articleID ID
     * @param ranges    ranges
     * @param text      text
     * @param quote     quote
     * @return a {@link Call}
     */
    public Call<Annotation> addAnnotationCall(int articleID, List<Annotation.Range> ranges, String text, String quote) {
        nonNegativeNumber(articleID, "articleID");
        nonEmptyCollection(ranges, "ranges");
        nonNullValue(text, "text");

        // use object serialization instead?
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("text", text);
        if (quote != null) parameters.put("quote", quote);
        parameters.put("ranges", ranges);

        return wallabagApiService.addAnnotation(articleID, parameters);
    }

    /**
     * Adds an annotation with the provided parameters to the specified by {@code articleID} article,
     * returns the resulting {@link Annotation} object or {@code null} if the article was not found.
     * <p>See {@link Annotation} for annotation format description.
     * <p>This method is an alias for {@link #addAnnotation(int, List, String, String, NotFoundPolicy)}
     * with {@link NotFoundPolicy#SMART}.
     *
     * @param articleID the ID of the article to add annotation to
     * @param ranges    annotation ranges
     * @param text      annotation text
     * @param quote     {@code null}able quote text
     * @return an {@link Annotation} object or {@code null} if the article was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Annotation addAnnotation(int articleID, List<Annotation.Range> ranges, String text, String quote)
            throws IOException, UnsuccessfulResponseException {
        return addAnnotation(articleID, ranges, text, quote, NotFoundPolicy.SMART);
    }

    /**
     * Adds an annotation with the provided parameters to the specified by {@code articleID} article,
     * returns the resulting {@link Annotation} object or {@code null} if the article was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>See {@link Annotation} for annotation format description.
     *
     * @param articleID      the ID of the article to add annotation to
     * @param ranges         annotation ranges
     * @param text           annotation text
     * @param quote          {@code null}able quote text
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Annotation} object or {@code null} if the article was not found
     * (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the article with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Annotation addAnnotation(int articleID, List<Annotation.Range> ranges, String text, String quote,
                                    NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(addAnnotationCall(articleID, ranges, text, quote)), this,
                CompatibilityHelper::isAddAnnotationSupported, null);
    }

    /**
     * See {@link #updateAnnotation(int, String)}.
     *
     * @param annotationID ID
     * @param text         text
     * @return a {@link Call}
     */
    public Call<Annotation> updateAnnotationCall(int annotationID, String text) {
        nonNegativeNumber(annotationID, "annotationID");
        nonNullValue(text, "text");

        Map<String, String> parameters = new HashMap<>(1);
        parameters.put("text", text);

        return wallabagApiService.updateAnnotation(annotationID, parameters);
    }

    /**
     * Updates the text of the specified by {@code annotationID} annotation,
     * returns the resulting {@link Annotation} object or {@code null} if the annotation was not found.
     * <p>This method is an alias for {@link #updateAnnotation(int, String, NotFoundPolicy)}
     * with {@link NotFoundPolicy#SMART}.
     *
     * @param annotationID the ID of the annotation to update
     * @param text         the new text
     * @return an updated {@link Annotation} object or {@code null} if the annotation was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Annotation updateAnnotation(int annotationID, String text)
            throws IOException, UnsuccessfulResponseException {
        return updateAnnotation(annotationID, text, NotFoundPolicy.SMART);
    }

    /**
     * Updates the text of the specified by {@code annotationID} annotation,
     * returns the resulting {@link Annotation} object or {@code null} if the annotation was not found
     * (depends on the {@code notFoundPolicy}).
     *
     * @param annotationID   the ID of the annotation to update
     * @param text           the new text
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an updated {@link Annotation} object or {@code null} if the annotation was not found
     * (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the annotation with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Annotation updateAnnotation(int annotationID, String text, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(updateAnnotationCall(annotationID, text)), this,
                CompatibilityHelper::isUpdateAnnotationSupported, null);
    }

    /**
     * See {@link #deleteAnnotation(int)}.
     *
     * @param annotationID ID
     * @return a {@link Call}
     */
    public Call<Annotation> deleteAnnotationCall(int annotationID) {
        return wallabagApiService.deleteAnnotation(nonNegativeNumber(annotationID, "annotationID"));
    }

    /**
     * Deletes the specified by {@code annotationID} annotation,
     * returns the deleted {@link Annotation} object or {@code null} if the annotation was not found.
     * <p>The value of {@link Annotation#id} of the returned object may be 0.
     * <p>This method is an alias for {@link #deleteAnnotation(int, NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @param annotationID the ID of the annotation to delete
     * @return an {@link Annotation} object corresponding to the deleted annotation
     * or {@code null} if the annotation was not found
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Annotation deleteAnnotation(int annotationID) throws IOException, UnsuccessfulResponseException {
        return deleteAnnotation(annotationID, NotFoundPolicy.SMART);
    }

    /**
     * Deletes the specified by {@code annotationID} annotation,
     * returns the deleted {@link Annotation} object or {@code null} if the article was not found
     * (depends on the {@code notFoundPolicy}).
     * <p>The value of {@link Annotation#id} of the returned object may be 0.
     *
     * @param annotationID   the ID of the annotation to delete
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return an {@link Annotation} object corresponding to the deleted annotation
     * or {@code null} if the annotation was not found (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the annotation with the specified ID was not found
     *                                       (depends on the {@code notFoundPolicy})
     */
    public Annotation deleteAnnotation(int annotationID, NotFoundPolicy notFoundPolicy)
            throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(deleteAnnotationCall(annotationID)), this,
                CompatibilityHelper::isDeleteAnnotationSupported, null);
    }

    /**
     * See {@link #getInfo()}.
     *
     * @return a {@link Call}
     */
    public Call<Info> getInfoCall() {
        return wallabagApiService.getInfo();
    }

    /**
     * Returns the wallabag instance info as {@link Info} or {@code null} if the API method is not available.
     * <p>This method is an alias for {@link #getInfo(NotFoundPolicy)} with {@link NotFoundPolicy#SMART}.
     *
     * @return the wallabag instance info as {@link Info} or {@code null} if the API method is not available
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public Info getInfo() throws IOException, UnsuccessfulResponseException {
        return getInfo(NotFoundPolicy.SMART);
    }

    /**
     * Returns the wallabag instance info as {@link Info} or {@code null} if the API method is not available
     * (depends on the {@code notFoundPolicy}).
     * Unlike other methods, this method does not perform {@link CompatibilityHelper#isInfoSupported(String)} check,
     * so policies like {@link NotFoundPolicy#SMART} do not throw {@code NotFoundException}s.
     *
     * @param notFoundPolicy the {@link NotFoundPolicy} to use
     * @return the wallabag instance info as {@link Info} or {@code null} if the API method is not available
     * (depends on the {@code notFoundPolicy})
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     * @throws NotFoundException             if the API method is not available (depends on the {@code notFoundPolicy})
     */
    public Info getInfo(NotFoundPolicy notFoundPolicy) throws IOException, UnsuccessfulResponseException {
        return notFoundPolicy.call(() -> execAndCheckBody(getInfoCall()), this);
    }

    /**
     * See {@link #getVersion()}.
     *
     * @return a {@link Call}
     */
    public Call<String> getVersionCall() {
        return wallabagApiService.getVersion();
    }

    /**
     * Returns the API version {@code String} as returned by the server.
     * <p>The value can be used with {@link CompatibilityHelper}.
     *
     * @return the server version as {@code String}
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public String getVersion() throws IOException, UnsuccessfulResponseException {
        return execAndCheckBody(getVersionCall());
    }

    /**
     * Returns a cached server version (in the same format as {@link #getVersion()}).
     * <p>Caching behavior depends on the {@link CachedVersionHandler}
     * provided to {@code WallabagService} during creation.
     * <p>Server-side version changes may not be automatically detected. See {@link #resetCachedVersion()}.
     *
     * @return the server version as {@code String}
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public String getCachedVersion() throws IOException, UnsuccessfulResponseException {
        return nonNullValue(cachedVersionHandler.getCachedVersion(this), "Cached version");
    }

    /**
     * Resets the cached server version so the next call to {@link #getCachedVersion()}
     * fetches and returns a new value.
     * <p>Caching behavior depends on the {@link CachedVersionHandler}
     * provided to {@code WallabagService} during creation.
     */
    public void resetCachedVersion() {
        cachedVersionHandler.resetCachedVersion(this);
    }

    /**
     * This method runs a test query against the server and finishes gracefully
     * if the test completes without errors, otherwise throws an exception.
     * This method can be used to test authentication or general API availability.
     * <p>This method is used by the {@link NotFoundPolicy#SMART} policy.
     *
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public void testServerAccessibility() throws IOException, UnsuccessfulResponseException {
        // just execute some call that should not result in NFE
        articleExistsByUrl("wallabag-apiwrapper-test.example.com");
/*
        // slower (?) alternative
        getArticlesBuilder()
                .perPage(1)
                .since(System.currentTimeMillis() + 31536000000L) // ~ a year in the future
                .detailLevel(ArticlesQueryBuilder.DetailLevel.METADATA)
                .execute();
*/
    }

    private <T> T execAndCheckBody(Call<T> call) throws IOException, UnsuccessfulResponseException {
        return checkResponseBody(call.execute());
    }

    private <T> T checkResponseBody(Response<T> response) throws IOException, UnsuccessfulResponseException {
        T body = checkResponse(response).body();
        if (body == null) { // AFAIK should not happen
            throw new UnsuccessfulResponseException(response.code(), "Response is empty", null);
        }
        return body;
    }

    private <T> Response<T> checkResponse(Response<T> response) throws IOException, UnsuccessfulResponseException {
        if (!response.isSuccessful()) {
            String bodyString = response.errorBody() != null ? response.errorBody().string() : null;
            switch (response.code()) {
                case 400:
                case 401:
                    throw new AuthorizationException(
                            response.code(), response.message(), bodyString);

                case 404:
                    throw new NotFoundException(
                            response.code(), response.message(), bodyString);

                default:
                    throw new UnsuccessfulResponseException(
                            response.code(), response.message(), bodyString);
            }
        }

        return response;
    }

}
