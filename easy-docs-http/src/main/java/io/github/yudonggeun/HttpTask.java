package io.github.yudonggeun;

import io.github.yudonggeun.http.annotation.HttpRequestInput;
import io.github.yudonggeun.http.annotation.HttpResponseInput;

public class HttpTask implements Task {

    private HttpRequestInput requestInput;
    private HttpResponseInput responseInput;

    public HttpTask(HttpRequestInput requestForm, HttpResponseInput responseForm) {
        this.requestInput = requestForm;
        this.responseInput = responseForm;
    }

    @Override
    public void execute() {
    }
}
