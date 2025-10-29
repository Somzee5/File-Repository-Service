package com.example.file_repository_service.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class MediaTypeDetector {

    private final Tika tika;

    public MediaTypeDetector() {
        this.tika = new Tika();
    }

    public String detectMimeType(Path filePath) {
        try {
            return tika.detect(filePath);
        } catch (IOException e) {
            return "application/octet-stream"; // default fallback
        }
    }

    public String detectMimeType(byte[] content) {
        try {
            return tika.detect(content);
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}
