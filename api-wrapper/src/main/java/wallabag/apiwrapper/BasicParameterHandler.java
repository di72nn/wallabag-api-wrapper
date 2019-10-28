package wallabag.apiwrapper;

import wallabag.apiwrapper.models.TokenResponse;

/**
 * {@code BasicParameterHandler} represent the most basic implementation of {@link ParameterHandler}.
 * See {@link ParameterHandler} description for more details on parameters' usage.
 */
public class BasicParameterHandler implements ParameterHandler {

    /** The username. */
    protected String username;
    /** The password. */
    protected String password;
    /** The client ID. */
    protected String clientID;
    /** The client secret. */
    protected String clientSecret;
    /** The refresh token. */
    protected volatile String refreshToken;
    /** The access token. */
    protected volatile String accessToken;

    /**
     * Constructs a new {@code BasicParameterHandler} instance
     * initializing credentials fields with the arguments.
     *
     * @param username     username
     * @param password     password
     * @param clientID     client ID
     * @param clientSecret client secret
     * @param refreshToken refresh token
     * @param accessToken  access token
     */
    public BasicParameterHandler(String username, String password,
                                 String clientID, String clientSecret,
                                 String refreshToken, String accessToken) {
        this.username = username;
        this.password = password;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    /**
     * Constructs a new {@code BasicParameterHandler} instance
     * initializing credentials fields with the arguments.
     *
     * @param username     username
     * @param password     password
     * @param clientID     client ID
     * @param clientSecret client secret
     */
    public BasicParameterHandler(String username, String password, String clientID, String clientSecret) {
        this(username, password, clientID, clientSecret, null, null);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation note:
     * {@link #refreshToken} is replaced with {@link TokenResponse#refreshToken} if it is not {@code null}.
     * {@link #accessToken} is replaced with {@link TokenResponse#accessToken}.
     *
     * @param token the updated {@link TokenResponse}
     * @return {@code true} if {@link TokenResponse#accessToken} is not {@code null} or empty
     */
    @Override
    public boolean tokensUpdated(TokenResponse token) {
        if (token.refreshToken != null) refreshToken = token.refreshToken;
        accessToken = token.accessToken;

        return accessToken != null && !accessToken.isEmpty();
    }

}
