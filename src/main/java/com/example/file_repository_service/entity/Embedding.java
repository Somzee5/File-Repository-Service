package com.example.file_repository_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


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

    // Store as JSON string to avoid pgvector driver issues
    @Column(name = "embeddings", columnDefinition = "TEXT")
    private String embeddings;

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