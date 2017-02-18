package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.models.TokenResponse;

public interface ParameterHandler {

	String getUsername();

	String getPassword();

	String getClientID();

	String getClientSecret();

	String getRefreshToken();

	String getAccessToken();

	boolean tokensUpdated(TokenResponse token);

}
