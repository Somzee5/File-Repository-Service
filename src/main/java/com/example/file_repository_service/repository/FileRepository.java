package com.example.file_repository_service.repository;

import com.example.file_repository_service.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    // We’ll add custom queries later (search, filter, etc.)
}
