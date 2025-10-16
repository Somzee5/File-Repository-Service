package com.example.file_repository_service.service;

import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.repository.TenantConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.file_repository_service.dto.request.TenantConfigRequest;
import com.example.file_repository_service.exception.TenantNotFoundException;
import org.apache.tika.Tika;


@Service
@Transactional
public class TenantConfigService
{

    private final TenantConfigRepository tenantRepo;

    public TenantConfigService(TenantConfigRepository tenantRepo) { this.tenantRepo = tenantRepo;}

    public TenantConfig createOrUpdateTenant(Integer tenantId, String tenantCode, Object config) {
        Optional<TenantConfig> existing = tenantRepo.findById(tenantId);

        TenantConfig tenant = existing.orElseGet(() -> new TenantConfig());
        tenant.setTenantId(tenantId);
        tenant.setTenantCode(tenantCode);
        tenant.setConfig((java.util.Map<String, Object>) config);

        return tenantRepo.save(tenant);
    }

    public Optional<TenantConfig> getTenantConfig(Integer tenantId) {
        return tenantRepo.findById(tenantId);
    }

    public void deleteTenant(Integer tenantId) {
        tenantRepo.deleteById(tenantId);
    }

    public TenantConfig createTenantConfig(Map<String, Object> config) {

        TenantConfig tenant = new TenantConfig();



        String tempCode = "TMP" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        tenant.setTenantCode(tempCode);

        // first save to get an auto-generated tenant_id
        tenant = tenantRepo.save(tenant);

        // now generate the final tenant code based on the generated id and persist config
        String generatedCode = String.format("TEN%03d", tenant.getTenantId());
        tenant.setTenantCode(generatedCode);
        tenant.setConfig(config);

        return tenantRepo.save(tenant);
    }


    // fetch or throw
    public TenantConfig getTenantConfigOrThrow(Integer tenantId) {
        return tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant ID " + tenantId + " not found"));
    }

    // GET all tenants
    public List<TenantConfig> getAllTenants() {
        return tenantRepo.findAll();
    }


    // create from DTO
    public TenantConfig createTenantConfigFromRequest(TenantConfigRequest request)
    {
        Map<String, Object> config = Map.of(
                "maxFileSizeKBytes", request.getMaxFileSizeKBytes(),
                "allowedExtensions", request.getAllowedExtensions(),
                "forbiddenExtensions", request.getForbiddenExtensions(),
                "allowedMimeTypes", request.getAllowedMimeTypes(),
                "forbiddenMimeTypes", request.getForbiddenMimeTypes()
        );
        return createTenantConfig(config);
    }


    public TenantConfig updateTenantConfigFromRequest(Integer tenantId, TenantConfigRequest request) {
        TenantConfig existing = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant ID " + tenantId + " not found"));

        Map<String, Object> updatedConfig = Map.of(
                "maxFileSizeKBytes", request.getMaxFileSizeKBytes(),
                "allowedExtensions", request.getAllowedExtensions(),
                "forbiddenExtensions", request.getForbiddenExtensions(),
                "allowedMimeTypes", request.getAllowedMimeTypes(),
                "forbiddenMimeTypes", request.getForbiddenMimeTypes()
        );

        existing.setConfig(updatedConfig);
        return tenantRepo.save(existing);
    }
}