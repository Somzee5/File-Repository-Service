package com.example.file_repository_service.repository;

import com.example.file_repository_service.entity.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantConfigRepository extends JpaRepository<TenantConfig, Integer>
{
//    Optional<TenantConfig> findByTenantCode(String tenantCode);
}