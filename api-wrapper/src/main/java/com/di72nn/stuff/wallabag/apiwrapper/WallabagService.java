package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.adapters.NumericBooleanAdapter;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.AuthorizationException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.NotFoundException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.*;
import com.di72nn.stuff.wallabag.apiwrapper.services.WallabagApiService;
import com.di72nn.stuff.wallabag.apiwrapper.services.WallabagAuthService;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
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

	private String serverVersion;

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

	abstract static class AbstractTagsBuilder<T extends AbstractTagsBuilder<T>> {

		Set<String> tags;

		abstract T self();

		public T tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return self();
		}

		public T tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>(tags.size());
			}
			tagsLocal.addAll(tags);

			return self();
		}

		String getTagsString() {
			if(tags != null && !tags.isEmpty()) {
				return Utils.join(tags, ",");
			}
			return null;
		}

		void copyTags(T copy) {
			if(tags != null) copy.tags = new HashSet<>(tags);
		}

	}

	public class ArticlesQueryBuilder extends AbstractTagsBuilder<ArticlesQueryBuilder> {

		private Boolean archive;
		private Boolean starred;
		private SortCriterion sortCriterion = SortCriterion.CREATED;
		private SortOrder sortOrder = SortOrder.DESCENDING;
		private int page = 1;
		private int perPage = 30;
		private long since = 0;
		private Boolean isPublic;

		private ArticlesQueryBuilder() {}

		@Override
		ArticlesQueryBuilder self() {
			return this;
		}

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

		public ArticlesQueryBuilder since(long since) {
			this.since = since;
			return this;
		}

		public ArticlesQueryBuilder setPublic(Boolean isPublic) {
			this.isPublic = isPublic;
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
			String tagsString = getTagsString();
			if (tagsString != null) parameters.put("tags", tagsString);
			parameters.put("since", String.valueOf(since));
			if (isPublic != null) parameters.put("public", Utils.booleanToNumberString(isPublic));

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
			copyTags(copy);
			copy.since = since;
			copy.isPublic = isPublic;

			return copy;
		}

	}

	abstract static class AbstractArticleBuilder<T extends AbstractArticleBuilder<T>> extends AbstractTagsBuilder<T> {

		String title;
		String content;
		String language;
		String previewPicture;
		Boolean starred;
		Boolean archive;
		Date publishedAt;
		List<String> authors;
		Boolean isPublic;
		String originUrl;

		public T title(String title) {
			this.title = nonEmptyString(title, "title");
			return self();
		}

		public T content(String content) {
			this.content = nonEmptyString(content, "content");
			return self();
		}

		public T language(String language) {
			this.language = nonEmptyString(language, "language");
			return self();
		}

		public T previewPicture(String previewPicture) {
			this.previewPicture = nonEmptyString(previewPicture, "previewPicture");
			return self();
		}

		public T starred(boolean starred) {
			this.starred = starred;
			return self();
		}

		public T archive(boolean archive) {
			this.archive = archive;
			return self();
		}

		public T publishedAt(Date publishedAt) {
			this.publishedAt = nonNullValue(publishedAt, "publishedAt");
			return self();
		}

		public T author(String author) {
			nonEmptyString(author, "author");

			List<String> authors = this.authors;
			if(authors == null) {
				this.authors = authors = new ArrayList<>(1);
			}
			authors.add(author);

			return self();
		}

		public T authors(Collection<String> authors) {
			nonEmptyCollection(authors, "authors");

			List<String> authorsLocal = this.authors;
			if(authorsLocal == null) {
				this.authors = authorsLocal = new ArrayList<>(authors.size());
			}
			authorsLocal.addAll(authors);

			return self();
		}

		public T isPublic(boolean isPublic) {
			this.isPublic = isPublic;
			return self();
		}

		public T originUrl(String originUrl) {
			this.originUrl = nonEmptyString(originUrl, "originUrl");
			return self();
		}

		String getPublishedAtString() {
			if (publishedAt != null) {
				return String.valueOf(publishedAt.getTime() / 1000);
			}
			return null;
		}

		String getAuthorsString() {
			if(authors != null && !authors.isEmpty()) {
				return Utils.join(authors, ",");
			}
			return null;
		}

		FormBody.Builder populateFormBodyBuilder(FormBody.Builder bodyBuilder) {
			addParameter(bodyBuilder, "title", title);
			addParameter(bodyBuilder, "content", content);
			addParameter(bodyBuilder, "language", language);
			addParameter(bodyBuilder, "preview_picture", previewPicture);
			addParameter(bodyBuilder, "starred", Utils.booleanToNullableNumberString(starred));
			addParameter(bodyBuilder, "archive", Utils.booleanToNullableNumberString(archive));
			addParameter(bodyBuilder, "published_at", getPublishedAtString());
			addParameter(bodyBuilder, "authors", getAuthorsString());
			addParameter(bodyBuilder, "tags", getTagsString());
			addParameter(bodyBuilder, "public", Utils.booleanToNullableNumberString(isPublic));
			addParameter(bodyBuilder, "origin_url", originUrl);

			return bodyBuilder;
		}

		void addParameter(FormBody.Builder bodyBuilder, String paramName, String paramValue) {
			if (paramValue != null) bodyBuilder.add(paramName, paramValue);
		}

	}

	public class AddArticleBuilder extends AbstractArticleBuilder<AddArticleBuilder> {

		final String url;

		AddArticleBuilder(String url) {
			this.url = nonEmptyString(url, "url");
		}

		@Override
		AddArticleBuilder self() {
			return this;
		}

		RequestBody build() {
			FormBody.Builder bodyBuilder = new FormBody.Builder()
					.add("url", url);

			return populateFormBodyBuilder(bodyBuilder).build();
		}

		public Call<Article> buildCall() {
			return addArticleCall(build());
		}

		public Article execute() throws IOException, UnsuccessfulResponseException {
			return addArticle(build());
		}

	}

	public class ModifyArticleBuilder extends AbstractArticleBuilder<ModifyArticleBuilder> {

		final int id;

		ModifyArticleBuilder(int id) {
			this.id = nonNegativeNumber(id, "id");
		}

		@Override
		ModifyArticleBuilder self() {
			return this;
		}

		RequestBody build() {
			FormBody formBody = populateFormBodyBuilder(new FormBody.Builder()).build();

			if(formBody.size() == 0) {
				throw new IllegalStateException("No changes done");
			}

			return formBody;
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

		@SuppressWarnings("ConstantConditions") // constant URL
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

			@SuppressWarnings("ConstantConditions") // always non-empty query
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

		public Call<Map<String, Integer>> buildCallWithId() {
			return articlesExistWithIdCall(urls);
		}

		public Map<String, Boolean> execute() throws IOException, UnsuccessfulResponseException {
			return articlesExist(urls);
		}

		public Map<String, Integer> executeWithId() throws IOException, UnsuccessfulResponseException {
			return articlesExistWithId(urls);
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
					ResponseBody body = response.body();
					LOG.debug("response body: " + (body != null ? body.string() : null));

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

		if(!apiBaseURL.endsWith("/")) apiBaseURL += "/";

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
								.add(Date.class, new Rfc3339DateJsonAdapter().nullSafe())
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

	public Call<ExistsWithIdResponse> articleExistsWithIdCall(String url) {
		return wallabagApiService.exists(nonEmptyString(url, "URL"), "1");
	}

	public Integer articleExistsWithId(String url) throws IOException, UnsuccessfulResponseException {
		return checkResponse(articleExistsWithIdCall(url).execute()).body().id;
	}

	public Call<Map<String, Boolean>> articlesExistCall(Collection<String> urls) {
		return wallabagApiService.exists(nonEmptyCollection(urls, "urls"));
	}

	public Map<String, Boolean> articlesExist(Collection<String> urls)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(articlesExistCall(urls).execute()).body();
	}

	public Call<Map<String, Integer>> articlesExistWithIdCall(Collection<String> urls) {
		return wallabagApiService.exists(nonEmptyCollection(urls, "urls"), "1");
	}

	public Map<String, Integer> articlesExistWithId(Collection<String> urls)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(articlesExistWithIdCall(urls).execute()).body();
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

	public Call<DeleteWithIdResponse> deleteArticleWithIdCall(int articleID) {
		return wallabagApiService.deleteArticle(nonNegativeNumber(articleID, "articleID"), "id");
	}

	public Integer deleteArticleWithId(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteArticleWithIdCall(articleID).execute()).body().id;
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

	// always throws 404 because of server bug
	public Call<List<Tag>> deleteTagsCall(Collection<String> tags) {
		return wallabagApiService.deleteTags(join(nonNullValue(tags, "tags"), ","));
	}

	public List<Tag> deleteTags(Collection<String> tags) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteTagsCall(tags).execute()).body();
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
		nonNullValue(text, "text");

		// use object serialization instead?
		Map<String, Object> parameters = new HashMap<>(3);
		parameters.put("text", text);
		if(quote != null) parameters.put("quote", quote);
		parameters.put("ranges", ranges);

		return wallabagApiService.addAnnotation(articleID, parameters);
	}

	public Annotation addAnnotation(int articleID, List<Annotation.Range> ranges, String text, String quote)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(addAnnotationCall(articleID, ranges, text, quote).execute()).body();
	}

	public Call<Annotation> updateAnnotationCall(int articleID, String text) {
		nonNegativeNumber(articleID, "articleID");
		nonNullValue(text, "text");

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

	String getServerVersion() throws IOException, UnsuccessfulResponseException {
		if(serverVersion == null) {
			serverVersion = getVersion();
		}

		return serverVersion;
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
