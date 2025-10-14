package com.example.file_repository_service.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "cf_filerepo_file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @Column(length = 64, nullable = false)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "file_name", length = 256)
    private String fileName;

    @Column(name = "file_path", length = 512)
    private String filePath;

    @Column(name = "media_type", length = 256)
    private String mediaType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "tag", length = 64)
    private String tag;

    // JSONB field (PostgreSQL)
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;


    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;
}
