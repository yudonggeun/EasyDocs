package io.github.yudonggeun.form;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yudonggeun.enums.HttpMethod;
import io.github.yudonggeun.enums.JsonType;
import io.github.yudonggeun.schema.ArraySchema;
import io.github.yudonggeun.schema.Schema;
import io.github.yudonggeun.schema.SchemaUtil;
import io.github.yudonggeun.spec.HeaderSpec;
import io.github.yudonggeun.spec.PathSpec;
import io.github.yudonggeun.spec.QuerySpec;
import io.github.yudonggeun.spec.RequestSpec;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpRequestForm {

    private HttpRequestFormData request;

    private HttpMethod method;
    private String url;
    private String operationId;
    private String summary;
    private String description;
    private String[] tags;

    private List<PathForm> pathParams;
    private List<QueryForm> queryParams;
    private List<HeaderForm> headers;
    private Object bodyValue;
    private JSONObject bodySchema;

    public HttpRequestForm(HttpRequestFormData request) {
        Class<?> clazz = request.getClass();
        if (!clazz.isAnnotationPresent(RequestSpec.class))
            throw new IllegalArgumentException("the input must be an instance of a class with the RequestSpec annotation.");

        this.request = request;
        initRequestSpec();
        initHeaders();
        initPathParams();
        initQueryParams();
        initBody();
    }

    private void initBody() {
        Class<?> clazz = request.getClass();
        try {
            Field bodyField = clazz.getDeclaredField("body");
            bodyField.setAccessible(true);
            Object body = bodyField.get(request);
            Class<?> schemaClass = body.getClass();
            if (schemaClass.isAnnotationPresent(Schema.class)) {
                Schema schema = schemaClass.getAnnotation(Schema.class);
                bodySchema = SchemaUtil.getSchema(schema.description(), schemaClass);
            } else if (schemaClass.isAnnotationPresent(ArraySchema.class)) {
                ArraySchema schema = schemaClass.getAnnotation(ArraySchema.class);
                bodySchema = SchemaUtil.getArraySchema(schema.description(), schemaClass);
            }
            this.bodyValue = body;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void initRequestSpec() {
        Class<?> clazz = request.getClass();
        RequestSpec requestSpec = clazz.getAnnotation(RequestSpec.class);
        this.method = requestSpec.method();
        this.url = requestSpec.url();
        this.operationId = requestSpec.operationId();
        this.summary = requestSpec.summary();
        this.description = requestSpec.description();
        this.tags = requestSpec.tags();
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

    private void initPathParams() {
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

    private void initQueryParams() {

        queryParams = new ArrayList<>();
        Class<?> clazz = request.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(QuerySpec.class)) {
                QuerySpec spec = field.getAnnotation(QuerySpec.class);
                field.setAccessible(true);

                String name = spec.name();
                String description = spec.description();
                boolean required = spec.required();

                try {
                    Object value = field.get(request);
                    JsonType type = null;

                    if (name.isEmpty()) {
                        name = field.getName();
                    }

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
                    }

                    queryParams.add(new QueryForm(name, description, required, type, value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("HeaderSpec is not available on static fields.", e);
                }
            }
        }
        queryParams = Collections.unmodifiableList(queryParams);
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

    public Optional<QueryForm> getQueryParam(String name) {
        return queryParams.stream()
                .filter(queryParam -> queryParam.getName().equals(name))
                .findFirst();
    }

    public List<QueryForm> getQueryParams() {
        return queryParams;
    }

    public byte[] getBody() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return objectMapper.writeValueAsBytes(bodyValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getBodySchema() {
        return bodySchema;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    public Object getParameterSchema() {
        JSONArray parameters = new JSONArray();
        // path
        for (PathForm pathParam : pathParams) {
            JSONObject parameter = new JSONObject();
            String description = pathParam.getDescription();
            String name = pathParam.getName();
            String in = "path";
            boolean required = true;
            JSONObject schema = new JSONObject();
            schema.put("type", "string");
            parameter.put("description", description);
            parameter.put("required", required);
            parameter.put("name", name);
            parameter.put("in", in);
            parameter.put("schema", schema);
            parameters.put(parameter);
        }
        // query
        for (QueryForm queryParam : queryParams) {
            JSONObject parameter = new JSONObject();
            String description = queryParam.getDescription();
            String name = queryParam.getName();
            String in = "query";
            boolean required = queryParam.isRequired();
            JSONObject schema = new JSONObject();
            schema.put("type", queryParam.getJsonType().name().toLowerCase());
            parameter.put("description", description);
            parameter.put("required", required);
            parameter.put("name", name);
            parameter.put("in", in);
            parameter.put("schema", schema);
            parameters.put(parameter);
        }
        return parameters;
    }
}
