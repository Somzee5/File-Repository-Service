package com.example.file_repository_service.controller;

import com.example.file_repository_service.entity.FileEntity;
import com.example.file_repository_service.service.FileService;
import com.example.file_repository_service.dto.request.FileSearchRequest;
import com.example.file_repository_service.dto.response.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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

        String tenantCode = "TENANT_" + tenantId;  // temporary

        FileEntity savedFile = fileService.uploadFile(tenantId, tenantCode, file, tag);


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
}