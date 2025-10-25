package com.example.file_repository_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Gemini client for embeddings (REST).
 * Uses: POST https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent
 * Header: x-goog-api-key: <API_KEY>
 *
 * Based on Gemini docs/examples for embedContent (embedContent endpoint and/or batchEmbedContents).
 * See: Gemini Embeddings docs. :contentReference[oaicite:2]{index=2}
 */
@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    // Endpoint for single/batch embedContent
    private static final String GEMINI_EMBED_CONTENT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    public GeminiClient(@Value("${gemini.api.key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    /**
     * Generate an embedding for a single text chunk.
     * Returns List<Float> representing the embedding vector.
     */
    public List<Float> generateEmbeddings(String text) {
        try {
            // Build request JSON:
            // { "model": "models/gemini-embedding-001",
            //   "content": { "parts":[ { "text": "<text>" } ] } }
            JsonNode payload = objectMapper.createObjectNode()
                    .put("model", "models/gemini-embedding-001")
                    .set("content",
                            objectMapper.createObjectNode().set("parts",
                                    objectMapper.createArrayNode().add(
                                            objectMapper.createObjectNode().put("text", text)
                                    )
                            )
                    );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // IMPORTANT: pass API key in header as x-goog-api-key
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_EMBED_CONTENT_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Gemini API returned: " + response.getStatusCode() +
                        " body: " + response.getBody());
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            // Response structure (per docs): result.embeddings -> list of embedding objects,
            // each has .values (array of numbers). For embedContent with a single content, docs
            // show embeddings in result.embeddings[0].values.
            // We'll try both common shapes:
            // 1) { "embeddings": [ { "values": [ ... ] } ] }
            // 2) { "embedding": { "values": [ ... ] } } (older)
            List<Float> vector = new ArrayList<>();

            if (root.has("embeddings") && root.get("embeddings").isArray()) {
                JsonNode embeddingsArr = root.get("embeddings");
                JsonNode first = embeddingsArr.get(0);
                if (first != null && first.has("values") && first.get("values").isArray()) {
                    for (JsonNode v : first.get("values")) {
                        vector.add(v.floatValue());
                    }
                    return vector;
                }
            }

            if (root.has("embedding") && root.get("embedding").has("values")) {
                for (JsonNode v : root.get("embedding").get("values")) {
                    vector.add(v.floatValue());
                }
                return vector;
            }

            // Fallback: try root.path("embedding").path("values") (defensive)
            JsonNode fallback = root.path("embedding").path("values");
            if (fallback.isArray()) {
                for (JsonNode v : fallback) vector.add(v.floatValue());
                return vector;
            }

            throw new RuntimeException("Unexpected Gemini response: " + response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embeddings: " + e.getMessage(), e);
        }
    }
}
