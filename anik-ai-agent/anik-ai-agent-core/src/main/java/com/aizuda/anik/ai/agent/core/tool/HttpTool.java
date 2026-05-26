package com.aizuda.anik.ai.agent.core.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Built-in HTTP tool - supports LLM to directly initiate HTTP requests to call remote interfaces
 * <p>
 * The injection method is consistent with ShellTool: ToolCallbacks.from(new HttpTool(...))
 */
@Slf4j
public class HttpTool {

    private static final int MAX_RESPONSE_LENGTH = 50000;

    private final long timeoutMs;

    private final HttpClient httpClient;

    public HttpTool(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Tool(name = "http_request", description = "Make an HTTP request to a remote API. Supports GET and POST methods. "
            + "Can be used to call external APIs, fetch remote data, send webhook notifications, etc.")
    public String request(
            @ToolParam(description = "Request URL, must start with http:// or https://") String url,
            @ToolParam(description = "HTTP method: GET or POST", required = false) String method,
            @ToolParam(description = "Request body (POST only, typically a JSON string)", required = false) String body,
            @ToolParam(description = "Content-Type header (optional, defaults to application/json)", required = false) String contentType,
            @ToolParam(description = "Additional headers, format: Header1:Value1\\nHeader2:Value2", required = false) String headers) {
        log.info("http_request url:{} body:{}", url, body);

        if (url == null || url.trim().isEmpty()) {
            return "Error: URL cannot be empty";
        }

        String trimmedUrl = url.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            return "Error: URL must start with http:// or https://";
        }

        String httpMethod = (method != null && !method.trim().isEmpty())
                ? method.trim().toUpperCase() : "GET";
        if (!"GET".equals(httpMethod) && !"POST".equals(httpMethod)) {
            return "Error: Only GET and POST methods are supported";
        }

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(trimmedUrl))
                    .timeout(Duration.ofMillis(timeoutMs));

            String effectiveContentType = (contentType != null && !contentType.trim().isEmpty())
                    ? contentType.trim() : "application/json";

            if (headers != null && !headers.trim().isEmpty()) {
                for (String headerLine : headers.split("\\n")) {
                    String trimmed = headerLine.trim();
                    int colonIdx = trimmed.indexOf(':');
                    if (colonIdx > 0 && colonIdx < trimmed.length() - 1) {
                        String key = trimmed.substring(0, colonIdx).trim();
                        String value = trimmed.substring(colonIdx + 1).trim();
                        requestBuilder.header(key, value);
                    }
                }
            }

            if ("POST".equals(httpMethod)) {
                String requestBody = (body != null) ? body : "";
                requestBuilder.header("Content-Type", effectiveContentType);
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            } else {
                requestBuilder.GET();
            }

            HttpRequest httpRequest = requestBuilder.build();
            log.debug("http_request: {} {}", httpMethod, trimmedUrl);

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            StringBuilder result = new StringBuilder();
            result.append("HTTP ").append(response.statusCode()).append("\n");

            String responseBody = response.body();
            if (responseBody == null || responseBody.isEmpty()) {
                result.append("<No response body>");
            } else if (responseBody.length() > MAX_RESPONSE_LENGTH) {
                result.append(responseBody, 0, MAX_RESPONSE_LENGTH);
                result.append("\n\n... The response has been truncated before displaying ").append(MAX_RESPONSE_LENGTH).append(" characters (total ")
                        .append(responseBody.length()).append(" character)");
            } else {
                result.append(responseBody);
            }

            return result.toString();

        } catch (Exception e) {
            log.error("HTTP request failed: {} {}", httpMethod, trimmedUrl, e);
            return "Error: " + e.getMessage();
        }
    }
}
