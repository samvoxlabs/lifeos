package com.familyos.familyos.domain.controller;

import com.familyos.familyos.domain.dto.DomainProcessResponse;
import com.familyos.familyos.domain.dto.ProcessExtractionRequest;
import com.familyos.familyos.domain.service.DomainPersistenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/domain")
public class DomainController {

    private final DomainPersistenceService domainPersistenceService;

    public DomainController(DomainPersistenceService domainPersistenceService) {
        this.domainPersistenceService = domainPersistenceService;
    }

    @PostMapping("/process")
    public ResponseEntity<DomainProcessResponse> process(@RequestBody ProcessExtractionRequest request) {
        return ResponseEntity.ok(domainPersistenceService.process(request));
    }
}
