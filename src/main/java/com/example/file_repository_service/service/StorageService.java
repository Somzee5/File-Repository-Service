package com.example.file_repository_service.service;

import com.example.file_repository_service.exception.FileStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.file_repository_service.exception.FileStorageException;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



import com.example.file_repository_service.config.StorageProperties;

@Service
public class StorageService {

    private final Path basePath;
    private final Path tempPath;
    private final Tika tika;

    public StorageService(StorageProperties properties) throws IOException {
        this.basePath = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
        this.tempPath = Paths.get(properties.getTempPath()).toAbsolutePath().normalize();
        this.tika = new Tika();

        // Ensure base folders exist
        Files.createDirectories(basePath);
        Files.createDirectories(tempPath);
    }



    public String saveFile(MultipartFile file, String tenantCode, String fileId) throws IOException {
        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.') + 1);
            }

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
        } catch (IOException e) {
            throw new FileStorageException("Failed to save file to disk for tenant " + tenantCode, e);
        }

    }



    public void deleteFile(String relativePath) {
        try {
            Path fileToDelete = basePath.resolve(relativePath).normalize();
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Warning: failed to delete file " + relativePath + " : " + e.getMessage());
        }
    }


    public Path resolveFilePath(String relativePath) {
        return basePath.resolve(relativePath).normalize();
    }

    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = resolveFilePath(relativePath);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("File not found or not readable: " + relativePath);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileStorageException("Failed to load file as resource: " + relativePath, e);
        }
    }

    public String detectMimeType(Path filePath) {
        try {
            return tika.detect(filePath);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }



}
