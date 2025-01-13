package io.github.yudonggeun.schema;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class SchemaUtil {

    public static JSONObject getSchema(String description, Class<?> clazz) {

        JSONObject result = new JSONObject();
        result.put("type", "object");
        result.put("description", description);

        Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            return result;
        }

        result.put("properties", new JSONObject());
        result.put("required", new JSONArray());
        for (Field field : fields) {

            JSONObject properties = result.getJSONObject("properties");
            JSONArray required = result.getJSONArray("required");

            JSONObject property = new JSONObject();
            Class<?> type = field.getType();

            boolean isRequired = false;

            if (field.isAnnotationPresent(Schema.class)) {
                Schema meta = field.getAnnotation(Schema.class);
                isRequired = meta.required();
                if (type.equals(String.class)) {
                    property.put("type", "string");
                } else if (type.equals(Integer.class) || type.equals(int.class) ||
                        type.equals(Long.class) || type.equals(long.class) ||
                        type.equals(Double.class) || type.equals(double.class) ||
                        type.equals(Float.class) || type.equals(float.class)
                ) {
                    property.put("type", "number");
                } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                    property.put("type", "boolean");
                }
                property.put("description", meta.description());
            } else {
                if (type.isAnnotationPresent(Schema.class)) {
                    Schema meta = type.getAnnotation(Schema.class);
                    isRequired = meta.required();
                    property = getSchema(meta.description(), type);
                } else if (type.isAnnotationPresent(ArraySchema.class)) {
                    ArraySchema meta = type.getAnnotation(ArraySchema.class);
                    isRequired = meta.required();
                    property = getArraySchema(meta.description(), type);
                }
            }

            if (isRequired) {
                required.put(field.getName());
            }

            properties.put(field.getName(), property);
        }
        return result;
    }

    public static JSONObject getArraySchema(String description, Class<?> clazz) {
        JSONObject result = new JSONObject();
        result.put("type", "array");
        result.put("items", getSchema(description, clazz));
        return result;
    }
}
