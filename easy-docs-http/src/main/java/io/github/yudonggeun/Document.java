package io.github.yudonggeun;

import io.github.yudonggeun.http.form.HttpRequestForm;
import io.github.yudonggeun.http.form.HttpResponseForm;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Document {

    private static Document docs = new Document();

    private List<ServerSpec> servers = new ArrayList<>();

    public static void addApi(HttpRequestForm requestForm, HttpResponseForm responseForm) {
    }

    public static void addServer(ServerSpec server) {
        docs.servers.add(server);
    }

    public static String getOpenApi3() {
        JSONObject root = new JSONObject();
        JSONArray serverArray = new JSONArray();

        for (ServerSpec server : docs.servers) {
            serverArray.put(server.toJson());
        }
        root.put("servers", serverArray);

        return root.toString();
    }
}
