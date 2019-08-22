package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;

import java.io.IOException;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonNullValue;

public class CompatibilityHelper {

	private static final int VERSION_CODE_OLDER = 0;
	private static final int VERSION_CODE_2_1_3 = 2010300;
	private static final int VERSION_CODE_2_2_0 = 2020000;
	private static final int VERSION_CODE_2_3_0 = 2030000;
	private static final int VERSION_CODE_2_3_7 = 2030700;
	private static final int VERSION_CODE_NEWER = 999999999;

	public static boolean isGetArticlesSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isGetArticlesSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isGetArticlesSupported(wallabagService.getServerVersion());
	}

	public static boolean isAddArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isAddArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isAddArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isReloadArticleSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
	}

	public static boolean isReloadArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isReloadArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isArticleExistsSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isArticleExistsSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isArticleExistsSupported(wallabagService.getServerVersion());
	}

	public static boolean isArticleExistsWithIdSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
	}

	public static boolean isArticleExistsWithIdSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isArticleExistsWithIdSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isDeleteArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteArticleWithIdSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_3_7;
	}

	public static boolean isDeleteArticleWithIdSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteArticleWithIdSupported(wallabagService.getServerVersion());
	}

	public static boolean isGetArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isGetArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isGetArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isExportArticleSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
	}

	public static boolean isExportArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isExportArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isModifyArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isModifyArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isModifyArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isGetTagsSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isGetTagsSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isGetTagsSupported(wallabagService.getServerVersion());
	}

	public static boolean isGetTagsForArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isGetTagsForArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isGetTagsForArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isAddTagsToArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isAddTagsToArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isAddTagsToArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteTagFromArticleSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isDeleteTagFromArticleSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteTagFromArticleSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteTagByLabelSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
	}

	public static boolean isDeleteTagByLabelSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteTagByLabelSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteTagByIdSupported(String serverVersion) {
		return isBaseSupported(serverVersion);
	}

	public static boolean isDeleteTagByIdSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteTagByIdSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteTagsByLabelSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_3_0;
	}

	public static boolean isDeleteTagsByLabelSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteTagsByLabelSupported(wallabagService.getServerVersion());
	}

	public static boolean isGetAnnotationsSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
	}

	public static boolean isGetAnnotationsSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isGetAnnotationsSupported(wallabagService.getServerVersion());
	}

	public static boolean isAddAnnotationSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
	}

	public static boolean isAddAnnotationSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isAddAnnotationSupported(wallabagService.getServerVersion());
	}

	public static boolean isUpdateAnnotationSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
	}

	public static boolean isUpdateAnnotationSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isUpdateAnnotationSupported(wallabagService.getServerVersion());
	}

	public static boolean isDeleteAnnotationSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_2_0;
	}

	public static boolean isDeleteAnnotationSupported(WallabagService wallabagService)
			throws IOException, UnsuccessfulResponseException {
		return isDeleteAnnotationSupported(wallabagService.getServerVersion());
	}

	private static boolean isBaseSupported(String serverVersion) {
		return getVersionCode(serverVersion) >= VERSION_CODE_2_1_3;
	}

	private static int getVersionCode(String serverVersion) {
		nonNullValue(serverVersion, "serverVersion");

		switch(serverVersion) {
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
		}

		// TODO: real version comparison
		if("2.3.8".compareTo(serverVersion) < 0) {
			return VERSION_CODE_NEWER;
		}
		if("2.1.3".compareTo(serverVersion) > 0) {
			return VERSION_CODE_OLDER;
		}

		throw new IllegalArgumentException("Unknown server version");
	}

}
