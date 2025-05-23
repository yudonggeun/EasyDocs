package io.github.yudonggeun.form;

public class PathForm {

    private final String name;
    private final String description;
    private final Object value;

    public PathForm(String name, String description, Object value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value.toString();
    }
}
