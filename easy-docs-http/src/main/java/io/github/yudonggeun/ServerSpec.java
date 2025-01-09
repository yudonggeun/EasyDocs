package io.github.yudonggeun;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerSpec implements JsonSpec {

    public final String url;
    public final String description;
    public final List<ServerVariableSpec> variables = new ArrayList<>();

    public ServerSpec(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public ServerSpec addVariable(ServerVariableSpec variable) {
        variables.add(variable);
        return this;
    }

    @Override
    public Object toJson() {
        JSONObject server = new JSONObject();
        server.put("url", url);
        server.put("description", description);

        JSONObject variables = new JSONObject();
        server.put("variables", variables);
        for (ServerVariableSpec variable : this.variables) {
            Object json = variable.toJson();
            variables.put(variable.getName(), json);
        }
        return server;
    }
}
