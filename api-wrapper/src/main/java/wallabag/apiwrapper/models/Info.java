package wallabag.apiwrapper.models;

import com.squareup.moshi.Json;
import wallabag.apiwrapper.CompatibilityHelper;
import wallabag.apiwrapper.WallabagService;

/**
 * The {@code Info} class represents the wallabag instance info
 * returned by the {@link WallabagService#getInfo()} call.
 */
public class Info {

    /**
     * The application name. {@code "wallabag"} in case of wallabag.
     */
    public String appname;

    /**
     * The wallabag instance version {@code String} as returned by the server.
     * <p>The value can be used with {@link CompatibilityHelper}.
     */
    public String version;

    /**
     * The value indicating whether registration is allowed on the server.
     */
    @Json(name = "allowed_registration")
    public Boolean registrationAllowed;

    @Override
    public String toString() {
        return "Info{" +
                "appname='" + appname + '\'' +
                ", version='" + version + '\'' +
                ", registrationAllowed=" + registrationAllowed +
                '}';
    }

}
