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
}
