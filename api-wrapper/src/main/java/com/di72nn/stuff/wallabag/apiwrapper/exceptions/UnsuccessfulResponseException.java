package com.di72nn.stuff.wallabag.apiwrapper.exceptions;

public class UnsuccessfulResponseException extends Exception {

	private int responseCode;
	private String responseBody;

	public UnsuccessfulResponseException() {}

	public UnsuccessfulResponseException(String message) {
		super(message);
	}

	public UnsuccessfulResponseException(String message, int responseCode, String responseBody) {
		super(message);
		this.responseCode = responseCode;
		this.responseBody = responseBody;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

}
