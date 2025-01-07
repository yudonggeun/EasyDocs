package io.github.yudonggeun;

import io.github.yudonggeun.http.HttpMethod;
import io.github.yudonggeun.http.annotation.HttpRequestInput;
import io.github.yudonggeun.http.annotation.HttpResponseInput;
import io.github.yudonggeun.http.form.HeaderForm;
import io.github.yudonggeun.http.form.HttpRequestForm;
import io.github.yudonggeun.http.form.PathForm;
import io.github.yudonggeun.http.form.QueryForm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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

            // response
            int responseCode = connection.getResponseCode();
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("POST Response Code : " + responseCode);
            System.out.println("response = \n" + response.toString());
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
