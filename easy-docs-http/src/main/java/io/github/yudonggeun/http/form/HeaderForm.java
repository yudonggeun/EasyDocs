package io.github.yudonggeun.http.form;

import io.github.yudonggeun.http.JsonType;

public class HeaderForm {

    private final String name;
    private final String description;
    private final boolean required;
    private final JsonType type;
    private final Object value;

    public HeaderForm(String name, String description, boolean required, JsonType type, Object value) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.type = type;
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

    public JsonType getType() {
        return type;
    }

    public String getValue() {
        return value.toString();
    }
}
