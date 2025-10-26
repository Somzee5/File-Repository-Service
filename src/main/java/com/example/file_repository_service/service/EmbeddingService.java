package com.example.file_repository_service.service;

import com.example.file_repository_service.entity.Embedding;
import com.example.file_repository_service.entity.EmbeddingId;
import com.example.file_repository_service.entity.FileEntity;
import com.example.file_repository_service.exception.InvalidFileException;
import com.example.file_repository_service.repository.EmbeddingRepository;
import com.example.file_repository_service.repository.FileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;


import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;

@Service
public class EmbeddingService {

    private final FileRepository fileRepository;
    private final EmbeddingRepository embeddingRepository;
    private final StorageService storageService;
    private final GeminiClient geminiClient;

    public EmbeddingService(FileRepository fileRepository,
                            EmbeddingRepository embeddingRepository,
                            StorageService storageService,
                            GeminiClient geminiClient) {
        this.fileRepository = fileRepository;
        this.embeddingRepository = embeddingRepository;
        this.storageService = storageService;
        this.geminiClient = geminiClient;
    }

    /**
     * 🔹 Extracts text from PDF pages and generates embeddings per page.
     */
    @Transactional
    public void generateEmbeddingsForFile(Long tenantId, String fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("File not found for ID: " + fileId));

        if (!file.getTenantId().equals(tenantId)) {
            throw new InvalidFileException("File does not belong to tenant " + tenantId);
        }

        try {
            Path path = storageService.resolveFilePath(file.getFilePath());
            File pdfFile = path.toFile();

            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                int totalPages = document.getNumberOfPages();

                for (int i = 1; i <= totalPages; i++) {
                    stripper.setStartPage(i);
                    stripper.setEndPage(i);
                    String text = stripper.getText(document).trim();

                    if (text.isEmpty()) continue;

                    List<Float> vector = geminiClient.generateEmbeddings(text);
                    if (vector.size() > 1536) {
                        vector = vector.subList(0, 1536); // 🔥 truncate extra dimensions
                    }

                    String vectorString = vector.toString(); // e.g. "[0.12, 0.34, ...]"
                    Embedding embedding = Embedding.builder()
                            .id(new EmbeddingId(fileId, i))
                            .ocr(text)
                            .embeddings(vectorString)
                            .build();


                    embeddingRepository.save(embedding);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while generating embeddings: " + e.getMessage(), e);
        }
    }

    /**
     * 🔹 Fetch all embeddings for a file.
     */
    public List<Embedding> getEmbeddingsForFile(Long tenantId, String fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("File not found for ID: " + fileId));

        if (!file.getTenantId().equals(tenantId)) {
            throw new InvalidFileException("File does not belong to tenant " + tenantId);
        }

        // Debug: Check if embeddings exist for this fileId
        List<Embedding> allEmbeddings = embeddingRepository.findAllEmbeddings();
        System.out.println("DEBUG: Total embeddings in database: " + allEmbeddings.size());
        
        List<Embedding> embeddings = embeddingRepository.findByFileId(fileId);
        System.out.println("DEBUG: Found " + embeddings.size() + " embeddings for fileId: " + fileId);
        
        // Debug: Show all fileIds in database
        for (Embedding e : allEmbeddings) {
            System.out.println("DEBUG: Database contains embedding for fileId: " + e.getFileId());
        }
        
        return embeddings;
    }

    /**
     * 🔹 Search embeddings by cosine similarity to query text.
     */
    public List<java.util.Map<String, Object>> searchInEmbeddings(Long tenantId, String fileId, String query) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("File not found for ID: " + fileId));

        if (!file.getTenantId().equals(tenantId)) {
            throw new InvalidFileException("File does not belong to tenant " + tenantId);
        }

        List<Float> queryVector = geminiClient.generateEmbeddings(query);
        List<Embedding> embeddings = embeddingRepository.findByFileId(fileId);

        return embeddings.stream().map(e -> {
                    List<Float> storedVector = parseVector(e.getEmbeddings());
                    double similarity = cosineSimilarity(storedVector, queryVector);

                    Map<String, Object> map = new HashMap<>();
                    map.put("page_id", e.getPageId());
                    map.put("similarity", similarity);
                    map.put("text_preview", e.getOcr().substring(0, Math.min(200, e.getOcr().length())));
                    return map;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("similarity"), (Double) a.get("similarity")))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Float> parseVector(String vectorString) {
        vectorString = vectorString.replace("[", "").replace("]", "").trim();
        String[] parts = vectorString.split(",");
        java.util.ArrayList<Float> list = new java.util.ArrayList<>();
        for (String p : parts) {
            try {
                list.add(Float.parseFloat(p.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return list;
    }

    private double cosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1.isEmpty() || v2.isEmpty()) return 0;
        double dot = 0, mag1 = 0, mag2 = 0;
        int len = Math.min(v1.size(), v2.size());
        for (int i = 0; i < len; i++) {
            dot += v1.get(i) * v2.get(i);
            mag1 += v1.get(i) * v1.get(i);
            mag2 += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(mag1) * Math.sqrt(mag2) + 1e-10);
    }
}
