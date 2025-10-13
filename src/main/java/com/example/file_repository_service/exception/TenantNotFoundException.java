package com.example.file_repository_service.exception;

/**
 * Custom exception when a tenant ID or code is not found.
 */
public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String message) {
        super(message);
    }
}
