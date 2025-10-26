package com.example.file_repository_service.repository;

import com.example.file_repository_service.entity.Embedding;
import com.example.file_repository_service.entity.EmbeddingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, EmbeddingId>
 {

    // Get all embeddings for a given file - using JPQL with proper composite key handling
    @Query("SELECT e FROM Embedding e WHERE e.id.fileId = :fileId")
    List<Embedding> findByFileId(@Param("fileId") String fileId);

    // Optionally, find one specific page embedding
    @Query("SELECT e FROM Embedding e WHERE e.id.fileId = :fileId AND e.id.pageId = :pageId")
    Embedding findByFileIdAndPageId(@Param("fileId") String fileId, @Param("pageId") Integer pageId);
    
    // Debug: Get all embeddings to see what's in the database
    @Query("SELECT e FROM Embedding e")
    List<Embedding> findAllEmbeddings();
}
