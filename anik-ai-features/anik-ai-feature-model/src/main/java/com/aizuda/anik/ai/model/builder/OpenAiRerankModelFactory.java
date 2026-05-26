package com.aizuda.anik.ai.model.builder;

import com.aizuda.anik.ai.common.model.ModelCallException;
import com.aizuda.anik.ai.common.model.RerankApiClient;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.common.model.ConfigExtAttrsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generic Rerank factory implementation
 * Compatible with standard rerank HTTP interfaces such as Jina Reranker, Cohere Reranker, and BGE Reranker
 *
 * Standard interface format:
 * POST {baseUrl}/rerank
 * Authorization: Bearer {apiKey}
 * { "model": "...", "query": "...", "documents": [...], "top_n": N }
 */
@Slf4j
@Component
public class OpenAiRerankModelFactory implements RerankModelFactory {

    @Override
    public String getSupportedProvider() {
        return "openai";
    }

    @Override
    public RerankApiClient createRerankClient(String providerKey, String baseUrl, String apiKey,
                                               String modelKey, ConfigExtAttrsDTO config) throws Exception {
        if (!isConfigValid(baseUrl, apiKey)) {
            throw new ModelCallException(ModelCallException.ErrorCode.INVALID_PARAMETER,
                    "baseUrl and apiKey cannot be empty");
        }

        // Parse configJson to get custom rerank path (default /rerank) and timeout
        String rerankPath = "/rerank";
        long readTimeoutMs = 60_000L;
        if (config.getRerankPath() != null && !config.getRerankPath().isBlank()) {
            rerankPath = config.getRerankPath();
        }
        if (config.getTimeoutMs() != null && config.getTimeoutMs() > 0) {
            readTimeoutMs = config.getTimeoutMs();
        }

        String fullUrl = baseUrl.replaceAll("/+$", "") + rerankPath;

        log.debug("Creating RerankApiClient for provider: {}, model: {}, url: {}", providerKey, modelKey, fullUrl);

        return new HttpRerankApiClient(fullUrl, apiKey, modelKey, readTimeoutMs);
    }

    /**
     * Rerank API client based on HTTP
     */
    static class HttpRerankApiClient implements RerankApiClient {

        private final String url;
        private final String apiKey;
        private final String modelKey;
        private final RestClient restClient;

        HttpRerankApiClient(String url, String apiKey, String modelKey, long timeoutMs) {
            this.url = url;
            this.apiKey = apiKey;
            this.modelKey = modelKey;
            long connectTimeoutMs = Math.min(timeoutMs, 10_000L);
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
            requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
            this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<RerankResultItem> rerank(String query, List<String> documents, int topN) {
            if (documents == null || documents.isEmpty()) {
                return List.of();
            }
            int n = Math.min(Math.max(topN, 1), documents.size());
            RerankParams params = new RerankParams(modelKey, new RerankOptions(query, documents, n));
            log.debug("rerank {}", params);
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(params)
                    .retrieve()
                    .body(Map.class);

            log.info("rerank response: {}", JsonUtil.toJson(response));
            if (response == null) {
                log.warn("Rerank API returned null body");
                return List.of();
            }
            if (response.containsKey("error")) {
                log.error("Rerank API error: {}", response.get("error"));
                return List.of();
            }
            JsonNode json = JsonUtil.toJson(response);
            // Compatible with output.results (such as Alibaba Cloud) and top-level results (such as Jina/Cohere) formats
            JsonNode results = json.path("output").path("results");
            if (results.isMissingNode() || !results.isArray()) {
                results = json.path("results");
            }
            if (results.isMissingNode() || !results.isArray() || results.isEmpty()) {
                log.warn("Rerank API returned unexpected response (no results): {}", response.keySet());
                return List.of();
            }

            List<RerankResultItem> items = new ArrayList<>(results.size());
            for (JsonNode o : results) {
                JsonNode idxObj = o.get("index");
                int index = idxObj.asInt();
                double score = extractScore(o);
                items.add(new RerankResultItem(index, score));
            }
            return items;
        }

        /** Cohere: relevance_score; some compatible interfaces use score */
        private static double extractScore(JsonNode r) {
            JsonNode rel = r.get("relevance_score");
            if (Objects.nonNull(rel)) {
                return rel.doubleValue();
            }
            JsonNode sc = r.get("score");
            if (Objects.nonNull(sc)) {
                return sc.doubleValue();
            }
            return 0.0;
        }
    }

    public record RerankParams(String model, RerankOptions input) {

    }

    public record RerankOptions(String query, List<String> documents, int top_n){}
}
