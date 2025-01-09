package io.github.yudonggeun;

import io.github.yudonggeun.http.form.HeaderForm;
import io.github.yudonggeun.http.form.HttpRequestForm;
import io.github.yudonggeun.http.form.HttpResponseForm;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Document {

    private static Document docs = new Document();

    private List<ServerSpec> servers = new ArrayList<>();
    private JSONObject paths = new JSONObject();

    public static void addApi(HttpRequestForm requestForm, HttpResponseForm responseForm) {

        String url = requestForm.getUrl();
        String method = requestForm.getMethod().name().toLowerCase();

        JSONObject requestPath = docs.paths.has(url) ? docs.paths.getJSONObject(url) : new JSONObject();
        JSONObject operation = requestPath.has(method) ? requestPath.getJSONObject(method) : new JSONObject();
        JSONObject requestBody = operation.has("requestBody") ? operation.getJSONObject("requestBody") : new JSONObject();
        JSONObject responses = operation.has("responses") ? operation.getJSONObject("responses") : new JSONObject();

        String[] tags = requestForm.getTags();
        String operationId = requestForm.getOperationId();
        String description = requestForm.getDescription();
        String summary = requestForm.getSummary();
        if (tags != null && tags.length > 0) {
            operation.put("tags", tags);
        }
        operation.put("operationId", operationId);
        operation.put("description", description);
        operation.put("summary", summary);
        operation.put("parameters", requestForm.getParameterSchema());
        operation.put("responses", responses);

        // request body
        JSONObject requestSchema = requestForm.getBodySchema();
        JSONObject requestDetailContent = new JSONObject();
        JSONObject requestContent = new JSONObject();
        requestContent.put("application/json", requestDetailContent);
        requestDetailContent.put("schema", requestSchema);
        requestBody.put("content", requestContent);
        operation.put("requestBody", requestBody);

        int statusCode = responseForm.getStatusCode();
        JSONObject detailResponse = responses.has(statusCode + "") ? responses.getJSONObject(statusCode + "") : new JSONObject();
        detailResponse.put("description", responseForm.getDescription());

        // response
        Optional<HeaderForm> contentType = responseForm.getHeader("Content-Type");
        if (contentType.isPresent()) {
            JSONObject schema = responseForm.getBodySchema();
            JSONObject detailContent = new JSONObject();
            JSONObject content = new JSONObject();
            content.put(contentType.get().getValue(), detailContent);
            detailContent.put("schema", schema);
            detailResponse.put("content", content);
        }

        responses.put(statusCode + "", detailResponse);
        requestPath.put(method, operation);
        docs.paths.put(url, requestPath);
    }

    public static void addServer(ServerSpec server) {
        docs.servers.add(server);
    }

    public static String getOpenApi3(int indent) {
        JSONObject root = new JSONObject();
        JSONArray serverArray = new JSONArray();

        for (ServerSpec server : docs.servers) {
            serverArray.put(server.toJson());
        }
        root.put("openapi", "3.0.0");
        root.put("servers", serverArray);
        root.put("paths", docs.paths);

        return root.toString(indent);
    }
}
