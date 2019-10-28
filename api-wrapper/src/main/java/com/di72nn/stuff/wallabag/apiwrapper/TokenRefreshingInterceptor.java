package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.TokenResponse;
import com.di72nn.stuff.wallabag.apiwrapper.services.WallabagAuthService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.io.IOException;

import static com.di72nn.stuff.wallabag.apiwrapper.Constants.*;
import static com.di72nn.stuff.wallabag.apiwrapper.Utils.isEmpty;

class TokenRefreshingInterceptor implements Interceptor {

    private static class GetTokenException extends Exception {

        private retrofit2.Response<TokenResponse> response;

        GetTokenException(Response<TokenResponse> response) {
            this.response = response;
        }

        Response<TokenResponse> getResponse() {
            return response;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(TokenRefreshingInterceptor.class);

    private final WallabagAuthService wallabagAuthService; // TODO: lazy init?

    private final ParameterHandler parameterHandler;

    private final Object tokenUpdateLock = new Object();

    TokenRefreshingInterceptor(String apiBaseURL, OkHttpClient okHttpClient, ParameterHandler parameterHandler) {
        wallabagAuthService = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(apiBaseURL)
                .build()
                .create(WallabagAuthService.class);

        this.parameterHandler = parameterHandler;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        LOG.debug("intercept() started");

        Request originalRequest = chain.request();

        Request request = setHeaders(originalRequest.newBuilder()).build();
        okhttp3.Response response = chain.proceed(request);

        LOG.debug("intercept() got response");
        if (!response.isSuccessful()) {
            LOG.info("intercept() unsuccessful response; code: " + response.code());

            if (response.code() == 401) {
                ResponseBody body = response.body();
                LOG.debug("intercept() response body: " + (body != null ? body.string() : null));

                synchronized (tokenUpdateLock) {
                    boolean successfullyUpdatedToken = false;
                    try {
                        if (getAccessToken()) {
                            successfullyUpdatedToken = true;
                        }
                    } catch (GetTokenException e) {
                        LOG.debug("intercept() got GetTokenException");

                        response.close();

                        Response<TokenResponse> tokenResponse = e.getResponse();
                        response = tokenResponse.raw().newBuilder().body(tokenResponse.errorBody()).build();
                    }

                    if (successfullyUpdatedToken) {
                        response.close();

                        // retry the original request
                        request = setHeaders(originalRequest.newBuilder()).build();
                        response = chain.proceed(request);
                    }
                }
            }
        }

        return response;
    }

    private Request.Builder setHeaders(Request.Builder requestBuilder) {
        requestBuilder.addHeader(HTTP_ACCEPT_HEADER, HTTP_ACCEPT_VALUE_ANY); // compatibility

        return setAuthHeader(requestBuilder);
    }

    private Request.Builder setAuthHeader(Request.Builder requestBuilder) {
        String accessToken = parameterHandler.getAccessToken();
        if (isEmpty(accessToken)) return requestBuilder;

        return requestBuilder.addHeader(HTTP_AUTHORIZATION_HEADER,
                HTTP_AUTHORIZATION_BEARER_VALUE + accessToken);
    }

    private boolean getAccessToken() throws IOException, GetTokenException {
        LOG.info("Access token requested");

        LOG.info("Refreshing token");
        try {
            if (getAccessToken(true)) return true;
        } catch (GetTokenException e) {
            LOG.debug("getAccessToken() got GetTokenException");

            Response<TokenResponse> response = e.getResponse();
            if (response.code() != 400) { // also handle 401?
                LOG.warn("Unexpected error code: " + response.code());
                throw e;
            }
        }

        LOG.info("Requesting new token");
        return getAccessToken(false);
    }

    private boolean getAccessToken(boolean refresh) throws IOException, GetTokenException {
        LOG.info("getAccessToken(" + refresh + ") started");

        String clientID = parameterHandler.getClientID();
        String clientSecret = parameterHandler.getClientSecret();
        if (isEmpty(clientID) || isEmpty(clientSecret)) {
            LOG.debug("getAccessToken() clientID and/or clientSecret is empty or null, aborting");
            return false;
        }

        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add(CLIENT_ID_PARAM, clientID)
                .add(CLIENT_SECRET_PARAM, clientSecret);

        if (refresh) {
            String refreshToken = parameterHandler.getRefreshToken();
            if (isEmpty(refreshToken)) {
                LOG.debug("getAccessToken() refresh token is empty or null, aborting");
                return false;
            }
            bodyBuilder.add(GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN)
                    .add(REFRESH_TOKEN_PARAM, refreshToken);
        } else {
            String username = parameterHandler.getUsername();
            String password = parameterHandler.getPassword();
            if (isEmpty(username) || isEmpty(password)) {
                LOG.debug("getAccessToken() username and/or password is empty or null, aborting");
                return false;
            }
            bodyBuilder.add(GRANT_TYPE, GRANT_TYPE_PASSWORD)
                    .add(USERNAME_PARAM, username)
                    .add(PASSWORD_PARAM, password);
        }
        RequestBody body = bodyBuilder.build();

        Response<TokenResponse> response = wallabagAuthService.token(body).execute();

        if (!response.isSuccessful()) {
            throw new GetTokenException(response);
        }

        TokenResponse tokenResponse = response.body();
        boolean result = parameterHandler.tokensUpdated(tokenResponse);

        LOG.info("getAccessToken() finished; result: {}", result);
        return result;
    }

}
