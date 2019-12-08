package wallabag.apiwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;

import static wallabag.apiwrapper.Utils.nonNullValue;

/**
 * The {@code CompatibilityHelper} class contains methods for determining whether particular API features
 * are available in a given server version.
 * The naming of the methods is loosely based on the names of the corresponding {@link WallabagService} methods.
 * <p>All methods accept a {@code String} version as returned by {@link WallabagService#getVersion()}
 * or a {@link WallabagService} instance, in which case the {@link WallabagService#getCachedVersion()} is used.
 * <p>The information is hardcoded in this class.
 * The first known version is {@code 2.1.3} which is considered "a base version" ({@link #isBaseSupported(String)}).
 * Earlier server versions may work, but lack some features.
 */
public class CompatibilityHelper {

    private static final int VERSION_CODE_OLDER = 0;
    private static final int VERSION_CODE_2_1_3 = 2010300;
    private static final int VERSION_CODE_2_2_0 = 2020000;
    private static final int VERSION_CODE_2_3_0 = 2030000;
    private static final int VERSION_CODE_2_3_7 = 2030700;
    private static final int VERSION_CODE_2_4_0 = 2040000;
    private static final int VERSION_CODE_NEWER = 999999999;

    private static final Logger LOG = LoggerFactory.getLogger(CompatibilityHelper.class);

    /**
     * Returns {@code true} if {@link WallabagService#getArticle(int)} and {@link WallabagService#getArticlesBuilder()}
     * methods are supported. Equivalent to {@link #isBaseSupported(String)}.
     *
     * @param serverVersion the version to check
     * @return {@code true} if basic article getting methods are supported
     */
    public static boolean isGetArticlesSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    /**
     * Returns {@code true} if {@link WallabagService#getArticle(int)} and {@link WallabagService#getArticlesBuilder()}
     * methods are supported. Equivalent to {@link #isBaseSupported(String)}.
     *
     * @param wallabagService the {@link WallabagService} instance to get version from
     * @return {@code true} if basic article getting methods are supported
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public static boolean isGetArticlesSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isGetArticlesSupported(wallabagService.getCachedVersion());
    }

    public static boolean isSearchSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_4_0;
    }

    public static boolean isSearchSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isSearchSupported(wallabagService.getCachedVersion());
    }

    /**
     * Returns {@code true} if {@link WallabagService#addArticle(String)} and {@link WallabagService#addArticleBuilder(String)}
     * methods are supported. Equivalent to {@link #isBaseSupported(String)}.
     *
     * @param serverVersion the version to check
     * @return {@code true} if article adding methods are supported
     */
    public static boolean isAddArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    /**
     * Returns {@code true} if {@link WallabagService#addArticle(String)} and {@link WallabagService#addArticleBuilder(String)}
     * methods are supported. Equivalent to {@link #isBaseSupported(String)}.
     *
     * @param wallabagService the {@link WallabagService} instance to get version from
     * @return {@code true} if article adding methods are supported
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public static boolean isAddArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isAddArticleSupported(wallabagService.getCachedVersion());
    }

    /**
     * Returns {@code true} if the server supports adding articles with
     * directly provided {@link AddArticleBuilder#content(String)}.
     *
     * @param serverVersion the version to check
     * @return {@code true} if advanced article adding methods are supported
     */
    public static boolean isAddArticleWithContentSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
    }

    /**
     * Returns {@code true} if the server supports adding articles with
     * directly provided {@link AddArticleBuilder#content(String)}.
     *
     * @param wallabagService the {@link WallabagService} instance to get version from
     * @return {@code true} if advanced article adding methods are supported
     * @throws IOException                   in case of network errors
     * @throws UnsuccessfulResponseException (and subclasses) in case of known wallabag-specific errors
     */
    public static boolean isAddArticleWithContentSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isAddArticleWithContentSupported(wallabagService.getCachedVersion());
    }

    public static boolean isReloadArticleSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
    }

    public static boolean isReloadArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isReloadArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isArticleExistsSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isArticleExistsSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isArticleExistsSupported(wallabagService.getCachedVersion());
    }

    public static boolean isArticleExistsWithIdSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
    }

    public static boolean isArticleExistsWithIdSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isArticleExistsWithIdSupported(wallabagService.getCachedVersion());
    }

    public static boolean isArticleExistsByHashSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_4_0;
    }

    public static boolean isArticleExistsByHashSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isArticleExistsByHashSupported(wallabagService.getCachedVersion());
    }

    static boolean isArticleExistsByHashSupportedSafe(WallabagService wallabagService) {
        try {
            return isArticleExistsByHashSupported(wallabagService.getCachedVersion());
        } catch (IOException | UnsuccessfulResponseException e) {
            LOG.warn("isArticleExistsByHashSupportedSafe() exception while detecting exists by hash support", e);
        }
        return false;
    }

    public static boolean isDeleteArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isDeleteArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isDeleteArticleWithIdSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_3_7;
    }

    public static boolean isDeleteArticleWithIdSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteArticleWithIdSupported(wallabagService.getCachedVersion());
    }

    public static boolean isGetArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isGetArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isGetArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isExportArticleSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
    }

    public static boolean isExportArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isExportArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isModifyArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isModifyArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isModifyArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isGetTagsSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isGetTagsSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isGetTagsSupported(wallabagService.getCachedVersion());
    }

    public static boolean isGetTagsForArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isGetTagsForArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isGetTagsForArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isAddTagsToArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isAddTagsToArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isAddTagsToArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isDeleteTagFromArticleSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isDeleteTagFromArticleSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteTagFromArticleSupported(wallabagService.getCachedVersion());
    }

    public static boolean isDeleteTagByLabelSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
    }

    public static boolean isDeleteTagByLabelSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteTagByLabelSupported(wallabagService.getCachedVersion());
    }

    public static boolean isDeleteTagByIdSupported(String serverVersion) {
        return isBaseSupported(serverVersion);
    }

    public static boolean isDeleteTagByIdSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteTagByIdSupported(wallabagService.getCachedVersion());
    }

    public static boolean isDeleteTagsByLabelSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
    }

    public static boolean isDeleteTagsByLabelSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteTagsByLabelSupported(wallabagService.getCachedVersion());
    }

    public static boolean isGetAnnotationsSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
    }

    public static boolean isGetAnnotationsSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isGetAnnotationsSupported(wallabagService.getCachedVersion());
    }

    public static boolean isAddAnnotationSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
    }

    public static boolean isAddAnnotationSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isAddAnnotationSupported(wallabagService.getCachedVersion());
    }

    public static boolean isUpdateAnnotationSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
    }

    public static boolean isUpdateAnnotationSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isUpdateAnnotationSupported(wallabagService.getCachedVersion());
    }

    public static boolean isDeleteAnnotationSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
    }

    public static boolean isDeleteAnnotationSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isDeleteAnnotationSupported(wallabagService.getCachedVersion());
    }

    public static boolean isInfoSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_4_0;
    }

    public static boolean isInfoSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isInfoSupported(wallabagService.getCachedVersion());
    }

    public static boolean isBaseSupported(String serverVersion) {
        return getVersionCode(serverVersion) >= VERSION_CODE_2_1_3;
    }

    public static boolean isBaseSupported(WallabagService wallabagService)
            throws IOException, UnsuccessfulResponseException {
        return isBaseSupported(wallabagService.getCachedVersion());
    }

    private static int getVersionCode(String serverVersion) {
        nonNullValue(serverVersion, "serverVersion");

        // TODO: real version parsing and comparison

        switch (serverVersion) {
            case "2.1.3":
            case "2.1.4":
            case "2.1.5":
            case "2.1.6":
                return VERSION_CODE_2_1_3;

            case "2.2.0":
            case "2.2.1":
            case "2.2.2":
            case "2.2.3":
                return VERSION_CODE_2_2_0;

            case "2.3.0":
            case "2.3.1":
            case "2.3.2":
            case "2.3.3":
            case "2.3.4":
            case "2.3.5":
            case "2.3.6":
                return VERSION_CODE_2_3_0;

            case "2.3.7":
            case "2.3.8":
                return VERSION_CODE_2_3_7;

            case "2.4.0":
                return VERSION_CODE_2_4_0;
        }

        if ("2.4.0".compareTo(serverVersion) < 0) {
            return VERSION_CODE_NEWER;
        }
        if ("2.1.3".compareTo(serverVersion) > 0) {
            return VERSION_CODE_OLDER;
        }

        throw new IllegalArgumentException("Unknown server version");
    }

}
