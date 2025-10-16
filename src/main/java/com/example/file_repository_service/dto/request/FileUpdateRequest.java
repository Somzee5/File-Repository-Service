package com.example.file_repository_service.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class FileUpdateRequest {
    private String tag;
    private Map<String, Object> metadata;
}
