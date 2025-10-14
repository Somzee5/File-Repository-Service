package com.example.file_repository_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.example.file_repository_service.config.StorageProperties;

@Service
public class StorageService {

    private final Path basePath;
    private final Path tempPath;

    public StorageService(StorageProperties properties) throws IOException {
        this.basePath = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
        this.tempPath = Paths.get(properties.getTempPath()).toAbsolutePath().normalize();

        // Ensure base folders exist
        Files.createDirectories(basePath);
        Files.createDirectories(tempPath);
    }

    /**
     * Saves the uploaded file to {basePath}/{tenantCode}/{yyyy_MM}/{fileId}.{ext}
     * Returns the relative path to store in DB.
     */
    public String saveFile(MultipartFile file, String tenantCode, String fileId) throws IOException {
        // Extract file extension safely
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.') + 1);
        }

        // Format folder: /{tenantCode}/{yyyy_MM}/
        String folderName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM"));
        Path tenantFolder = basePath.resolve(tenantCode).resolve(folderName);
        Files.createDirectories(tenantFolder);

        // Build final file path
        String fileName = fileId + (extension.isEmpty() ? "" : "." + extension);
        Path targetFile = tenantFolder.resolve(fileName);

        // Save file
        file.transferTo(targetFile);

        // Return relative path for DB
        Path relativePath = basePath.relativize(targetFile);
        return relativePath.toString().replace("\\", "/");
    }

    /**
     * Deletes a file by relative path (used on rollback or delete).
     */
    public void deleteFile(String relativePath) {
        try {
            Path fileToDelete = basePath.resolve(relativePath).normalize();
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Warning: failed to delete file " + relativePath + " : " + e.getMessage());
        }
    }
}
