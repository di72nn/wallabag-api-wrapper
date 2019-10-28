package wallabag.apiwrapper.services;

import wallabag.apiwrapper.models.TokenResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Ignore this.
 */
public interface WallabagAuthService {

    @POST("oauth/v2/token")
    Call<TokenResponse> token(@Body RequestBody body);

}
