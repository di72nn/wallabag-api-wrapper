package com.di72nn.stuff.wallabag.apiwrapper.exceptions;

public class AuthorizationException extends UnsuccessfulResponseException {

	public AuthorizationException(int responseCode, String responseMessage, String responseBody) {
		super(responseCode, responseMessage, responseBody);
	}

}
