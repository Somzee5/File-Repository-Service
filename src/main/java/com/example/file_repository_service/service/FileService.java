package com.example.file_repository_service.service;

import com.example.file_repository_service.entity.FileEntity;
import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.repository.FileRepository;
import com.example.file_repository_service.util.FileIdGenerator;
import com.example.file_repository_service.util.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

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

    /**
     * Uploads a file for a specific tenant.
     *
     * @param tenantId    Tenant's ID from path variable
     * @param tenantCode  Tenant's code from controller (already known)
     * @param file        The uploaded file (multipart)
     * @param tag         Optional tag for file categorization
     * @return            Saved FileEntity
     */
    public FileEntity uploadFile(Long tenantId, String tenantCode, MultipartFile file, String tag) throws IOException {
        // 1️⃣ Fetch tenant config using tenantId
        TenantConfig tenantConfig = tenantConfigService.getTenantConfigOrThrow(tenantId.intValue());

        // 2️⃣ Validate file using tenant’s configuration (max size, extensions, MIME, etc.)
        fileValidator.validateFile(file, tenantConfig);

        // 3️⃣ Generate unique file ID (includes tenant info + timestamp + random)
        String fileId = FileIdGenerator.generate(tenantId);

        // 4️⃣ Save the file to local storage (/storage/{tenantCode}/{yyyy_MM}/...)
        String relativePath = storageService.saveFile(file, tenantCode, fileId);

        // 5️⃣ Build FileEntity object for database persistence
        FileEntity fileEntity = FileEntity.builder()
                .id(fileId)
                .tenantId(tenantId)
                .fileName(file.getOriginalFilename())
                .filePath(relativePath)
                .mediaType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .tag(tag)
                .metadata(Map.of()) // You can later pass additional metadata if needed
                .createdAt(OffsetDateTime.now())
                .modifiedAt(OffsetDateTime.now())
                .build();

        // 6️⃣ Persist metadata into DB
        return fileRepository.save(fileEntity);
    }
}
