package com.example.file_repository_service.service;

import com.example.file_repository_service.dto.request.FileSearchRequest;
import com.example.file_repository_service.entity.FileEntity;
import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.repository.FileRepository;
import com.example.file_repository_service.util.FileIdGenerator;
import com.example.file_repository_service.util.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        (file.getTag() != null && file.getTag().equalsIgnoreCase(req.getTag())))
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

}