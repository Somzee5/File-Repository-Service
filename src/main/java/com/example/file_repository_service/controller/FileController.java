package com.example.file_repository_service.controller;

import com.example.file_repository_service.dto.request.FileUpdateRequest;
import com.example.file_repository_service.entity.FileEntity;
import com.example.file_repository_service.service.FileService;
import com.example.file_repository_service.dto.request.FileSearchRequest;
import com.example.file_repository_service.dto.response.ApiResponse;

import org.springframework.http.HttpStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Log4j2
@RestController
@RequestMapping("/v1/tenants/{tenantId}")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @PathVariable("tenantId") Long tenantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tag", required = false) String tag
    ) throws IOException {

        log.info("Upload request received - tenantId={}, fileName={}, tag={}", tenantId, file.getOriginalFilename(), tag);
        String tenantCode = "TENANT_" + tenantId;  // temporary
        FileEntity savedFile = fileService.uploadFile(tenantId, tenantCode, file, tag);
        log.info("File uploaded successfully - fileId={}, tenantId={}", savedFile.getId(), tenantId);


        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("id", savedFile.getId());
        fileData.put("fileName", savedFile.getFileName());
        fileData.put("mediaType", savedFile.getMediaType());
        fileData.put("fileSizeBytes", savedFile.getFileSizeBytes());
        fileData.put("tag", savedFile.getTag());
        fileData.put("storagePath", savedFile.getFilePath());
        fileData.put("createdAt", savedFile.getCreatedAt());

        response.put("file", fileData);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/files/search")
    public ResponseEntity<ApiResponse<List<FileEntity>>> searchFiles(
            @PathVariable("tenantId") Long tenantId,
            @RequestBody FileSearchRequest request) {

        List<FileEntity> results = fileService.searchFiles(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Files fetched successfully", results));
    }

    @PostMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<FileEntity>> updateFileMetadata(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("fileId") String fileId,
            @RequestBody FileUpdateRequest request) {

        FileEntity updatedFile = fileService.updateFileMetadata(tenantId, fileId, request);

        return ResponseEntity.ok(
                ApiResponse.success("File updated successfully", updatedFile)
        );
    }



    @GetMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<FileEntity>> getFileDetails(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("fileId") String fileId) {

        FileEntity file = fileService.getFileById(tenantId, fileId);
        return ResponseEntity.ok(ApiResponse.success("File fetched successfully", file));
    }


    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("fileId") String fileId) {

        fileService.deleteFile(tenantId, fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }


    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<FileEntity>>> getAllFilesByTenant(
            @PathVariable("tenantId") Long tenantId) {

        List<FileEntity> files = fileService.getAllFilesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Files fetched successfully", files));
    }


    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline
    ) {
        FileEntity fileEntity = fileService.getFileById(tenantId, fileId);
        Resource resource = fileService.getFileAsResource(fileEntity);
        String mimeType = fileService.getMediaType(fileEntity);

        String contentDisposition = (inline ? "inline" : "attachment") + "; filename=\"" + fileEntity.getFileName() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}