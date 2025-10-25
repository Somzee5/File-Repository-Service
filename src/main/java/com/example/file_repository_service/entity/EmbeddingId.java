package com.example.file_repository_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EmbeddingId implements Serializable {

    @Column(name = "file_id", length = 64)
    private String fileId;

    @Column(name = "page_id", nullable = false)
    private Integer pageId;

    public EmbeddingId() {}

    public EmbeddingId(String fileId, Integer pageId) {
        this.fileId = fileId;
        this.pageId = pageId;
    }

    // getters, setters, equals, hashCode
    public String getFileId() { return fileId; }
    public Integer getPageId() { return pageId; }

    public void setFileId(String fileId) { this.fileId = fileId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmbeddingId that)) return false;
        return Objects.equals(fileId, that.fileId) && Objects.equals(pageId, that.pageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, pageId);
    }
}
