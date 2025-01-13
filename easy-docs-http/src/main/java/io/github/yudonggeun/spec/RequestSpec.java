package io.github.yudonggeun.spec;

import io.github.yudonggeun.enums.HttpMethod;

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
