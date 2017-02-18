package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.adapters.NumericBooleanAdapter;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.AuthorizationException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.NotFoundException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.*;
import com.di72nn.stuff.wallabag.apiwrapper.services.WallabagApiService;
import com.di72nn.stuff.wallabag.apiwrapper.services.WallabagAuthService;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Rfc3339DateJsonAdapter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.io.IOException;
import java.util.*;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.*;

public class WallabagService {

	private static final Logger LOG = LoggerFactory.getLogger(WallabagService.class);

	// TODO: move somewhere?
	private static final String GRANT_TYPE_PASSWORD = "password";
	private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

	private final WallabagAuthService wallabagAuthService; // TODO: lazy init?
	private final WallabagApiService wallabagApiService;

	private final ParameterHandler parameterHandler;

	private final String apiBaseURL;

	public enum ResponseFormat {
		XML, JSON, TXT, CSV, PDF, EPUB, MOBI, HTML;

		public String apiValue() {
			return toString().toLowerCase();
		}

	}

	public enum SortCriterion {
		CREATED("created"), UPDATED("updated");

		private String value;

		SortCriterion(String value) {
			this.value = value;
		}

		public String apiValue() {
			return value;
		}

	}

	public enum SortOrder {
		ASCENDING("asc"), DESCENDING("desc");

		private String value;

		SortOrder(String value) {
			this.value = value;
		}

		public String apiValue() {
			return value;
		}

	}

	public final class ArticlesQueryBuilder {

		private Boolean archive;
		private Boolean starred;
		private SortCriterion sortCriterion = SortCriterion.CREATED;
		private SortOrder sortOrder = SortOrder.DESCENDING;
		private int page = 1;
		private int perPage = 30;
		private Set<String> tags;
		private long since = 0;

		private ArticlesQueryBuilder() {}

		public ArticlesQueryBuilder archive(boolean archive) {
			this.archive = archive;
			return this;
		}

		public ArticlesQueryBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}

		public ArticlesQueryBuilder sortCriterion(SortCriterion sortCriterion) {
			this.sortCriterion = sortCriterion;
			return this;
		}

		public ArticlesQueryBuilder sortOrder(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
			return this;
		}

		public ArticlesQueryBuilder page(int page) {
			this.page = positiveNumber(page, "page");
			return this;
		}

		public ArticlesQueryBuilder perPage(int perPage) {
			this.perPage = positiveNumber(perPage, "perPage");
			return this;
		}

		// TODO: reuse code
		public ArticlesQueryBuilder tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public ArticlesQueryBuilder tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>();
			}
			tags.addAll(tagsLocal);

			return this;
		}

		public ArticlesQueryBuilder since(long since) {
			this.since = since;
			return this;
		}

		private Map<String, String> build() {
			Map<String, String> parameters = new HashMap<>();

			if(archive != null) parameters.put("archive", Utils.booleanToNumberString(archive));
			if(starred != null) parameters.put("starred", Utils.booleanToNumberString(starred));
			parameters.put("sort", sortCriterion.apiValue());
			parameters.put("order", sortOrder.apiValue());
			parameters.put("page", String.valueOf(page));
			parameters.put("perPage", String.valueOf(perPage));
			if(tags != null && !tags.isEmpty()) {
				parameters.put("tags", Utils.join(tags, ","));
			}
			parameters.put("since", String.valueOf(since));

			return parameters;
		}

		public Call<Articles> buildCall() {
			return getArticlesCall(build());
		}

		public Articles execute() throws IOException, UnsuccessfulResponseException {
			return getArticles(build());
		}

		public ArticlesPageIterator pageIterator() {
			return pageIterator(true);
		}

		public ArticlesPageIterator pageIterator(boolean notFoundAsEmpty) {
			return new ArticlesPageIterator(copy(), notFoundAsEmpty);
		}

		private ArticlesQueryBuilder copy() {
			ArticlesQueryBuilder copy = new ArticlesQueryBuilder();

			copy.archive = archive;
			copy.starred = starred;
			copy.sortCriterion = sortCriterion;
			copy.sortOrder = sortOrder;
			copy.page = page;
			copy.perPage = perPage;
			copy.tags = tags; // no need to create new collection?
			copy.since = since;

			return copy;
		}

	}

	public class AddArticleBuilder {

		private final String url;
		private String title;
		private Set<String> tags;
		private Boolean starred;
		private Boolean archive;

		private AddArticleBuilder(String url) {
			this.url = nonEmptyString(url, "url");
		}

		public AddArticleBuilder title(String title) {
			this.title = nonEmptyString(title, "title");
			return this;
		}

		public AddArticleBuilder tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public AddArticleBuilder tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>();
			}
			tags.addAll(tagsLocal);

			return this;
		}

		public AddArticleBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}

		public AddArticleBuilder archive(boolean archive) {
			this.archive = archive;
			return this;
		}

		private RequestBody build() {
			FormBody.Builder bodyBuilder = new FormBody.Builder()
					.add("url", url);

			if(title != null) bodyBuilder.add("title", title);
			if(tags != null && !tags.isEmpty()) {
				bodyBuilder.add("tags", Utils.join(tags, ","));
			}
			if(starred != null) bodyBuilder.add("starred", Utils.booleanToNumberString(starred));
			if(archive != null) bodyBuilder.add("archive", Utils.booleanToNumberString(archive));

			return bodyBuilder.build();
		}

		public Call<Article> buildCall() {
			return addArticleCall(build());
		}

		public Article execute() throws IOException, UnsuccessfulResponseException {
			return addArticle(build());
		}

	}

	public class ModifyArticleBuilder {

		private final int id;
		private String title;
		private Set<String> tags;
		private Boolean starred;
		private Boolean archive;

		private ModifyArticleBuilder(int id) {
			this.id = nonNegativeNumber(id, "id");
		}

		public ModifyArticleBuilder title(String title) {
			this.title = nonEmptyString(title, "title");
			return this;
		}

		public ModifyArticleBuilder tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public ModifyArticleBuilder tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>();
			}
			tags.addAll(tagsLocal);

			return this;
		}

		public ModifyArticleBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}

		public ModifyArticleBuilder archive(boolean archive) {
			this.archive = archive;
			return this;
		}

		private RequestBody build() {
			FormBody.Builder bodyBuilder = new FormBody.Builder();

			boolean changed = false;

			if(title != null) {
				bodyBuilder.add("title", title);
				changed = true;
			}
			if(tags != null && !tags.isEmpty()) {
				bodyBuilder.add("tags", Utils.join(tags, ","));
				changed = true;
			}
			if(archive != null) {
				bodyBuilder.add("archive", Utils.booleanToNumberString(archive));
				changed = true;
			}
			if(starred != null) {
				bodyBuilder.add("starred", Utils.booleanToNumberString(starred));
				changed = true;
			}

			if(!changed) {
				throw new IllegalStateException("No changes done");
			}

			return bodyBuilder.build();
		}

		public Call<Article> buildCall() {
			return modifyArticleCall(id, build());
		}

		public Article execute() throws IOException, UnsuccessfulResponseException {
			return modifyArticle(id, build());
		}

	}

	public class BatchExistQueryBuilder {

		private int maxQueryLength;

		private final HttpUrl.Builder builder = HttpUrl.parse("https://a").newBuilder();

		private final List<String> urls = new ArrayList<>();
		private int currentRequestLength;

		private BatchExistQueryBuilder() {
			this(3990);
		}

		private BatchExistQueryBuilder(int maxQueryLength) {
			this.maxQueryLength = maxQueryLength;

			reset();
		}

		public void reset() {
			urls.clear();
			currentRequestLength = apiBaseURL.length() + "/api/entries/exists.json".length();
		}

		public boolean addUrl(String url) {
			nonNullValue(url, "url");

			int parameterLength = builder.setQueryParameter("urls[]", url).build().encodedQuery().length() + 1;
			if(currentRequestLength + parameterLength <= maxQueryLength) {
				urls.add(url);
				currentRequestLength += parameterLength;

				return true;
			}

			return false;
		}

		public Call<Map<String, Boolean>> buildCall() {
			return articlesExistCall(urls);
		}

		public Map<String, Boolean> execute() throws IOException, UnsuccessfulResponseException {
			return articlesExist(urls);
		}

	}

	public static class ArticlesPageIterator {

		private static final Logger LOG = LoggerFactory.getLogger(ArticlesPageIterator.class);

		private final ArticlesQueryBuilder queryBuilder;
		private final boolean notFoundAsEmpty;

		private int currentPage = 1;

		private Articles articles;
		private boolean ready;
		private boolean lastPageReached;

		private ArticlesPageIterator(ArticlesQueryBuilder articlesQueryBuilder, boolean notFoundAsEmpty) {
			this.queryBuilder = articlesQueryBuilder;
			this.notFoundAsEmpty = notFoundAsEmpty;
		}

		public boolean hasNext() throws IOException, UnsuccessfulResponseException {
			if(ready) return true;
			if(lastPageReached) return false;

			Articles articles;
			try {
				articles = queryBuilder.page(currentPage++).execute();
			} catch(NotFoundException nfe) {
				if(!notFoundAsEmpty) {
					throw nfe;
				}

				LOG.debug("Handling NFE as empty", nfe);
				articles = null;
			}

			this.articles = articles;

			if(articles != null) {
				LOG.trace("Page: {}/{}, total articles: {}", articles.page, articles.pages, articles.total);

				ready = true;
				if(articles.page == articles.pages) lastPageReached = true;
			} else {
				LOG.trace("articles == null");
			}

			ready = articles != null;
			return ready;
		}

		public Articles next() throws IOException, UnsuccessfulResponseException {
			if(!hasNext()) throw new NoSuchElementException();

			ready = false;
			return articles;
		}

	}

	// TODO: synchronization?
	private class TokenRefreshingInterceptor implements Interceptor {

		@Override
		public okhttp3.Response intercept(Chain chain) throws IOException {
			LOG.debug("intercept() started");

			Request originalRequest = chain.request();

			Request request = setHeaders(originalRequest.newBuilder()).build();
			okhttp3.Response response = chain.proceed(request);

			LOG.debug("intercept() got response");
			if(!response.isSuccessful()) {
				LOG.info("intercept() unsuccessful response; code: " + response.code());

				if(response.code() == 401) {
					LOG.debug("response body: " + response.body().string());

					try {
						if(getAccessToken()) {
							request = setHeaders(originalRequest.newBuilder()).build();
							response = chain.proceed(request);
						}
					} catch(GetTokenException e) {
						LOG.debug("Got GetTokenException");

						Response<TokenResponse> tokenResponse = e.getResponse();
						response = tokenResponse.raw().newBuilder().body(tokenResponse.errorBody()).build();
					}
				}
			}

			return response;
		}

	}

	private static class GetTokenException extends Exception {

		private retrofit2.Response<TokenResponse> response;

		GetTokenException(Response<TokenResponse> response) {
			this.response = response;
		}

		Response<TokenResponse> getResponse() {
			return response;
		}

	}

	public WallabagService(String apiBaseURL, ParameterHandler parameterHandler) {
		this(apiBaseURL, parameterHandler, null);
	}

	public WallabagService(String apiBaseURL, ParameterHandler parameterHandler, OkHttpClient okHttpClient) {
		nonEmptyString(apiBaseURL, "apiBaseURL");
		if(parameterHandler == null) {
			throw new NullPointerException("parameterHandler is null");
		}

		this.apiBaseURL = apiBaseURL;
		this.parameterHandler = parameterHandler;

		if(okHttpClient == null) okHttpClient = new OkHttpClient();

		wallabagAuthService = new Retrofit.Builder()
				.addConverterFactory(MoshiConverterFactory.create())
				.client(okHttpClient)
				.baseUrl(apiBaseURL)
				.build()
				.create(WallabagAuthService.class);

		okHttpClient = okHttpClient.newBuilder().addInterceptor(new TokenRefreshingInterceptor()).build();

		wallabagApiService = new Retrofit.Builder()
				.addConverterFactory(MoshiConverterFactory.create(
						new Moshi.Builder()
								.add(new NumericBooleanAdapter())
								.add(Date.class, new Rfc3339DateJsonAdapter())
								.build()))
				.client(okHttpClient)
				.baseUrl(apiBaseURL)
				.build()
				.create(WallabagApiService.class);
	}

	public ArticlesQueryBuilder getArticlesBuilder() {
		return new ArticlesQueryBuilder();
	}

	public AddArticleBuilder addArticleBuilder(String url) {
		return new AddArticleBuilder(url);
	}

	public Article addArticle(String url) throws IOException, UnsuccessfulResponseException {
		return addArticleBuilder(url).execute();
	}

	public ModifyArticleBuilder modifyArticleBuilder(int id) {
		return new ModifyArticleBuilder(id);
	}

	private Call<Articles> getArticlesCall(Map<String, String> parameters) {
		return wallabagApiService.getArticles(parameters);
	}

	private Articles getArticles(Map<String, String> parameters) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getArticlesCall(parameters).execute()).body();
	}

	private Call<Article> addArticleCall(RequestBody requestBody) {
		return wallabagApiService.addArticle(requestBody);
	}

	private Article addArticle(RequestBody requestBody) throws IOException, UnsuccessfulResponseException {
		return checkResponse(addArticleCall(requestBody).execute()).body();
	}

	public Call<Article> reloadArticleCall(int articleID) {
		return wallabagApiService.reloadArticle(nonNegativeNumber(articleID, "articleID"));
	}

	public Article reloadArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		Response<Article> response = reloadArticleCall(articleID).execute();

		if(response.code() == 304) { // couldn't update
			return null;
		}

		return checkResponse(response).body();
	}

	public Call<ExistsResponse> articleExistsCall(String url) {
		return wallabagApiService.exists(nonEmptyString(url, "URL"));
	}

	public boolean articleExists(String url) throws IOException, UnsuccessfulResponseException {
		return checkResponse(articleExistsCall(url).execute()).body().exists;
	}

	public Call<Map<String, Boolean>> articlesExistCall(Collection<String> urls) {
		return wallabagApiService.exists(new HashSet<>(nonEmptyCollection(urls, "urls")));
	}

	public Map<String, Boolean> articlesExist(Collection<String> urls) throws IOException, UnsuccessfulResponseException {
		return checkResponse(articlesExistCall(urls).execute()).body();
	}

	public BatchExistQueryBuilder getArticlesExistQueryBuilder() {
		return new BatchExistQueryBuilder();
	}

	public BatchExistQueryBuilder getArticlesExistQueryBuilder(int maxQueryLength) {
		return new BatchExistQueryBuilder(maxQueryLength);
	}

	public Call<Article> deleteArticleCall(int articleID) {
		return wallabagApiService.deleteArticle(nonNegativeNumber(articleID, "articleID"));
	}

	public Article deleteArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteArticleCall(articleID).execute()).body();
	}

	public Call<Article> getArticleCall(int articleID) {
		return wallabagApiService.getArticle(nonNegativeNumber(articleID, "articleID"));
	}

	public Article getArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getArticleCall(articleID).execute()).body();
	}

	public Call<ResponseBody> exportArticleCall(int articleID, ResponseFormat format) {
		nonNegativeNumber(articleID, "articleID");
		nonNullValue(format, "format");

		return wallabagApiService.exportArticle(articleID, format.apiValue());
	}

	public Response<ResponseBody> exportArticleRaw(int articleID, ResponseFormat format)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(exportArticleCall(articleID, format).execute());
	}

	public ResponseBody exportArticle(int articleID, ResponseFormat format)
			throws IOException, UnsuccessfulResponseException {
		return exportArticleRaw(articleID, format).body();
	}

	private Call<Article> modifyArticleCall(int articleID, RequestBody requestBody) {
		return wallabagApiService.modifyArticle(nonNegativeNumber(articleID, "articleID"), requestBody);
	}

	private Article modifyArticle(int articleID, RequestBody requestBody)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(modifyArticleCall(articleID, requestBody).execute()).body();
	}

	public Call<List<Tag>> getTagsCall(int articleID) {
		return wallabagApiService.getTags(nonNegativeNumber(articleID, "articleID"));
	}

	public List<Tag> getTags(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getTagsCall(articleID).execute()).body();
	}

	public Call<Article> addTagsCall(int articleID, Collection<String> tags) {
		nonNegativeNumber(articleID, "articleID");
		nonEmptyCollection(tags, "tags");

		return wallabagApiService.addTags(articleID, Utils.join(tags, ","));
	}

	public Article addTags(int articleID, Collection<String> tags) throws IOException, UnsuccessfulResponseException {
		return checkResponse(addTagsCall(articleID, tags).execute()).body();
	}

	public Call<Article> deleteTagCall(int articleID, int tagID) {
		nonNegativeNumber(articleID, "articleID");
		nonNegativeNumber(tagID, "tagID");

		return wallabagApiService.deleteTag(articleID, tagID);
	}

	public Article deleteTag(int articleID, int tagID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteTagCall(articleID, tagID).execute()).body();
	}

	public Call<List<Tag>> getTagsCall() {
		return wallabagApiService.getTags();
	}

	public List<Tag> getTags() throws IOException, UnsuccessfulResponseException {
		return checkResponse(getTagsCall().execute()).body();
	}

	// always throws 404 because of server bug
	public Call<Tag> deleteTagCall(String tagLabel) {
		return wallabagApiService.deleteTag(nonEmptyString(tagLabel, "tagLabel"));
	}

	public Tag deleteTag(String tagLabel) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteTagCall(tagLabel).execute()).body();
	}

	public Call<Tag> deleteTagCall(int tagID) {
		return wallabagApiService.deleteTag(nonNegativeNumber(tagID, "tagID"));
	}

	public Tag deleteTag(int tagID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteTagCall(tagID).execute()).body();
	}

	public Call<Annotations> getAnnotationsCall(int articleID)  {
		return wallabagApiService.getAnnotations(nonNegativeNumber(articleID, "articleID"));
	}

	public Annotations getAnnotations(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getAnnotationsCall(articleID).execute()).body();
	}

	// TODO: turn into builder?
	public Call<Annotation> addAnnotationCall(int articleID, List<Annotation.Range> ranges, String text, String quote) {
		nonNegativeNumber(articleID, "articleID");
		nonEmptyCollection(ranges, "ranges");
		nonEmptyString(text, "text"); // TODO: check

		// use object serialization instead?
		Map<String, Object> parameters = new HashMap<>(3);
		parameters.put("text", text);
		if(quote != null) parameters.put("quote", quote);
		parameters.put("ranges", ranges); // TODO: copy list?

		return wallabagApiService.addAnnotation(articleID, parameters);
	}

	public Annotation addAnnotation(int articleID, List<Annotation.Range> ranges, String text, String quote)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(addAnnotationCall(articleID, ranges, text, quote).execute()).body();
	}

	public Call<Annotation> updateAnnotationCall(int articleID, String text) {
		nonNegativeNumber(articleID, "articleID");
		nonEmptyString(text, "text"); // TODO: check

		Map<String, String> parameters = new HashMap<>(1);
		parameters.put("text", text);

		return wallabagApiService.updateAnnotation(articleID, parameters);
	}

	public Annotation updateAnnotation(int articleID, String text) throws IOException, UnsuccessfulResponseException {
		return checkResponse(updateAnnotationCall(articleID, text).execute()).body();
	}

	public Call<Annotation> deleteAnnotationCall(int annotationID) {
		return wallabagApiService.deleteAnnotation(nonNegativeNumber(annotationID, "annotationID"));
	}

	public Annotation deleteAnnotation(int annotationID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteAnnotationCall(annotationID).execute()).body();
	}

	public Call<String> getVersionCall() {
		return wallabagApiService.getVersion();
	}

	public String getVersion() throws IOException, UnsuccessfulResponseException {
		return checkResponse(getVersionCall().execute()).body();
	}

	private <T> Response<T> checkResponse(Response<T> response) throws IOException, UnsuccessfulResponseException {
		if(!response.isSuccessful()) {
			switch(response.code()) {
				case 400:
				case 401:
					throw new AuthorizationException(
							response.code(), response.message(), response.errorBody().string());

				case 404:
					throw new NotFoundException(
							response.code(), response.message(), response.errorBody().string());

				default:
					throw new UnsuccessfulResponseException(
							response.code(), response.message(), response.errorBody().string());
			}
		}

		return response;
	}

	private boolean getAccessToken() throws IOException, GetTokenException {
		LOG.info("Access token requested");

		LOG.info("Refreshing token");
		try {
			if(getAccessToken(true)) return true;
		} catch(GetTokenException e) {
			LOG.debug("GetTokenException");

			Response<TokenResponse> response = e.getResponse();
			if(response.code() != 400) { // also handle 401?
				LOG.warn("Unexpected error code: " + response.code());
				throw e;
			}
		}

		LOG.info("Requesting new token");
		return getAccessToken(false);
	}

	private boolean getAccessToken(boolean refresh) throws IOException, GetTokenException {
		LOG.info("started");

		FormBody.Builder bodyBuilder = new FormBody.Builder()
				.add("client_id", nonNullValue(parameterHandler.getClientID(), "clientID"))
				.add("client_secret", nonNullValue(parameterHandler.getClientSecret(), "clientSecret"));

		if(refresh) {
			String refreshToken = parameterHandler.getRefreshToken();
			if(refreshToken == null || refreshToken.isEmpty()) {
				LOG.debug("Refresh token is empty or null");
				return false;
			}
			bodyBuilder.add("grant_type", GRANT_TYPE_REFRESH_TOKEN)
					.add("refresh_token", refreshToken);
		} else {
			bodyBuilder.add("grant_type", GRANT_TYPE_PASSWORD)
					.add("username", nonNullValue(parameterHandler.getUsername(), "username"))
					.add("password", nonNullValue(parameterHandler.getPassword(), "password"));
		}
		RequestBody body = bodyBuilder.build();

		Call<TokenResponse> tokenResponseCall = wallabagAuthService.token(body);
		Response<TokenResponse> response = tokenResponseCall.execute();

		if(!response.isSuccessful()) {
			throw new GetTokenException(response);
		}

		TokenResponse tokenResponse = response.body();
		boolean result = parameterHandler.tokensUpdated(tokenResponse);

		LOG.info("finished; result: {}", result);
		return result;
	}

	private Request.Builder setHeaders(Request.Builder requestBuilder) {
		requestBuilder.addHeader("Accept", "*/*"); // compatibility

		return setAuthHeader(requestBuilder);
	}

	private Request.Builder setAuthHeader(Request.Builder requestBuilder) {
		return requestBuilder.addHeader("Authorization", "Bearer " + parameterHandler.getAccessToken());
	}

}
