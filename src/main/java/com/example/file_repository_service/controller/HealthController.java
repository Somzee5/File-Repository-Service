package com.example.file_repository_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String,String> health() {
        log.info("Health check requested");
        return Map.of("status","UP");
    }
}
