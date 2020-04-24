package wallabag.apiwrapper.services;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import wallabag.apiwrapper.models.Tag;
import wallabag.apiwrapper.models.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static wallabag.apiwrapper.services.Markers.REQUIRES_AUTH;

/**
 * Ignore this.
 */
public interface WallabagApiService {

    @Headers(REQUIRES_AUTH)
    @GET("api/entries.json")
    Call<Articles> getArticles(@QueryMap Map<String, String> parameters);

    @Headers(REQUIRES_AUTH)
    @GET("api/search.json")
    Call<Articles> search(@QueryMap Map<String, String> parameters);

    @Headers(REQUIRES_AUTH)
    @POST("api/entries.json")
    Call<Article> addArticle(@Body RequestBody requestBody);

    @Headers(REQUIRES_AUTH)
    @PATCH("api/entries/{entry}/reload.json")
    Call<Article> reloadArticle(@Path("entry") int articleID);

    @Headers(REQUIRES_AUTH)
    @GET("api/entries/exists.json")
    Call<ExistsResponse> exists(@Query("url") String url, @Query("hashed_url") String hashedUrl);

    @Headers(REQUIRES_AUTH)
    @GET("api/entries/exists.json")
    Call<ExistsWithIdResponse> existsWithId(@Query("url") String url,
                                            @Query("hashed_url") String hashedUrl,
                                            @Query("return_id") String returnId);

    @Headers(REQUIRES_AUTH)
    @GET("api/entries/exists.json")
    Call<Map<String, Boolean>> exists(@Query("urls[]") Collection<String> urls,
                                      @Query("hashed_urls[]") Collection<String> hashedUrls);

    @Headers(REQUIRES_AUTH)
    @GET("api/entries/exists.json")
    Call<Map<String, Integer>> existsWithId(@Query("urls[]") Collection<String> urls,
                                            @Query("hashed_urls[]") Collection<String> hashedUrls,
                                            @Query("return_id") String returnId);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/entries/{entry}.json")
    Call<Article> deleteArticle(@Path("entry") int articleID);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/entries/{entry}.json")
    Call<DeleteWithIdResponse> deleteArticle(@Path("entry") int articleID, @Query("expect") String expect);

    @Headers(REQUIRES_AUTH)
    @GET("api/entries/{entry}.json")
    Call<Article> getArticle(@Path("entry") int articleID);

    @Headers(REQUIRES_AUTH)
    @Streaming
    @GET("api/entries/{entry}/export.{format}")
    Call<ResponseBody> exportArticle(@Path("entry") int articleID, @Path("format") String format);

    @Headers(REQUIRES_AUTH)
    @PATCH("api/entries/{entry}.json")
    Call<Article> modifyArticle(@Path("entry") int articleID, @Body RequestBody requestBody);

    @Headers(REQUIRES_AUTH)
    @GET("api/entries/{entry}/tags.json")
    Call<List<Tag>> getTags(@Path("entry") int articleID);

    @Headers(REQUIRES_AUTH)
    @FormUrlEncoded
    @POST("api/entries/{entry}/tags.json")
    Call<Article> addTags(@Path("entry") int articleID, @Field("tags") String tags);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/entries/{entry}/tags/{tag}.json")
    Call<Article> deleteTag(@Path("entry") int articleID, @Path("tag") int tagID);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/tag/label.json")
    Call<Tag> deleteTag(@Query("tag") String tag);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/tags/label.json")
    Call<List<Tag>> deleteTags(@Query("tags") String tags);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/tags/{tag_id}.json")
    Call<Tag> deleteTag(@Path("tag_id") int tagID);

    @Headers(REQUIRES_AUTH)
    @GET("api/tags.json")
    Call<List<Tag>> getTags();

    @Headers(REQUIRES_AUTH)
    @GET("api/annotations/{entry}.json")
    Call<Annotations> getAnnotations(@Path("entry") int articleID);

    @Headers(REQUIRES_AUTH)
    @POST("api/annotations/{entry}.json")
    Call<Annotation> addAnnotation(@Path("entry") int articleID, @Body Map<String, Object> body);

    @Headers(REQUIRES_AUTH)
    @PUT("api/annotations/{entry}.json")
    Call<Annotation> updateAnnotation(@Path("entry") int annotationID, @Body Map<String, String> body);

    @Headers(REQUIRES_AUTH)
    @DELETE("api/annotations/{annotation}.json")
    Call<Annotation> deleteAnnotation(@Path("annotation") int annotationID);

    @GET("api/info.json")
    Call<Info> getInfo();

    @GET("api/version.json")
    Call<String> getVersion();

    @POST("oauth/v2/token")
    Call<TokenResponse> token(@Body RequestBody body);

}
