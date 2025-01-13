package io.github.yudonggeun;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yudonggeun.http.HttpMethod;
import io.github.yudonggeun.http.annotation.HttpRequestInput;
import io.github.yudonggeun.http.annotation.HttpResponseInput;
import io.github.yudonggeun.http.form.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpTask implements Task {

    private final HttpRequestInput requestInput;
    private final HttpResponseInput responseInput;

    public HttpTask(HttpRequestInput request, HttpResponseInput response) {
        this.requestInput = request;
        this.responseInput = response;
    }

    @Override
    public void execute() {
        HttpRequestForm requestForm = new HttpRequestForm(requestInput);
        HttpResponseForm responseForm = new HttpResponseForm(responseInput);

        HttpMethod method = requestForm.getMethod();
        String urlTemplate = "http://localhost" + requestForm.getUrl();
        List<PathForm> pathParams = requestForm.getPathParams();
        List<HeaderForm> headers = requestForm.getHeaders();
        List<QueryForm> queryParams = requestForm.getQueryParams();
        byte[] body = requestForm.getBody();

        try {
            // request
            URL url = new URL(processUrlTemplate(urlTemplate, pathParams, queryParams));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.name());

            for (HeaderForm header : headers) {
                connection.setRequestProperty(header.getName(), header.getValue());
            }

            if (body.length > 0) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body, 0, body.length);
                }
            }

            // validator
            int responseCode = connection.getResponseCode();
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();

            BufferedReader in;
            if (responseCode >= 400) {
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            boolean isMatch = true;
            StringBuilder report = new StringBuilder();

            // status matcher
            if (responseCode == responseForm.getStatusCode()) {
            } else {
                isMatch = false;
                report.append("##error reporting##\n")
                        .append("   response status code\n").append(responseCode).append("\n")
                        .append("   expected status code\n").append(responseForm.getStatusCode()).append("\n");
            }
            // header matcher
            for (HeaderForm header : responseForm.getHeaders()) {
                List<String> headerValues = responseHeaders.getOrDefault(header.getName(), Collections.emptyList());
                if (!headerValues.contains(header.getValue())) {
                    report.append("##error reporting##\n")
                            .append("   response header name\n").append(header.getName()).append("\n")
                            .append("   response header value\n").append(headerValues).append("\n")
                            .append("   expected response header value\n").append(header.getValue()).append("\n");
                    isMatch = false;
                }
            }

            // body matcher
            if (!objectMapper.writeValueAsString(responseForm.getBody()).contentEquals(response)) {
                isMatch = false;
                report.append("##error reporting##\n")
                        .append("   expected response body\n").append(objectMapper.writeValueAsString(responseForm.getBody())).append("\n")
                        .append("   received response body\n").append(response).append("\n");
            }

            if (isMatch) {
                Document.addApi(requestForm, responseForm);
            } else {
                System.err.println(report);
            }
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // util
    public String processUrlTemplate(String urlTemplate, List<PathForm> params, List<QueryForm> queryParams) {
        for (PathForm param : params) {
            urlTemplate = urlTemplate.replace("{" + param.getName() + "}", param.getValue());
        }
        if (!queryParams.isEmpty()) {
            StringBuilder query = new StringBuilder().append("?");
            for (QueryForm queryParam : queryParams) {
                query.append(queryParam.getName()).append("=").append(queryParam.getValue()).append("&");
            }
            query.deleteCharAt(query.length() - 1);
            urlTemplate += query.toString();
        }
        return urlTemplate;
    }
}
