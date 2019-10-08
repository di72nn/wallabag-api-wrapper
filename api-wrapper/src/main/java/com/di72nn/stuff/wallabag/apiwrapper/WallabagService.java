package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.adapters.NumericBooleanAdapter;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.AuthorizationException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.NotFoundException;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.*;
import com.di72nn.stuff.wallabag.apiwrapper.services.WallabagApiService;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.io.IOException;
import java.util.*;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.*;

public class WallabagService {

	private final WallabagApiService wallabagApiService;

	private final String apiBaseURL;

	private String serverVersion;

	public enum ResponseFormat {
		XML, JSON, TXT, CSV, PDF, EPUB, MOBI, HTML;

		public String apiValue() {
			return toString().toLowerCase();
		}

	}

	public WallabagService(String apiBaseURL, ParameterHandler parameterHandler) {
		this(apiBaseURL, parameterHandler, null);
	}

	public WallabagService(String apiBaseURL, ParameterHandler parameterHandler, OkHttpClient okHttpClient) {
		nonEmptyString(apiBaseURL, "apiBaseURL");
		nonNullValue(parameterHandler, "parameterHandler");

		if(!apiBaseURL.endsWith("/")) apiBaseURL += "/";

		this.apiBaseURL = apiBaseURL;

		if(okHttpClient == null) okHttpClient = new OkHttpClient();

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
		return checkResponseBody(getArticlesCall(parameters).execute());
	}

	Call<Article> addArticleCall(RequestBody requestBody) {
		return wallabagApiService.addArticle(requestBody);
	}

	Article addArticle(RequestBody requestBody) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(addArticleCall(requestBody).execute());
	}

	public Call<Article> reloadArticleCall(int articleID) {
		return wallabagApiService.reloadArticle(nonNegativeNumber(articleID, "articleID"));
	}

	public Article reloadArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		Response<Article> response = reloadArticleCall(articleID).execute();

		if(response.code() == 304) { // couldn't update
			return null;
		}

		return checkResponseBody(response);
	}

	public Call<ExistsResponse> articleExistsCall(String url) {
		return wallabagApiService.exists(nonEmptyString(url, "URL"));
	}

	public boolean articleExists(String url) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(articleExistsCall(url).execute()).exists;
	}

	public Call<ExistsWithIdResponse> articleExistsWithIdCall(String url) {
		return wallabagApiService.exists(nonEmptyString(url, "URL"), "1");
	}

	public Integer articleExistsWithId(String url) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(articleExistsWithIdCall(url).execute()).id;
	}

	public Call<Map<String, Boolean>> articlesExistCall(Collection<String> urls) {
		return wallabagApiService.exists(nonEmptyCollection(urls, "urls"));
	}

	public Map<String, Boolean> articlesExist(Collection<String> urls)
			throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(articlesExistCall(urls).execute());
	}

	public Call<Map<String, Integer>> articlesExistWithIdCall(Collection<String> urls) {
		return wallabagApiService.exists(nonEmptyCollection(urls, "urls"), "1");
	}

	public Map<String, Integer> articlesExistWithId(Collection<String> urls)
			throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(articlesExistWithIdCall(urls).execute());
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
		return checkResponseBody(deleteArticleCall(articleID).execute());
	}

	public Call<DeleteWithIdResponse> deleteArticleWithIdCall(int articleID) {
		return wallabagApiService.deleteArticle(nonNegativeNumber(articleID, "articleID"), "id");
	}

	public Integer deleteArticleWithId(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(deleteArticleWithIdCall(articleID).execute()).id;
	}

	public Call<Article> getArticleCall(int articleID) {
		return wallabagApiService.getArticle(nonNegativeNumber(articleID, "articleID"));
	}

	public Article getArticle(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(getArticleCall(articleID).execute());
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
		return checkResponseBody(modifyArticleCall(articleID, requestBody).execute());
	}

	public Call<List<Tag>> getTagsCall(int articleID) {
		return wallabagApiService.getTags(nonNegativeNumber(articleID, "articleID"));
	}

	public List<Tag> getTags(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(getTagsCall(articleID).execute());
	}

	public Call<Article> addTagsCall(int articleID, Collection<String> tags) {
		nonNegativeNumber(articleID, "articleID");
		nonEmptyCollection(tags, "tags");

		return wallabagApiService.addTags(articleID, Utils.join(tags, ","));
	}

	public Article addTags(int articleID, Collection<String> tags) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(addTagsCall(articleID, tags).execute());
	}

	public Call<Article> deleteTagCall(int articleID, int tagID) {
		nonNegativeNumber(articleID, "articleID");
		nonNegativeNumber(tagID, "tagID");

		return wallabagApiService.deleteTag(articleID, tagID);
	}

	public Article deleteTag(int articleID, int tagID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(deleteTagCall(articleID, tagID).execute());
	}

	public Call<List<Tag>> getTagsCall() {
		return wallabagApiService.getTags();
	}

	public List<Tag> getTags() throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(getTagsCall().execute());
	}

	public Call<Tag> deleteTagCall(String tagLabel) {
		return wallabagApiService.deleteTag(nonEmptyString(tagLabel, "tagLabel"));
	}

	public Tag deleteTag(String tagLabel) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(deleteTagCall(tagLabel).execute());
	}

	public Call<Tag> deleteTagCall(int tagID) {
		return wallabagApiService.deleteTag(nonNegativeNumber(tagID, "tagID"));
	}

	public Tag deleteTag(int tagID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(deleteTagCall(tagID).execute());
	}

	public Call<List<Tag>> deleteTagsCall(Collection<String> tags) {
		return wallabagApiService.deleteTags(join(nonNullValue(tags, "tags"), ","));
	}

	public List<Tag> deleteTags(Collection<String> tags) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(deleteTagsCall(tags).execute());
	}

	public Call<Annotations> getAnnotationsCall(int articleID)  {
		return wallabagApiService.getAnnotations(nonNegativeNumber(articleID, "articleID"));
	}

	public Annotations getAnnotations(int articleID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(getAnnotationsCall(articleID).execute());
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
		return checkResponseBody(addAnnotationCall(articleID, ranges, text, quote).execute());
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
		return checkResponseBody(updateAnnotationCall(annotationID, text).execute());
	}

	public Call<Annotation> deleteAnnotationCall(int annotationID) {
		return wallabagApiService.deleteAnnotation(nonNegativeNumber(annotationID, "annotationID"));
	}

	public Annotation deleteAnnotation(int annotationID) throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(deleteAnnotationCall(annotationID).execute());
	}

	public Call<String> getVersionCall() {
		return wallabagApiService.getVersion();
	}

	public String getVersion() throws IOException, UnsuccessfulResponseException {
		return checkResponseBody(getVersionCall().execute());
	}

	String getServerVersion() throws IOException, UnsuccessfulResponseException {
		if(serverVersion == null) {
			serverVersion = getVersion();
		}

		return serverVersion;
	}

	private <T> T checkResponseBody(Response<T> response) throws IOException, UnsuccessfulResponseException {
		T body = checkResponse(response).body();
		if (body == null) { // AFAIK should not happen
			throw new UnsuccessfulResponseException(response.code(), "Response is empty", null);
		}
		return body;
	}

	private <T> Response<T> checkResponse(Response<T> response) throws IOException, UnsuccessfulResponseException {
		if(!response.isSuccessful()) {
			String bodyString = response.errorBody() != null ? response.errorBody().string() : null;
			switch(response.code()) {
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
