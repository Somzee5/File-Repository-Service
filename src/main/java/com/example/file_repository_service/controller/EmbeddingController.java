package com.example.file_repository_service.controller;

import com.example.file_repository_service.dto.response.ApiResponse;
import com.example.file_repository_service.entity.Embedding;
import com.example.file_repository_service.service.EmbeddingService;
import com.example.file_repository_service.service.GeminiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/tenants/{tenantId}")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final GeminiClient geminiClient;

    public EmbeddingController(EmbeddingService embeddingService, GeminiClient geminiClient) {
        this.embeddingService = embeddingService;
        this.geminiClient = geminiClient;
    }

    // ✅ Test endpoint (still available)
    @GetMapping("/test-embedding")
    public ResponseEntity<Map<String, Object>> testEmbedding(@RequestParam("text") String text) {
        List<Float> vector = geminiClient.generateEmbeddings(text);
        int size = vector != null ? vector.size() : 0;
        List<Float> sample = vector != null && !vector.isEmpty()
                ? vector.subList(0, Math.min(5, vector.size()))
                : List.of();

        return ResponseEntity.ok(Map.of(
                "embedding_size", size,
                "sample_values", sample
        ));
    }

    // 🔹 Generate embeddings for a PDF file
    @PostMapping("/embeddings/{fileId}")
    public ResponseEntity<ApiResponse<String>> generateEmbeddingsForFile(
            @PathVariable Long tenantId,
            @PathVariable String fileId
    ) {
        embeddingService.generateEmbeddingsForFile(tenantId, fileId);
        return ResponseEntity.ok(ApiResponse.success("Embeddings generated and stored successfully", null));
    }

    // 🔹 Get stored embeddings for file
    @GetMapping("/embeddings/{fileId}")
    public ResponseEntity<ApiResponse<List<Embedding>>> getEmbeddingsForFile(
            @PathVariable Long tenantId,
            @PathVariable String fileId
    ) {
        List<Embedding> embeddings = embeddingService.getEmbeddingsForFile(tenantId, fileId);
        return ResponseEntity.ok(ApiResponse.success("Embeddings fetched successfully", embeddings));
    }

    // 🔹 Search embeddings (semantic search)
    @PostMapping("/embeddings/search/{fileId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchEmbeddingsForFile(
            @PathVariable Long tenantId,
            @PathVariable String fileId,
            @RequestParam("query") String query
    ) {
        List<Map<String, Object>> results = embeddingService.searchInEmbeddings(tenantId, fileId, query);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }
}
