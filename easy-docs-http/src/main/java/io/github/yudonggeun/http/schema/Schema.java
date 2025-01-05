package io.github.yudonggeun.http.schema;

import io.github.yudonggeun.http.JsonType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Schema {

    JsonType type();
    String name() default "";
    String description() default "";
}
