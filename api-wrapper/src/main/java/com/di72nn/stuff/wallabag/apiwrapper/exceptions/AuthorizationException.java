package com.di72nn.stuff.wallabag.apiwrapper.exceptions;

import com.di72nn.stuff.wallabag.apiwrapper.ParameterHandler;

/**
 * Thrown to indicate that it was not possible to get authorization to access API
 * using credentials provided by {@link ParameterHandler}.
 */
public class AuthorizationException extends UnsuccessfulResponseException {

    /**
     * Constructs an {@code AuthorizationException} with the given parameters.
     *
     * @param responseCode    HTTP response code
     * @param responseMessage response message
     * @param responseBody    response body as {@code String}
     */
    public AuthorizationException(int responseCode, String responseMessage, String responseBody) {
        super(responseCode, responseMessage, responseBody);
    }

}
