package com.familyos.familyos.extraction.controller;

import com.familyos.familyos.extraction.dto.ExtractionResponse;
import com.familyos.familyos.extraction.service.ExtractionService;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/extraction")
@PreAuthorize("isAuthenticated()")
public class ExtractionController {
    
    private final ExtractionService extractionService;
    
    public ExtractionController(ExtractionService extractionService) {
        this.extractionService = extractionService;
    }
    
    @PostMapping("/process")
    public ResponseEntity<ExtractionResponse> process(@RequestBody NormalizedDocument document) {
        ExtractionResponse response = extractionService.process(document);
        return ResponseEntity.ok(response);
    }
}
