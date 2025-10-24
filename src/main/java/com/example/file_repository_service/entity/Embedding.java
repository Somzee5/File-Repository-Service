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
public class Embedding {

    @Id
    @Column(name = "file_id", length = 64)
    private String fileId;

    @Column(name = "page_id", nullable = false)
    private Integer pageId;

    @Column(name = "ocr", columnDefinition = "TEXT")
    private String ocr;

    /**
     * Embedding vector — we’ll store as a list of floats.
     * pgvector supports array-like representation, and
     * Hibernate will handle this using the string representation (e.g. [0.12, 0.34, ...])
     */
    @Column(name = "embeddings", columnDefinition = "vector(1536)")
    private String embeddings;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
