package com.example.file_repository_service.repository;

import com.example.file_repository_service.entity.Embedding;
import com.example.file_repository_service.entity.EmbeddingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, EmbeddingId>
 {

    // Get all embeddings for a given file
    List<Embedding> findByIdFileId(String fileId);

    // Optionally, find one specific page embedding
    Embedding findByIdFileIdAndIdPageId(String fileId, Integer pageId);
}
