package wallabag.apiwrapper.exceptions;

import wallabag.apiwrapper.WallabagService;

/**
 * {@code UnsuccessfulResponseException} is the superclass of exceptions that can be thrown
 * by API-accessing methods of {@link WallabagService} indicating an unsuccessful response.
 * Subclasses indicate particular situations, while {@code UnsuccessfulResponseException}
 * indicate a general (sometimes unknown) error.
 */
public class UnsuccessfulResponseException extends Exception {

    private int responseCode;
    private String responseMessage;
    private String responseBody;

    /**
     * Constructs an {@code UnsuccessfulResponseException} with the given parameters.
     *
     * @param responseCode    HTTP response code
     * @param responseMessage response message
     * @param responseBody    response body as {@code String}
     */
    public UnsuccessfulResponseException(int responseCode, String responseMessage, String responseBody) {
        super("HTTP response: " + responseCode + " " + responseMessage);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseBody = responseBody;
    }

    /**
     * Returns an HTTP response code
     *
     * @return an HTTP response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Returns a response message
     *
     * @return a response message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Returns a response body string
     *
     * @return a response body string
     */
    public String getResponseBody() {
        return responseBody;
    }

}
