package com.example.file_repository_service.repository;

import com.example.file_repository_service.entity.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, String> {

    // Get all embeddings for a given file
    List<Embedding> findByFileId(String fileId);

    // Optionally, find one specific page embedding
    Embedding findByFileIdAndPageId(String fileId, Integer pageId);
}
