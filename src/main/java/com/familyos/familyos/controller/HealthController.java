package com.familyos.familyos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple health/status endpoint.
 *
 * Used by humans (open it in a browser) and by hosting platforms
 * (Cloud Run, Oracle, load balancers) to check the service is alive.
 *
 * GET /api/health or /health -> {"status":"UP","service":"FamilyOS","timestamp":"..."}
 */
@RestController
@RequestMapping({"/api/health", "/health"})
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        // LinkedHashMap keeps the fields in this insertion order in the JSON output.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "FamilyOS");
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
