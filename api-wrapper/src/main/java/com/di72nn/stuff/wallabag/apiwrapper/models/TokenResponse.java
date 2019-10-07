package com.di72nn.stuff.wallabag.apiwrapper.models;

import com.squareup.moshi.Json;

public class TokenResponse {

	@Json(name = "access_token")
	public final String accessToken;

	@Json(name = "expires_in")
	public final int expiresIn;

	@Json(name = "refresh_token")
	public final String refreshToken;

	public final String scope;

	@Json(name = "token_type")
	public final String tokenType; // TODO: enum?

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
