package io.github.yudonggeun.form;

import io.github.yudonggeun.enums.JsonType;

public class QueryForm {

    private final String name;
    private final String description;
    private final boolean required;
    private final JsonType jsonType;
    private final Object value;

    public QueryForm(String name, String description, boolean required, JsonType jsonType, Object value) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.jsonType = jsonType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public JsonType getJsonType() {
        return jsonType;
    }

    public Object getValue() {
        return value;
    }
}
