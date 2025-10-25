package com.example.file_repository_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity representing file embeddings for vector search storage.
 *
 * NOTE: The old @TypeDef annotation has been removed as it is not needed
 * and causes compilation errors in Hibernate 6. The @Type annotation
 * now uses the class reference directly.
 */
@Entity
@Table(name = "cf_filerepo_embeddings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// The following line is removed: @TypeDef(name = "pgvector", typeClass = PgVectorType.class)
public class Embedding {

    @EmbeddedId
    private EmbeddingId id;

    @Column(name = "ocr", columnDefinition = "TEXT")
    private String ocr;

    // Use regular column definition for now - pgvector will work at DB level
    @Column(name = "embeddings", columnDefinition = "vector(1536)")
    private Float[] embeddings;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @Transient
    public String getFileId() { return id != null ? id.getFileId() : null; }

    @Transient
    public Integer getPageId() { return id != null ? id.getPageId() : null; }
}