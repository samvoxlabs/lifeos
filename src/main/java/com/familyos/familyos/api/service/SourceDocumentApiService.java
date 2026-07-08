package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.SourceDocumentResponse;
import com.familyos.familyos.api.exception.ApiNotFoundException;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SourceDocumentApiService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final ApiDtoMapper apiDtoMapper;

    public SourceDocumentApiService(SourceDocumentRepository sourceDocumentRepository, ApiDtoMapper apiDtoMapper) {
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.apiDtoMapper = apiDtoMapper;
    }

    public PagedResponse<SourceDocumentResponse> getSourceDocuments(Pageable pageable) {
        Page<SourceDocumentResponse> page = sourceDocumentRepository.findAll(pageable).map(apiDtoMapper::toSourceDocumentResponse);
        return PagedResponse.from(page, pageable.getSort().toString());
    }

    public SourceDocumentResponse getSourceDocument(UUID id) {
        return sourceDocumentRepository.findById(id)
            .map(apiDtoMapper::toSourceDocumentResponse)
            .orElseThrow(() -> new ApiNotFoundException("SourceDocument not found: " + id));
    }
}
