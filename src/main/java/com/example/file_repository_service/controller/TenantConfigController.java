package com.example.file_repository_service.controller;

import com.example.file_repository_service.dto.request.TenantConfigRequest;
import com.example.file_repository_service.dto.response.ApiResponse;
import com.example.file_repository_service.entity.TenantConfig;
import com.example.file_repository_service.service.TenantConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tenants")
public class TenantConfigController
{

    private final TenantConfigService tenantService;

    public TenantConfigController(TenantConfigService tenantService) {
        this.tenantService = tenantService;
    }

    // POST /v1/tenants/config - create new tenant
    @PostMapping("/config")
    public ResponseEntity<ApiResponse<TenantConfig>> createTenantConfig(
            @Valid @RequestBody TenantConfigRequest request) {

        TenantConfig saved = tenantService.createTenantConfigFromRequest(request);

        return ResponseEntity.ok(ApiResponse.success(
                "Tenant created successfully", saved));
    }

    // GET /v1/tenants/{tenantId}/config
    @GetMapping("/{tenantId}/config")
    public ResponseEntity<ApiResponse<TenantConfig>> getTenantConfig(
            @PathVariable Integer tenantId) {

        TenantConfig tenant = tenantService.getTenantConfigOrThrow(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Tenant fetched", tenant));
    }

    // DELETE /v1/tenants/{tenantId}
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<ApiResponse<String>> deleteTenant(@PathVariable Integer tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Tenant deleted successfully", null));
    }

    // POST update tenant
    @PostMapping("/{tenantId}/config")
    public ResponseEntity<ApiResponse<TenantConfig>> updateTenantConfig(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantConfigRequest request) {

        TenantConfig updated = tenantService.updateTenantConfigFromRequest(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant config updated successfully", updated));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantConfig>>> getAllTenants() {
        List<TenantConfig> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(ApiResponse.success("All tenants fetched", tenants));
    }



}