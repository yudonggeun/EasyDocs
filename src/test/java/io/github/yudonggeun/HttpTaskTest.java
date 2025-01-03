package io.github.yudonggeun;

import io.github.yudonggeun.http.HttpMethod;
import io.github.yudonggeun.http.annotation.RequestSpec;
import io.github.yudonggeun.http.annotation.ResponseSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpTaskTest {

    @RequestSpec(method = HttpMethod.GET, url = "/")
    class SampleRequestSpec {
    }

    @ResponseSpec(status = 200)
    class SampleResponseSpec {
    }

    @DisplayName("The request object must be an instance of a class with the RequestSpec annotation.")
    @Test
    void fail1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new HttpTask("test", new SampleResponseSpec());
        });
    }

    @DisplayName("The response object must be an instance of a class with the ResponseSpec annotation.")
    @Test
    void fail2() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new HttpTask( new SampleRequestSpec(), "test");
        });
    }
}