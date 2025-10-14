package com.example.file_repository_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.repository.storage")
@Getter
@Setter
public class StorageProperties
{
    private String basePath;
    private String tempPath;
}