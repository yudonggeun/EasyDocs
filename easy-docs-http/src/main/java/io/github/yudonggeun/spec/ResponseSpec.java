package io.github.yudonggeun.spec;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResponseSpec {
    int statusCode();
    String description();
}
