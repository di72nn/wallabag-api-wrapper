package wallabag.apiwrapper;

interface Constants {

    // OAuth
    String GRANT_TYPE = "grant_type";
    String GRANT_TYPE_PASSWORD = "password";
    String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    String CLIENT_ID_PARAM = "client_id";
    String CLIENT_SECRET_PARAM = "client_secret";
    String REFRESH_TOKEN_PARAM = "refresh_token";
    String USERNAME_PARAM = "username";
    String PASSWORD_PARAM = "password";

    // HTTP
    String HTTP_ACCEPT_HEADER = "Accept";
    String HTTP_ACCEPT_VALUE_ANY = "*/*";
    String HTTP_AUTHORIZATION_HEADER = "Authorization";
    String HTTP_AUTHORIZATION_BEARER_VALUE = "Bearer ";

}
