package io.github.yudonggeun.http.form;

import io.github.yudonggeun.http.HttpMethod;
import io.github.yudonggeun.http.JsonType;
import io.github.yudonggeun.http.annotation.HeaderSpec;
import io.github.yudonggeun.http.annotation.PathSpec;
import io.github.yudonggeun.http.annotation.RequestSpec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpRequestForm {

    private Object request;
    private HttpMethod method;
    private String url;
    private List<PathForm> pathParams;
    private List<HeaderForm> headers;

    public HttpRequestForm(Object request) {
        Class<?> clazz = request.getClass();
        if (!clazz.isAnnotationPresent(RequestSpec.class))
            throw new IllegalArgumentException("the input must be an instance of a class with the RequestSpec annotation.");

        this.request = request;
        initStartLine();
        initHeaders();
        initPaths();
    }

    private void initStartLine() {
        Class<?> clazz = request.getClass();
        RequestSpec requestSpec = clazz.getAnnotation(RequestSpec.class);
        this.method = requestSpec.method();
        this.url = requestSpec.url();
    }

    private void initHeaders() {

        headers = new ArrayList<>();
        Class<?> clazz = request.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(HeaderSpec.class)) {
                HeaderSpec spec = field.getAnnotation(HeaderSpec.class);
                field.setAccessible(true);

                String name = spec.name();
                String description = spec.description();
                boolean required = spec.required();
                JsonType type = spec.type();

                try {
                    Object value = field.get(request);

                    if (name.isEmpty()) {
                        name = field.getName();
                    }
                    if (type == JsonType.NULL) {
                        Class<?> fieldType = field.getType();
                        if (fieldType.equals(String.class)) {
                            type = JsonType.STRING;
                        } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class) ||
                                fieldType.equals(Long.class) || fieldType.equals(long.class) ||
                                fieldType.equals(Double.class) || fieldType.equals(double.class) ||
                                fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                            type = JsonType.NUMBER;
                        } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                            type = JsonType.BOOLEAN;
                        } else if (fieldType.isArray()) {
                            type = JsonType.ARRAY;
                        } else {
                            type = JsonType.OBJECT;
                        }
                    }

                    headers.add(new HeaderForm(name, description, required, type, value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("HeaderSpec is not available on static fields.", e);
                }
            }
        }
        headers = Collections.unmodifiableList(headers);
    }

    private void initPaths() {
        pathParams = new ArrayList<>();
        Class<?> clazz = request.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(PathSpec.class)) {
                PathSpec spec = field.getAnnotation(PathSpec.class);
                field.setAccessible(true);

                String name = spec.name();
                String description = spec.description();

                try {
                    Object value = field.get(request);

                    if (name.isEmpty()) {
                        name = field.getName();
                    }

                    pathParams.add(new PathForm(name, description, value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("PathSpec is not available on static fields.", e);
                }
            }
        }
        pathParams = Collections.unmodifiableList(pathParams);
    }

    public String getUrl() {
        Class<?> clazz = request.getClass();
        RequestSpec requestSpec = clazz.getAnnotation(RequestSpec.class);
        return requestSpec.url();
    }

    public HttpMethod getMethod() {
        Class<?> clazz = request.getClass();
        RequestSpec requestSpec = clazz.getAnnotation(RequestSpec.class);
        return requestSpec.method();
    }

    public Optional<HeaderForm> getHeader(String name) {
        return headers.stream()
                .filter(header -> header.getName().equals(name))
                .findFirst();
    }

    public List<HeaderForm> getHeaders() {
        return headers;
    }

    public Optional<PathForm> getPathParam(String name) {
        return pathParams.stream()
                .filter(pathParam -> pathParam.getName().equals(name))
                .findFirst();
    }

    public List<PathForm> getPathParams() {
        return pathParams;
    }
}
