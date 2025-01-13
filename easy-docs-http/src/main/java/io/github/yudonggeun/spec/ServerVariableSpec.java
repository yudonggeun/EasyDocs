package io.github.yudonggeun.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerVariableSpec implements JsonSpec {

    private String name;
    private String defaultValue;
    private String description;
    private String[] enums;

    public ServerVariableSpec(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ServerVariableSpec setDescription(String description) {
        this.description = description;
        return this;
    }

    public ServerVariableSpec setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ServerVariableSpec setEnums(String... enums) {
        this.enums = enums;
        return this;
    }

    @Override
    public Object toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        JSONObject result = new JSONObject();

        if (defaultValue != null) {
            result.put("default", defaultValue);
        }
        if (enums != null) {
            JSONArray jsonEnum = new JSONArray();
            for (String value : enums) {
                jsonEnum.put(value);
            }
            result.put("enum", jsonEnum);
        }
        if (description != null) {
            result.put("description", description);
        }
        return result;
    }
}
