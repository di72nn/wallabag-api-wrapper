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

public class WallabagService {

	private static final Logger LOG = LoggerFactory.getLogger(WallabagService.class);

	// TODO: move somewhere?
	private static final String GRANT_TYPE_PASSWORD = "password";
	private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

	private final OkHttpClient okHttpClient;
	private final WallabagApiService wallabagApiService;

	private final String apiBaseURL;
	private final String clientID;
	private final String clientSecret;
	private final String username;
	private final String password;

	private String refreshToken;
	private String accessToken;

	public static final class EntriesQueryBuilder {

		public enum SortCriterion {
			CREATED("created"), UPDATED("updated");

			private String value;

			SortCriterion(String value) {
				this.value = value;
			}

			@Override
			public String toString() {
				return value;
			}

		}

		public enum SortOrder {
			ASCENDING("asc"), DESCENDING("desc");

			private String value;

			SortOrder(String value) {
				this.value = value;
			}

			@Override
			public String toString() {
				return value;
			}

		}

		private final WallabagService wallabagService;

		private Boolean archive;
		private Boolean starred;
		private SortCriterion sortCriterion = SortCriterion.CREATED;
		private SortOrder sortOrder = SortOrder.DESCENDING;
		private int page = 1;
		private int perPage = 30;
		private Set<String> tags;
		private long since = 0;

		private EntriesQueryBuilder(WallabagService wallabagService) {
			this.wallabagService = wallabagService;
		}

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
			if(tag == null || tag.isEmpty()) {
				throw new IllegalArgumentException("Tag is empty or null");
			}

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public EntriesQueryBuilder tags(Collection<String> tags) {
			if(tags == null || tags.isEmpty()) {
				throw new IllegalArgumentException("Tags is empty or null");
			}

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
			parameters.put("sort", sortCriterion.toString());
			parameters.put("order", sortOrder.toString());
			parameters.put("page", String.valueOf(page));
			parameters.put("perPage", String.valueOf(perPage));
			// TODO: check that there is no need to URL-encode tags
			if(tags != null && !tags.isEmpty()) {
				parameters.put("tags", Utils.join(tags, ","));
			}
			parameters.put("since", String.valueOf(since));

			return parameters;
		}

		public Entries execute() throws IOException {
			return wallabagService.getEntries(build());
		}

	}

	public class AddEntryBuilder {

		private String url;
		private String title;
		private Set<String> tags;
		private Boolean starred;
		private Boolean archive;

		private AddEntryBuilder() {}

		// TODO: move to constructor?
		public AddEntryBuilder url(String url) {
			if(url == null || url.isEmpty()) {
				throw new IllegalArgumentException("URL is empty or null");
			}

			this.url = url;
			return this;
		}

		public AddEntryBuilder title(String title) {
			this.title = title;
			return this;
		}

		public AddEntryBuilder tag(String tag) {
			if(tag == null || tag.isEmpty()) {
				throw new IllegalArgumentException("Tag is empty or null");
			}

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public AddEntryBuilder tags(Collection<String> tags) {
			if(tags == null || tags.isEmpty()) {
				throw new IllegalArgumentException("Tags is empty or null");
			}

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
			if(url == null || url.isEmpty()) {
				throw new IllegalArgumentException("URL can't be empty or null");
			}

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
			this.title = title;
			return this;
		}

		public ModifyEntryBuilder tag(String tag) {
			if(tag == null || tag.isEmpty()) {
				throw new IllegalArgumentException("Tag is empty or null");
			}

			Set<String> tags = this.tags;
			if(tags == null) {
				this.tags = tags = new HashSet<>();
			}
			tags.add(tag);

			return this;
		}

		public ModifyEntryBuilder tags(Collection<String> tags) {
			if(tags == null || tags.isEmpty()) {
				throw new IllegalArgumentException("Tags is empty or null");
			}

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

			if(title != null) bodyBuilder.add("title", title);
			if(tags != null && !tags.isEmpty()) {
				bodyBuilder.add("tags", Utils.join(tags, ","));
			}
			if(archive != null) bodyBuilder.add("archive", Utils.booleanToNumberString(archive));
			if(starred != null) bodyBuilder.add("starred", Utils.booleanToNumberString(starred));

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

	public WallabagService(String apiBaseURL, String clientID, String clientSecret, String username, String password) {
		checkString(apiBaseURL, "apiBaseURL");
		checkString(clientID, "clientID");
		checkString(clientSecret, "clientSecret");
		checkString(username, "username");
		checkString(password, "password");

		this.apiBaseURL = apiBaseURL;
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.username = username;
		this.password = password;

		okHttpClient = new OkHttpClient.Builder()
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

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public EntriesQueryBuilder getEntriesBuilder() {
		return new EntriesQueryBuilder(this);
	}

	public AddEntryBuilder addEntryBuilder() {
		return new AddEntryBuilder();
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
		checkString(url, "URL");

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
		checkString(tagLabel, "tagLabel");

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

		if(refreshToken != null && !refreshToken.isEmpty()) {
			LOG.info("Refreshing token");
			if(getAccessToken(true) != null) return;
		}

		LOG.info("Requesting new token");
		if(getAccessToken(false) == null) {
			throw new IllegalStateException("Couldn't get access token");
		}
	}

	private TokenResponse getAccessToken(boolean refresh) throws IOException {
		LOG.info("started");

		FormBody.Builder bodyBuilder = new FormBody.Builder()
				.add("client_id", clientID)
				.add("client_secret", clientSecret);

		if(refresh) {
			bodyBuilder.add("grant_type", GRANT_TYPE_REFRESH_TOKEN)
					.add("refresh_token", refreshToken);
		} else {
			bodyBuilder.add("grant_type", GRANT_TYPE_PASSWORD)
					.add("username", username)
					.add("password", password);
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
				return null;
			} else {
				// TODO: decent exception
				throw new IllegalStateException("Response is unsuccessful; code: " + response.code());
			}
		}

		TokenResponse tokenResponse = response.body();
		LOG.info("Got token: " + tokenResponse); // TODO: remove: sensitive
		accessToken = tokenResponse.accessToken;
		if(tokenResponse.refreshToken != null) refreshToken = tokenResponse.refreshToken;

		LOG.info("finished");

		return tokenResponse;
	}

	private Request.Builder setAuthHeader(Request.Builder requestBuilder) {
		return requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
	}

	private static void checkString(String value, String name) {
		if(value == null || value.isEmpty()) {
			throw new IllegalArgumentException(name + " is empty or null");
		}
	}

}
