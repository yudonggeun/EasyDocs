package io.github.yudonggeun.form;

import io.github.yudonggeun.enums.JsonType;
import io.github.yudonggeun.spec.HeaderSpec;
import io.github.yudonggeun.spec.ResponseSpec;
import io.github.yudonggeun.schema.ArraySchema;
import io.github.yudonggeun.schema.Schema;
import io.github.yudonggeun.schema.SchemaUtil;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpResponseForm {

    private String description;

    private Object input;
    private int statusCode = 200;
    private List<HeaderForm> headers;
    private Object bodyValue;
    private JSONObject bodySchema;

    public HttpResponseForm(HttpResponseFormData input){
        this.input = input;
        initResponseSpec();
        initHeaders();
        initBody();
    }

    private void initResponseSpec(){
        Class<?> clazz = input.getClass();
        ResponseSpec spec = clazz.getAnnotation(ResponseSpec.class);
        statusCode = spec.statusCode();
        description = spec.description();
    }

    private void initHeaders() {

        headers = new ArrayList<>();
        Class<?> clazz = input.getClass();
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
                    Object value = field.get(input);

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

    private void initBody() {
        Class<?> clazz = input.getClass();
        try {
            Field bodyField = clazz.getDeclaredField("body");
            bodyField.setAccessible(true);
            Object body = bodyField.get(input);
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

    public int getStatusCode() {
        return statusCode;
    }

    public Object getBody() {
        return bodyValue;
    }

    public Optional<HeaderForm> getHeader(String name) {
        return headers.stream()
                .filter(header -> header.getName().equals(name))
                .findFirst();
    }

    public List<HeaderForm> getHeaders() {
        return headers;
    }

    public JSONObject getBodySchema(){
        return bodySchema;
    }

    public String getDescription() {
        return description;
    }
}
