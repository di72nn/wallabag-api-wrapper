package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.TokenResponse;

public class BasicParameterHandler implements ParameterHandler {

	protected String username;
	protected String password;
	protected String clientID;
	protected String clientSecret;
	protected String refreshToken;
	protected String accessToken;

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

	@Override
	public boolean tokensUpdated(TokenResponse token) {
		if(token.refreshToken != null) refreshToken = token.refreshToken;
		accessToken = token.accessToken;

		return accessToken != null && !accessToken.isEmpty();
	}

}
