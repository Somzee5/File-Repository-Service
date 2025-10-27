package com.example.file_repository_service.service;

import com.example.file_repository_service.config.StorageProperties;
import com.example.file_repository_service.exception.FileStorageException;
import com.example.file_repository_service.util.MediaTypeDetector;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class StorageService {

    private final Path basePath;
    private final Path tempPath;
    private final MediaTypeDetector mediaTypeDetector;

    public StorageService(StorageProperties properties, MediaTypeDetector mediaTypeDetector) throws IOException {
        this.basePath = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
        this.tempPath = Paths.get(properties.getTempPath()).toAbsolutePath().normalize();
        this.mediaTypeDetector = mediaTypeDetector;

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

            String fileName = fileId + (extension.isEmpty() ? "" : "." + extension);
            Path targetFile = tenantFolder.resolve(fileName);
            file.transferTo(targetFile);

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

    // Updated to use MediaTypeDetector
    public String detectMimeType(Path filePath) {
        return mediaTypeDetector.detectMimeType(filePath);
    }

    public Path extractZipToTemp(MultipartFile zipFile, String tenantCode, String zipId) throws IOException {
        Path zipTempDir = tempPath.resolve(tenantCode).resolve(zipId);
        Files.createDirectories(zipTempDir);

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                Path extractedFile = zipTempDir.resolve(entry.getName()).normalize();
                Files.createDirectories(extractedFile.getParent());

                try (OutputStream os = Files.newOutputStream(extractedFile)) {
                    zis.transferTo(os);
                }
            }
        }

        return zipTempDir;
    }

    public void deleteTempFolder(Path tempFolder) {
        if (Files.exists(tempFolder)) {
            try (Stream<Path> walk = Files.walk(tempFolder)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                });
            } catch (IOException ignored) {}
        }
    }
}
