package io.github.yudonggeun;

import io.github.yudonggeun.http.annotation.RequestSpec;
import io.github.yudonggeun.http.annotation.ResponseSpec;

public class HttpTask implements Task {

    private Object requestForm;
    private Object responseForm;

    public HttpTask(Object requestForm, Object responseForm) {

        if (!requestForm.getClass().isAnnotationPresent(RequestSpec.class))
            throw new IllegalArgumentException("The request object must be an instance of a class with the RequestSpec annotation.");
        if (!responseForm.getClass().isAnnotationPresent(ResponseSpec.class))
            throw new IllegalArgumentException("The response object must be an instance of a class with the ResponseSpec annotation.");

        this.requestForm = requestForm;
        this.responseForm = responseForm;
    }

    @Override
    public void execute() {
    }
}
