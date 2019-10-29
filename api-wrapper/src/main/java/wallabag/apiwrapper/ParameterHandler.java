package wallabag.apiwrapper;

import wallabag.apiwrapper.models.TokenResponse;

/**
 * The {@code ParameterHandler} interface is intended to provide {@link WallabagService}
 * with credentials (via the {@code get*()} methods)
 * and to provide the client code with updated token data (via the {@link #tokensUpdated(TokenResponse)} callback).
 * <p>The {@code client ID} and {@code client secret} fields are the API client credentials available at
 * {@code https://your.wallabag.instance/developer}.
 * See <a href="https://doc.wallabag.org/en/developer/api/oauth.html">the official documentation</a> for details.
 * <p>See {@link BasicParameterHandler} for basic implementation.
 *
 * <p>The credentials data provided by {@code ParameterHandler} is used in several ways:
 * <ul>
 *     <li>
 *         Whenever {@code WallabagService} makes a request,
 *         it first uses only {@code access token} (returned by {@link #getAccessToken()});
 *         if the request succeeds, no other credentials are used.
 *         So {@code access token} is the absolute minimum to access the API.
 *     </li>
 *     <li>
 *         If the server responds with {@code 401 Unauthorized}, the token refresh procedure is performed:
 *         <ol>
 *             <li>
 *                 First, the combination of {@code client ID} + {@code client secret} + {@code refresh token}
 *                 is used to acquire a new {@code access token} (and possibly a new {@code refresh token} for later use).
 *                 If any of the parameters is {@code null} or empty, the step is skipped.
 *             </li>
 *             <li>
 *                 If the first step didn't succeed (because the {@code refresh token} is expired
 *                 or any of the required parameters were empty),
 *                 a combination of {@code username} + {@code password} + {@code client ID} + {@code client secret}
 *                 is used to acquire a new pair of {@code access token} and {@code refresh token}.
 *                 If any of the parameters is {@code null} or empty, the step is skipped.
 *             </li>
 *         </ol>
 *         At the end of each performed step, the new tokens are passed to {@link #tokensUpdated(TokenResponse)};
 *         then if {@link #tokensUpdated(TokenResponse)} returns {@code true}, the step is considered successful.
 *         If one of the steps succeeded, the initial API request is retried using the new {@code access token}.
 *     </li>
 * </ul>
 * <p>If the provided {@code access token} is empty or {@code null}, no auth headers are added to initial request.
 * If the method requires auth, the token update procedure is performed;
 * if it succeeds, the request is retried with a newly acquired token.
 * <p>{@code WallabagService} does not create additional threads,
 * so if only one thread is using a {@code WallabagService} instance,
 * {@code ParameterHandler} usage is inherently thread-safe.
 * <p>Thread safety is enforced on a {@code WallabagService}-instance level,
 * i.e. if a single {@code ParameterHandler} instance is passed to more than one {@code WallabagService} instance,
 * the thread safety guarantees do not apply.
 * <p>The {@code get*()} methods may be invoked by more than one thread simultaneously.
 * The {@link #tokensUpdated(TokenResponse)} method may only be invoked by one thread at a time.
 * While the {@link #tokensUpdated(TokenResponse)} method call is in progress,
 * the {@code get*()} methods still may be invoked by other threads.
 */
public interface ParameterHandler {

    /**
     * Returns the username of the wallabag user.
     *
     * @return the username of the wallabag user, {@code null}able
     */
    String getUsername();

    /**
     * Returns the password of the wallabag user.
     *
     * @return the password of the wallabag user, {@code null}able
     */
    String getPassword();

    /**
     * Returns the API client ID.
     *
     * @return the API client ID, {@code null}able
     */
    String getClientID();

    /**
     * Returns the API client secret.
     *
     * @return the API client secret, {@code null}able
     */
    String getClientSecret();

    /**
     * Returns the API refresh token.
     *
     * @return the API refresh token, {@code null}able
     */
    String getRefreshToken();

    /**
     * Returns the API access token.
     *
     * @return the API access token, {@code null}able
     */
    String getAccessToken();

    /**
     * Accepts {@link TokenResponse} and returns {@code true} if the token is considered acceptable,
     * i.e. the requests to the API can be continued using the new {@code access token}.
     * <p>It is recommended to {@code return token.accessToken != null && !token.accessToken.isEmpty();},
     * it is however safe to always {@code return true}.
     *
     * @param token the updated {@link TokenResponse}
     * @return {@code true} if the token is considered acceptable.
     */
    boolean tokensUpdated(TokenResponse token);

}
