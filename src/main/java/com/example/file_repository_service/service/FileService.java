package com.example.file_repository_service.service;

import com.example.file_repository_service.dto.request.FileSearchRequest;
import com.example.file_repository_service.dto.request.FileUpdateRequest;
import com.example.file_repository_service.entity.FileEntity;
import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.exception.InvalidFileException;
import com.example.file_repository_service.exception.TenantNotFoundException;
import com.example.file_repository_service.repository.FileRepository;
import com.example.file_repository_service.util.FileIdGenerator;
import com.example.file_repository_service.util.FileValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import java.nio.file.*;


@Service
public class FileService {

    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final TenantConfigService tenantConfigService;
    private final FileValidator fileValidator;

    public FileService(FileRepository fileRepository,
                       StorageService storageService,
                       TenantConfigService tenantConfigService,
                       FileValidator fileValidator) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.tenantConfigService = tenantConfigService;
        this.fileValidator = fileValidator;
    }


    public FileEntity uploadFile(Long tenantId, String tenantCode, MultipartFile file, String tag) throws IOException {
        TenantConfig tenantConfig = tenantConfigService.getTenantConfigOrThrow(tenantId.intValue());

        fileValidator.validateFile(file, tenantConfig);

        String fileId = FileIdGenerator.generate(tenantId);

        String relativePath = storageService.saveFile(file, tenantCode, fileId);

        FileEntity fileEntity = FileEntity.builder()
                .id(fileId)
                .tenantId(tenantId)
                .fileName(file.getOriginalFilename())
                .filePath(relativePath)
                .mediaType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .tag(tag)
                .metadata(Map.of())
                .createdAt(OffsetDateTime.now())
                .modifiedAt(OffsetDateTime.now())
                .build();

        return fileRepository.save(fileEntity);
    }

    public List<FileEntity> searchFiles(Long tenantId, FileSearchRequest req) {
        List<FileEntity> tenantFiles = fileRepository.findByTenantId(tenantId);

        return tenantFiles.stream()
                .filter(file -> req.getFileName() == null ||
                        file.getFileName().toLowerCase().contains(req.getFileName().toLowerCase()))
                .filter(file -> req.getTag() == null ||
                        (file.getTag() != null && file.getTag().toLowerCase().contains(req.getTag().toLowerCase())))
                .filter(file -> req.getMediaType() == null ||
                        (file.getMediaType() != null && file.getMediaType().equalsIgnoreCase(req.getMediaType())))
                .filter(file -> req.getMinSizeBytes() == null || file.getFileSizeBytes() >= req.getMinSizeBytes())
                .filter(file -> req.getMaxSizeBytes() == null || file.getFileSizeBytes() <= req.getMaxSizeBytes())
                .filter(file ->
                {
                    if (req.getStartDate() == null || req.getEndDate() == null)
                        return true;

                    OffsetDateTime createdAt = file.getCreatedAt();

                    return !createdAt.toLocalDate().isBefore(req.getStartDate()) &&
                            !createdAt.toLocalDate().isAfter(req.getEndDate());
                })
                .collect(Collectors.toList());
    }

    public FileEntity updateFileMetadata(Long tenantId, String fileId, FileUpdateRequest request) {

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new TenantNotFoundException("File not found with ID: " + fileId));

        if (!fileEntity.getTenantId().equals(tenantId)) {
            throw new InvalidFileException("File does not belong to tenant " + tenantId);
        }

        if (request.getTag() != null) {
            fileEntity.setTag(request.getTag());
        }

        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            fileEntity.setMetadata(request.getMetadata());
        }

        fileEntity.setModifiedAt(java.time.OffsetDateTime.now());

        return fileRepository.save(fileEntity);
    }


    // getting specific file
    public FileEntity getFileById(Long tenantId, String fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("File not found with ID: " + fileId));

        if (!file.getTenantId().equals(tenantId)) {
            throw new InvalidFileException("File does not belong to tenant " + tenantId);
        }

        return file;
    }

    @Transactional
    public void deleteFile(Long tenantId, String fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("File not found with ID: " + fileId));

        if (!file.getTenantId().equals(tenantId)) {
            throw new InvalidFileException("File does not belong to tenant " + tenantId);
        }

        // Delete physical file from disk
        storageService.deleteFile(file.getFilePath());

        // Delete metadata from DB
        fileRepository.delete(file);
    }


    public List<FileEntity> getAllFilesByTenant(Long tenantId) {
        List<FileEntity> files = fileRepository.findByTenantId(tenantId);

        if (files.isEmpty()) {
            throw new InvalidFileException("No files found for tenant ID: " + tenantId);
        }

        return files;
    }

    public Resource getFileAsResource(FileEntity fileEntity) {
        return storageService.loadFileAsResource(fileEntity.getFilePath());
    }

    public String getMediaType(FileEntity fileEntity) {
        Path filePath = storageService.resolveFilePath(fileEntity.getFilePath());
        String detectedType = storageService.detectMimeType(filePath);
        return detectedType != null ? detectedType : "application/octet-stream";
    }

}