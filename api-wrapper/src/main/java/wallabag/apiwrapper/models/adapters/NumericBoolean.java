package wallabag.apiwrapper.models.adapters;

import com.squareup.moshi.JsonQualifier;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Ignore this.
 */
@Retention(RUNTIME)
@JsonQualifier
public @interface NumericBoolean {}
