package com.apkspectrum.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that a parameter, field or method return value can never be null.
 * <p/>
 * This is a marker annotation and it has no specific attributes.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({METHOD,PARAMETER,LOCAL_VARIABLE,FIELD})
public @interface NonNull {
}
