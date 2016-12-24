package com.di72nn.stuff.wallabag.apiwrapper.exceptions;

public class UnsuccessfulResponseException extends Exception {

	private int responseCode;
	private String responseMessage;
	private String responseBody;

	public UnsuccessfulResponseException(int responseCode, String responseMessage, String responseBody) {
		super("HTTP response: " + responseCode + " " + responseMessage);
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.responseBody = responseBody;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getResponseBody() {
		return responseBody;
	}

}
