package com.example.file_repository_service.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FileSearchRequest {
    private String fileName;
    private String tag;
    private String mediaType;

    private Long minSizeBytes;
    private Long maxSizeBytes;

    private LocalDate startDate;
    private LocalDate endDate;
}
