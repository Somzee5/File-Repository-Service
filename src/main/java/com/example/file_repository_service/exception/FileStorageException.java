package com.example.file_repository_service.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageException(String msg)
    {
        super(msg);
    }
}
