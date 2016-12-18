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

	public final class EntriesQueryBuilder {

		private Boolean archive;
		private Boolean starred;
		private SortCriterion sortCriterion = SortCriterion.CREATED;
		private SortOrder sortOrder = SortOrder.DESCENDING;
		private int page = 1;
		private int perPage = 30;
		private Set<String> tags;
		private long since = 0;

		private EntriesQueryBuilder() {}

		public EntriesQueryBuilder archive(boolean archive) {
			this.archive = archive;
			return this;
		}

		public EntriesQueryBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}

		public EntriesQueryBuilder sortCriterion(SortCriterion sortCriterion) {
			this.sortCriterion = sortCriterion;
			return this;
		}

		public EntriesQueryBuilder sortOrder(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
			return this;
		}

		public EntriesQueryBuilder page(int page) {
			this.page = page;
			return this;
		}

		public EntriesQueryBuilder perPage(int perPage) {
			this.perPage = perPage;
			return this;
		}

		// TODO: reuse code
		public EntriesQueryBuilder tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public EntriesQueryBuilder tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>();
			}
			tags.addAll(tagsLocal);

			return this;
		}

		public EntriesQueryBuilder since(long since) {
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

		public Call<Entries> buildCall() {
			return getEntriesCall(build());
		}

		public Entries execute() throws IOException, UnsuccessfulResponseException {
			return getEntries(build());
		}

	}

	public class AddEntryBuilder {

		private final String url;
		private String title;
		private Set<String> tags;
		private Boolean starred;
		private Boolean archive;

		private AddEntryBuilder(String url) {
			this.url = nonEmptyString(url, "url");
		}

		public AddEntryBuilder title(String title) {
			this.title = nonEmptyString(title, "title");
			return this;
		}

		public AddEntryBuilder tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public AddEntryBuilder tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>();
			}
			tags.addAll(tagsLocal);

			return this;
		}

		public AddEntryBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}

		public AddEntryBuilder archive(boolean archive) {
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
			return addEntryCall(build());
		}

		public Article execute() throws IOException, UnsuccessfulResponseException {
			return addEntry(build());
		}

	}

	public class ModifyEntryBuilder {

		private final int id;
		private String title;
		private Set<String> tags;
		private Boolean starred;
		private Boolean archive;

		private ModifyEntryBuilder(int id) {
			if(id < 0) throw new IllegalArgumentException("ID is less then zero: " + id);

			this.id = id;
		}

		public ModifyEntryBuilder title(String title) {
			this.title = nonEmptyString(title, "title");
			return this;
		}

		public ModifyEntryBuilder tag(String tag) {
			nonEmptyString(tag, "tag");

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public ModifyEntryBuilder tags(Collection<String> tags) {
			nonEmptyCollection(tags, "tags");

			Set<String> tagsLocal = this.tags;
			if(tagsLocal == null) {
				this.tags = tagsLocal = new HashSet<>();
			}
			tags.addAll(tagsLocal);

			return this;
		}

		public ModifyEntryBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}

		public ModifyEntryBuilder archive(boolean archive) {
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
			return modifyEntryCall(id, build());
		}

		public Article execute() throws IOException, UnsuccessfulResponseException {
			return modifyEntry(id, build());
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

	public EntriesQueryBuilder getEntriesBuilder() {
		return new EntriesQueryBuilder();
	}

	public AddEntryBuilder addEntryBuilder(String url) {
		return new AddEntryBuilder(url);
	}

	public Article addEntry(String url) throws IOException, UnsuccessfulResponseException {
		return new AddEntryBuilder(url).execute();
	}

	public ModifyEntryBuilder modifyEntryBuilder(int id) {
		return new ModifyEntryBuilder(id);
	}

	private Call<Entries> getEntriesCall(Map<String, String> parameters) {
		return wallabagApiService.getEntries(parameters);
	}

	private Entries getEntries(Map<String, String> parameters) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getEntriesCall(parameters).execute()).body();
	}

	private Call<Article> addEntryCall(RequestBody requestBody) {
		return wallabagApiService.addEntry(requestBody);
	}

	private Article addEntry(RequestBody requestBody) throws IOException, UnsuccessfulResponseException {
		return checkResponse(addEntryCall(requestBody).execute()).body();
	}

	public Call<ExistsResponse> entryExistsCall(String url) {
		return wallabagApiService.exists(nonEmptyString(url, "URL"));
	}

	public boolean entryExists(String url) throws IOException, UnsuccessfulResponseException {
		return checkResponse(entryExistsCall(url).execute()).body().exists;
	}

	public Call<Article> deleteEntryCall(int entryID) {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		return wallabagApiService.deleteEntry(entryID);
	}

	public Article deleteEntry(int entryID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(deleteEntryCall(entryID).execute()).body();
	}

	public Call<Article> getEntryCall(int entryID) {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		return wallabagApiService.getEntry(entryID);
	}

	public Article getEntry(int entryID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getEntryCall(entryID).execute()).body();
	}

	private Call<Article> modifyEntryCall(int entryID, RequestBody requestBody) {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		return wallabagApiService.modifyEntry(entryID, requestBody);
	}

	private Article modifyEntry(int entryID, RequestBody requestBody)
			throws IOException, UnsuccessfulResponseException {
		return checkResponse(modifyEntryCall(entryID, requestBody).execute()).body();
	}

	public Call<List<Tag>> getTagsCall(int entryID) {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		return wallabagApiService.getTags(entryID);
	}

	public List<Tag> getTags(int entryID) throws IOException, UnsuccessfulResponseException {
		return checkResponse(getTagsCall(entryID).execute()).body();
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
