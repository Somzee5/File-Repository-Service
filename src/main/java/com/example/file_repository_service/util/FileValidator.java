package com.example.file_repository_service.util;

import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.exception.InvalidFileException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class FileValidator {

    private final Tika tika = new Tika();

    public void validateFile(MultipartFile file, TenantConfig tenantConfig)
    {
        Map<String, Object> config = tenantConfig.getConfig();
        String tenantCode = tenantConfig.getTenantCode();

        log.debug("Validating file - tenantCode={}, fileName={}, size={} bytes",
                tenantCode, file.getOriginalFilename(), file.getSize());

        Integer maxFileSizeKBytes = (Integer) config.getOrDefault("maxFileSizeKBytes", 2048);
        long maxBytes = maxFileSizeKBytes * 1024L;

        if (file.getSize() > maxBytes) {
            log.warn("File too large - size={} bytes, max={} bytes", file.getSize(), maxBytes);
            throw new InvalidFileException("File size exceeds the limit of " + maxFileSizeKBytes + " KB");
        }


        String originalName = file.getOriginalFilename();
        String detectedMimeType;

        try {
            detectedMimeType = tika.detect(file.getInputStream(), originalName);
        } catch (IOException e) {
            log.error("MIME detection failed for file {}: {}", originalName, e.getMessage());
            throw new InvalidFileException("Failed to detect MIME type for file: " + originalName);
        }

        List<String> allowedMime = (List<String>) config.getOrDefault("allowedMimeTypes", List.of());
        List<String> forbiddenMime = (List<String>) config.getOrDefault("forbiddenMimeTypes", List.of());

        if (!allowedMime.isEmpty() && !allowedMime.contains(detectedMimeType)) {
            log.warn("MIME type not allowed - detected={}, tenant={}", detectedMimeType, tenantCode);
            throw new InvalidFileException("Detected MIME type '" + detectedMimeType + "' is not allowed for tenant " + tenantCode);
        }
        if (!forbiddenMime.isEmpty() && forbiddenMime.contains(detectedMimeType)) {
            log.warn("MIME type forbidden - detected={}, tenant={}", detectedMimeType, tenantCode);
            throw new InvalidFileException("Detected MIME type '" + detectedMimeType + "' is forbidden for tenant " + tenantCode);
        }



        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        }

        List<String> allowedExt = (List<String>) config.getOrDefault("allowedExtensions", List.of());
        List<String> forbiddenExt = (List<String>) config.getOrDefault("forbiddenExtensions", List.of());

        if (!allowedExt.isEmpty() && !allowedExt.contains(extension)) {
            log.warn("Extension not allowed - ext={}, tenant={}", extension, tenantCode);
            throw new InvalidFileException("Extension '" + extension + "' is not allowed for tenant " + tenantCode);
        }
        if (!forbiddenExt.isEmpty() && forbiddenExt.contains(extension)) {
            log.warn("Extension forbidden - ext={}, tenant={}", extension, tenantCode);
            throw new InvalidFileException("Extension '" + extension + "' is forbidden for tenant " + tenantCode);
        }


        log.debug("Validation passed - tenantCode={}, fileName={}", tenantCode, originalName);
    }
}