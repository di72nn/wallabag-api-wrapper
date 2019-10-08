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

import static com.di72nn.stuff.wallabag.apiwrapper.Constants.*;
import static com.di72nn.stuff.wallabag.apiwrapper.Utils.*;

public class WallabagService {

	private static final Logger LOG = LoggerFactory.getLogger(WallabagService.class);

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

	String getApiBaseURL() {
		return apiBaseURL;
	}

	public ArticlesQueryBuilder getArticlesBuilder() {
		return new ArticlesQueryBuilder(this);
	}

	public AddArticleBuilder addArticleBuilder(String url) {
		return new AddArticleBuilder(this, url);
	}

	public Article addArticle(String url) throws IOException, UnsuccessfulResponseException {
		return addArticleBuilder(url).execute();
	}

	public ModifyArticleBuilder modifyArticleBuilder(int id) {
		return new ModifyArticleBuilder(this, id);
	}

	Call<Articles> getArticlesCall(Map<String, String> parameters) {
		return wallabagApiService.getArticles(parameters);
	}

	Articles getArticles(Map<String, String> parameters) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getArticlesCall(parameters).execute()).body();
	}

	Call<Article> addArticleCall(RequestBody requestBody) {
		return wallabagApiService.addArticle(requestBody);
	}

	Article addArticle(RequestBody requestBody) throws IOException, UnsuccessfulResponseException {
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
		return new BatchExistQueryBuilder(this);
	}

	public BatchExistQueryBuilder getArticlesExistQueryBuilder(int maxQueryLength) {
		return new BatchExistQueryBuilder(this, maxQueryLength);
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

	Call<Article> modifyArticleCall(int articleID, RequestBody requestBody) {
		return wallabagApiService.modifyArticle(nonNegativeNumber(articleID, "articleID"), requestBody);
	}

	Article modifyArticle(int articleID, RequestBody requestBody)
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

	public Call<Annotation> updateAnnotationCall(int annotationID, String text) {
		nonNegativeNumber(annotationID, "annotationID");
		nonNullValue(text, "text");

		Map<String, String> parameters = new HashMap<>(1);
		parameters.put("text", text);

		return wallabagApiService.updateAnnotation(annotationID, parameters);
	}

	public Annotation updateAnnotation(int annotationID, String text)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(updateAnnotationCall(annotationID, text).execute()).body();
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
				.add(CLIENT_ID_PARAM, nonNullValue(parameterHandler.getClientID(), "clientID"))
				.add(CLIENT_SECRET_PARAM, nonNullValue(parameterHandler.getClientSecret(), "clientSecret"));

		if(refresh) {
			String refreshToken = parameterHandler.getRefreshToken();
			if(refreshToken == null || refreshToken.isEmpty()) {
				LOG.debug("Refresh token is empty or null");
				return false;
			}
			bodyBuilder.add(GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN)
					.add(REFRESH_TOKEN_PARAM, refreshToken);
		} else {
			bodyBuilder.add(GRANT_TYPE, GRANT_TYPE_PASSWORD)
					.add(USERNAME_PARAM, nonNullValue(parameterHandler.getUsername(), "username"))
					.add(PASSWORD_PARAM, nonNullValue(parameterHandler.getPassword(), "password"));
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
		requestBuilder.addHeader(HTTP_ACCEPT_HEADER, HTTP_ACCEPT_VALUE_ANY); // compatibility

		return setAuthHeader(requestBuilder);
	}

	private Request.Builder setAuthHeader(Request.Builder requestBuilder) {
		return requestBuilder.addHeader(HTTP_AUTHORIZATION_HEADER,
				HTTP_AUTHORIZATION_BEARER_VALUE + parameterHandler.getAccessToken());
	}

}
