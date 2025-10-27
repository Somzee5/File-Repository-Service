package com.example.file_repository_service.controller;

import com.example.file_repository_service.dto.request.EmbeddingSearchRequest;
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



    // Generate embeddings for a PDF file
    @PostMapping("/embeddings/{fileId}")
    public ResponseEntity<ApiResponse<String>> generateEmbeddingsForFile(
            @PathVariable Long tenantId,
            @PathVariable String fileId
    ) {
        embeddingService.generateEmbeddingsForFile(tenantId, fileId);
        return ResponseEntity.ok(ApiResponse.success("Embeddings generated and stored successfully", null));
    }

    //  Get stored embeddings for file
    @GetMapping("/embeddings/{fileId}")
    public ResponseEntity<ApiResponse<List<Embedding>>> getEmbeddingsForFile(
            @PathVariable Long tenantId,
            @PathVariable String fileId
    ) {
        List<Embedding> embeddings = embeddingService.getEmbeddingsForFile(tenantId, fileId);
        return ResponseEntity.ok(ApiResponse.success("Embeddings fetched successfully", embeddings));
    }

    // Search embeddings (semantic search)
    @PostMapping("embeddings/search/{fileId}")
    public ResponseEntity<?> searchEmbeddings(
            @PathVariable Long tenantId,
            @PathVariable String fileId,
            @RequestBody EmbeddingSearchRequest request) {

        List<Map<String, Object>> results =
                embeddingService.searchInEmbeddings(tenantId, fileId, request.getQuery());

        return ResponseEntity.ok(Map.of("success", true, "results", results));
    }
}
