package com.example.file_repository_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * Represents tenant configuration request payload.
 * Example JSON:
 * {
 *   "maxFileSizeKBytes": 2048,
 *   "allowedExtensions": [".pdf", ".jpg"],
 *   "forbiddenExtensions": [".zip"]
 * }
 */

// Tenant validatoin

@Data
public class TenantConfigRequest {

    @NotNull(message = "maxFileSizeKBytes is required")
    @Min(value = 1, message = "maxFileSizeKBytes must be positive")
    private Integer maxFileSizeKBytes;

    private List<String> allowedExtensions;
    private List<String> forbiddenExtensions;
    private List<String> allowedMimeTypes;
    private List<String> forbiddenMimeTypes;
}
