package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.SourceDocumentResponse;
import com.familyos.familyos.api.service.SourceDocumentApiService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/source-documents")
public class SourceDocumentApiController {

    private final SourceDocumentApiService sourceDocumentApiService;

    public SourceDocumentApiController(SourceDocumentApiService sourceDocumentApiService) {
        this.sourceDocumentApiService = sourceDocumentApiService;
    }

    @GetMapping
    public PagedResponse<SourceDocumentResponse> getSourceDocuments(Pageable pageable) {
        return sourceDocumentApiService.getSourceDocuments(pageable);
    }

    @GetMapping("/{id}")
    public SourceDocumentResponse getSourceDocument(@PathVariable UUID id) {
        return sourceDocumentApiService.getSourceDocument(id);
    }
}
