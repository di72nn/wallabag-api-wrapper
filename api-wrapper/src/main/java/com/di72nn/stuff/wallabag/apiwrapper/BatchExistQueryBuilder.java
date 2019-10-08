package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import okhttp3.HttpUrl;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonNullValue;

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

	public void reset() {
		urls.clear();
		currentRequestLength = wallabagService.getApiBaseURL().length() + "/api/entries/exists.json".length();
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
		return wallabagService.articlesExistCall(urls);
	}

	public Call<Map<String, Integer>> buildCallWithId() {
		return wallabagService.articlesExistWithIdCall(urls);
	}

	public Map<String, Boolean> execute() throws IOException, UnsuccessfulResponseException {
		return wallabagService.articlesExist(urls);
	}

	public Map<String, Integer> executeWithId() throws IOException, UnsuccessfulResponseException {
		return wallabagService.articlesExistWithId(urls);
	}

}
