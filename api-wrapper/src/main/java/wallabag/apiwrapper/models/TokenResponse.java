package wallabag.apiwrapper.models;

import wallabag.apiwrapper.ParameterHandler;
import com.squareup.moshi.Json;

/**
 * The {@code TokenResponse} class represents the OAuth token response used in
 * {@link ParameterHandler#tokensUpdated(TokenResponse)}.
 */
public class TokenResponse {

    /** The access token. It is used to access the API. */
    @Json(name = "access_token")
    public final String accessToken;

    /** The expiration time of the access token (in seconds). */
    @Json(name = "expires_in")
    public final int expiresIn;

    /** The refresh token. It is used to request new access tokens. */
    @Json(name = "refresh_token")
    public final String refreshToken;

    /** The OAuth scope. {@code null}able. */
    public final String scope;

    /** The OAuth token type. */
    @Json(name = "token_type")
    public final String tokenType; // TODO: enum?

    /**
     * Constructs a new {@code TokenResponse} with the specified values.
     *
     * @param accessToken  the access token
     * @param expiresIn    the access token expiration time
     * @param refreshToken the refresh token
     * @param scope        the OAuth scope
     * @param tokenType    the OAuth token type
     */
    public TokenResponse(String accessToken, int expiresIn,
                         String refreshToken, String scope,
                         String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }

}
