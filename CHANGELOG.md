# Change Log

## Version 2.0.0-beta.5

*2020.04.25*

 * Initial request without auth headers is no longer performed:  
 the token refresh procedure is performed right away if no auth token is provided by the `ParameterHandler`.
 * Auth headers no longer set on requests that don't require authorization.


## Version 2.0.0-beta.4

*2020.04.17*

 * Fixed Android compatibility (`NoSuchMethodError` with `exists` methods).
 * Updated dependencies.


## Version 2.0.0-beta.3

*2020.01.09*

 * Fixed `BatchExistQueryBuilder.addUrl(String)` which never returned `true`.


## Version 2.0.0-beta.2

*2019.12.26*

 * A `CachedVersionHandler` is introduced, which allows to alter the behavior of `WallabagService.getCachedVersion()`.
 * The default behavior of `WallabagService.getCachedVersion()` is changed, see `SimpleCachedVersionHandler`.
 * A breaking change: a new parameter is added to `WallabagService.instance(String, ParameterHandler, OkHttpClient)`.


## Version 2.0.0-beta.1

*2019.12.08*

 * A `NotFoundPolicy` is introduced, which allows for a better `NotFoundException` handling
 through its (recommended) `NotFoundPolicy.SMART` policy.
 * Many methods, that were previously throwing `NotFoundException`s in case of not found entities,
 are now using that `NotFoundPolicy.SMART` policy by default
 and returning default values (`null`, `false`, etc.) instead of throwing the `NotFoundException`.
 * Java 8 is now the lowest supported Java version.
 Java 8 APIs are not used, so the library is usable with Android.


## Version 1.0.0-rc.4

*2019.12.08*

 * New 2.4.0 features:
   * `exists` by URL hash.
   * New `Article` fields: `givenUrl`, `hashedGivenUrl`, `archivedAt`.
   * New `ArticlesQueryBuilder.SortCriterion`: `ARCHIVED`.
   * `ArticlesQueryBuilder.DetailLevel` allows load articles without content.
   * New method `WallabagService.getInfo()`.
   * New search endpoint support (available through `WallabagService.searchArticlesBuilder()`).
 * `WallabagService.delete(int)` is improved.

**Breaking changes:**

 * `BatchExistQueryBuilder.addUrl(String)` throws `IllegalArgumentException` if the `url` is empty.
 * `WallabagService.deleteArticle(int)` renamed to `WallabagService.deleteArticleWithObject(int)`,
 `WallabagService.deleteArticleCall(int)` renamed to `WallabagService.deleteArticleWithObjectCall(int)`;
 an improved `WallabagService.deleteArticle(int)` is introduced instead (with different return type).


## Version 1.0.0-rc.3

*2019.11.30*

 * Added `ArticleIterator`.
 * Internal changes:
   * Simplified `ArticlesPageIterator`.
   * Removed unneeded optimization attempts in abstract builders.


## Version 1.0.0-rc.2

*2019.11.14*

 * Add a convenience-constructor to `Annotation.Range`.
 * Some documentation clarifications.


## Version 1.0.0-rc.1

*2019.10.28*

 * Some refactoring.
 * Added decent javadocs.
 * Improved README, added CHANGELOG.

**Breaking changes**:

 * `ArticlesQueryBuilder#since(long)` now accepts milliseconds instead of seconds.
 * `ArticlesPageIterator` now starts from the page set in the `ArticlesQueryBuilder`.
 * `ParameterHandler` logic change: if `access token` is not provided, no auth headers are added to initial request.  
 As a result, the `getVersion()` method works without any credentials (so it is not suitable for auth testing).  
 If a method does require auth, the token update procedure is performed and the request is retried with a new token.
 * Obvious relocations and renaming.

Other notable changes:

 * `ParameterHandler` is now allowed to return empty values.
 * Builders now accept `null`s, so the values can be reset and the builders reused.


## Version 0.1.0

*2019.08.22*

Add some new fields and features available in server versions 2.3.7 and 2.3.8.


## Version 0.0.1

*ages ago*

The first usable version.
