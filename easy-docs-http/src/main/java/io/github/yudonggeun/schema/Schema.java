package io.github.yudonggeun.schema;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Schema {

    String description() default "";

    boolean required() default true;
}
