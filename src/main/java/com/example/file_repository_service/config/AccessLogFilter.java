package com.example.file_repository_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger logger = LogManager.getLogger(AccessLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String tenantId = extractTenantId(request.getRequestURI());
            logger.info("ACCESS [{}] {} {} status={} duration={}ms ip={}",
                    tenantId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    request.getRemoteAddr());
        }
    }

    private String extractTenantId(String uri) {
        try {
            String[] parts = uri.split("/");
            int tenantIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("tenants")) {
                    tenantIndex = i + 1;
                    break;
                }
            }
            return (tenantIndex != -1 && tenantIndex < parts.length) ? parts[tenantIndex] : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
