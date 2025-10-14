package com.example.file_repository_service.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "cf_filerepo_tenant_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "tenant_code", nullable = false, unique = true, length = 16)
    private String tenantCode;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    // Hooks that auto-update timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        modifiedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = OffsetDateTime.now();
    }
}