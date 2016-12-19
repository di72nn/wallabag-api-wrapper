package com.di72nn.stuff.wallabag.apiwrapper.exceptions;

public class NotFoundException extends UnsuccessfulResponseException {

	public NotFoundException(int responseCode, String responseMessage, String responseBody) {
		super(responseCode, responseMessage, responseBody);
	}

}
