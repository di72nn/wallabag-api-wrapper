package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.adapters.NumericBooleanAdapter;
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

		// TODO: make package-private?
		public Map<String, String> build() {
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

		public Entries execute() throws IOException {
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

		// TODO: make package-private?
		public RequestBody build() {
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

		public Article execute() throws IOException {
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

		// TODO: make package-private?
		public RequestBody build() {
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

		public Article execute() throws IOException {
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

	public ModifyEntryBuilder modifyEntryBuilder(int id) {
		return new ModifyEntryBuilder(id);
	}

	public Entries getEntries(Map<String, String> parameters) throws IOException {
		Response<Entries> response = wallabagApiService.getEntries(parameters).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	// TODO: add a convenience method with one parameter?
	private Article addEntry(RequestBody requestBody) throws IOException {
		Response<Article> response = wallabagApiService.addEntry(requestBody).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public boolean entryExists(String url) throws IOException {
		nonEmptyString(url, "URL");

		Response<ExistsResponse> response = wallabagApiService.exists(url).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body().exists;
	}

	public Article deleteEntry(int entryID) throws IOException {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		Response<Article> response = wallabagApiService.deleteEntry(entryID).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public Article getEntry(int entryID) throws IOException {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		Response<Article> response = wallabagApiService.getEntry(entryID).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	private Article modifyEntry(int entryID, RequestBody requestBody) throws IOException {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		Response<Article> response = wallabagApiService.modifyEntry(entryID, requestBody).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public List<Tag> getTags(int entryID) throws IOException {
		if(entryID < 0) throw new IllegalArgumentException("entryID is less than zero: " + entryID);

		Response<List<Tag>> response = wallabagApiService.getTags(entryID).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public List<Tag> getTags() throws IOException {
		Response<List<Tag>> response = wallabagApiService.getTags().execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public Tag deleteTag(String tagLabel) throws IOException {
		nonEmptyString(tagLabel, "tagLabel");

		Response<Tag> response = wallabagApiService.deleteTag(tagLabel).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public Tag deleteTag(int tagID) throws IOException {
		if(tagID < 0) throw new IllegalArgumentException("tagID is less than zero: " + tagID);

		Response<Tag> response = wallabagApiService.deleteTag(tagID).execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	public String getVersion() throws IOException {
		Response<String> response = wallabagApiService.getVersion().execute();

		// TODO: handle errors
		checkForError(response);

		return response.body();
	}

	private void checkForError(Response<?> response) throws IOException {
		if(!response.isSuccessful()) {
			// TODO: decent exception
			throw new IllegalStateException("Response is unsuccessful; code: " + response.code()
					+ ", body: " + response.errorBody().string());
		}
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
