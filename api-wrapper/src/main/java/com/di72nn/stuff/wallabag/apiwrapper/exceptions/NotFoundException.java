package com.di72nn.stuff.wallabag.apiwrapper.exceptions;

import com.di72nn.stuff.wallabag.apiwrapper.CompatibilityHelper;
import com.di72nn.stuff.wallabag.apiwrapper.ParameterHandler;
import com.di72nn.stuff.wallabag.apiwrapper.WallabagService;

/**
 * Thrown to indicate that server returned HTTP 404 response code.
 * This may happen in several cases:
 * <ul>
 *     <li>
 *         The base URL provided to {@link WallabagService#instance(String, ParameterHandler)}
 *         does not point to the right location.
 *     </li>
 *     <li>
 *         The entity corresponding to an ID parameter passed to an API-accessing method was not found.
 *     </li>
 *     <li>
 *         The API method was not found (for example due to an older server version).
 *         See {@link CompatibilityHelper}.
 *     </li>
 * </ul>
 */
public class NotFoundException extends UnsuccessfulResponseException {

	/**
	 * Constructs a {@code NotFoundException} with the given parameters.
	 * @param responseCode    HTTP response code
	 * @param responseMessage response message
	 * @param responseBody    response body as {@code String}
	 */
	public NotFoundException(int responseCode, String responseMessage, String responseBody) {
		super(responseCode, responseMessage, responseBody);
	}

}
