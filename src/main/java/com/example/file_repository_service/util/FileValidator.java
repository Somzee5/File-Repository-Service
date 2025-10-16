package com.example.file_repository_service.util;

import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.exception.InvalidFileException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class FileValidator {

    private final Tika tika = new Tika();

    public void validateFile(MultipartFile file, TenantConfig tenantConfig)
    {
        Map<String, Object> config = tenantConfig.getConfig();
        String tenantCode = tenantConfig.getTenantCode();


        Integer maxFileSizeKBytes = (Integer) config.getOrDefault("maxFileSizeKBytes", 2048);
        long maxBytes = maxFileSizeKBytes * 1024L;

        if (file.getSize() > maxBytes) {
            throw new InvalidFileException("File size exceeds the limit of " + maxFileSizeKBytes + " KB");
        }


        String originalName = file.getOriginalFilename();
        String detectedMimeType;

        try {
            detectedMimeType = tika.detect(file.getInputStream(), originalName);
        } catch (IOException e) {
            throw new InvalidFileException("Failed to detect MIME type for file: " + originalName);
        }

        List<String> allowedMime = (List<String>) config.getOrDefault("allowedMimeTypes", List.of());
        List<String> forbiddenMime = (List<String>) config.getOrDefault("forbiddenMimeTypes", List.of());

        if (!allowedMime.isEmpty() && !allowedMime.contains(detectedMimeType)) {
            throw new InvalidFileException("Detected MIME type '" + detectedMimeType + "' is not allowed for tenant " + tenantCode);
        }
        if (!forbiddenMime.isEmpty() && forbiddenMime.contains(detectedMimeType)) {
            throw new InvalidFileException("Detected MIME type '" + detectedMimeType + "' is forbidden for tenant " + tenantCode);
        }



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


    }
}