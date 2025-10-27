package com.example.file_repository_service.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class for detecting MIME types using Apache Tika.
 * This centralizes file type detection logic.
 */
@Component
public class MediaTypeDetector {

    private final Tika tika;

    public MediaTypeDetector() {
        this.tika = new Tika();
    }

    /**
     * Detects MIME type for a given file path.
     *
     * @param filePath Path to the file
     * @return MIME type string (e.g., "application/pdf", "image/png")
     */
    public String detectMimeType(Path filePath) {
        try {
            return tika.detect(filePath);
        } catch (IOException e) {
            return "application/octet-stream"; // default fallback
        }
    }

    /**
     * Detects MIME type for byte array content.
     * Useful when working with in-memory uploads.
     */
    public String detectMimeType(byte[] content) {
        try {
            return tika.detect(content);
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}
