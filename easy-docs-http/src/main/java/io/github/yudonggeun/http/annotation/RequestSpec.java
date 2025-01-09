package io.github.yudonggeun.http.annotation;

import io.github.yudonggeun.http.HttpMethod;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestSpec {

    HttpMethod method();

    String url();

    String operationId();

    String summary();

    String description();

    String[] tags() default {};
}
