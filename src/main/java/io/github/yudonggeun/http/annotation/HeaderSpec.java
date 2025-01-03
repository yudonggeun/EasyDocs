package io.github.yudonggeun.http.annotation;

import io.github.yudonggeun.http.JsonType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HeaderSpec {

    String name() default "";

    String description() default "";

    boolean required() default false;

    JsonType type() default JsonType.NULL;
}
