package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.adapters.NumericBooleanAdapter;
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

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonEmptyString;
import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonEmptyCollection;

public class WallabagService {

	private static final Logger LOG = LoggerFactory.getLogger(WallabagService.class);

	// TODO: move somewhere?
	private static final String GRANT_TYPE_PASSWORD = "password";
	private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

	private final WallabagApiService wallabagApiService;

	private final ParameterHandler parameterHandler;

	private final String apiBaseURL;

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
			this.page = page;
			return this;
		}

		public ArticlesQueryBuilder perPage(int perPage) {
			this.perPage = perPage;
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
			if(id < 0) throw new IllegalArgumentException("ID is less then zero: " + id);

			this.id = id;
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

	// TODO: synchronization?
	private class TokenRefreshingInterceptor implements Interceptor {

		@Override
		public okhttp3.Response intercept(Chain chain) throws IOException {
			LOG.debug("intercept() started");

			Request originalRequest = chain.request();

			Request request = setAuthHeader(originalRequest.newBuilder()).build();

			okhttp3.Response response = chain.proceed(request);

			LOG.debug("intercept() got response");
			if(!response.isSuccessful()) {
				LOG.info("intercept() unsuccessful response; code: " + response.code());

				if(response.code() == 401) {
					LOG.debug("response body: " + response.body().string());

					getAccessToken();

					request = setAuthHeader(originalRequest.newBuilder()).build();
					response = chain.proceed(request);
				}
			}

			return response;
		}

	}

	public WallabagService(String apiBaseURL, ParameterHandler parameterHandler) {
		nonEmptyString(apiBaseURL, "apiBaseURL");
		if(parameterHandler == null) {
			throw new NullPointerException("parameterHandler is null");
		}

		this.apiBaseURL = apiBaseURL;
		this.parameterHandler = parameterHandler;

		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new TokenRefreshingInterceptor()).build();

		Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(MoshiConverterFactory.create(
						new Moshi.Builder()
								.add(new NumericBooleanAdapter())
								.add(Date.class, new Rfc3339DateJsonAdapter())
								.build()))
				.client(okHttpClient)
				.baseUrl(apiBaseURL)
				.build();

		wallabagApiService = retrofit.create(WallabagApiService.class);
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

	public Call<ExistsResponse> articleExistsCall(String url) {
		return wallabagApiService.exists(nonEmptyString(url, "URL"));
	}

	public boolean articleExists(String url) throws IOException, UnsuccessfulResponseException {
		return checkResponse(articleExistsCall(url).execute()).body().exists;
	}

	public Call<Article> deleteArticleCall(int articleID) {
		if(articleID < 0) throw new IllegalArgumentException("articleID is less than zero: " + articleID);

		return wallabagApiService.deleteArticle(articleID);
	}

	public Article deleteArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteArticleCall(articleID).execute()).body();
	}

	public Call<Article> getArticleCall(int articleID) {
		if(articleID < 0) throw new IllegalArgumentException("articleID is less than zero: " + articleID);

		return wallabagApiService.getArticle(articleID);
	}

	public Article getArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getArticleCall(articleID).execute()).body();
	}

	private Call<Article> modifyArticleCall(int articleID, RequestBody requestBody) {
		if(articleID < 0) throw new IllegalArgumentException("articleID is less than zero: " + articleID);

		return wallabagApiService.modifyArticle(articleID, requestBody);
	}

	private Article modifyArticle(int articleID, RequestBody requestBody)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(modifyArticleCall(articleID, requestBody).execute()).body();
	}

	public Call<List<Tag>> getTagsCall(int articleID) {
		if(articleID < 0) throw new IllegalArgumentException("articleID is less than zero: " + articleID);

		return wallabagApiService.getTags(articleID);
	}

	public List<Tag> getTags(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getTagsCall(articleID).execute()).body();
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
		if(tagID < 0) throw new IllegalArgumentException("tagID is less than zero: " + tagID);

		return wallabagApiService.deleteTag(tagID);
	}

	public Tag deleteTag(int tagID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteTagCall(tagID).execute()).body();
	}

	public Call<String> getVersionCall() {
		return wallabagApiService.getVersion();
	}

	public String getVersion() throws IOException, UnsuccessfulResponseException {
		return checkResponse(getVersionCall().execute()).body();
	}

	private <T> Response<T> checkResponse(Response<T> response) throws IOException, UnsuccessfulResponseException {
		if(!response.isSuccessful()) {
			throw new UnsuccessfulResponseException("Unsuccessful response",
					response.code(), response.errorBody().string());
		}

		return response;
	}

	private void getAccessToken() throws IOException {
		LOG.info("Access token requested");

		LOG.info("Refreshing token");
		if(getAccessToken(true) != null) return;

		LOG.info("Requesting new token");
		if(getAccessToken(false) == null) {
			throw new IllegalStateException("Couldn't get access token");
		}
	}

	private TokenResponse getAccessToken(boolean refresh) throws IOException {
		LOG.info("started");

		// TODO: check values from parameterHandler?

		FormBody.Builder bodyBuilder = new FormBody.Builder()
				.add("client_id", parameterHandler.getClientID())
				.add("client_secret", parameterHandler.getClientSecret());

		if(refresh) {
			String refreshToken = parameterHandler.getRefreshToken();
			if(refreshToken == null || refreshToken.isEmpty()) {
				LOG.debug("Refresh token is empty or null");
				return null;
			}
			bodyBuilder.add("grant_type", GRANT_TYPE_REFRESH_TOKEN)
					.add("refresh_token", refreshToken);
		} else {
			bodyBuilder.add("grant_type", GRANT_TYPE_PASSWORD)
					.add("username", parameterHandler.getUsername())
					.add("password", parameterHandler.getPassword());
		}
		RequestBody body = bodyBuilder.build();

		Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(MoshiConverterFactory.create())
				.baseUrl(apiBaseURL)
				.build();

		WallabagAuthService wallabagAuthService = retrofit.create(WallabagAuthService.class);
		Call<TokenResponse> tokenResponseCall = wallabagAuthService.token(body);
		Response<TokenResponse> response = tokenResponseCall.execute();

		// TODO: handle errors
		if(!response.isSuccessful()) {
			if(response.code() == 400) {
				LOG.debug("response code: " + response.code() + ", body: " + response.errorBody().string());
				return null;
			} else {
				// TODO: decent exception
				throw new IllegalStateException("Response is unsuccessful; code: " + response.code()
						+ ", body: " + response.errorBody().string());
			}
		}

		TokenResponse tokenResponse = response.body();
		LOG.info("Got token: " + tokenResponse); // TODO: remove: sensitive

		parameterHandler.tokensUpdated(tokenResponse);

		LOG.info("finished");

		return tokenResponse;
	}

	private Request.Builder setAuthHeader(Request.Builder requestBuilder) {
		return requestBuilder.addHeader("Authorization", "Bearer " + parameterHandler.getAccessToken());
	}

}
