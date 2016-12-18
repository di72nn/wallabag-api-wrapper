package com.di72nn.stuff.wallabag.apiwrapper.services;

import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import com.di72nn.stuff.wallabag.apiwrapper.models.Articles;
import com.di72nn.stuff.wallabag.apiwrapper.models.ExistsResponse;
import com.di72nn.stuff.wallabag.apiwrapper.models.Tag;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface WallabagApiService {

	@GET("api/entries.json")
	Call<Articles> getArticles(@QueryMap Map<String, String> parameters);

	@POST("api/entries.json")
	Call<Article> addArticle(@Body RequestBody requestBody);

	@GET("api/entries/exists.json")
	Call<ExistsResponse> exists(@Query("url") String url);

	@DELETE("api/entries/{entry}.json")
	Call<Article> deleteArticle(@Path("entry") int articleID);

	@GET("api/entries/{entry}.json")
	Call<Article> getArticle(@Path("entry") int articleID);

	@PATCH("api/entries/{entry}.json")
	Call<Article> modifyArticle(@Path("entry") int articleID, @Body RequestBody requestBody);

	@GET("api/entries/{entry}/tags.json")
	Call<List<Tag>> getTags(@Path("entry") int articleID);

	@DELETE("/api/tag/label.json")
	Call<Tag> deleteTag(@Query("tag") String tag);

	@DELETE("/api/tags/{tag_id}.json")
	Call<Tag> deleteTag(@Path("tag_id") int tagID);

	@GET("api/tags.json")
	Call<List<Tag>> getTags();

	@GET("api/version.json")
	Call<String> getVersion();

}
