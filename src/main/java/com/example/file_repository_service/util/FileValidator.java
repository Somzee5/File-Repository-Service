package com.example.file_repository_service.util;

import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.exception.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Component
public class FileValidator {

    /**
     * Validates file size, extension, and MIME type using tenant's config JSON.
     */
    public void validateFile(MultipartFile file, TenantConfig tenantConfig) {
        Map<String, Object> config = tenantConfig.getConfig();
        String tenantCode = tenantConfig.getTenantCode();

        // --- 1️⃣ Validate max file size ---
        Integer maxFileSizeKBytes = (Integer) config.getOrDefault("maxFileSizeKBytes", 2048);
        long maxBytes = maxFileSizeKBytes * 1024L;
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException(
                    String.format("File size %.2f MB exceeds limit of %.2f MB for tenant %s",
                            file.getSize() / 1048576.0,
                            maxFileSizeKBytes / 1024.0,
                            tenantCode));
        }

        // --- 2️⃣ Validate file extension ---
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        }

        List<String> allowedExt = (List<String>) config.getOrDefault("allowedExtensions", List.of());
        List<String> forbiddenExt = (List<String>) config.getOrDefault("forbiddenExtensions", List.of());

        if (!allowedExt.isEmpty() && !allowedExt.contains(extension)) {
            throw new InvalidFileException("Extension '" + extension + "' is not allowed for tenant " + tenantCode);
        }
        if (!forbiddenExt.isEmpty() && forbiddenExt.contains(extension)) {
            throw new InvalidFileException("Extension '" + extension + "' is forbidden for tenant " + tenantCode);
        }

        // --- 3️⃣ Validate MIME type ---
        String mimeType = file.getContentType();
        List<String> allowedMime = (List<String>) config.getOrDefault("allowedMimeTypes", List.of());
        List<String> forbiddenMime = (List<String>) config.getOrDefault("forbiddenMimeTypes", List.of());

        if (!allowedMime.isEmpty() && !allowedMime.contains(mimeType)) {
            throw new InvalidFileException("MIME type '" + mimeType + "' is not allowed for tenant " + tenantCode);
        }
        if (!forbiddenMime.isEmpty() && forbiddenMime.contains(mimeType)) {
            throw new InvalidFileException("MIME type '" + mimeType + "' is forbidden for tenant " + tenantCode);
        }
    }
}
