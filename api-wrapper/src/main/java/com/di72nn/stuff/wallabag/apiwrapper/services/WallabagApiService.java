package com.di72nn.stuff.wallabag.apiwrapper.services;

import com.di72nn.stuff.wallabag.apiwrapper.models.*;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface WallabagApiService {

	@GET("api/entries.json")
	Call<Articles> getArticles(@QueryMap Map<String, String> parameters);

	@POST("api/entries.json")
	Call<Article> addArticle(@Body RequestBody requestBody);

	@PATCH("api/entries/{entry}/reload.json")
	Call<Article> reloadArticle(@Path("entry") int articleID);

	@GET("api/entries/exists.json")
	Call<ExistsResponse> exists(@Query("url") String url);

	@GET("api/entries/exists.json")
	Call<Map<String, Boolean>> exists(@Query("urls[]") Collection<String> urls);

	@DELETE("api/entries/{entry}.json")
	Call<Article> deleteArticle(@Path("entry") int articleID);

	@GET("api/entries/{entry}.json")
	Call<Article> getArticle(@Path("entry") int articleID);

	@Streaming
	@GET("api/entries/{entry}/export.{format}")
	Call<ResponseBody> exportArticle(@Path("entry") int articleID, @Path("format") String format);

	@PATCH("api/entries/{entry}.json")
	Call<Article> modifyArticle(@Path("entry") int articleID, @Body RequestBody requestBody);

	@GET("api/entries/{entry}/tags.json")
	Call<List<Tag>> getTags(@Path("entry") int articleID);

	@FormUrlEncoded
	@POST("api/entries/{entry}/tags.json")
	Call<Article> addTags(@Path("entry") int articleID, @Field("tags") String tags);

	@DELETE("api/entries/{entry}/tags/{tag}.json")
	Call<Article> deleteTag(@Path("entry") int articleID, @Path("tag") int tagID);

	@DELETE("api/tag/label.json")
	Call<Tag> deleteTag(@Query("tag") String tag);

	@DELETE("api/tags/label.json")
	Call<List<Tag>> deleteTags(@Query("tags") String tags);

	@DELETE("api/tags/{tag_id}.json")
	Call<Tag> deleteTag(@Path("tag_id") int tagID);

	@GET("api/tags.json")
	Call<List<Tag>> getTags();

	@GET("api/annotations/{entry}.json")
	Call<Annotations> getAnnotations(@Path("entry") int articleID);

	@POST("api/annotations/{entry}.json")
	Call<Annotation> addAnnotation(@Path("entry") int articleID, @Body Map<String, Object> body);

	@PUT("api/annotations/{entry}.json")
	Call<Annotation> updateAnnotation(@Path("entry") int articleID, @Body Map<String, String> body);

	@DELETE("api/annotations/{annotation}.json")
	Call<Annotation> deleteAnnotation(@Path("annotation") int annotationID);

	@GET("api/version.json")
	Call<String> getVersion();

}
