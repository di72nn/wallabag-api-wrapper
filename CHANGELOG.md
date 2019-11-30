# Change Log

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
