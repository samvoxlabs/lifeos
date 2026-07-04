package com.familyos.familyos.domain.validation;

import com.familyos.familyos.domain.dto.ProcessExtractionRequest;
import com.familyos.familyos.domain.exception.DomainValidationException;
import org.springframework.stereotype.Component;

@Component
public class ProcessExtractionRequestValidator {

    public void validate(ProcessExtractionRequest request) {
        if (request == null) {
            throw new DomainValidationException("Request is required");
        }
        if (request.sourceDocument() == null) {
            throw new DomainValidationException("sourceDocument is required");
        }
        if (isBlank(request.sourceDocument().provider())) {
            throw new DomainValidationException("sourceDocument.provider is required");
        }
        if (isBlank(request.sourceDocument().sourceType())) {
            throw new DomainValidationException("sourceDocument.sourceType is required");
        }
        if (isBlank(request.sourceDocument().externalId())) {
            throw new DomainValidationException("sourceDocument.externalId is required");
        }
        if (request.extractionResult() != null && isBlank(request.extractionResult().summary())) {
            throw new DomainValidationException("extractionResult.summary is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
